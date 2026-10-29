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

import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.render.MeteorToast;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.superz.Superz;
import meteordevelopment.superz.profile.SuperzProfile;
import meteordevelopment.superz.render.SuperzParticles;
import meteordevelopment.superz.theme.SuperzGuiTheme;
import meteordevelopment.superz.theme.SuperzPalette;
import meteordevelopment.superz.theme.SuperzStyle;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import org.lwjgl.glfw.GLFW;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * Keybind driven transformation switcher. Changes the active SUPERZ palette (GUI, HUD and effects),
 * plays a cosmetic burst and shows a SUPERZ styled toast.
 */
public class Transformation extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgKeys = settings.createGroup("Keys");

    private final Setting<Boolean> particles = sgGeneral.add(new BoolSetting.Builder()
        .name("particles")
        .description("Cosmetic burst when transforming.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> sound = sgGeneral.add(new BoolSetting.Builder()
        .name("sound")
        .description("Plays a vanilla sound when transforming (game sound events only).")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> notification = sgGeneral.add(new BoolSetting.Builder()
        .name("notification")
        .description("Shows a SUPERZ toast when transforming.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> switchGuiTheme = sgGeneral.add(new BoolSetting.Builder()
        .name("switch-gui-theme")
        .description("Selects the SUPERZ GUI theme automatically if another theme is active.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Keybind> cycleKey = sgKeys.add(new KeybindSetting.Builder()
        .name("cycle-key")
        .description("Cycles to the next transformation.")
        .defaultValue(Keybind.fromKey(GLFW.GLFW_KEY_J))
        .build()
    );

    private final Setting<Keybind> superSaiyanKey = sgKeys.add(new KeybindSetting.Builder()
        .name("super-saiyan-key")
        .description("Super Saiyan (gold).")
        .defaultValue(Keybind.fromKey(GLFW.GLFW_KEY_1))
        .build()
    );

    private final Setting<Keybind> superSaiyanBlueKey = sgKeys.add(new KeybindSetting.Builder()
        .name("super-saiyan-blue-key")
        .description("Super Saiyan Blue (cyan).")
        .defaultValue(Keybind.fromKey(GLFW.GLFW_KEY_2))
        .build()
    );

    private final Setting<Keybind> ultraInstinctKey = sgKeys.add(new KeybindSetting.Builder()
        .name("ultra-instinct-key")
        .description("Ultra Instinct (silver).")
        .defaultValue(Keybind.fromKey(GLFW.GLFW_KEY_3))
        .build()
    );

    private final Setting<Keybind> kaiokenKey = sgKeys.add(new KeybindSetting.Builder()
        .name("kaioken-key")
        .description("Kaioken (red).")
        .defaultValue(Keybind.fromKey(GLFW.GLFW_KEY_4))
        .build()
    );

    private final Setting<Keybind> scouterKey = sgKeys.add(new KeybindSetting.Builder()
        .name("scouter-key")
        .description("Scouter (green).")
        .defaultValue(Keybind.fromKey(GLFW.GLFW_KEY_5))
        .build()
    );

    private final Setting<Keybind> destroyerKey = sgKeys.add(new KeybindSetting.Builder()
        .name("destroyer-key")
        .description("Destroyer (violet).")
        .defaultValue(Keybind.fromKey(GLFW.GLFW_KEY_6))
        .build()
    );

    public Transformation() {
        super(Superz.CATEGORY, "transformation", "Switches between the six SUPERZ transformations with keybinds.");
    }

    @EventHandler
    private void onKey(KeyEvent event) {
        if (event.action != KeyAction.Press) return;
        if (mc.currentScreen != null) return;

        if (matches(cycleKey.get(), event)) {
            cycle();
            return;
        }

        if (matches(superSaiyanKey.get(), event)) transform(SuperzPalette.SuperSaiyan);
        else if (matches(superSaiyanBlueKey.get(), event)) transform(SuperzPalette.SuperSaiyanBlue);
        else if (matches(ultraInstinctKey.get(), event)) transform(SuperzPalette.UltraInstinct);
        else if (matches(kaiokenKey.get(), event)) transform(SuperzPalette.Kaioken);
        else if (matches(scouterKey.get(), event)) transform(SuperzPalette.Scouter);
        else if (matches(destroyerKey.get(), event)) transform(SuperzPalette.Destroyer);
    }

    private boolean matches(Keybind keybind, KeyEvent event) {
        return keybind.isSet() && keybind.matches(true, event.key(), event.modifiers());
    }

    private void cycle() {
        SuperzPalette[] palettes = SuperzPalette.values();
        SuperzPalette current = SuperzStyle.palette();

        transform(palettes[(current.ordinal() + 1) % palettes.length]);
    }

    /** Applies a transformation across GUI, HUD and effects. */
    public void transform(SuperzPalette palette) {
        SuperzGuiTheme theme = SuperzStyle.theme();

        if (theme == null && switchGuiTheme.get()) {
            GuiThemes.select("SUPERZ");
            theme = SuperzStyle.theme();
        }

        if (theme != null) {
            theme.palette.set(palette);
            theme.applyPalette();

            // Persist the selection immediately so a crash cannot lose the transformation.
            GuiThemes.save();
        }

        SuperzProfile profile = SuperzProfile.get();
        if (profile != null) profile.addTransformation(palette.title);

        if (particles.get() && mc.player != null && mc.world != null) {
            SuperzParticles.burst(SuperzParticles.forPalette(palette), mc.player.getX(), mc.player.getY(), mc.player.getZ(), 18, 0.35);
        }

        if (sound.get()) {
            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_BEACON_ACTIVATE.value(), 1.2f, 1));
        }

        if (notification.get()) {
            MeteorToast toast = new MeteorToast.Builder("TRANSFORMATION")
                .text(palette.title)
                .icon(Items.NETHER_STAR)
                .sound(null)
                .build();

            mc.getToastManager().add(toast);
        }
    }
}
