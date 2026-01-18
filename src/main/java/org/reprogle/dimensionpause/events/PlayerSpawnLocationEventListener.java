package org.reprogle.dimensionpause.events;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.inject.Inject;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent;
import org.bukkit.event.Listener;
import org.reprogle.dimensionpause.ConfigManager;
import org.reprogle.dimensionpause.DimensionPausePlugin;
import org.reprogle.dimensionpause.DimensionState;

import java.util.UUID;

public class PlayerSpawnLocationEventListener implements Listener {

    @Inject
    private ConfigManager configManager;
    
    @Inject
    private DimensionState state;

    @Inject
    private DimensionPausePlugin plugin;

    // AsyncPlayerSpawnLocationEvent is only available in 1.21+
    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerSpawn(AsyncPlayerSpawnLocationEvent event) {
        if (event.isNewPlayer()) return;
        World world = event.getSpawnLocation().getWorld();
        String kickWorld = configManager.getPluginConfig().getString("kick-world");

        // No need to do anything if the player is already in the world they would be kicked to
        if (world.getName().equals(kickWorld)) {
            return;
        }

        // Grab the bypassable values for the nether and end.
		boolean netherBypass = configManager.getPluginConfig().getBoolean("dimensions.nether.bypassable");
		boolean endBypass = configManager.getPluginConfig().getBoolean("dimensions.end.bypassable");

        // If the environment the player is teleporting to is disabled, do the following
        if (!state.getState(world, world.getEnvironment()).enabled()) {
            
            // If the player can bypass the environment, quit processing
            UUID playerUuid = event.getConnection().getProfile().getId();
            Player player = Bukkit.getPlayer(event.getConnection().getProfile().getId());
            if (playerUuid == null || player == null) {
                plugin.getLogger().warning("A player just spawned but their profile could not be retrieved, so we cannot check if they're allowed in this world or not. Check the above logs for the spawn event!");
                return;
            }

			if (state.canBypass(player, world.getEnvironment().equals(World.Environment.NETHER) ? netherBypass : endBypass))
				return;

			Location location = state.kickToWorld(player, world.getEnvironment(), false);

            if (location != null) {
                event.setSpawnLocation(location);
                state.alertPlayers.add(playerUuid);
            }
        }
    }
}
