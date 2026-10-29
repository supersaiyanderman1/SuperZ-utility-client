/*
 * This file is part of SUPERZ, a modified derivative of Meteor Client
 * (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development and SUPERZ contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. See the LICENSE file for details.
 */

package meteordevelopment.superz.profile;

import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.superz.Superz;
import net.minecraft.nbt.NbtCompound;

/**
 * Persistent, purely cosmetic SUPERZ profile data (power level, training and transformation counters).
 *
 * <p>Nothing in this system influences gameplay, combat or movement. It exists so the Power Level and
 * Scouter HUD elements survive restarts.</p>
 */
public class SuperzProfile extends System<SuperzProfile> {
    private volatile long powerLevel = Superz.BASE_POWER_LEVEL;
    private volatile int charges;
    private volatile int transformations;

    private String lastTransformation = "Super Saiyan";

    public SuperzProfile() {
        super("superz-profile");
    }

    public static SuperzProfile get() {
        return Systems.get(SuperzProfile.class);
    }

    public long getPowerLevel() {
        return powerLevel;
    }

    public void addPower(long amount) {
        if (amount <= 0) return;
        powerLevel = Math.min(Long.MAX_VALUE - 1, powerLevel + amount);
    }

    public void addCharge() {
        charges++;
    }

    public int getCharges() {
        return charges;
    }

    public void addTransformation(String title) {
        transformations++;
        lastTransformation = title;
    }

    public int getTransformations() {
        return transformations;
    }

    public String getLastTransformation() {
        return lastTransformation;
    }

    public void reset() {
        powerLevel = Superz.BASE_POWER_LEVEL;
        charges = 0;
        transformations = 0;
        lastTransformation = "Super Saiyan";
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putLong("powerLevel", powerLevel);
        tag.putInt("charges", charges);
        tag.putInt("transformations", transformations);
        tag.putString("lastTransformation", lastTransformation);

        return tag;
    }

    @Override
    public SuperzProfile fromTag(NbtCompound tag) {
        powerLevel = Math.max(0, tag.getLong("powerLevel", Superz.BASE_POWER_LEVEL));
        charges = tag.getInt("charges", 0);
        transformations = tag.getInt("transformations", 0);
        lastTransformation = tag.getString("lastTransformation", "Super Saiyan");

        return this;
    }
}
