package org.reprogle.dimensionpause.events;

import com.google.inject.Inject;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.reprogle.dimensionpause.utils.ConfigManager;
import org.reprogle.dimensionpause.DimensionPausePlugin;
import org.reprogle.dimensionpause.utils.DimensionState;
import org.reprogle.dimensionpause.commands.CommandFeedback;
import org.reprogle.dimensionpause.utils.WorldUtils;

public class PlayerJoinEventListener implements Listener {
    @Inject
    DimensionState state;
    @Inject
    ConfigManager configManager;
    @Inject
    DimensionPausePlugin plugin;
    @Inject
    CommandFeedback commandFeedback;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        World.Environment currentEnv = player.getWorld().getEnvironment();
        World overworld = WorldUtils.getOverworld(player.getWorld());
        World currentWorld = player.getWorld();
        String kickWorld = configManager.getPluginConfig().getString("kick-world");

        // No need to do anything if the player is already in the world they would be kicked to
        if (currentWorld.getName().equals(kickWorld)) return;

        if (!state.getState(overworld, currentEnv).enabled()) {

            // If the player can bypass the environment, quit processing
            if (state.canBypass(player, overworld, currentEnv)) return;

            final int delay = configManager.getPluginConfig().getInt("on-join-kick-delay");
            final int[] t = {delay};

            player.getScheduler().runAtFixedRate(plugin, task -> {
                if (!player.isOnline()) {
                    task.cancel();
                    return;
                }

                if (t[0] <= 0) {
                    task.cancel();
                    state.kickToWorld(player, currentEnv);
                    return;
                }

                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 1.4f);

                player.showTitle(commandFeedback.getTitleForTeleport(t[0], t[0] == delay));

                t[0]--;
            }, null, 20L, 20L);
        }
    }

}
