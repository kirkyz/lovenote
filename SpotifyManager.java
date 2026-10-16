package com.lovenote.spotify;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Windows Spotify media-session reader. No Spotify API or Premium required. */
public final class SpotifyManager {
    public static final SpotifyManager I = new SpotifyManager();

    // Kept for compatibility with existing menu code.
    public volatile String clientId = "";
    public volatile String status = "Windows media: not started";
    public volatile boolean connected, hasTrack, playing;
    public volatile String title = "", artist = "", album = "";
    public volatile long durationMs, progressMs, progressStamp;
    public volatile int volume = -1;
    public volatile Identifier artTexture;

    private volatile boolean started;
    private volatile String lastCover = "";
    private int artCounter;
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Lovenote-Windows-Media");
        t.setDaemon(true);
        return t;
    });

    private SpotifyManager() {}

    private Path helperPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("lovenote-media-bridge.exe");
    }

    public void start() {
        if (started) return;
        started = true;
        connect();
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2500);
                    if (connected) poll();
                } catch (InterruptedException e) {
                    return;
                } catch (Exception e) {
                    hasTrack = false;
                    status = "Windows media error: " + e.getMessage();
                }
            }
        }, "Lovenote-Windows-Media-Poll");
        t.setDaemon(true);
        t.start();
    }

    public void setClientId(String ignored) { /* No Spotify client ID needed. */ }

    public void connect() {
        if (!Files.isRegularFile(helperPath())) {
            connected = false;
            status = "Missing config/lovenote-media-bridge.exe";
            return;
        }
        connected = true;
        status = "Windows media connected - waiting for Spotify";
    }

    public void disconnect() {
        connected = false;
        hasTrack = false;
        status = "Not connected";
    }

    public long progress() {
        if (!hasTrack) return 0;
        long p = progressMs;
        if (playing) p += System.currentTimeMillis() - progressStamp;
        return Math.max(0, durationMs > 0 ? Math.min(p, durationMs) : p);
    }

    private JsonObject runHelper(String... args) throws Exception {
        String[] cmd = new String[args.length + 1];
        cmd[0] = helperPath().toString();
        System.arraycopy(args, 0, cmd, 1, args.length);
        Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        String line;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            line = reader.readLine();
        }
        if (!p.waitFor(8, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            throw new IllegalStateException("media helper timed out");
        }
        if (line == null || !line.startsWith("{"))
            throw new IllegalStateException("media helper returned no JSON");
        JsonObject o = JsonParser.parseString(line).getAsJsonObject();
        if (o.has("error")) throw new IllegalStateException(o.get("error").getAsString());
        return o;
    }

    private void poll() throws Exception {
        if (!Files.isRegularFile(helperPath())) {
            connected = false;
            hasTrack = false;
            status = "Missing config/lovenote-media-bridge.exe";
            return;
        }
        JsonObject o = runHelper();
        if (!o.has("found") || !o.get("found").getAsBoolean()) {
            hasTrack = false;
            status = "Windows media connected - open Spotify and play music";
            return;
        }
        title = getString(o, "title");
        artist = getString(o, "artist");
        album = getString(o, "album");
        playing = o.get("playing").getAsBoolean();
        progressMs = o.get("progressMs").getAsLong();
        durationMs = o.get("durationMs").getAsLong();
        progressStamp = System.currentTimeMillis();
        hasTrack = !title.isBlank();
        status = hasTrack ? "Connected (Windows media)" : "Spotify session has no track";

        String cover = getString(o, "cover");
        if (!cover.equals(lastCover)) {
            lastCover = cover;
            if (cover.isEmpty()) {
                MinecraftClient.getInstance().execute(() -> {
                    Identifier old = artTexture;
                    artTexture = null;
                    if (old != null) MinecraftClient.getInstance().getTextureManager().destroyTexture(old);
                });
            } else {
                try { loadCover(cover); } catch (Exception ignored) { /* show note placeholder */ }
            }
        }
    }

    private static String getString(JsonObject o, String name) {
        return o.has(name) && !o.get(name).isJsonNull() ? o.get(name).getAsString() : "";
    }

    private void loadCover(String base64) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(base64);
        BufferedImage bi = ImageIO.read(new ByteArrayInputStream(bytes));
        if (bi == null) return;
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", png);
        NativeImage image = NativeImage.read(new ByteArrayInputStream(png.toByteArray()));
        MinecraftClient.getInstance().execute(() -> {
            Identifier old = artTexture;
            Identifier id = Identifier.of("lovenote", "spotify_art_" + (artCounter++));
            MinecraftClient.getInstance().getTextureManager().registerTexture(id, new NativeImageBackedTexture(() -> "lovenote_spotify_art", image));
            artTexture = id;
            if (old != null) MinecraftClient.getInstance().getTextureManager().destroyTexture(old);
        });
    }

    private void command(String... args) {
        if (!connected) return;
        worker.submit(() -> {
            try { runHelper(args); }
            catch (Exception e) { status = "Media control error: " + e.getMessage(); }
        });
    }

    public void next() { command("next"); }
    public void previous() { command("previous"); }
    public void togglePlay() { command("toggle"); }
    public void seek(long ms) { command("seek", Long.toString(ms)); }
    public void setVolume(int pct) {
        // GSMTC doesn't expose Spotify's volume; use Windows/Spotify volume instead.
        status = "Change volume in Spotify or Windows";
    }
}
