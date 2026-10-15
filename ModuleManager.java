can upackage com.lovenote.module;

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

	private static final Map<String, String> DESCRIPTIONS = new HashMap<>();

	static {
		DESCRIPTIONS.put("KillAura", "Swings at anything in reach automatically.");
		DESCRIPTIONS.put("Triggerbot", "Attacks the moment your crosshair lands.");
		DESCRIPTIONS.put("AimAssist", "Smoothly pulls your aim onto targets.");
		DESCRIPTIONS.put("Criticals", "Turns every hit into a critical.");
		DESCRIPTIONS.put("Velocity", "Cuts knockback so you stay planted.");
		DESCRIPTIONS.put("AutoTotem", "Keeps a totem in your off hand.");
		DESCRIPTIONS.put("CrystalBreak", "Breaks crystals the instant they place.");
		DESCRIPTIONS.put("AutoCrystal", "Places and pops end crystals for you.");
		DESCRIPTIONS.put("Surround", "Wraps your feet in obsidian.");
		DESCRIPTIONS.put("AutoTrap", "Boxes the enemy in from above.");
		DESCRIPTIONS.put("AutoCity", "Mines the enemy out of their hole.");
		DESCRIPTIONS.put("Sprint", "Always sprint, no double-tap.");
		DESCRIPTIONS.put("Speed", "Extra ground speed.");
		DESCRIPTIONS.put("Flight", "Free flight in survival.");
		DESCRIPTIONS.put("Bhop", "Bunnyhop across the map.");
		DESCRIPTIONS.put("Scaffold", "Places blocks under you as you walk.");
		DESCRIPTIONS.put("NoFall", "Removes fall damage.");
		DESCRIPTIONS.put("AntiVoid", "Catches you before the void.");
		DESCRIPTIONS.put("ESP", "Boxes players through walls.");
		DESCRIPTIONS.put("Glow", "Outlines entities with the glow effect.");
		DESCRIPTIONS.put("Tracers", "Lines from your view to each player.");
		DESCRIPTIONS.put("JumpCircles", "Ring effect on every jump.");
		DESCRIPTIONS.put("Trail", "Ribbon that follows you.");
		DESCRIPTIONS.put("Orbit", "Particles orbiting your body.");
		DESCRIPTIONS.put("Wings", "Cosmetic wings on your back.");
		DESCRIPTIONS.put("HitParticles", "Sparks on every landed hit.");
		DESCRIPTIONS.put("KillEffects", "Burst when you get a kill.");
		DESCRIPTIONS.put("PopEffects", "Effect when a totem pops.");
		DESCRIPTIONS.put("AmbientParticles", "Idle particles around the world.");
		DESCRIPTIONS.put("HitMarker", "Crosshair marker on hit.");
		DESCRIPTIONS.put("HitSound", "Audio cue on hit.");
		DESCRIPTIONS.put("TimeChanger", "Client-side time of day.");
		DESCRIPTIONS.put("Fullbright", "Brightness everywhere.");
		DESCRIPTIONS.put("SkyShader", "Custom skies, stars and aurora.");
		DESCRIPTIONS.put("StaffDetector", "Alerts when staff join.");
		DESCRIPTIONS.put("AutoClicker", "Holds the mouse for you.");
		DESCRIPTIONS.put("AutoFarm", "Farms crops and mobs.");
		DESCRIPTIONS.put("AutoFish", "Fishes automatically.");
		DESCRIPTIONS.put("CommandMacro", "Runs chat commands on a key.");
		DESCRIPTIONS.put("Interface", "Theme, layout and menu behaviour.");
		DESCRIPTIONS.put("Watermark", "Brand chip with fps, ping, server and clock.");
		DESCRIPTIONS.put("ModuleList", "Arraylist of enabled modules.");
		DESCRIPTIONS.put("HitLog", "Log of recent hits.");
		DESCRIPTIONS.put("Notifications", "Toast popups on every toggle.");
		DESCRIPTIONS.put("TargetHud", "Health and distance of your last target.");
		DESCRIPTIONS.put("Keystrokes", "On-screen WASD keys.");
		DESCRIPTIONS.put("Info", "Coordinates and speed readout.");
		DESCRIPTIONS.put("Spotify", "Now playing widget.");
		DESCRIPTIONS.put("AutoLog", "Disconnects at low health.");
		DESCRIPTIONS.put("AutoTool", "Picks the right tool for the block.");
		DESCRIPTIONS.put("FastPlace", "Removes the place delay.");
		DESCRIPTIONS.put("Panic", "One key to disable everything.");
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
		modules.add(new Nametags());
		modules.add(new Trajectories());
		// hypixel
		modules.add(new StaffDetector());
		modules.add(new AutoClicker());
		modules.add(new AutoFarm());
		modules.add(new AutoFish());
		modules.add(new CommandMacro());
		modules.add(new AutoGG());
		// hud
		on(new InterfaceModule());
		on(new Watermark());
		on(new ModuleList());
		on(new HitLog());
		on(new Notifications());
		on(new TargetHud());
		modules.add(new Keystrokes());
		modules
			}
}
