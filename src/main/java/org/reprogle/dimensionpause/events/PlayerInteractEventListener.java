package org.reprogle.dimensionpause.events;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.reprogle.dimensionpause.ConfigManager;
import org.reprogle.dimensionpause.DimensionPausePlugin;

public class PlayerInteractEventListener implements Listener {

	@EventHandler()
	public static void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && event.getClickedBlock().getType().equals(Material.END_PORTAL_FRAME)) {

			boolean bypassable = ConfigManager.getPluginConfig().getBoolean("dimensions.end.bypassable");

			if (ConfigManager.getPluginConfig().getBoolean("dimensions.end.paused")) {
				if (DimensionPausePlugin.ds.canBypass(event.getPlayer(), bypassable)) return;
				event.setCancelled(true);
				//TODO - Alert player why they can't place eyes of ender
			}
		}
	}
}
