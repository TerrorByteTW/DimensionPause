package org.reprogle.dimensionpause.events;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.reprogle.dimensionpause.ConfigManager;
import org.reprogle.dimensionpause.DimensionPausePlugin;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class PlayerSpawnLocationEventListener implements Listener{

    @EventHandler(priority = EventPriority.HIGHEST)
	public static void onPlayerSpawn(PlayerSpawnLocationEvent event) {
        World world = event.getSpawnLocation().getWorld();
        String kickWorld = ConfigManager.getPluginConfig().getString("kick-world");

        // No need to do anything if the player is already in the world they would be kicked to
        if (world.getName().equals(kickWorld)) {
            return;
        }

        // Grab the bypassable values for the nether and end.
		boolean netherBypass = ConfigManager.getPluginConfig().getBoolean("dimensions.nether.bypassable");
		boolean endBypass = ConfigManager.getPluginConfig().getBoolean("dimensions.end.bypassable");

        // If the environment the player is teleporting to is disabled, do the following
        if (DimensionPausePlugin.ds.getState(world.getEnvironment())) {
            
            // If the player can bypass the environment, quit processing
			if (DimensionPausePlugin.ds.canBypass(event.getPlayer(), world.getEnvironment().equals(World.Environment.NETHER) ? netherBypass : endBypass))
				return;

            // If the all of the above fail, set the spawn to the kick world
			Location location = DimensionPausePlugin.ds.kickToWorld(event.getPlayer(), world.getEnvironment(), false);

            if (location != null) {
                event.setSpawnLocation(location);
            }
        }
    }
}
