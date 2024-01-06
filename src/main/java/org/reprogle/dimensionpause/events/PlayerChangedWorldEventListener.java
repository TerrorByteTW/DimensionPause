package org.reprogle.dimensionpause.events;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.reprogle.dimensionpause.ConfigManager;
import org.reprogle.dimensionpause.DimensionPausePlugin;
import org.reprogle.dimensionpause.commands.CommandFeedback;

// TODO: Add support for the filter list added in config.yml
public class PlayerChangedWorldEventListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public static void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
		// Grab the environment and the player. If the player is teleporting to the
		// overworld, ignore it
		World.Environment env = event.getPlayer().getWorld().getEnvironment();
		Player p = event.getPlayer();
		if (env.equals(World.Environment.NORMAL))
			return;

		// Grab the bypassable values for the nether and end.
		boolean netherBypass = ConfigManager.getPluginConfig().getBoolean("dimensions.nether.bypassable");
		boolean endBypass = ConfigManager.getPluginConfig().getBoolean("dimensions.end.bypassable");

		// If the environment the player is teleporting to is disabled, do the following
		if (DimensionPausePlugin.ds.getState(env)) {

			// If the player can bypass the environment, quit processing
			if (DimensionPausePlugin.ds.canBypass(p, env.equals(World.Environment.NETHER) ? netherBypass : endBypass))
				return;

			// If the all of the above fail, force them back to the world they came from
			p.teleport(event.getFrom().getSpawnLocation());

			// Send the player the proper title for the environment they tried to access
			String environment = env.equals(World.Environment.NETHER) ? "nether" : "end";
			boolean sendTitle = ConfigManager.getPluginConfig()
					.getBoolean("dimensions." + environment + ".alert.title.enabled");
			boolean sendChat = ConfigManager.getPluginConfig()
					.getBoolean("dimensions." + environment + ".alert.chat.enabled");

			if (sendTitle) {
				p.showTitle(CommandFeedback.getTitleForDimension(env));
			}

			if (sendChat) {
				p.sendMessage(CommandFeedback.getChatForDimension(env));
			}
		}
	}

}