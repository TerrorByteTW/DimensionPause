package org.reprogle.dimensionpause.events;

import com.google.inject.Inject;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;
import org.reprogle.dimensionpause.utils.ConfigManager;
import org.reprogle.dimensionpause.utils.DimensionState;
import org.reprogle.dimensionpause.commands.CommandFeedback;

public class PortalCreateEventListener implements Listener {

	@Inject
	DimensionState state;
	@Inject
	ConfigManager configManager;
	@Inject
	CommandFeedback commandFeedback;

	@EventHandler()
	public void onPortalCreateEvent(PortalCreateEvent event) {
		// We only want to disable the portal creation if a player lights it
		if (!(event.getEntity() instanceof Player p)) return;
		// We want to NOT block the creation of portals in the Nether, even if the Nether is disabled. Players should always be allowed to escape if necessary
		// In theory, this only matters if a player gets stuck in the Nether even though they don't have bypass permissions
		if(p.getWorld().getEnvironment().equals(World.Environment.NETHER)) return;
		// If the nether is NOT disabled for this world, ignore the event
		if(state.getState(p.getWorld(), World.Environment.NETHER).enabled()) return;

		// We only want to check create reason of FIRE, because the other two, END_PLATFORM, and NETHER_PAIR, should never be canceled
		if (event.getReason().equals(PortalCreateEvent.CreateReason.FIRE)) {

			// Check if the nether is paused
			if (!state.getState(p.getWorld(), World.Environment.NETHER).enabled()) {
				// If the player can bypass the nether, quit processing
				if (state.canBypass(p, p.getWorld(), World.Environment.NETHER)) return;

				// Block portal creation
				event.setCancelled(true);

				// Send the player the Nether title and chat messages, if configured
				boolean sendTitle = configManager.getPluginConfig().getBoolean("dimensions.nether.alert.title");
				boolean sendChat = configManager.getPluginConfig().getBoolean("dimensions.nether.alert.chat");

				if (sendTitle) {
					p.showTitle(commandFeedback.getTitleForDimension(World.Environment.NETHER));
				}

				if (sendChat) {
					p.sendMessage(commandFeedback.getDimensionIsPausedMessage(World.Environment.NETHER));
				}
			}
		}
	}
}
