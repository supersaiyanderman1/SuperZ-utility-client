/*
 * This file is part of SUPERZ, a modified derivative of Meteor Client
 * (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development and SUPERZ contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. See the LICENSE file for details.
 */

package meteordevelopment.superz.pack;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.superz.Superz;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.ResourcePackManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Installs and toggles the standalone "SUPERZ Goku Totem" resource pack.
 *
 * <p>The pack is shipped inside the mod jar as {@code assets/superz/resourcepack/goku_totem.zip}.
 * Enabling copies it into the game's {@code resourcepacks} folder, registers it with the vanilla
 * resource pack manager and reloads resources; disabling removes it from the enabled list and reloads
 * again, which restores the vanilla Totem of Undying texture everywhere (inventory, held, dropped and
 * item frames alike) because the override only exists while the pack is active.</p>
 */
public final class GokuTotemPack {
    private static final String RESOURCE_PATH = "/assets/superz/resourcepack/goku_totem.zip";

    private GokuTotemPack() {
    }

    public static String id() {
        return "file/" + Superz.TOTEM_PACK_FILE;
    }

    private static Path installedPath() {
        return MinecraftClient.getInstance().getResourcePackDir().resolve(Superz.TOTEM_PACK_FILE);
    }

    /** Copies the bundled pack into the game's resourcepacks folder (idempotent). */
    public static boolean install() {
        Path target = installedPath();

        try (InputStream in = GokuTotemPack.class.getResourceAsStream(RESOURCE_PATH)) {
            if (in == null) {
                MeteorClient.LOG.error("SUPERZ Goku Totem pack is missing from the mod jar ({})", RESOURCE_PATH);
                return false;
            }

            byte[] bytes = in.readAllBytes();

            if (Files.exists(target) && Files.size(target) == bytes.length) return true;

            Files.createDirectories(target.getParent());

            Path temp = Files.createTempFile(target.getParent(), "superz-totem", ".zip");
            Files.write(temp, bytes);
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);

            MeteorClient.LOG.info("Installed SUPERZ Goku Totem resource pack at {}", target);
            return true;
        }
        catch (IOException e) {
            MeteorClient.LOG.error("Failed to install the SUPERZ Goku Totem resource pack", e);
            return false;
        }
    }

    /** Installs the pack, enables it and reloads resources. */
    public static boolean enable() {
        if (!install()) return false;

        MinecraftClient client = MinecraftClient.getInstance();
        ResourcePackManager manager = client.getResourcePackManager();

        if (manager == null) return false;

        manager.scanPacks();

        if (!manager.enable(id())) {
            MeteorClient.LOG.error("Could not enable the SUPERZ Goku Totem resource pack ({})", id());
            return false;
        }

        if (!client.options.resourcePacks.contains(id())) client.options.resourcePacks.add(id());

        client.options.write();
        client.reloadResources();

        return true;
    }

    /** Removes the pack from the enabled list and reloads resources, restoring vanilla textures. */
    public static boolean disable() {
        MinecraftClient client = MinecraftClient.getInstance();
        ResourcePackManager manager = client.getResourcePackManager();

        if (manager != null) {
            manager.scanPacks();
            manager.disable(id());
        }

        client.options.resourcePacks.remove(id());
        client.options.write();
        client.reloadResources();

        return true;
    }

    /** Whether the pack is currently listed as enabled in the vanilla options. */
    public static boolean isEnabled() {
        return MinecraftClient.getInstance().options.resourcePacks.contains(id());
    }
}
