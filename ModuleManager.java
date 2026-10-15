package com.lovenote.module;

import com.lovenote.module.impl.*;
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
		// ---- combat
		modules.add(new Aura());
		modules.add(new AimAssist());
		modules.add(new Triggerbot());
		modules.add(new AutoClicker());
		modules.add(new Criticals());
		modules.add(new WTap());
		modules.add(new Velocity());
		modules.add(new AntiBot());
		modules.add(new AutoTotem());
		modules.add(new AutoArmor());
		modules.add(new AutoGApple());
		modules.add(new AutoPotion());
		modules.add(new AutoCrystal());
		modules.add(new CrystalTap());
		modules.add(new CrystalBreak());
		modules.add(new AutoCity());
		modules.add(new AutoTrap());
		modules.add(new AutoWeb());
		modules.add(new Surround());
		// ---- movement
		modules.add(new Sprint());
		modules.add(new Speed());
		modules.add(new Flight());
		modules.add(new Bhop());
		modules.add(new AirJump());
		modules.add(new Parkour());
		modules.add(new Scaffold());
		modules.add(new NoFall());
		modules.add(new AntiVoid());
		// ---- visuals
		modules.add(new ESP());
		modules.add(new ItemESP());
		modules.add(new Glow());
		modules.add(new Tracers());
		modules.add(new TargetESP());
		modules.add(new Wings());
	    modules.add(new SkyShader()); 
		modules.add(new SwingAnimation());
		modules.add(new JumpCircles());
		modules.add(new Trail());
		modules.add(new Orbit());
		modules.add(new HitParticles());
		modules.add(new KillEffects());
		modules.add(new PopEffects());
		modules.add(new AmbientParticles());
		modules.add(new HitMarker());
		modules.add(new HitSound());
		modules.add(new TimeChanger());
		modules.add(new Fullbright());
		modules.add(new Zoom());
		modules.add(new ParticleField());
		modules.add(new GroundRings());
		modules.add(new TrailRibbon());
		modules.add(new Halo());
		modules.add(new WorldTint());
		// ---- hud widgets
		modules.add(new InterfaceModule());
		modules.add(new Watermark());
		modules.add(new SpotifyHud());
		modules.add(new StaffList());
		modules.add(new TargetHud());
		modules.add(new SpeedWidget());
		modules.add(new Coordinates());
		modules.add(new KeybindsWidget());
		modules.add(new PotionsWidget());
		modules.add(new CooldownsWidget());
		modules.add(new InventoryWidget());
		modules.add(new Keystrokes());
		modules.add(new Radar());
		modules.add(new ModuleList());
		modules.add(new HitLog());
		modules.add(new Notifications());
		// ---- misc
		modules.add(new AutoTool());
		modules.add(new FastPlace());
		modules.add(new AutoRespawn());
		modules.add(new AutoLog());
		modules.add(new FakePlayer());
		modules.add(new Panic());

		describe();
		for (String n : new String[]{"Watermark", "ModuleList", "TargetHud", "Notifications", "HitLog"}) {
			for (Module m : modules) if (m.name.equals(n)) m.enabled = true;
		}
	}

	private void d(String name, String text) {
		for (Module m : modules) if (m.name.equals(name)) m.description = text;
	}

	private void describe() {
		d("Aura", "Automatically attacks nearby entities");
		d("AimAssist", "Smoothly aims at nearby targets");
		d("Triggerbot", "Attacks when your crosshair is on a target");
		d("AutoClicker", "Holds a steady CPS while you hold attack");
		d("Criticals", "Allows you to do critical hits even when it's impossible");
		d("WTap", "Resets sprint on hit for full knockback");
		d("Velocity", "Reduces or cancels knockback");
		d("AntiBot", "Removes bots created by anti-cheat");
		d("AutoTotem", "Automatically places a totem in the off-hand");
		d("AutoArmor", "Automatically equips the best armor");
		d("AutoGApple", "Eats golden apples when reaching a certain health level");
		d("AutoPotion", "Automatically uses splash healing potions");
		d("AutoCrystal", "Places and breaks end crystals around targets");
		d("CrystalTap", "Places and detonates crystals under your crosshair");
		d("CrystalBreak", "Breaks end crystals that are in range");
		d("AutoCity", "Mines the obsidian surrounding a target");
		d("AutoTrap", "Automatically places traps around targets");
		d("AutoWeb", "Automatically places webs on targets");
		d("Surround", "Surrounds your feet with obsidian");

		d("Sprint", "Keeps you sprinting while moving forward");
		d("Speed", "Moves you faster along the ground");
		d("Flight", "Fly with adjustable speeds");
		d("Bhop", "Hops automatically while you move");
		d("AirJump", "Lets you jump again while in the air");
		d("Parkour", "Jumps automatically at the edge of blocks");
		d("Scaffold", "Places blocks under your feet as you walk");
		d("NoFall", "Cancels fall damage");
		d("AntiVoid", "Bounces you back up before you hit the void");

		d("ESP", "Highlights player models with configurable boxes");
		d("ItemESP", "Highlights dropped items");
		d("Glow", "Vanilla outline on players, visible through walls");
		d("Tracers", "Draws lines from your crosshair to players");
		d("TargetESP", "Animated rings around your current target");
		d("Wings", "Translucent wings behind your back");
		d("WorldShaders", "Recolours the sky and clouds with presets");
		d("SwingAnimation", "Changes first-person item swing animations");
		d("JumpCircles", "Expanding ring where you jump");
		d("Trail", "Leaves a particle trail behind you");
		d("Orbit", "Particles circling your player");
		d("HitParticles", "Particle burst when you hit something");
		d("KillEffects", "Lightning, totem or blood burst on kills");
		d("PopEffects", "Particles and a log line on totem pops");
		d("AmbientParticles", "Snow, glow and stars around you");
		d("HitMarker", "Crosshair marker flash when you hit");
		d("HitSound", "Plays a sound when you hit something");
		d("TimeChanger", "Client-side time of day");
		d("Fullbright", "See clearly in the dark");
		d("Zoom", "Custom field of view, bind it to zoom");
		d("ParticleField", "Ambient particle field drifting around you.");
		d("GroundRings", "Expanding rings on the floor under you.");
		d("TrailRibbon", "Camera-facing ribbon trailing behind you.");
		d("Halo", "Glowing column and rising spark spiral.");
		d("WorldTint", "Tints the sky, fog and screen edges.");

		d("Watermark", "Client name, user, fps, time and IP");
		d("Music", "Now-playing widget for Spotify");
		d("StaffList", "Lists spectators (vanished staff)");
		d("TargetHud", "Shows the player you last hit");
		d("Speed Display", "Shows your current speed in blocks per second");
		d("Coordinates", "Shows your coordinates");
		d("Keybinds", "Lists your module keybinds");
		d("Potions", "Shows active potion effects");
		d("Cooldowns", "Shows item cooldowns");
		d("Inventory", "Shows your inventory on screen");
		d("Keystrokes", "Shows WASD and mouse buttons");
		d("Radar", "Top-down radar of nearby players");
		d("ModuleList", "List of enabled modules");
		d("HitLog", "Log of hits, pops and events");
		d("Notifications", "Toasts when modules are toggled");

		d("AutoTool", "Switches to the best tool while mining");
		d("FastPlace", "Removes the block placing delay");
		d("AutoRespawn", "Respawns automatically when you die");
		d("AutoLog", "Disconnects when your health gets low");
		d("FakePlayer", "Spawns a dummy player to test on");
		d("Panic", "Turns every module off at once");
	}

	public List<Module> all() { return modules; }

	@SuppressWarnings("unchecked")
	public <T extends Module> T get(Class<T> c) {
		for (Module m : modules) if (c.isInstance(m)) return (T) m;
		return null;
	}

	public List<Module> in(Category c) {
		List<Module> out = new ArrayList<>();
		for (Module m : modules) if (m.category == c) out.add(m);
		return out;
	}

	public void tick() {
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
