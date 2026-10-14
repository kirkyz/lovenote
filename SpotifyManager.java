package com.lovenote.spotify;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Spotify Web API client (Authorization Code + PKCE, no client secret needed).
 * Reading playback works on any account; skip/pause/seek/volume need Spotify Premium.
 */
public final class SpotifyManager {
	public static final SpotifyManager I = new SpotifyManager();

	private static final String REDIRECT = "http://127.0.0.1:8888/callback";
	private static final String SCOPES = "user-read-playback-state user-modify-playback-state user-read-currently-playing";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	// ---- state read by the UI (written from worker threads) ----
	public volatile String clientId = "";
	public volatile String status = "Not connected";
	public volatile boolean connected;
	public volatile boolean hasTrack;
	public volatile boolean playing;
	public volatile String title = "", artist = "", album = "";
	public volatile long durationMs, progressMs, progressStamp;
	public volatile int volume = -1;
	public volatile Identifier artTexture;

	private volatile String refreshToken = "", accessToken = "";
	private volatile long expiresAt;
	private volatile String verifier = "";
	private volatile String loadedArtUrl = "";
	private volatile boolean started;
	private int artCounter;

	private final ExecutorService exec = Executors.newSingleThreadExecutor(r -> {
		Thread t = new Thread(r, "Lovenote-Spotify-Cmd");
		t.setDaemon(true);
		return t;
	});

	private SpotifyManager() {}

	private record Resp(int code, String body) {}

	// ------------------------------------------------------------ lifecycle

	public void start() {
		if (started) return;
		started = true;
		loadConfig();
		if (!refreshToken.isEmpty() && !clientId.isEmpty()) {
			connected = true;
			status = "Connected";
		}
		Thread t = new Thread(this::pollLoop, "Lovenote-Spotify-Poll");
		t.setDaemon(true);
		t.start();
	}

	public void setClientId(String id) {
		if (id.equals(clientId)) return;
		clientId = id;
		saveConfig();
	}

	public long progress() {
		if (!hasTrack) return 0;
		long p = progressMs;
		if (playing) p += System.currentTimeMillis() - progressStamp;
		return Math.min(p, durationMs);
	}

	public void disconnect() {
		connected = false;
		hasTrack = false;
		refreshToken = "";
		accessToken = "";
		status = "Not connected";
		saveConfig();
	}

	// ------------------------------------------------------------ auth

	public void connect() {
		if (clientId.isBlank()) {
			status = "Paste your Client ID first";
			return;
		}
		verifier = randomString(64);
		String challenge;
		try {
			byte[] hash = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII));
			challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
		} catch (Exception e) {
			status = "Crypto error";
			return;
		}
		String url = "https://accounts.spotify.com/authorize?client_id=" + enc(clientId)
				+ "&response_type=code&redirect_uri=" + enc(REDIRECT)
				+ "&code_challenge_method=S256&code_challenge=" + challenge
				+ "&scope=" + enc(SCOPES);

		final ServerSocket ss;
		try {
			ss = new ServerSocket(8888, 1, InetAddress.getByName("127.0.0.1"));
			ss.setSoTimeout(180_000);
		} catch (Exception e) {
			status = "Port 8888 busy - close other apps using it";
			return;
		}
		status = "Waiting for browser login...";

		Thread t = new Thread(() -> {
			try (ss; Socket s = ss.accept()) {
				String line = new java.io.BufferedReader(new java.io.InputStreamReader(s.getInputStream())).readLine();
				String code = null;
				if (line != null && line.contains("code=")) {
					String q = line.substring(line.indexOf("code=") + 5);
					int end = q.indexOf('&');
					if (end < 0) end = q.indexOf(' ');
					code = java.net.URLDecoder.decode(q.substring(0, end), StandardCharsets.UTF_8);
				}
				String msg = code != null
						? "Lovenote is connected to Spotify. You can close this tab."
						: "Login was cancelled or failed. You can close this tab.";
				String html = "<html><body style='background:#111;color:#eee;font-family:sans-serif;text-align:center;padding-top:20vh'><h2>"
						+ msg + "</h2></body></html>";
				byte[] body = html.getBytes(StandardCharsets.UTF_8);
				OutputStream o = s.getOutputStream();
				o.write(("HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\nContent-Length: " + body.length
						+ "\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.UTF_8));
				o.write(body);
				o.flush();
				if (code != null) exchange(code); else status = "Login cancelled";
			} catch (java.net.SocketTimeoutException e) {
				status = "Login timed out";
			} catch (Exception e) {
				status = "Login failed: " + e.getMessage();
			}
		}, "Lovenote-Spotify-Auth");
		t.setDaemon(true);
		t.start();

		try {
			MinecraftClient.getInstance().keyboard.setClipboard(url);
			Util.getOperatingSystem().open(URI.create(url));
			status = "Browser opened (link also copied to clipboard)";
		} catch (Exception e) {
			status = "Open the link from your clipboard in a browser";
		}
	}

	private void exchange(String code) {
		try {
			String form = "grant_type=authorization_code&code=" + enc(code) + "&redirect_uri=" + enc(REDIRECT)
					+ "&client_id=" + enc(clientId) + "&code_verifier=" + enc(verifier);
			Resp r = http("POST", "https://accounts.spotify.com/api/token", form, null);
			if (!parseToken(r)) status = "Token exchange failed (" + r.code + ") - check redirect URI";
		} catch (Exception e) {
			status = "Token exchange failed: " + e.getMessage();
		}
	}

	private synchronized boolean refresh() {
		if (refreshToken.isEmpty() || clientId.isEmpty()) return false;
		try {
			String form = "grant_type=refresh_token&refresh_token=" + enc(refreshToken) + "&client_id=" + enc(clientId);
			return parseToken(http("POST", "https://accounts.spotify.com/api/token", form, null));
		} catch (Exception e) {
			status = "Refresh failed: " + e.getMessage();
			return false;
		}
	}

	private boolean parseToken(Resp r) {
		if (r.code != 200) return false;
		JsonObject o = JsonParser.parseString(r.body).getAsJsonObject();
		accessToken = o.get("access_token").getAsString();
		if (o.has("refresh_token")) refreshToken = o.get("refresh_token").getAsString();
		expiresAt = System.currentTimeMillis() + o.get("expires_in").getAsLong() * 1000L;
		connected = true;
		status = "Connected";
		saveConfig();
		return true;
	}

	private void ensureToken() {
		if (accessToken.isEmpty() || System.currentTimeMillis() > expiresAt - 30_000) refresh();
	}

	// ------------------------------------------------------------ polling

	private void pollLoop() {
		while (true) {
			try {
				Thread.sleep(2500);
				if (!connected) continue;
				ensureToken();
				fetch();
			} catch (InterruptedException e) {
				return;
			} catch (Exception e) {
				status = "Error: " + e.getMessage();
			}
		}
	}

	private void fetch() throws Exception {
		Resp r = http("GET", "https://api.spotify.com/v1/me/player", null, accessToken);
		if (r.code == 401) {
			if (refresh()) r = http("GET", "https://api.spotify.com/v1/me/player", null, accessToken);
		}
		if (r.code == 204 || r.body.isBlank()) {
			hasTrack = false;
			status = "Connected - start playing on any Spotify device";
			return;
		}
		if (r.code != 200) {
			status = "Spotify error " + r.code;
			return;
		}
		status = "Connected";
		JsonObject o = JsonParser.parseString(r.body).getAsJsonObject();
		JsonElement item = o.get("item");
		if (item == null || item.isJsonNull()) {
			hasTrack = false;
			return;
		}
		JsonObject it = item.getAsJsonObject();
		title = it.get("name").getAsString();
		durationMs = it.get("duration_ms").getAsLong();

		StringBuilder sb = new StringBuilder();
		JsonArray arts = it.getAsJsonArray("artists");
		for (int i = 0; i < arts.size(); i++) {
			if (i > 0) sb.append(", ");
			sb.append(arts.get(i).getAsJsonObject().get("name").getAsString());
		}
		artist = sb.toString();

		String artUrl = "";
		if (it.has("album") && it.get("album").isJsonObject()) {
			JsonObject al = it.getAsJsonObject("album");
			album = al.get("name").getAsString();
			JsonArray imgs = al.getAsJsonArray("images");
			if (imgs != null && imgs.size() > 0) artUrl = imgs.get(imgs.size() - 1).getAsJsonObject().get("url").getAsString();
		}

		playing = o.has("is_playing") && o.get("is_playing").getAsBoolean();
		progressMs = o.has("progress_ms") && !o.get("progress_ms").isJsonNull() ? o.get("progress_ms").getAsLong() : 0;
		progressStamp = System.currentTimeMillis();
		if (o.has("device") && o.get("device").isJsonObject()) {
			JsonElement v = o.getAsJsonObject("device").get("volume_percent");
			if (v != null && !v.isJsonNull()) volume = v.getAsInt();
		}
		hasTrack = true;

		if (!artUrl.isEmpty() && !artUrl.equals(loadedArtUrl)) {
			loadedArtUrl = artUrl;
			loadArt(artUrl);
		}
	}

	private void loadArt(String url) {
		exec.submit(() -> {
			try {
				HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
				c.setConnectTimeout(6000);
				c.setReadTimeout(8000);
				byte[] jpg;
				try (InputStream in = c.getInputStream()) { jpg = in.readAllBytes(); }
				// Spotify serves JPEG; NativeImage only reads PNG, so convert through ImageIO.
				java.awt.image.BufferedImage bi = javax.imageio.ImageIO.read(new ByteArrayInputStream(jpg));
				ByteArrayOutputStream png = new ByteArrayOutputStream();
				javax.imageio.ImageIO.write(bi, "png", png);
				NativeImage img = NativeImage.read(new ByteArrayInputStream(png.toByteArray()));
				MinecraftClient mc = MinecraftClient.getInstance();
				mc.execute(() -> {
					Identifier old = artTexture;
					Identifier id = new Identifier("lovenote", "spotify_art_" + (artCounter++));
					mc.getTextureManager().registerTexture(id, new NativeImageBackedTexture(img));
					artTexture = id;
					if (old != null) mc.getTextureManager().destroyTexture(old);
				});
			} catch (Throwable t) {
				artTexture = null; // fall back to placeholder
			}
		});
	}

	// ------------------------------------------------------------ controls

	public void next() {
		command("POST", "/next");
	}

	public void previous() {
		command("POST", "/previous");
	}

	public void togglePlay() {
		boolean wasPlaying = playing;
		// optimistic UI update
		progressMs = progress();
		progressStamp = System.currentTimeMillis();
		playing = !wasPlaying;
		command("PUT", wasPlaying ? "/pause" : "/play");
	}

	public void seek(long ms) {
		progressMs = ms;
		progressStamp = System.currentTimeMillis();
		command("PUT", "/seek?position_ms=" + ms);
	}

	public void setVolume(int pct) {
		volume = pct;
		command("PUT", "/volume?volume_percent=" + pct);
	}

	private void command(String method, String path) {
		if (!connected) return;
		exec.submit(() -> {
			try {
				ensureToken();
				Resp r = http(method, "https://api.spotify.com/v1/me/player" + path, "", accessToken);
				if (r.code == 401 && refresh()) r = http(method, "https://api.spotify.com/v1/me/player" + path, "", accessToken);
				if (r.code == 403) status = "Spotify Premium is required for playback controls";
				else if (r.code == 404) status = "No active Spotify device";
				Thread.sleep(400);
				fetch();
			} catch (Exception e) {
				status = "Error: " + e.getMessage();
			}
		});
	}

	// ------------------------------------------------------------ http + config helpers

	private static Resp http(String method, String url, String body, String bearer) throws Exception {
		HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
		c.setRequestMethod(method);
		c.setConnectTimeout(6000);
		c.setReadTimeout(8000);
		c.setRequestProperty("User-Agent", "Lovenote");
		if (bearer != null) c.setRequestProperty("Authorization", "Bearer " + bearer);
		if (body != null) {
			byte[] b = body.getBytes(StandardCharsets.UTF_8);
			if (!body.isEmpty()) c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			c.setDoOutput(true);
			c.setFixedLengthStreamingMode(b.length);
			try (OutputStream o = c.getOutputStream()) { o.write(b); }
		}
		int code = c.getResponseCode();
		InputStream in = code >= 400 ? c.getErrorStream() : c.getInputStream();
		String s = in == null ? "" : new String(in.readAllBytes(), StandardCharsets.UTF_8);
		return new Resp(code, s);
	}

	private static String enc(String s) {
		return URLEncoder.encode(s, StandardCharsets.UTF_8);
	}

	private static String randomString(int n) {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~";
		SecureRandom r = new SecureRandom();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) sb.append(chars.charAt(r.nextInt(chars.length())));
		return sb.toString();
	}

	private Path cfg() {
		return FabricLoader.getInstance().getConfigDir().resolve("lovenote-spotify.json");
	}

	private void loadConfig() {
		try {
			Path p = cfg();
			if (!Files.exists(p)) return;
			JsonObject o = JsonParser.parseString(Files.readString(p)).getAsJsonObject();
			if (o.has("clientId")) clientId = o.get("clientId").getAsString();
			if (o.has("refreshToken")) refreshToken = o.get("refreshToken").getAsString();
		} catch (Exception ignored) {}
	}

	private void saveConfig() {
		try {
			JsonObject o = new JsonObject();
			o.addProperty("clientId", clientId);
			o.addProperty("refreshToken", refreshToken);
			Files.writeString(cfg(), GSON.toJson(o));
		} catch (Exception ignored) {}
	}
}
