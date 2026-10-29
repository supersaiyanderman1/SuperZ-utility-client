/*
 * This file is part of SUPERZ, a modified derivative of Meteor Client
 * (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development and SUPERZ contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. See the LICENSE file for details.
 */

package meteordevelopment.superz;

import meteordevelopment.meteorclient.systems.modules.Category;
import net.minecraft.item.Items;

/**
 * Identity constants and the SUPERZ module category.
 */
public final class Superz {
    /** Brand shown in the GUI, HUD watermark and chat prefix. */
    public static final String NAME = "SUPERZ";
    public static final String TAGLINE = "Hack Client / Utility Mod";
    public static final String VERSION = "1.0.0";

    /** Cosmetic baseline power level (a joke reference, not a measurement of anything). */
    public static final long BASE_POWER_LEVEL = 9000L;

    /**
     * Name of the standalone resource pack shipped with the client. The resource pack files live in
     * {@code src/main/resources/assets/superz/resourcepack/} and are also delivered as a separate ZIP.
     */
    public static final String TOTEM_PACK_FILE = "SUPERZ Goku Totem.zip";

    public static final Category CATEGORY = new Category("SUPERZ", Items.DRAGON_BREATH.getDefaultStack());

    private Superz() {
    }
}
