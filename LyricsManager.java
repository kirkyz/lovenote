package com.lovenote.spotify;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Fetches synced lyrics from lrclib.net. No API key, no extra dependency. */
public final class LyricsManager {
	public static final LyricsManager I = new LyricsManager();

	private static final Pattern LRC = Pattern.compile("\\[(\\d{1,3}):(\\d{1,2})(?:[.:](\\d{1,3}))?]\\s*(.*)");

	public volatile String status = "idle";
	public volatile String track = "";

	private final List<Long> times = new ArrayList<>();
	private final List<String> texts = new ArrayList<>();
	private int curIndex = -1;
	private String key = "";
	private volatile boolean busy;
	private long lastAttempt;

	private final HttpClient http = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(6))
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();

	private LyricsManager() {}

	public synchronized boolean hasLyrics() { return !times.isEmpty(); }

	public synchronized void clear() {
		times.clear();
		texts.clear();
		curIndex = -1;
		key = "";
		track = "";
		status = "idle";
	}

	/** Called every client tick from the Lyrics widget. */
	public void tick(long progressMs) {
		SpotifyManager s = SpotifyManager.I;
		if (!s.hasTrack) {
			if (hasLyrics()) clear();
			return;
		}
		String k = s.artist + "|" + s.title + "|" + s.durationMs;
		if (!k.equals(key)) {
			clear();
			key = k;
			track = s.artist + " - " + s.title;
			startFetch(s.artist, s.title);
		}
		if (hasLyrics()) {
			update(progressMs);
		} else if (!busy && System.currentTimeMillis() - lastAttempt > 4000) {
			startFetch(s.artist, s.title);
		}
	}

	private void startFetch(String artist, String title) {
		if (artist.isBlank() || title.isBlank()) { status = "no track info"; return; }
		if (busy) return;
		busy = true;
		lastAttempt = System.currentTimeMillis();
		status = "searching...";
		Thread t = new Thread(() -> fetch(artist, title), "Lovenote-Lyrics");
		t.setDaemon(true);
		t.start();
	}

	private void fetch(String artist, String title) {
		try {
			String url = "https://lrclib.net/api/search?artist_name=" + enc(artist) + "&track_name=" + enc(title);
			HttpRequest req = HttpRequest.newBuilder(URI.create(url))
					.timeout(Duration.ofSeconds(10))
					.header("User-Agent", "Lovenote Client (github.com/kirkyz/lovenote)")
					.header("Accept", "application/json")
					.GET()
					.build();
			HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
			if (res.statusCode() != 200) { status = "http " + res.statusCode(); return; }
			JsonElement root = JsonParser.parseString(res.body());
			if (!root.isJsonArray()) { status = "no lyrics"; return; }
			JsonArray arr = root.getAsJsonArray();
			String synced = null, plain = null;
			for (JsonElement el : arr) {
				if (!el.isJsonObject()) continue;
				JsonObject o = el.getAsJsonObject();
				String syn = str(o, "syncedLyrics");
				if (!syn.isBlank()) { synced = syn; break; }
				if (plain == null) {
					String p = str(o, "plainLyrics");
					if (!p.isBlank()) plain = p;
				}
			}
			if (synced == null && plain == null) { status = "no lyrics found"; return; }
			parse(synced, plain);
		} catch (Exception e) {
			status = "error: " + e.getMessage();
		} finally {
			busy = false;
		}
	}

	private synchronized void parse(String synced, String plain) {
		List<Long> t = new ArrayList<>();
		List<String> x = new ArrayList<>();

		if (synced != null) {
			for (String raw : synced.split("\r?\n")) {
				Matcher m = LRC.matcher(raw.trim());
				List<Long> stamps = new ArrayList<>();
				String last = null;
				while (m.find()) {
					int min = Integer.parseInt(m.group(1));
					int sec = Integer.parseInt(m.group(2));
					String frac = m.group(3);
					int ms = 0;
					if (frac != null) {
						String f = frac.length() >= 3 ? frac.substring(0, 3) : (frac + "00").substring(0, 3);
						ms = Integer.parseInt(f);
					}
					stamps.add(min * 60_000L + sec * 1000L + ms);
					last = m.group(4);
				}
				if (stamps.isEmpty()) continue;
				String text = last == null ? "" : last.trim();
				for (long s : stamps) { t.add(s); x.add(text); }
			}
		}

		if (t.isEmpty() && plain != null) {
			String[] lines = plain.split("\r?\n");
			long dur = SpotifyManager.I.durationMs;
			for (int i = 0; i < lines.length; i++) {
				t.add(dur > 0 ? dur * i / Math.max(1, lines.length) : i * 4000L);
				x.add(lines[i].trim());
			}
		}

		if (t.isEmpty()) { status = "no lyrics found"; return; }

		List<Integer> order = new ArrayList<>();
		for (int i = 0; i < t.size(); i++) order.add(i);
		order.sort((a, b) -> Long.compare(t.get(a), t.get(b)));

		times.clear();
		texts.clear();
		for (int i : order) { times.add(t.get(i)); texts.add(x.get(i)); }
		curIndex = -1;
		status = synced != null ? "synced" : "unsynced";
	}

	private synchronized void update(long progressMs) {
		int idx = -1;
		for (int i = 0; i < times.size(); i++) {
			if (times.get(i) <= progressMs) idx = i;
			else break;
		}
		curIndex = idx;
	}

	public synchronized String lineAt(int delta) {
		int i = curIndex + delta;
		if (i < 0 || i >= texts.size()) return "";
		return texts.get(i);
	}

	private static String str(JsonObject o, String n) {
		return o.has(n) && !o.get(n).isJsonNull() ? o.get(n).getAsString() : "";
	}

	private static String enc(String s) {
		return URLEncoder.encode(s, StandardCharsets.UTF_8);
	}
}
