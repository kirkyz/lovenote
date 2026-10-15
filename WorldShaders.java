package com.lovenote.module.impl;

import net.minecraft.util.math.Vec3d;

public class WorldShaders {

    public boolean enabled = false;

    public Vec3d sky(Vec3d original) {
        if (original == null) {
            return Vec3d.ZERO;
        }

        return original;
    }

    public Vec3d clouds(Vec3d original) {
        if (original == null) {
            return Vec3d.ZERO;
        }

        return original;
    }
}
