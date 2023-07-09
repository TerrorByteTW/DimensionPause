package org.reprogle.dimensionpause.events;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;
import org.reprogle.dimensionpause.ConfigManager;
import org.reprogle.dimensionpause.DimensionPausePlugin;
import org.reprogle.dimensionpause.commands.CommandFeedback;

public class PortalCreateEventListener implements Listener {

	@EventHandler()
	public static void onPortalCreateEvent(PortalCreateEvent event) {
		// We only want to disable the portal creation if a player lights it
		if (!(event.getEntity() instanceof Player p)) return;
		if(!DimensionPausePlugin.ds.getState(World.Environment.NETHER)) return;

		// We only want to check create reason of FIRE, because the other two, END_PLATFORM, and NETHER_PAIR, should never be cancelled
		if (event.getReason().equals(PortalCreateEvent.CreateReason.FIRE)) {

			// Check if the nether is bypassable
			boolean bypassable = ConfigManager.getPluginConfig().getBoolean("dimensions.nether.bypassable");

			// Check if the nether is paused
			if (DimensionPausePlugin.ds.getState(World.Environment.NETHER)) {
				// If the player can bypass the nether, quit processing
				if (DimensionPausePlugin.ds.canBypass(p, bypassable)) return;

				// Block portal creation
				event.setCancelled(true);

				// Send the player the Nether title and chat messages, if configured
				boolean sendTitle = ConfigManager.getPluginConfig().getBoolean("dimensions.nether.alert.title.enabled");
				boolean sendChat = ConfigManager.getPluginConfig().getBoolean("dimensions.nether.alert.chat.enabled");

				if (sendTitle) {
					p.showTitle(CommandFeedback.getTitleForDimension(World.Environment.NETHER));
				}

				if (sendChat) {
					p.sendMessage(CommandFeedback.getChatForDimension(World.Environment.NETHER));
				}
			}
		}
	}
}
