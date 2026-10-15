package com.lovenote.module.impl;

import com.lovenote.module.Module;
import net.minecraft.util.math.Vec3d;

public class WorldShaders extends Module {

    public WorldShaders() {
        super("WorldShaders", Category.RENDER);
    }

    public Vec3d sky(Vec3d original) {
        return original == null ? Vec3d.ZERO : original;
    }

    public Vec3d clouds(Vec3d original) {
        return original == null ? Vec3d.ZERO : original;
    }
}
