package com.lovenote.module;

import com.lovenote.module.impl.*;
import com.lovenote.util.Colors;
import com.lovenote.util.RenderUtil;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
private final List<Module> modules = new ArrayList<>();

public ModuleManager() {
// combat
modules.add(new KillAura());
modules.add(new Triggerbot());
modules.add(new AimAssist());
modules.add(new Criticals());
modules.add(new Velocity());
modules.add(new AutoTotem());
modules.add(new CrystalBreak());
modules.add(new AutoCrystal());
modules.add(new Surround());
modules.add(new AutoTrap());
modules.add(new AutoCity());
// movement
modules.add(new Sprint());
modules.add(new Speed());
modules.add(new Flight());
modules.add(new Bhop());
modules.add(new Scaffold());
modules.add(new NoFall());
modules.add(new AntiVoid());
// visuals
modules.add(new ESP());
modules.add(new Glow());
modules.add(new Tracers());
modules.add(new JumpCircles());
modules.add(new Trail());
modules.add(new Orbit());
modules.add(new Wings());
modules.add(new HitParticles());
modules.add(new KillEffects());
modules.add(new PopEffects());
modules.add(new AmbientParticles());
modules.add(new HitMarker());
modules.add(new HitSound());
modules.add(new TimeChanger());
modules.add(new Fullbright());
modules.add(new SkyShader());
// hypixel
modules.add(new StaffDetector());
modules.add(new AutoClicker());
modules.add(new AutoFarm());
modules.add(new AutoFish());
modules.add(new CommandMacro());
// hud
on(new InterfaceModule());
on(new Watermark());
on(new ModuleList());
on(new HitLog());
on(new Notifications());
on(new TargetHud());
modules.add(new Keystrokes());
modules.add(new InfoHud());
modules.add(new SpotifyHud());
// misc
modules.add(new AutoLog());
modules.add(new AutoTool());
modules.add(new FastPlace());
modules.add(new Panic());

// surface the server-relevant modules in the hypixel tab too
also(Category.HYPIXEL,
KillAura.class, Triggerbot.class, AimAssist.class, Velocity.class, Criticals.class,
AutoTotem.class, Sprint.class, NoFall.class, Scaffold.class,
ESP.class, Tracers.class, AutoTool.class, FastPlace.class, InfoHud.class);
}

private void on(Module m) {
m.enabled = true;
modules.add(m);
}

@SafeVarargs
private void also(Category c, Class<? extends Module>... classes) {
for (Class<? extends Module> cl : classes) {
Module m = get(cl);
if (m != null) m.alsoIn.add(c);
}
}

public List<Module> all() { return modules; }

@SuppressWarnings("unchecked")
public <T extends Module> T get(Class<T> c) {
for (Module m : modules) if (c.isInstance(m)) return (T) m;
return null;
}

public List<Module> in(Category c) {
List<Module> out = new ArrayList<>();
for (Module m : modules) if (m.category == c || m.alsoIn.contains(c)) out.add(m);
return out;
}

public void tick() {
Colors.applyCurrent();
for (Module m : modules) if (m.enabled) m.onTick();
}

public void attack(Entity target) {
for (Module m : modules) if (m.enabled) m.onAttack(target);
}

public void totemPop(Entity e) {
for (Module m : modules) if (m.enabled) m.onTotemPop(e);
}

public void render3D(WorldRenderContext ctx) {
MatrixStack m = ctx.matrixStack();
Vec3d cam = ctx.camera().getPos();
m.push();
m.translate(-cam.x, -cam.y, -cam.z);
RenderUtil.begin3D(1.5f);
for (Module mod : modules) if (mod.enabled) mod.onRender3D(ctx, m);
RenderUtil.end3D();
m.pop();
}

public void hud(DrawContext ctx, float td) {
for (Module m : modules) if (m.enabled) m.onHud(ctx, td);
}
}