package org.reprogle.dimensionpause.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;
import org.reprogle.dimensionpause.ConfigManager;
import org.reprogle.dimensionpause.DimensionPausePlugin;

public class PortalCreateEventListener implements Listener {

	@EventHandler()
	public static void onPortalCreateEvent(PortalCreateEvent event) {
		// We only want to disable the portal creation if a player lights it
		if (!(event.getEntity() instanceof Player p)) return;
		// We only want to check create reason of FIRE, because the other two, END_PLATFORM, and NETHER_PAIR, should never be cancelled
		if (event.getReason().equals(PortalCreateEvent.CreateReason.FIRE)) {

			boolean bypassable = ConfigManager.getPluginConfig().getBoolean("dimensions.nether.bypassable");
			
			if (ConfigManager.getPluginConfig().getBoolean("dimensions.nether.paused")) {
				if (DimensionPausePlugin.ds.canBypass(p, bypassable)) return;
				event.setCancelled(true);
				//TODO - Alert player why they can't create nether portals
			}
		}
	}
}
