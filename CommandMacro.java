package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.text.Text;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Runs commands on a timer from config/lovenote-macros.txt. */
public class CommandMacro extends Module {
private static final String DEFAULT = """
# lovenote command macros
# one per line:  <interval in seconds>  <command>
# lines starting with # are ignored. toggle the module to reload.

1800 /tip all
1200 /warp garden
""";

private static final class Macro {
final String cmd;
final long base;
long left;
Macro(String cmd, long base) { this.cmd = cmd; this.base = base; this.left = base; }
}

private final NumberSetting jitter = add(new NumberSetting("Jitter (%)", 15, 0, 60, 5));
private final BoolSetting noScreen = add(new BoolSetting("Skip while in GUI", true));
private final BoolSetting log = add(new BoolSetting("Log to chat", false));

private final List<Macro> macros = new ArrayList<>();
private final Random rnd = new Random();

public CommandMacro() {
super("CommandMacro", Category.HYPIXEL);
}

@Override
public void onEnable() {
load();
}

@Override
public void onDisable() {
macros.clear();
}

private void load() {
macros.clear();
Path p = FabricLoader.getInstance().getConfigDir().resolve("lovenote-macros.txt");
try {
if (!Files.exists(p)) Files.writeString(p, DEFAULT);
for (String raw : Files.readAllLines(p)) {
String line = raw.trim();
if (line.isEmpty() || line.startsWith("#")) continue;
String[] parts = line.split("\\s+", 2);
if (parts.length < 2) continue;
long secs;
try { secs = Long.parseLong(parts[0]); } catch (NumberFormatException e) { continue; }
if (secs < 1) secs = 1;
macros.add(new Macro(parts[1].trim(), secs * 20L));
}
} catch (Exception e) {
e.printStackTrace();
}
}

@Override
public void onTick() {
if (mc.player == null) return;
if (noScreen.get() && mc.currentScreen != null) return;
for (Macro m : macros) {
if (--m.left > 0) continue;
m.left = jittered(m.base);
send(m.cmd);
}
}

private long jittered(long base) {
long j = base * jitter.getI() / 100L;
if (j <= 0) return base;
return Math.max(20L, base + (long) ((rnd.nextDouble() * 2 - 1) * j));
}

private void send(String cmd) {
if (mc.getNetworkHandler() == null) return;
{ if (cmd.startsWith("/")) mc.getNetworkHandler().sendChatCommand(cmd.substring(1)); else mc.getNetworkHandler().sendChatMessage(cmd); }if (log.get()) mc.inGameHud.getChatHud().addMessage(Text.literal("[Lovenote] > " + cmd));
}
}
