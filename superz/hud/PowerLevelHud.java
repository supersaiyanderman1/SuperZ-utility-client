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
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.superz.profile.SuperzProfile;
import meteordevelopment.superz.theme.SuperzStyle;

import java.util.Locale;

/**
 * Cosmetic power level readout. The value lives in {@link SuperzProfile} and only grows by training
 * (Ki Charge) - it is a fun score, never a measurement of player or target ability.
 */
public class PowerLevelHud extends HudElement {
    public static final HudGroup GROUP = new HudGroup("SUPERZ");

    public static final HudElementInfo<PowerLevelHud> INFO = new HudElementInfo<>(
        GROUP,
        "power-level",
        "Displays your cosmetic SUPERZ power level.",
        PowerLevelHud::new
    );

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgScale = settings.createGroup("Scale");
    private final SettingGroup sgBackground = settings.createGroup("Background");

    private final Setting<String> label = sgGeneral.add(new StringSetting.Builder()
        .name("label")
        .description("Text shown above the value.")
        .defaultValue("POWER LEVEL")
        .build()
    );

    private final Setting<Boolean> showBar = sgGeneral.add(new BoolSetting.Builder()
        .name("bar")
        .description("Shows a charge bar under the value.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> barWidth = sgGeneral.add(new IntSetting.Builder()
        .name("bar-width")
        .description("Width of the charge bar.")
        .defaultValue(84)
        .min(20)
        .sliderRange(20, 200)
        .visible(showBar::get)
        .build()
    );

    private final Setting<Boolean> themeColor = sgGeneral.add(new BoolSetting.Builder()
        .name("theme-color")
        .description("Use the active transformation colour.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("Custom value colour.")
        .defaultValue(new SettingColor(255, 190, 40))
        .visible(() -> !themeColor.get())
        .build()
    );

    private final Setting<Boolean> animate = sgGeneral.add(new BoolSetting.Builder()
        .name("animate")
        .description("Smoothly counts up to the current value.")
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
        .defaultValue(new SettingColor(20, 20, 20, 150))
        .visible(background::get)
        .build()
    );

    private double displayed;

    public PowerLevelHud() {
        super(INFO);

        displayed = SuperzProfile.get() == null ? 0 : SuperzProfile.get().getPowerLevel();
    }

    @Override
    public void tick(HudRenderer renderer) {
        double s = getScale();
        double pad = 3 * s;

        double target = SuperzProfile.get() == null ? 0 : SuperzProfile.get().getPowerLevel();

        if (!animate.get() || SuperzStyle.lowPerformance()) displayed = target;
        else {
            double diff = target - displayed;
            if (Math.abs(diff) < 1) displayed = target;
            else displayed += diff * 0.12;
        }

        String labelText = label.get();
        String valueText = format(displayed);

        double textWidth = Math.max(labelText.isEmpty() ? 0 : renderer.textWidth(labelText, true, s), renderer.textWidth(valueText, true, s));
        double height = renderer.textHeight(true, s) * (labelText.isEmpty() ? 1 : 2) + (showBar.get() ? 4 * s : 0);

        setSize(Math.max(textWidth, showBar.get() ? barWidth.get() * s : 0) + pad * 2, height + pad * 2);
    }

    @Override
    public void render(HudRenderer renderer) {
        double s = getScale();
        double pad = 3 * s;
        double x = this.x + pad;
        double y = this.y + pad;

        Color accent = themeColor.get() ? SuperzStyle.accent() : color.get();

        if (background.get()) {
            renderer.quad(this.x, this.y, getWidth(), getHeight(), backgroundColor.get());
            renderer.quad(this.x, this.y, SuperzStyle.lowPerformance() ? 1 : 1.5, getHeight(), SuperzStyle.accent(200));
        }

        String labelText = label.get();

        if (!labelText.isEmpty()) {
            renderer.text(labelText, x, y, SuperzStyle.accent(200), true, s);
            y += renderer.textHeight(true, s);
        }

        renderer.text(format(displayed), x, y, accent, true, s);

        if (showBar.get()) {
            double barY = y + renderer.textHeight(true, s) + 1 * s;
            double w = Math.max(getWidth() - pad * 2, barWidth.get() * s);

            renderer.quad(x, barY, w, 2 * s, SuperzStyle.panelBackground(200));

            double progress = Math.min(1, (displayed % 100000.0) / 100000.0);
            if (progress < 0.02) progress = 0.02;

            renderer.quad(x, barY, w * progress, 2 * s, accent);
        }
    }

    private double getScale() {
        return customScale.get() ? scale.get() : Hud.get().getTextScale();
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%,d", (long) Math.round(value));
    }
}
