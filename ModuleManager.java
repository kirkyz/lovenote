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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleManager {
	private final List<Module> modules = new ArrayList<>();
	private static final Map<String, String> DESC = new HashMap<>();

	static {
		DESC.put("KillAura", "Swings at anything in reach automatically.");
		DESC.put("Triggerbot", "Attacks the moment your crosshair lands.");
		DESC.put("AimAssist", "Smoothly pulls your aim onto targets.");
		DESC.put("Criticals", "Turns every hit into a critical.");
		DESC.put("Velocity", "Cuts knockback so you stay planted.");
		DESC.put("AutoTotem", "Keeps a totem in your off hand.");
		DESC.put("CrystalBreak", "Breaks crystals the instant they place.");
		DESC.put("AutoCrystal", "Places and pops end crystals for you.");
		DESC.put("Surround", "Wraps your feet in obsidian.");
		DESC.put("AutoTrap", "Boxes the enemy in from above.");
		DESC.put("AutoCity", "Mines the enemy out of their hole.");
		DESC.put("Backtrack", "Shows targets at their delayed position.");
		DESC.put("TargetStrafe", "Strafes around whoever is closest.");
		DESC.put("AutoWeb", "Places blocks around the target's feet.");
		DESC.put("AutoArmor", "Wears your best armour automatically.");
		DESC.put("Reach", "Extends your block and entity reach.");
		DESC.put("Sprint", "Always sprint, no double-tap.");
		DESC.put("Speed", "Extra ground speed.");
		DESC.put("Flight", "Free flight in survival.");
		DESC.put("Bhop", "Bunnyhop across the map.");
		DESC.put("Scaffold", "Places blocks under you as you walk.");
		DESC.put("NoFall", "Removes fall damage.");
		DESC.put("AntiVoid", "Catches you before the void.");
		DESC.put("Step", "Steps up full blocks without jumping.");
		DESC.put("NoSlow", "No slowdown while eating or blocking.");
		DESC.put("FastFall", "Drops faster so you land first.");
		DESC.put("ElytraPlus", "Elytra auto-deploy and rocket boost.");
		DESC.put("ESP", "Boxes players through walls.");
		DESC.put("Glow", "Outlines entities with the glow effect.");
		DESC.put("Tracers", "Lines from your view to each player.");
		DESC.put("StorageESP", "Highlights chests, barrels and shulkers.");
		DESC.put("HoleESP", "Marks safe holes around you.");
		DESC.put("BlockESP", "Highlights ore and other blocks around you.");
		DESC.put("Nametags", "Bigger nametags with health and distance.");
		DESC.put("Trajectories", "Shows where your projectile lands.");
		DESC.put("Zoom", "Hold a key to zoom in.");
		DESC.put("Freelook", "Hold a key to look around freely.");
		DESC.put("JumpCircles", "Ring effect on every jump.");
		DESC.put("Trail", "Ribbon that follows you.");
		DESC.put("Orbit", "Particles orbiting your body.");
		DESC.put("Wings", "Cosmetic wings on your back.");
		DESC.put("HitParticles", "Sparks on every landed hit.");
		DESC.put("KillEffects", "Burst when you get a kill.");
		DESC.put("PopEffects", "Effect when a totem pops.");
		DESC.put("AmbientParticles", "Idle particles around the world.");
		DESC.put("HitMarker", "Crosshair marker on hit.");
		DESC.put("HitSound", "Audio cue on hit.");
		DESC.put("TimeChanger", "Client-side time of day.");
		DESC.put("Fullbright", "Brightness everywhere.");
		DESC.put("SkyShader", "Custom skies, stars and aurora.");
		DESC.put("StaffDetector", "Alerts when staff join.");
		DESC.put("AutoClicker", "Holds the mouse for you.");
		DESC.put("AutoFarm", "Farms crops and mobs.");
		DESC.put("AutoFish", "Fishes automatically.");
		DESC.put("CommandMacro", "Runs chat commands on a key.");
		DESC.put("Interface", "Theme, layout and menu behaviour.");
		DESC.put("Watermark", "Brand chip with fps, ping and clock.");
		DESC.put("ModuleList", "Arraylist of enabled modules.");
		DESC.put("HitLog", "Log of recent hits.");
		DESC.put("Notifications", "Toast popups on every toggle.");
		DESC.put("TargetHud", "Health and distance of your last target.");
		DESC.put("Keystrokes", "On-screen WASD keys.");
		DESC.put("Info", "Coordinates and speed readout.");
		DESC.put("PotionList", "Active effects with timers.");
		DESC.put("ArmorHud", "Armour and durability readout.");
		DESC.put("CpsHud", "Clicks per second counter.");
		DESC.put("CoordsHud", "Position, chunk and facing.");
		DESC.put("ServerInfo", "Server, ping and player count.");
		DESC.put("PingHud", "Ping and connection quality.");
		DESC.put("Spotify", "Now playing widget.");
		DESC.put("AutoLog", "Disconnects at low health.");
		DESC.put("AutoTool", "Picks the right tool for the block.");
		DESC.put("FastPlace", "Removes the place delay.");
		DESC.put("AutoEat", "Eats when you get hungry.");
		DESC.put("AntiAFK", "Moves so you are never idle.");
		DESC.put("MiddleClick", "Middle click sends a pearl or totem to your off hand.");
		DESC.put("ChatSuffix", "Adds a tag to the end of your chat messages.");
		DESC.put("Panic", "One key to disable everything.");
	}

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
		modules.add(new Backtrack());
		modules.add(new TargetStrafe());
		modules.add(new AutoWeb());
		modules.add(new AutoArmor());
		modules.add(new Reach());
		// movement
		modules.add(new Sprint());
		modules.add(new Speed());
		modules.add(new Flight());
		modules.add(new Bhop());
		modules.add(new Scaffold());
		modules.add(new NoFall());
		modules.add(new AntiVoid());
		modules.add(new Step());
		modules.add(new NoSlow());
		modules.add(new FastFall());
		modules.add(new ElytraPlus());
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
		modules.add(new StorageESP());
		modules.add(new HoleESP());
		modules.add(new BlockESP());
		modules.add(new Nametags());
		modules.add(new Trajectories());
		modules.add(new Zoom());
		modules.add(new Freelook());
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
		modules.add(new PotionList());
		modules.add(new ArmorHud());
		modules.add(new CpsHud());
		modules.add(new CoordsHud());
		modules.add(new ServerInfo());
		modules.add(new PingHud());
		modules.add(new SpotifyHud());
		// misc
		modules.add(new AutoLog());
		modules.add(new AutoTool());
		modules.add(new FastPlace());
		modules.add(new AutoEat());
		modules.add(new AntiAFK());
		modules.add(new MiddleClick());
		modules.add(new ChatSuffix());
		modules.add(new Panic());

		// surface the server-relevant modules in the hypixel tab too
		also(Category.HYPIXEL,
				KillAura.class, Triggerbot.class, AimAssist.class, Velocity.class, Criticals.class,
				AutoTotem.class, Sprint.class, NoFall.class, Scaffold.class,
				ESP.class, Tracers.class, AutoTool.class, FastPlace.class, InfoHud.class);

		// attach card descriptions for every module that does not set its own
		for (Module m : modules) {
			String d = DESC.get(m.name);
			if (d != null && m.description.isEmpty()) m.description = d;
		}
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
