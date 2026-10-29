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
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.superz.modules.KiCharge;
import meteordevelopment.superz.theme.SuperzStyle;

/**
 * Ki charge meter. Reads the cosmetic charge level from the Ki Charge module and animates a bar that
 * pulses while charging.
 */
public class KiHud extends HudElement {
    public static final HudElementInfo<KiHud> INFO = new HudElementInfo<>(
        PowerLevelHud.GROUP,
        "ki-meter",
        "Shows the current Ki charge.",
        KiHud::new
    );

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgScale = settings.createGroup("Scale");
    private final SettingGroup sgBackground = settings.createGroup("Background");

    private final Setting<Boolean> showLabel = sgGeneral.add(new BoolSetting.Builder()
        .name("label")
        .description("Shows the KI label and percentage.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> barWidth = sgGeneral.add(new IntSetting.Builder()
        .name("bar-width")
        .description("Width of the Ki bar.")
        .defaultValue(84)
        .min(24)
        .sliderRange(24, 200)
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
        .defaultValue(new SettingColor(20, 20, 20, 140))
        .visible(background::get)
        .build()
    );

    private double shown;
    private double pulse;

    public KiHud() {
        super(INFO);
    }

    @Override
    public void tick(HudRenderer renderer) {
        double s = getScale();
        double charge = KiCharge.getCharge();

        if (SuperzStyle.lowPerformance() || !SuperzStyle.animations()) shown = charge;
        else {
            double diff = charge - shown;
            shown += Math.abs(diff) < 0.002 ? diff : diff * 0.25;
        }

        if (KiCharge.isCharging() && SuperzStyle.animations()) {
            pulse += 0.18;
            if (pulse > Math.PI * 2) pulse -= Math.PI * 2;
        }
        else pulse = 0;

        double width = Math.max(barWidth.get() * s, showLabel.get() ? renderer.textWidth("KI 100%", true, s) : 0);
        double height = renderer.textHeight(true, s) + 6 * s + (showLabel.get() ? renderer.textHeight(true, s) : 0);

        setSize(width + 8 * s, height + 6 * s);
    }

    @Override
    public void render(HudRenderer renderer) {
        double s = getScale();
        double pad = 4 * s;
        double x = this.x + pad;
        double y = this.y + pad;

        if (background.get()) {
            renderer.quad(this.x, this.y, getWidth(), getHeight(), backgroundColor.get());
            renderer.quad(this.x, this.y, 1.5, getHeight(), SuperzStyle.accent(200));
        }

        if (showLabel.get()) {
            String label = KiCharge.isCharging()
                ? "CHARGING " + Math.round(shown * 100) + "%"
                : "KI " + Math.round(shown * 100) + "%";
            renderer.text(label, x, y, SuperzStyle.accent(230), true, s);
            y += renderer.textHeight(true, s);
        }

        double w = Math.max(getWidth() - pad * 2, barWidth.get() * s);
        double barY = y + 1 * s;

        renderer.quad(x, barY, w, 4 * s, SuperzStyle.panelBackground(210));

        Color fill = KiCharge.isCharging()
            ? SuperzStyle.accent(200 + (int) (Math.sin(pulse) * 55))
            : SuperzStyle.accent(220);

        renderer.quad(x, barY, w * Math.max(0, Math.min(1, shown)), 4 * s, fill);

        if (KiCharge.isCharging() && !SuperzStyle.lowPerformance()) {
            double glow = 1 + Math.sin(pulse) * 0.5;
            renderer.quad(x, barY - glow, w * Math.max(0, Math.min(1, shown)), 1, SuperzStyle.accent(90));
        }
    }

    private double getScale() {
        return customScale.get() ? scale.get() : Hud.get().getTextScale();
    }
}
