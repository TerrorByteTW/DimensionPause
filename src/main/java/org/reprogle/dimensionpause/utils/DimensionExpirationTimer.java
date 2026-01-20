package org.reprogle.dimensionpause.utils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.dimensionpause.store.TrackedWorldsRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class DimensionExpirationTimer {
    private final Map<String, Instant> expirations = new HashMap<>();
    private ScheduledTask nextTask;

    @Inject
    DimensionState state;

    @Inject
    JavaPlugin plugin;

    public void refresh() {
        expirations.clear();
        setExpirations();
        scheduleNext();
    }


    private void setExpirations() {
        Set<World> bases = Bukkit.getWorlds().stream()
                .map(WorldUtils::getOverworld)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Instant now = Instant.now();

        for (World base : bases) {
            for (World.Environment env : new World.Environment[]{World.Environment.NETHER, World.Environment.THE_END}) {
                TrackedWorldsRepository.WorldPauseStatus status = state.getState(base, env);
                Instant exp = status.expiresAt();
                if (exp != null && exp.isAfter(now) && !status.enabled()) {
                    plugin.getLogger().info("Monitoring world pause expiration for world: " + base.getName() + " " + env);
                    expirations.put(base.getName() + ":" + env.name(), exp);
                }
            }
        }
    }

    private void scheduleNext() {
        if (nextTask != null) {
            nextTask.cancel();
            nextTask = null;
        }

        var next = expirations.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .orElse(null);

        if (next == null) return;

        long ticks = Math.max(1L, Duration.between(Instant.now(), next.getValue()).toSeconds() * 20L);

        nextTask = Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> {
            expireDue();
            scheduleNext();
        }, ticks);
    }

    private void expireDue() {
        Instant now = Instant.now();

        List<Map.Entry<String, Instant>> due = expirations.entrySet().stream()
                .filter(e -> !e.getValue().isAfter(now)) // <= now
                .toList();

        for (var e : due) {
            expirations.remove(e.getKey());

            String[] parts = e.getKey().split(":", 2);
            String worldName = parts[0];
            World.Environment env = World.Environment.valueOf(parts[1]);

            World base = Bukkit.getWorld(worldName);
            if (base == null) continue;

            plugin.getLogger().info("Expiring pause for for world: " + base.getName() + " " + env);
            state.setDimensionState(base, env, DimensionState.State.ENABLED);
        }
    }


}
