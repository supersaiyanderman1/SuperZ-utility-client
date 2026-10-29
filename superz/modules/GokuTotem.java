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

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.superz.Superz;
import meteordevelopment.superz.pack.GokuTotemPack;

/**
 * Enables the bundled "SUPERZ Goku Totem" resource pack while active and restores the vanilla Totem
 * of Undying texture when disabled. The pack overrides the real item texture, so it shows up in
 * inventories, in hand, on the ground and inside item frames.
 */
public class GokuTotem extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> chatFeedbackOnToggle = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-message")
        .description("Prints a chat message when the pack is enabled or disabled.")
        .defaultValue(true)
        .build()
    );

    public GokuTotem() {
        super(Superz.CATEGORY, "goku-totem", "Swaps the Totem of Undying texture for the bundled SUPERZ Goku resource pack.");
    }

    @Override
    public void onActivate() {
        if (GokuTotemPack.enable()) {
            if (chatFeedbackOnToggle.get()) info("Goku Totem resource pack enabled. Reloading resources...");
        }
        else {
            error("Could not enable the Goku Totem resource pack, see the log for details.");
        }
    }

    @Override
    public void onDeactivate() {
        GokuTotemPack.disable();

        if (chatFeedbackOnToggle.get()) info("Goku Totem resource pack disabled, vanilla totem restored.");
    }

    @Override
    public String getInfoString() {
        return GokuTotemPack.isEnabled() ? "Goku" : null;
    }
}
