
package com.lovenote.util;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ParticleUtil {
    private static final Map<String, ParticleEffect> MAP = new LinkedHashMap<>();

    static {
        // sparkles / magic
        MAP.put("End Rod", ParticleTypes.END_ROD);
        MAP.put("Glow", ParticleTypes.GLOW);
        MAP.put("Enchant", ParticleTypes.ENCHANT);
        MAP.put("Enchanted Hit", ParticleTypes.ENCHANTED_HIT);
        MAP.put("Crit", ParticleTypes.CRIT);
        MAP.put("Electric Spark", ParticleTypes.ELECTRIC_SPARK);
        MAP.put("Wax On", ParticleTypes.WAX_ON);
        MAP.put("Wax Off", ParticleTypes.WAX_OFF);
        MAP.put("Scrape", ParticleTypes.SCRAPE);
        MAP.put("Flash", ParticleTypes.FLASH);
        MAP.put("Firework", ParticleTypes.FIREWORK);
        MAP.put("Totem", ParticleTypes.TOTEM_OF_UNDYING);
        MAP.put("Sonic Boom", ParticleTypes.SONIC_BOOM);
        MAP.put("Sculk Soul", ParticleTypes.SCULK_SOUL);
        MAP.put("Sculk Pop", ParticleTypes.SCULK_CHARGE_POP);
        MAP.put("Soul", ParticleTypes.SOUL);

        // fire / heat
        MAP.put("Flame", ParticleTypes.FLAME);
        MAP.put("Soul Flame", ParticleTypes.SOUL_FIRE_FLAME);
        MAP.put("Lava", ParticleTypes.LAVA);
        MAP.put("Dripping Lava", ParticleTypes.DRIPPING_LAVA);
        MAP.put("Falling Lava", ParticleTypes.FALLING_LAVA);
        MAP.put("Landing Lava", ParticleTypes.LANDING_LAVA);
        MAP.put("Ember", ParticleTypes.ASH);
        MAP.put("White Ash", ParticleTypes.WHITE_ASH);
        MAP.put("Campfire", ParticleTypes.CAMPFIRE_COSY_SMOKE);
        MAP.put("Signal Smoke", ParticleTypes.CAMPFIRE_SIGNAL_SMOKE);
        MAP.put("Smoke", ParticleTypes.SMOKE);
        MAP.put("Large Smoke", ParticleTypes.LARGE_SMOKE);
        MAP.put("Cloud", ParticleTypes.CLOUD);
        MAP.put("Dust Plume", ParticleTypes.WHITE_ASH);

        // nature
        MAP.put("Snow", ParticleTypes.SNOWFLAKE);
        MAP.put("Cherry", ParticleTypes.CHERRY_LEAVES);
        MAP.put("Spore Blossom", ParticleTypes.SPORE_BLOSSOM_AIR);
        MAP.put("Crimson Spore", ParticleTypes.CRIMSON_SPORE);
        MAP.put("Warped Spore", ParticleTypes.WARPED_SPORE);
        MAP.put("Mycelium", ParticleTypes.MYCELIUM);
        MAP.put("Composter", ParticleTypes.COMPOSTER);
        MAP.put("Happy Villager", ParticleTypes.HAPPY_VILLAGER);
        MAP.put("Angry Villager", ParticleTypes.ANGRY_VILLAGER);
        MAP.put("Heart", ParticleTypes.HEART);
        MAP.put("Note", ParticleTypes.NOTE);
        MAP.put("Poof", ParticleTypes.POOF);
        MAP.put("Egg Crack", ParticleTypes.EGG_CRACK);
        MAP.put("Sneeze", ParticleTypes.SNEEZE);
        MAP.put("Spit", ParticleTypes.SPIT);

        // water / weather
        MAP.put("Rain", ParticleTypes.RAIN);
        MAP.put("Splash", ParticleTypes.SPLASH);
        MAP.put("Bubble", ParticleTypes.BUBBLE);
        MAP.put("Bubble Pop", ParticleTypes.BUBBLE_POP);
        MAP.put("Column Up", ParticleTypes.BUBBLE_COLUMN_UP);
        MAP.put("Current Down", ParticleTypes.CURRENT_DOWN);
        MAP.put("Underwater", ParticleTypes.UNDERWATER);
        MAP.put("Dripping Water", ParticleTypes.DRIPPING_WATER);
        MAP.put("Falling Water", ParticleTypes.FALLING_WATER);
        MAP.put("Dolphin", ParticleTypes.DOLPHIN);
        MAP.put("Nautilus", ParticleTypes.NAUTILUS);

        // otherworldly
        MAP.put("Portal", ParticleTypes.PORTAL);
        MAP.put("Dragon", ParticleTypes.DRAGON_BREATH);
        MAP.put("Glow Ink", ParticleTypes.GLOW_SQUID_INK);
        MAP.put("Squid Ink", ParticleTypes.SQUID_INK);
        MAP.put("Elder Guardian", ParticleTypes.ELDER_GUARDIAN);
        MAP.put("Witch", ParticleTypes.WITCH);
        MAP.put("Effect", ParticleTypes.EFFECT);
        MAP.put("Entity Effect", ParticleTypes.ENTITY_EFFECT);
        MAP.put("Ambient Effect", ParticleTypes.AMBIENT_ENTITY_EFFECT);
        MAP.put("Instant Effect", ParticleTypes.INSTANT_EFFECT);
        MAP.put("Damage", ParticleTypes.DAMAGE_INDICATOR);
        MAP.put("Sweep", ParticleTypes.SWEEP_ATTACK);
        MAP.put("Explosion", ParticleTypes.EXPLOSION);
        MAP.put("Boom", ParticleTypes.EXPLOSION_EMITTER);
        MAP.put("Fishing", ParticleTypes.FISHING);
    }

    private ParticleUtil() {}

    public static String[] names() {
        return MAP.keySet().toArray(new String[0]);
    }

    public static ParticleEffect get(String name) {
        return MAP.getOrDefault(name, ParticleTypes.END_ROD);
    }
}
