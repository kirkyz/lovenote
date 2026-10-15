package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import net.minecraft.util.math.Vec3d;

public class SkyShader extends Module {

    public SkyShader() {
        super("SkyShader", Category.RENDER);
    }

    public boolean render(
            MatrixStack matrices,
            Matrix4f projectionMatrix,
            float tickDelta,
            Camera camera,
            Runnable fogCallback) {
        return false;
    }

    public Vec3d sky(Vec3d original) {
        return original == null ? Vec3d.ZERO : original;
    }

    public Vec3d clouds(Vec3d original) {
        return original == null ? Vec3d.ZERO : original;
    }
}
