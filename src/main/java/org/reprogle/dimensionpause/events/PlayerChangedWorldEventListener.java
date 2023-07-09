package org.reprogle.dimensionpause.events;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.reprogle.dimensionpause.DimensionPausePlugin;

public class PlayerChangedWorldEventListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public static void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
		World.Environment env = event.getPlayer().getWorld().getEnvironment();
		Player p = event.getPlayer();
		if (env.equals(World.Environment.NORMAL)) return;

		// If the world the player is now in is of a disabled type, do the following
		if (DimensionPausePlugin.ds.getState(env)) {
			p.teleport(event.getFrom().getSpawnLocation());
			//TODO - Need to send feedback here as to why they teleported to their previous world's spawn
		}
	}

}