package org.reprogle.dimensionpause.events;

import com.google.inject.Inject;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.reprogle.dimensionpause.DimensionPausePlugin;
import org.reprogle.dimensionpause.utils.DimensionState;
import org.reprogle.dimensionpause.utils.WorldUtils;

public class PlayerTeleportEventListener implements Listener {
    @Inject
    DimensionState state;
    @Inject
    DimensionPausePlugin plugin;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // If the teleport is localized within the world, ignore the event
        if (event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            return;
        }
        // Grab the environment and the player. If the player is teleporting to the overworld, ignore it
        World.Environment env = event.getTo().getWorld().getEnvironment();
        Player p = event.getPlayer();
        if (env.equals(World.Environment.NORMAL)) return;
        World fromOverworld = WorldUtils.getOverworld(event.getFrom().getWorld());

        // If the environment the player is teleporting to is disabled, do the following
        if (!state.getState(fromOverworld, env).enabled()) {

            // If the player can bypass the environment, quit processing
            if (state.canBypass(p, fromOverworld, env))
                return;

            // If the all of the above fail cancel the event
            event.setCancelled(true);

            // Send the player the proper title for the environment they tried to access
            state.alertPlayer(p, env);

            // Little smoke effect for when teleport fails
            final Location base = event.getPlayer().getLocation().clone().add(0, 0.1, 0);
            risingSmoke(p, base, 0, 10);
        }
    }

    private void risingSmoke(Player p, Location base, int step, int maxSteps) {
        if (!p.isOnline() || step > maxSteps) return;

        Location loc = base.clone().add(0, step * 0.15, 0);
        loc.getWorld().spawnParticle(Particle.LARGE_SMOKE, loc, 8, 0.2, 0.05, 0.2, 0.01);

        p.getScheduler().runDelayed(plugin, t -> risingSmoke(p, base, step + 1, maxSteps), null, 1L);
    }

}
