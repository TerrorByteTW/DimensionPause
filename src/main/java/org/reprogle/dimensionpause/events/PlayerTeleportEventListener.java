package org.reprogle.dimensionpause.events;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.reprogle.dimensionpause.ConfigManager;
import org.reprogle.dimensionpause.DimensionPausePlugin;
import org.reprogle.dimensionpause.commands.CommandFeedback;

public class PlayerTeleportEventListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public static void onPlayerTeleport(PlayerTeleportEvent event) {
		// If the teleport is localized within the world, ignore the event
		if (event.getFrom().getWorld().equals(event.getTo().getWorld())) {
			return;
		}
		// Grab the environment and the player. If the player is teleporting to the overworld, ignore it
		World.Environment env = event.getTo().getWorld().getEnvironment();
		Player p = event.getPlayer();
		if (env.equals(World.Environment.NORMAL)) return;

		// Grab the bypassable values for the nether and end.
		boolean netherBypass = ConfigManager.getPluginConfig().getBoolean("dimensions.nether.bypassable");
		boolean endBypass = ConfigManager.getPluginConfig().getBoolean("dimensions.end.bypassable");

		// If the environment the player is teleporting to is disabled, do the following
		if (DimensionPausePlugin.ds.getState(env)) {

			// If the player can bypass the environment, quit processing
			if (DimensionPausePlugin.ds.canBypass(p, env.equals(World.Environment.NETHER) ? netherBypass : endBypass))
				return;

			// If the all of the above fail cancel the event
			event.setCancelled(true);

			// Send the player the proper title for the environment they tried to access
			String environment = env.equals(World.Environment.NETHER) ? "nether" : "end";
			boolean sendTitle = ConfigManager.getPluginConfig().getBoolean("dimensions." + environment + ".alert.title.enabled");
			boolean sendChat = ConfigManager.getPluginConfig().getBoolean("dimensions." + environment + ".alert.chat.enabled");

			if (sendTitle) {
				p.showTitle(CommandFeedback.getTitleForDimension(env));
			}

			if (sendChat) {
				p.sendMessage(CommandFeedback.getChatForDimension(env));
			}
		}
	}

}