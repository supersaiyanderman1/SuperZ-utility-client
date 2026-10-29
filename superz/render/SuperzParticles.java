/*
 * This file is part of SUPERZ, a modified derivative of Meteor Client
 * (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development and SUPERZ contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. See the LICENSE file for details.
 */

package meteordevelopment.superz.render;

import meteordevelopment.superz.theme.SuperzPalette;
import meteordevelopment.superz.theme.SuperzStyle;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * Cosmetic particle helpers. Only vanilla particle types are used (no external assets, no custom
 * shaders), and every call is a no-op unless a world is loaded, which keeps the cost negligible on
 * integrated graphics.
 */
public final class SuperzParticles {
    private SuperzParticles() {
    }

    public static ParticleEffect forPalette(SuperzPalette palette) {
        return switch (palette) {
            case SuperSaiyan -> ParticleTypes.FLAME;
            case SuperSaiyanBlue -> ParticleTypes.SOUL_FIRE_FLAME;
            case UltraInstinct -> ParticleTypes.END_ROD;
            case Kaioken -> ParticleTypes.SMALL_FLAME;
            case Scouter -> ParticleTypes.ELECTRIC_SPARK;
            case Destroyer -> ParticleTypes.WITCH;
        };
    }

    public static ParticleEffect active() {
        return forPalette(SuperzStyle.palette());
    }

    public static void spawn(ParticleEffect effect, double x, double y, double z, double vx, double vy, double vz) {
        if (mc.world == null) return;

        mc.world.addParticleClient(effect, false, false, x, y, z, vx, vy, vz);
    }

    /** Spawns {@code count} particles in a ring around the given point. */
    public static void ring(ParticleEffect effect, double x, double y, double z, double radius, int count, double speed) {
        if (mc.world == null || count <= 0) return;

        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2 / count) * i;
            double px = x + Math.cos(angle) * radius;
            double pz = z + Math.sin(angle) * radius;

            spawn(effect, px, y, pz, Math.cos(angle) * speed, 0.02, Math.sin(angle) * speed);
        }
    }

    /** Burst used for Ki release and transformation transitions. */
    public static void burst(ParticleEffect effect, double x, double y, double z, int count, double speed) {
        if (mc.world == null || count <= 0) return;

        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2;
            double vx = Math.cos(angle) * speed * (0.5 + Math.random() * 0.5);
            double vz = Math.sin(angle) * speed * (0.5 + Math.random() * 0.5);
            double vy = 0.15 + Math.random() * speed;

            spawn(effect, x, y + Math.random() * 1.4, z, vx, vy, vz);
        }
    }
}
