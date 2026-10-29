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

import meteordevelopment.meteorclient.utils.render.color.Color;

/**
 * The six SUPERZ transformations. A palette is pure data: the theme applies it to every widget and
 * HUD element through {@link SuperzStyle}, so there is no separate GUI implementation per theme.
 *
 * <p>The colours are treated as immutable by every consumer. Do not mutate them.</p>
 */
public enum SuperzPalette {
    SuperSaiyan(
        "Super Saiyan",
        new Color(255, 190, 40),
        new Color(196, 118, 12),
        new Color(255, 232, 150),
        new Color(26, 21, 12),
        new Color(44, 34, 18),
        new Color(12, 9, 5),
        new Color(255, 246, 224)
    ),
    SuperSaiyanBlue(
        "Super Saiyan Blue",
        new Color(64, 196, 255),
        new Color(24, 108, 214),
        new Color(176, 236, 255),
        new Color(11, 19, 30),
        new Color(19, 34, 52),
        new Color(5, 9, 16),
        new Color(232, 248, 255)
    ),
    UltraInstinct(
        "Ultra Instinct",
        new Color(226, 234, 246),
        new Color(146, 158, 180),
        new Color(255, 255, 255),
        new Color(14, 15, 20),
        new Color(28, 30, 38),
        new Color(6, 6, 9),
        new Color(245, 248, 255)
    ),
    Kaioken(
        "Kaioken",
        new Color(255, 66, 58),
        new Color(168, 20, 20),
        new Color(255, 150, 120),
        new Color(23, 10, 10),
        new Color(40, 16, 16),
        new Color(10, 3, 3),
        new Color(255, 233, 228)
    ),
    Scouter(
        "Scouter",
        new Color(84, 255, 126),
        new Color(22, 158, 74),
        new Color(196, 255, 208),
        new Color(8, 19, 11),
        new Color(15, 32, 20),
        new Color(3, 8, 5),
        new Color(230, 255, 236)
    ),
    Destroyer(
        "Destroyer",
        new Color(188, 92, 255),
        new Color(118, 38, 202),
        new Color(230, 190, 255),
        new Color(19, 11, 27),
        new Color(32, 19, 45),
        new Color(8, 4, 12),
        new Color(246, 236, 255)
    );

    public final String title;

    public final Color accent;
    public final Color accentDark;
    public final Color accentBright;
    public final Color background;
    public final Color backgroundRaised;
    public final Color outline;
    public final Color text;

    SuperzPalette(String title, Color accent, Color accentDark, Color accentBright, Color background, Color backgroundRaised, Color outline, Color text) {
        this.title = title;
        this.accent = accent;
        this.accentDark = accentDark;
        this.accentBright = accentBright;
        this.background = background;
        this.backgroundRaised = backgroundRaised;
        this.outline = outline;
        this.text = text;
    }

    /** Accent colour blended towards the palette background, used for subdued elements. */
    public Color mixWithBackground(double amount) {
        amount = Math.max(0, Math.min(1, amount));

        return new Color(
            (int) Math.round(accent.r + (background.r - accent.r) * amount),
            (int) Math.round(accent.g + (background.g - accent.g) * amount),
            (int) Math.round(accent.b + (background.b - accent.b) * amount),
            accent.a
        );
    }

    /** Accent colour with a custom alpha, used by overlays and glow effects. */
    public Color accent(int alpha) {
        return new Color(accent.r, accent.g, accent.b, Math.max(0, Math.min(255, alpha)));
    }

    public Color background(int alpha) {
        return new Color(background.r, background.g, background.b, Math.max(0, Math.min(255, alpha)));
    }

    @Override
    public String toString() {
        return title;
    }
}
