/*
 * This file is part of SUPERZ, a modified derivative of Meteor Client
 * (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development and SUPERZ contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. See the LICENSE file for details.
 */

package meteordevelopment.superz.hud;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.superz.theme.SuperzStyle;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.Locale;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * Scouter-style target readout. The estimated power level is a cosmetic formula over health, armour
 * and distance; it deliberately does not read hidden server data or player skill.
 */
public class ScouterHud extends HudElement {
    public static final HudElementInfo<ScouterHud> INFO = new HudElementInfo<>(
        PowerLevelHud.GROUP,
        "scouter",
        "Scouter readout of the nearest player.",
        ScouterHud::new
    );

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgScale = settings.createGroup("Scale");
    private final SettingGroup sgBackground = settings.createGroup("Background");

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Maximum distance of a scanned target.")
        .defaultValue(64)
        .min(4)
        .sliderRange(8, 128)
        .build()
    );

    private final Setting<Boolean> showHealth = sgGeneral.add(new BoolSetting.Builder()
        .name("health")
        .description("Shows the target health.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showDistance = sgGeneral.add(new BoolSetting.Builder()
        .name("distance")
        .description("Shows the target distance.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showPower = sgGeneral.add(new BoolSetting.Builder()
        .name("estimated-power")
        .description("Shows a cosmetic estimated power level.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> scanline = sgGeneral.add(new BoolSetting.Builder()
        .name("scanline")
        .description("Animated scanline across the panel.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> customScale = sgScale.add(new BoolSetting.Builder()
        .name("custom-scale")
        .description("Applies a custom scale to this element.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> scale = sgScale.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Custom scale.")
        .defaultValue(1)
        .min(0.5)
        .sliderRange(0.5, 3)
        .visible(customScale::get)
        .build()
    );

    private final Setting<Boolean> background = sgBackground.add(new BoolSetting.Builder()
        .name("background")
        .description("Draws a panel behind the element.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> backgroundColor = sgBackground.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Panel colour.")
        .defaultValue(new SettingColor(8, 19, 11, 175))
        .visible(background::get)
        .build()
    );

    private PlayerEntity target;
    private double scanPosition;

    public ScouterHud() {
        super(INFO);
    }

    @Override
    public void tick(HudRenderer renderer) {
        double s = getScale();

        if (isInEditor()) target = mc.player;
        else target = TargetUtils.getPlayerTarget(range.get(), SortPriority.LowestDistance);

        double width = 92 * s;
        double lines = 1 + (showHealth.get() ? 1 : 0) + (showDistance.get() ? 1 : 0) + (showPower.get() ? 1 : 0);

        setSize(width + 8 * s, renderer.textHeight(true, s) * (lines + 1) + 8 * s);

        if (scanline.get() && SuperzStyle.animations() && !SuperzStyle.lowPerformance()) {
            scanPosition += 0.02;
            if (scanPosition > 1) scanPosition = 0;
        }
    }

    @Override
    public void render(HudRenderer renderer) {
        double s = getScale();
        double pad = 4 * s;
        double x = this.x + pad;
        double y = this.y + pad;

        if (background.get()) {
            renderer.quad(this.x, this.y, getWidth(), getHeight(), backgroundColor.get());
            renderer.quad(this.x, this.y, 1.5, getHeight(), SuperzStyle.accent(220));

            if (scanline.get() && SuperzStyle.animations() && !SuperzStyle.lowPerformance()) {
                renderer.quad(this.x, this.y + getHeight() * scanPosition, getWidth(), 1, SuperzStyle.accent(60));
            }
        }

        renderer.text("SCOUTER", x, y, SuperzStyle.accent(230), true, s);
        y += renderer.textHeight(true, s);

        if (target == null) {
            renderer.text(isInEditor() ? "No target" : "Scanning...", x, y, SuperzStyle.accent(180), true, s);
            return;
        }

        renderer.text(target.getName().getString(), x, y, SuperzStyle.text(), true, s);
        y += renderer.textHeight(true, s);

        if (showHealth.get()) {
            String health = String.format(Locale.ROOT, "HP %,.1f", target.getHealth() + target.getAbsorptionAmount());
            renderer.text(health, x, y, SuperzStyle.accent(220), true, s);
            y += renderer.textHeight(true, s);
        }

        if (showDistance.get()) {
            double distance = isInEditor() ? 0 : mc.player.distanceTo(target);
            renderer.text(String.format(Locale.ROOT, "DIST %,dm", Math.round(distance)), x, y, SuperzStyle.accent(220), true, s);
            y += renderer.textHeight(true, s);
        }

        if (showPower.get()) {
            renderer.text("PL " + String.format(Locale.ROOT, "%,d", estimatePower(target)), x, y, SuperzStyle.accent(), true, s);
        }
    }

    /**
     * Cosmetic estimate. Equipped armour, health and absorption raise the number so the readout feels
     * alive during a fight.
     */
    private static long estimatePower(PlayerEntity player) {
        long power = 9000;

        power += Math.round(player.getHealth() * 420);
        power += Math.round(player.getAbsorptionAmount() * 250);

        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getEquippedStack(slot);
            if (!stack.isEmpty()) power += 1200 + stack.getMaxDamage() / 2L;
        }

        ItemStack mainHand = player.getMainHandStack();
        if (!mainHand.isEmpty()) power += 800;

        return Math.min(99_999_999L, Math.max(1L, power));
    }

    private double getScale() {
        return customScale.get() ? scale.get() : Hud.get().getTextScale();
    }
}
