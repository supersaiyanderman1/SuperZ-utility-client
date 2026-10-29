/*
 * This file is part of SUPERZ, a modified derivative of Meteor Client
 * (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development and SUPERZ contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. See the LICENSE file for details.
 */

package meteordevelopment.superz.theme;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.nbt.NbtCompound;

/**
 * The SUPERZ ClickGUI theme. It reuses the complete Meteor widget set (one widget implementation for
 * every palette) and only swaps colours, spacing and panel shape, which keeps the GUI consistent,
 * cheap to render and compatible with every existing setting widget.
 */
public class SuperzGuiTheme extends MeteorGuiTheme {
    private final SettingGroup sgSuperz = settings.createGroup("SUPERZ");

    public final Setting<SuperzPalette> palette = sgSuperz.add(new EnumSetting.Builder<SuperzPalette>()
        .name("transformation")
        .description("Active SUPERZ transformation palette.")
        .defaultValue(SuperzPalette.SuperSaiyan)
        .onChanged(p -> applyPalette())
        .build()
    );

    public final Setting<Double> intensity = sgSuperz.add(new DoubleSetting.Builder()
        .name("intensity")
        .description("How strongly the transformation colour bleeds into backgrounds and panels.")
        .defaultValue(0.55)
        .min(0)
        .max(1)
        .sliderRange(0, 1)
        .onChanged(v -> applyPalette())
        .build()
    );

    public final Setting<Double> glow = sgSuperz.add(new DoubleSetting.Builder()
        .name("glow")
        .description("Accent glow strength around panels and highlights. Set to 0 for a flat look.")
        .defaultValue(0.35)
        .min(0)
        .max(1)
        .sliderRange(0, 1)
        .onChanged(v -> applyPalette())
        .build()
    );

    public final Setting<Double> opacity = sgSuperz.add(new DoubleSetting.Builder()
        .name("opacity")
        .description("Opacity of GUI backgrounds.")
        .defaultValue(0.92)
        .min(0.35)
        .max(1)
        .sliderRange(0.35, 1)
        .onChanged(v -> applyPalette())
        .build()
    );

    public final Setting<Integer> cornerRadius = sgSuperz.add(new IntSetting.Builder()
        .name("corner-radius")
        .description("Rounding of panels. 0 gives the classic square panels.")
        .defaultValue(3)
        .min(0)
        .max(8)
        .sliderRange(0, 8)
        .build()
    );

    public final Setting<Boolean> animations = sgSuperz.add(new BoolSetting.Builder()
        .name("animations")
        .description("Animated toggles, window transitions and hover effects.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> lowPerformance = sgSuperz.add(new BoolSetting.Builder()
        .name("low-performance-mode")
        .description("Disables glow and reduces HUD decoration. Recommended on integrated graphics.")
        .defaultValue(false)
        .onChanged(v -> applyPalette())
        .build()
    );

    public SuperzGuiTheme() {
        super("SUPERZ");

        applyPalette();
    }

    @Override
    public GuiTheme fromTag(NbtCompound tag) {
        super.fromTag(tag);

        // Palette is authoritative: re-apply it so a saved colour set can never desync from the
        // selected transformation.
        applyPalette();

        return this;
    }

    /**
     * Pushes the selected palette into every colour setting the Meteor widgets already read from.
     */
    public void applyPalette() {
        SuperzPalette p = palette.get();
        double i = intensity.get();
        double o = opacity.get();

        int backgroundAlpha = (int) Math.round(235 * o);
        int hoveredAlpha = (int) Math.round(245 * o);

        accentColor.set(SuperzStyle.asSettingColor(p.accent));
        checkboxColor.set(SuperzStyle.asSettingColor(p.accent));
        favoriteColor.set(SuperzStyle.asSettingColor(p.accentBright));

        moduleBackground.set(SuperzStyle.asSettingColor(p.mixWithBackground(0.55 - 0.30 * i)));

        backgroundColor.set(
            SuperzStyle.withAlpha(p.background, backgroundAlpha),
            SuperzStyle.withAlpha(p.backgroundRaised, hoveredAlpha),
            SuperzStyle.withAlpha(p.mixWithBackground(0.35), hoveredAlpha)
        );

        outlineColor.set(
            SuperzStyle.withAlpha(p.outline, 255),
            SuperzStyle.withAlpha(p.accentDark, 255),
            SuperzStyle.withAlpha(p.accent, 255)
        );

        scrollbarColor.set(
            SuperzStyle.withAlpha(p.backgroundRaised, backgroundAlpha),
            SuperzStyle.withAlpha(p.mixWithBackground(0.65), hoveredAlpha),
            SuperzStyle.withAlpha(p.accentDark, hoveredAlpha)
        );

        sliderHandle.set(
            SuperzStyle.withAlpha(p.accentDark, 255),
            SuperzStyle.withAlpha(p.accent, 255),
            SuperzStyle.withAlpha(p.accentBright, 255)
        );

        sliderLeft.set(SuperzStyle.asSettingColor(p.accentDark));
        sliderRight.set(SuperzStyle.withAlpha(p.backgroundRaised, 255));

        textColor.set(new SettingColor(p.text.r, p.text.g, p.text.b, 255));
        titleTextColor.set(new SettingColor(p.text.r, p.text.g, p.text.b, 255));
        textHighlightColor.set(SuperzStyle.withAlpha(p.accent, 90));
        textSecondaryColor.set(new SettingColor(165, 170, 178, 255));
    }

    @Override
    public double cornerRadius() {
        return cornerRadius.get();
    }

    @Override
    public double panelGlow() {
        if (lowPerformance.get()) return 0;
        return glow.get() * 110;
    }
}
