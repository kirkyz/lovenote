package com.lovenote.module;

import com.lovenote.module.impl.*;
import com.lovenote.util.RenderUtil;
import com.lovenote.util.WorldRenderContext;
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
		modules.add(new KillAura());
		modules.add(new AimAssist());
		modules.add(new Triggerbot());
		modules.add(new Criticals());
		modules.add(new Velocity());
		modules.add(new WTap());
		modules.add(new AutoClicker());
		modules.add(new AutoTotem());
		modules.add(new AutoArmor());
		modules.add(new AutoCrystal());
		modules.add(new CrystalTap());
		modules.add(new CrystalBreak());
		modules.add(new Backtrack());
		modules.add(new TargetStrafe());
		modules.add(new AutoWeb());
		modules.add(new Reach());
		modules.add(new Surround());
		modules.add(new AutoTrap());
		modules.add(new AutoCity());
		modules.add(new OldCombat());
		// ---- hypixel
		modules.add(new AutoFarm());
		modules.add(new AutoFish());
		modules.add(new CommandMacro());
		modules.add(new ServerInfo());
		modules.add(new StaffDetector());
		modules.add(new AntiBot());
		modules.add(new AutoPotion());
		modules.add(new AutoGApple());
		modules.add(new AutoTool());
		modules.add(new AutoLog());
		modules.add(new AutoRespawn());
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
		modules.add(new Step());
		modules.add(new NoSlow());
		modules.add(new FastFall());
		modules.add(new ElytraPlus());
		// ---- player
		modules.add(new ToggleSprint());
		modules.add(new AutoJump());
		modules.add(new AntiHunger());
		modules.add(new FastLadder());
		// ---- visuals
		modules.add(new ESP());
		modules.add(new ItemESP());
		modules.add(new Glow());
		modules.add(new Chams());
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
		modules.add(new Freelook());
		modules.add(new ParticleField());
		modules.add(new CustomParticles());
		modules.add(new GroundRings());
		modules.add(new TrailRibbon());
		modules.add(new GhostTrail());
		modules.add(new Halo());
		modules.add(new Nebula());
		modules.add(new GroundFog());
		modules.add(new Shockwave());
		modules.add(new DamageIndicator());
		modules.add(new WorldTint());
		modules.add(new StorageESP());
		modules.add(new HoleESP());
		modules.add(new BlockESP());
		modules.add(new Nametags());
		modules.add(new Trajectories());
		modules.add(new ScreenFX());
		modules.add(new CustomCrosshair());
		modules.add(new Bloom());
		modules.add(new Beams());
		modules.add(new BlockHighlight());
		modules.add(new Waypoints());
		modules.add(new Aurora());
		modules.add(new Starfield());
		modules.add(new SunGlow());
		modules.add(new Vignette());
		// ---- render
		modules.add(new Fog());
		modules.add(new RenderDistance());
		modules.add(new Clouds());
		modules.add(new GraphicsMode());
		modules.add(new Particles());
		modules.add(new BiomeBlend());
		modules.add(new EntityShadows());
		modules.add(new MaxFps());
		modules.add(new ChatOpacity());
		modules.add(new Weather());
		modules.add(new SmoothCamera());
		modules.add(new FovOverride());
		modules.add(new NoViewBob());
		// ---- donut
		modules.add(new AutoSell());
		modules.add(new AutoBuy());
		modules.add(new AutoTip());
		modules.add(new ChestStealer());
		modules.add(new SpawnerESP());
		modules.add(new LootBeam());
		modules.add(new AutoReconnect());
		// ---- newer additions
		modules.add(new FastUse());
		modules.add(new NoJumpDelay());
		modules.add(new SafeWalk());
		modules.add(new AutoPearl());
		modules.add(new AntiLevitation());
		modules.add(new AutoJumpReset());
		modules.add(new AutoAnchor());
		modules.add(new PlayerTracker());
		modules.add(new KillCounter());
		modules.add(new DeathCoords());
		modules.add(new AutoGG());
		modules.add(new ChestTracker());
		// ---- hud widgets
		modules.add(new InterfaceModule());
		modules.add(new Watermark());
		modules.add(new SpotifyHud());
		modules.add(new Lyrics());
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
		modules.add(new InfoHud());
		modules.add(new PotionList());
		modules.add(new ArmorHud());
		modules.add(new CpsHud());
		modules.add(new CoordsHud());
		modules.add(new PingHud());
		modules.add(new TabList());
		modules.add(new ComboCounter());
		modules.add(new Compass());
		// ---- misc
		modules.add(new FastPlace());
		modules.add(new FakePlayer());
		modules.add(new AutoEat());
		modules.add(new AntiAFK());
		modules.add(new MiddleClick());
		modules.add(new ChatSuffix());
		modules.add(new Panic());

		// file the hypixel-specific stuff into its own tab as well (widgets stay on HUD)
		hypixel("Velocity", "WTap", "Criticals", "AntiBot", "AutoTrap", "AutoWeb", "Surround",
				"AutoCity", "AutoPotion", "AutoGApple", "AutoTool", "AutoLog", "AutoRespawn",
				"AutoFarm", "AutoFish", "CommandMacro", "ServerInfo", "StaffDetector",
				"FastUse", "SafeWalk", "NoJumpDelay", "AntiLevitation", "AutoJumpReset",
				"AutoPearl", "AutoAnchor", "OldCombat");
		donut("AutoSell", "AutoBuy", "AutoTip", "ChestStealer", "SpawnerESP", "LootBeam",
				"AutoReconnect", "ChestTracker", "AutoGG", "AutoFarm", "AutoFish", "AutoTool", "AutoArmor");

		describe();
		for (String n : new String[]{"Interface", "Watermark", "ModuleList", "TargetHud",
				"Notifications", "HitLog"}) {
			for (Module m : modules) if (m.name.equals(n)) m.enabled = true;
		}
	}

	private void hypixel(String... names) { alias(Category.HYPIXEL, names); }
	private void donut(String... names) { alias(Category.DONUT, names); }

	private void alias(Category c, String... names) {
		for (String n : names) {
			for (Module m : modules) {
				if (!m.name.equals(n)) continue;
				if (m instanceof HudWidget) continue;
				m.alsoIn.add(c);
			}
		}
	}

	private void d(String name, String text) {
		for (Module m : modules) if (m.name.equals(name)) m.description = text;
	}

	private void describe() {
		d("Aura", "Automatically attacks nearby entities");
		d("KillAura", "Swings at anything in reach automatically");
		d("AimAssist", "Smoothly aims at nearby targets");
		d("Triggerbot", "Attacks when your crosshair is on a target");
		d("AutoClicker", "Holds a steady CPS while you hold attack");
		d("AutoTotem", "Automatically places a totem in the off-hand");
		d("AutoArmor", "Automatically equips the best armor");
		d("AutoCrystal", "Places and breaks end crystals around targets");
		d("CrystalTap", "Places and detonates crystals under your crosshair");
		d("CrystalBreak", "Breaks end crystals that are in range");
		d("Backtrack", "Shows targets at their delayed position");
		d("TargetStrafe", "Strafes around whoever is closest");
		d("Reach", "Extends your block and entity reach");
		d("Velocity", "Reduces or cancels knockback");
		d("WTap", "Resets sprint on hit for full knockback");
		d("Criticals", "Allows you to do critical hits even when it's impossible");
		d("AntiBot", "Removes bots created by anti-cheat");
		d("AutoTrap", "Automatically places traps around targets");
		d("AutoWeb", "Automatically places webs on targets");
		d("Surround", "Surrounds your feet with obsidian");
		d("AutoCity", "Mines the obsidian surrounding a target");
		d("AutoPotion", "Automatically uses splash healing potions");
		d("AutoGApple", "Eats golden apples when reaching a certain health level");
		d("AutoTool", "Switches to the best tool while mining");
		d("AutoLog", "Disconnects when your health gets low");
		d("AutoRespawn", "Respawns automatically when you die");
		d("AutoFarm", "Farms crops and blocks around you");
		d("AutoFish", "Casts and reels in automatically");
		d("CommandMacro", "Runs chat commands on a keybind or interval");
		d("ServerInfo", "Shows the current server, TPS and ping");
		d("StaffDetector", "Alerts you when staff join the server");
		d("OldCombat", "1.8 style combat feel with no cooldown gate");

		d("Sprint", "Keeps you sprinting while moving forward");
		d("Speed", "Moves you faster along the ground");
		d("Flight", "Fly with adjustable speeds");
		d("Bhop", "Hops automatically while you move");
		d("AirJump", "Lets you jump again while in the air");
		d("Parkour", "Jumps automatically at the edge of blocks");
		d("Scaffold", "Places blocks under your feet as you walk");
		d("NoFall", "Cancels fall damage");
		d("AntiVoid", "Bounces you back up before you hit the void");
		d("Step", "Steps up full blocks without jumping");
		d("NoSlow", "No slowdown while eating or blocking");
		d("FastFall", "Drops faster so you land first");
		d("ElytraPlus", "Elytra auto-deploy and rocket boost");

		d("ToggleSprint", "Sprint stays on without holding the key");
		d("AutoJump", "Vanilla auto jump toggle");
		d("AntiHunger", "Sends onGround=false while moving to slow hunger");
		d("FastLadder", "Climb ladders and vines much faster");

		d("ESP", "Highlights player models with configurable boxes");
		d("ItemESP", "Highlights dropped items");
		d("Glow", "Vanilla outline on players, visible through walls");
		d("Chams", "Re-colours entity models, visible through walls");
		d("Tracers", "Draws lines from your crosshair to players");
		d("TargetESP", "Animated rings around your current target");
		d("Wings", "Translucent wings behind your back");
		d("SkyShader", "Custom sky, sun, stars and cloud tint");
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
		d("Freelook", "Hold a key to look around freely");
		d("ParticleField", "Ambient particle field drifting around you");
		d("CustomParticles", "Configurable particle shapes drifting around you");
		d("GroundRings", "Expanding rings on the floor under you");
		d("TrailRibbon", "Camera-facing ribbon trailing behind you");
		d("GhostTrail", "Fading afterimages of where you have been");
		d("Halo", "Glowing column and rising spark spiral");
		d("Nebula", "Drifting colour clouds high in the sky");
		d("GroundFog", "Low lying mist rolling across the ground");
		d("Shockwave", "Expanding ring bursts when you attack or jump");
		d("DamageIndicator", "Floating target names and health on hit");
		d("WorldTint", "Tints the sky, fog and screen edges");
		d("StorageESP", "Highlights chests, barrels and shulkers");
		d("HoleESP", "Marks safe holes around you");
		d("BlockESP", "Highlights ore and other blocks around you");
		d("Nametags", "Bigger nametags with health and distance");
		d("Trajectories", "Shows where your projectile lands");
		d("ScreenFX", "Grain, scanlines, damage flash and health pulse");
		d("CustomCrosshair", "Animated crosshair in your theme colour");
		d("Bloom", "Soft additive glow around players");
		d("Beams", "Vertical light shafts above players");
		d("BlockHighlight", "Outlines the block you are looking at");
		d("Waypoints", "Saved points with beams and distance labels");
		d("Aurora", "Drifting aurora curtains high in the sky");
		d("Starfield", "Twinkling stars spread across the sky");
		d("SunGlow", "Additive glow around the sun and moon");
		d("Vignette", "Soft darkened screen edges and letterbox bars");

		d("Fog", "Custom fog distance and colour");
		d("RenderDistance", "Overrides the client view distance");
		d("Clouds", "Cloud render mode, including fully off");
		d("GraphicsMode", "Forces fast, fancy or fabulous graphics");
		d("Particles", "Cuts particle spam for more fps");
		d("BiomeBlend", "Biome colour blend radius");
		d("EntityShadows", "Toggles entity drop shadows");
		d("MaxFps", "Caps the framerate");
		d("ChatOpacity", "Chat background transparency");
		d("Weather", "Client-side weather override");
		d("SmoothCamera", "Cinematic smoothed camera movement");
		d("FovOverride", "Locks your field of view");
		d("NoViewBob", "Removes walking camera bob");

		d("AutoSell", "Runs your sell command on a timer");
		d("AutoBuy", "Buys a set item on a timer");
		d("AutoTip", "Tips everyone on a timer for free coins");
		d("ChestStealer", "Empties any chest you open");
		d("SpawnerESP", "Boxes spawners through walls");
		d("LootBeam", "Vertical beams on dropped items");
		d("AutoReconnect", "Rejoins the server after a disconnect");

		d("FastUse", "Instantly uses items without the delay");
		d("NoJumpDelay", "Removes the delay between jumps");
		d("SafeWalk", "Stops you walking off block edges");
		d("AutoPearl", "Throws a pearl when you fall or get knocked");
		d("AntiLevitation", "Cancels the levitation effect");
		d("AutoJumpReset", "Resets your sprint after a jump");
		d("AutoAnchor", "Charges and detonates respawn anchors");
		d("PlayerTracker", "Tracks where players were last seen");
		d("KillCounter", "Counts your kills this session");
		d("DeathCoords", "Logs your death coordinates");
		d("AutoGG", "Says gg at the end of a game");
		d("ChestTracker", "Remembers chests you have opened");

		d("Watermark", "Client name, user, fps, time and IP");
		d("Music", "Now-playing widget");
		d("Lyrics", "Live synced lyrics from lrclib.net");
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
		d("Info", "Coordinates and speed readout");
		d("PotionList", "Active effects with timers");
		d("ArmorHud", "Armour and durability readout");
		d("CpsHud", "Clicks per second counter");
		d("CoordsHud", "Position, chunk and facing");
		d("PingHud", "Ping and connection quality");
		d("TabList", "Clean player list with ping");
		d("ComboCounter", "Counts consecutive hits without taking damage");
		d("Compass", "Horizontal compass strip");

		d("FastPlace", "Removes the block placing delay");
		d("FakePlayer", "Spawns a dummy player to test on");
		d("AutoEat", "Eats when you get hungry");
		d("AntiAFK", "Moves so you are never idle");
		d("MiddleClick", "Middle click sends a pearl or totem to your off-hand");
		d("ChatSuffix", "Adds a tag to the end of your chat messages");
		d("Panic", "Turns every module off at once");
	}

	public List<Module> all() { return modules; }

	@SuppressWarnings("unchecked")
	public <T extends Module> T get(Class<T> c) {
		for (Module m : modules) if (c.isInstance(m)) return (T) m;
		return null;
	}

	public List<Module> in(Category c) { return modulesIn(c); }

	public List<Module> modulesIn(Category c) {
		List<Module> out = new ArrayList<>();
		for (Module m : modules) if (m.category == c || m.alsoIn.contains(c)) out.add(m);
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
		Vec3d cam = ctx.camera().getCameraPos();
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
