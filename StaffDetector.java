package com.lovenote.module.impl;

import com.lovenote.LovenoteClient;
import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.NumberSetting;
import com.lovenote.util.Colors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Watches the tab list for Hypixel staff ranks and alerts you. */
public class StaffDetector extends Module {
private static final String[] RANKS = { "[ADMIN]", "[MOD]", "[MODERATOR]", "[HELPER]", "[GAME MASTER]", "[OWNER]" };
private static final String[] EXTRA = { "[YOUTUBE]", "[YT]", "[MOJANG]" };

private final BoolSetting youtubers = add(new BoolSetting("Include YouTubers", false));
private final BoolSetting chatAlert = add(new BoolSetting("Chat alert", true));
private final BoolSetting sound = add(new BoolSetting("Sound alert", true));
private final BoolSetting hud = add(new BoolSetting("HUD list", true));
private final BoolSetting nearOnly = add(new BoolSetting("Only when nearby", false));
private final NumberSetting range = add(new NumberSetting("Near range", 48, 8, 128, 4));
private final BoolSetting panic = add(new BoolSetting("Panic (disable modules)", false));

private final Set<String> seen = new HashSet<>();
private final Map<String, String> found = new HashMap<>();
private final List<Module> panicked = new ArrayList<>();
private int timer;

public StaffDetector() {
super("StaffDetector", Category.HYPIXEL);
}

@Override
public void onDisable() {
unpanic();
seen.clear();
found.clear();
}

@Override
public void onTick() {
if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null) return;
if (++timer % 20 != 0) return;

found.clear();
for (PlayerListEntry e : mc.getNetworkHandler().getPlayerList()) {
Text dn = e.getDisplayName();
String line = dn != null ? dn.getString() : e.getProfile().name();
if (line == null) continue;
String rank = rankOf(line);
if (rank == null) continue;
if (nearOnly.get() && !near(e.getProfile().name())) continue;
found.put(e.getProfile().name(), rank);
if (seen.add(e.getProfile().name())) alert(e.getProfile().name(), rank);
}
seen.retainAll(found.keySet());

if (panic.get() && !found.isEmpty()) doPanic();
else if (panic.get()) unpanic();
}

private String rankOf(String line) {
for (String r : RANKS) if (line.contains(r)) return r;
if (youtubers.get()) for (String r : EXTRA) if (line.contains(r)) return r;
return null;
}

private boolean near(String name) {
for (PlayerEntity p : mc.world.getPlayers()) {
if (p == mc.player) continue;
if (p.getGameProfile().name().equalsIgnoreCase(name))
return mc.player.distanceTo(p) <= range.get();
}
return false;
}

private void alert(String name, String rank) {
if (chatAlert.get()) {
mc.inGameHud.getChatHud().addMessage(Text.literal("[Lovenote] " + rank + " " + name + " detected"));
}
if (sound.get()) mc.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.0f, 1.6f);
}

private void doPanic() {
if (!panicked.isEmpty()) return;
for (Module m : LovenoteClient.modules.all()) {
if (m == this || !m.enabled || m.category == Category.HUD) continue;
panicked.add(m);
m.setEnabled(false);
}
}

private void unpanic() {
for (Module m : panicked) m.setEnabled(true);
panicked.clear();
}

@Override
public void onHud(DrawContext ctx, float td) {
if (!hud.get() || found.isEmpty()) return;
int y = 4;
ctx.drawTextWithShadow(mc.textRenderer, "staff: " + found.size(), 4, y, Colors.wave(0f));
y += 11;
for (Map.Entry<String, String> e : found.entrySet()) {
ctx.drawTextWithShadow(mc.textRenderer, e.getValue() + " " + e.getKey(), 4, y, 0xFFFFD27A);
y += 10;
}
}
}
