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
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

/**
 * Central access point for the SUPERZ look. GUI widgets, HUD elements and effects all read their
 * colours from here so a single palette switch repaints everything at once, including when the user
 * falls back to the stock Meteor theme.
 */
public final class SuperzStyle {
    public static final SuperzPalette DEFAULT_PALETTE = SuperzPalette.SuperSaiyan;

    private SuperzStyle() {
    }

    public static SuperzGuiTheme theme() {
        GuiTheme theme = GuiThemes.get();
        return theme instanceof SuperzGuiTheme superzTheme ? superzTheme : null;
    }

    public static SuperzPalette palette() {
        SuperzGuiTheme theme = theme();
        return theme != null ? theme.palette.get() : DEFAULT_PALETTE;
    }

    /** Accent used by HUD elements and effects. Follows the Meteor accent when that theme is selected. */
    public static Color accent() {
        SuperzGuiTheme theme = theme();
        if (theme != null) return theme.palette.get().accent;

        GuiTheme guiTheme = GuiThemes.get();
        if (guiTheme instanceof MeteorGuiTheme meteorTheme) return meteorTheme.accentColor.get();

        return DEFAULT_PALETTE.accent;
    }

    public static Color accent(int alpha) {
        Color accent = accent();
        return new Color(accent.r, accent.g, accent.b, Math.max(0, Math.min(255, alpha)));
    }

    public static Color accentBright() {
        SuperzGuiTheme theme = theme();
        return theme != null ? theme.palette.get().accentBright : accent();
    }

    public static Color text() {
        SuperzGuiTheme theme = theme();
        return theme != null ? theme.palette.get().text : Color.WHITE;
    }

    public static Color panelBackground(int alpha) {
        SuperzGuiTheme theme = theme();
        if (theme != null) return theme.palette.get().background(alpha);

        return new Color(20, 20, 20, alpha);
    }

    public static Color panelRaised(int alpha) {
        SuperzGuiTheme theme = theme();
        if (theme != null) return theme.palette.get().backgroundRaised(alpha);

        return new Color(30, 30, 30, alpha);
    }

    public static double intensity() {
        SuperzGuiTheme theme = theme();
        return theme != null ? theme.intensity.get() : 0.6;
    }

    public static double glow() {
        SuperzGuiTheme theme = theme();
        return theme != null ? theme.glow.get() : 0.25;
    }

    public static boolean animations() {
        SuperzGuiTheme theme = theme();
        return theme == null || theme.animations.get();
    }

    public static boolean lowPerformance() {
        SuperzGuiTheme theme = theme();
        return theme != null && theme.lowPerformance.get();
    }

    public static SettingColor asSettingColor(Color color) {
        return new SettingColor(color.r, color.g, color.b, color.a);
    }

    public static SettingColor withAlpha(Color color, int alpha) {
        return new SettingColor(color.r, color.g, color.b, Math.max(0, Math.min(255, alpha)));
    }
}
