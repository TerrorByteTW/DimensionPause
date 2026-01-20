package org.reprogle.dimensionpause.store;

import org.bukkit.World;

import java.time.Instant;

public record TrackedWorld(String world, World.Environment environment, boolean enabled, Instant expiresAt) {
}
