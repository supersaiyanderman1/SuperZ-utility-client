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
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.superz.Superz;
import meteordevelopment.superz.profile.SuperzProfile;
import meteordevelopment.superz.render.SuperzParticles;
import meteordevelopment.superz.theme.SuperzStyle;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import org.lwjgl.glfw.GLFW;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * Cosmetic Ki Charge. Holding the configured key builds up Ki, releasing it fires a visual burst and
 * trains the (cosmetic) power level. No packets, combat or movement behaviour is affected.
 */
public class KiCharge extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Keybind> chargeKey = sgGeneral.add(new KeybindSetting.Builder()
        .name("charge-key")
        .description("Hold to charge Ki.")
        .defaultValue(Keybind.fromKey(GLFW.GLFW_KEY_K))
        .build()
    );

    private final Setting<Double> chargeSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("charge-speed")
        .description("How fast Ki builds up while the key is held.")
        .defaultValue(0.035)
        .min(0.005)
        .sliderRange(0.005, 0.15)
        .build()
    );

    private final Setting<Double> minimumRelease = sgGeneral.add(new DoubleSetting.Builder()
        .name("minimum-release")
        .description("Minimum charge needed for a release burst.")
        .defaultValue(0.2)
        .min(0)
        .max(1)
        .sliderRange(0, 1)
        .build()
    );

    private final Setting<Integer> powerPerCharge = sgGeneral.add(new IntSetting.Builder()
        .name("power-per-charge")
        .description("Cosmetic power level gained by a full charge.")
        .defaultValue(250)
        .min(0)
        .sliderRange(0, 2000)
        .build()
    );

    private final Setting<Boolean> particles = sgGeneral.add(new BoolSetting.Builder()
        .name("particles")
        .description("Spawns aura particles while charging.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> sound = sgGeneral.add(new BoolSetting.Builder()
        .name("sound")
        .description("Plays a vanilla sound on release. Uses the game's own sound events only.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> chatFeedbackOnRelease = sgGeneral.add(new BoolSetting.Builder()
        .name("release-message")
        .description("Prints the release in chat.")
        .defaultValue(false)
        .build()
    );

    private static double charge;
    private static boolean charging;

    private int tick;

    public KiCharge() {
        super(Superz.CATEGORY, "ki-charge", "Cosmetic Ki build-up and release burst bound to a configurable key.");
    }

    public static double getCharge() {
        KiCharge module = Modules.get() == null ? null : Modules.get().get(KiCharge.class);
        return module != null && module.isActive() ? charge : 0;
    }

    public static boolean isCharging() {
        KiCharge module = Modules.get() == null ? null : Modules.get().get(KiCharge.class);
        return module != null && module.isActive() && charging;
    }

    @Override
    public void onActivate() {
        charge = 0;
        charging = false;
        tick = 0;
    }

    @Override
    public void onDeactivate() {
        charge = 0;
        charging = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;

        tick++;

        boolean held = chargeKey.get().isSet() && chargeKey.get().isPressed();

        if (held) {
            if (!charging) {
                charging = true;
                tick = 0;
            }

            charge = Math.min(1, charge + chargeSpeed.get());

            if (particles.get() && !SuperzStyle.lowPerformance() && tick % 2 == 0) {
                SuperzParticles.ring(SuperzParticles.active(), mc.player.getX(), mc.player.getY() + 0.15, mc.player.getZ(), 0.55 + charge * 0.4, 3, 0.02);
            }
        }
        else if (charging) {
            release();
        }
    }

    private void release() {
        charging = false;

        double released = charge;
        charge = 0;

        if (released < minimumRelease.get() || mc.player == null) return;

        if (particles.get()) {
            SuperzParticles.burst(SuperzParticles.active(), mc.player.getX(), mc.player.getY(), mc.player.getZ(), (int) (8 + released * 20), 0.25 + released * 0.45);
        }

        if (sound.get()) {
            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_BEACON_ACTIVATE.value(), 1.4f + (float) released * 0.4f, 1));
        }

        if (powerPerCharge.get() > 0) {
            SuperzProfile profile = SuperzProfile.get();
            if (profile != null) {
                profile.addPower(Math.round(powerPerCharge.get() * released));
                profile.addCharge();
            }
        }

        if (chatFeedbackOnRelease.get()) {
            info("Ki released at (highlight)%d%%(default).", Math.round(released * 100));
        }
    }
}
