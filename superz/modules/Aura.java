/*
 * This file is part of SUPERZ, a modified derivative of Meteor Client
 * (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development and SUPERZ contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. See the LICENSE file for details.
 */

package meteordevelopment.superz.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.superz.Superz;
import meteordevelopment.superz.render.SuperzParticles;
import meteordevelopment.superz.theme.SuperzStyle;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * Cosmetic transformation aura. Spawns a handful of vanilla particles around the player; density is
 * reduced automatically in low performance mode, and the module never touches packets or entity state.
 */
public class Aura extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<AuraStyle> style = sgGeneral.add(new EnumSetting.Builder<AuraStyle>()
        .name("style")
        .description("Particle style. Transformation follows the active SUPERZ palette.")
        .defaultValue(AuraStyle.Transformation)
        .build()
    );

    private final Setting<Integer> density = sgGeneral.add(new IntSetting.Builder()
        .name("density")
        .description("Particles spawned per tick.")
        .defaultValue(2)
        .min(1)
        .sliderRange(1, 10)
        .build()
    );

    private final Setting<Double> radius = sgGeneral.add(new DoubleSetting.Builder()
        .name("radius")
        .description("Radius of the aura around the player.")
        .defaultValue(0.65)
        .min(0.1)
        .sliderRange(0.1, 2)
        .build()
    );

    private final Setting<Double> height = sgGeneral.add(new DoubleSetting.Builder()
        .name("height")
        .description("Height of the aura around the player.")
        .defaultValue(1.7)
        .min(0.2)
        .sliderRange(0.2, 2.6)
        .build()
    );

    private final Setting<Boolean> onlyWhileMoving = sgGeneral.add(new BoolSetting.Builder()
        .name("only-while-moving")
        .description("Only show the aura while moving.")
        .defaultValue(false)
        .build()
    );

    public Aura() {
        super(Superz.CATEGORY, "aura", "Cosmetic SUPERZ aura particles around you. Purely visual.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;
        if (onlyWhileMoving.get() && mc.player.getVelocity().horizontalLengthSquared() < 0.001) return;

        ParticleEffect effect = effect();

        int count = density.get();
        if (SuperzStyle.lowPerformance()) count = Math.max(1, count / 2);

        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2;
            double r = radius.get() * (0.35 + Math.random() * 0.65);

            double x = mc.player.getX() + Math.cos(angle) * r;
            double y = mc.player.getY() + Math.random() * height.get();
            double z = mc.player.getZ() + Math.sin(angle) * r;

            double vx = Math.cos(angle) * 0.02;
            double vz = Math.sin(angle) * 0.02;
            double vy = 0.01 + Math.random() * 0.02;

            SuperzParticles.spawn(effect, x, y, z, vx, vy, vz);
        }
    }

    private ParticleEffect effect() {
        return switch (style.get()) {
            case Transformation -> SuperzParticles.active();
            case Flame -> ParticleTypes.FLAME;
            case SoulFlame -> ParticleTypes.SOUL_FIRE_FLAME;
            case ElectricSpark -> ParticleTypes.ELECTRIC_SPARK;
            case EndRod -> ParticleTypes.END_ROD;
            case Crit -> ParticleTypes.CRIT;
        };
    }

    public enum AuraStyle {
        Transformation,
        Flame,
        SoulFlame,
        ElectricSpark,
        EndRod,
        Crit
    }
}
