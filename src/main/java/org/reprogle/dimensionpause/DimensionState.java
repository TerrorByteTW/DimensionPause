package org.reprogle.dimensionpause;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.reprogle.dimensionpause.commands.CommandFeedback;

import java.util.Collection;

public class DimensionState {

	private boolean netherPaused = false;
	private boolean endPaused = false;

	// Suppress ConstantValue warning for netherPause and endPaused, because that's not true due to #toggleDimension
	public DimensionState(Plugin plugin) {
		boolean netherPaused = ConfigManager.getPluginConfig().getBoolean("dimensions.nether.paused");
		boolean endPaused = ConfigManager.getPluginConfig().getBoolean("dimensions.end.paused");

		plugin.getLogger().info("The Nether is currently " + (netherPaused ? "paused" : "active") + " and the End is currently " + (endPaused ? "paused" : "active") + ".");
		plugin.getLogger().info("You may change is at any time by running /dimensionpause toggle [end | nether] in-game\n");
		plugin.getLogger().info("Disabling any dimension will teleport out players currently in that dimension. See config for more info");
	}

	public void toggleDimension(World.Environment dimension) {
		// Get a string value for the dimension. This is useful later on.
		String environment = dimension.equals(World.Environment.NETHER) ? "nether" : "end";
		Collection<? extends Player> players = DimensionPausePlugin.plugin.getServer().getOnlinePlayers();

		// This method requires a Dimension enum, and since there's only two, if it's not one then it's the other
		if (dimension.equals(World.Environment.NETHER)) {
			netherPaused = !netherPaused;
			alertOfStateChange(players, dimension, netherPaused);
		} else {
			endPaused = !endPaused;
			alertOfStateChange(players, dimension, endPaused);
		}

		if (netherPaused) {
			boolean bypassable = ConfigManager.getPluginConfig().getBoolean("dimensions.nether.bypassable");
			for (Player player : DimensionPausePlugin.plugin.getServer().getOnlinePlayers()) {
				if (player.getWorld().getEnvironment().equals(World.Environment.NETHER) && !canBypass(player, bypassable)) {
					kickToWorld(player);

					// Send the player the proper title for the environment they tried to access
					boolean sendTitle = ConfigManager.getPluginConfig().getBoolean("dimensions." + environment + ".alert.title.enabled");
					boolean sendChat = ConfigManager.getPluginConfig().getBoolean("dimensions." + environment + ".alert.chat.enabled");

					if (sendTitle) {
						player.showTitle(CommandFeedback.getTitleForDimension(dimension));
					}

					if (sendChat) {
						player.sendMessage(CommandFeedback.getChatForDimension(dimension));
					}
				}
			}
		} else if (endPaused) {
			boolean bypassable = ConfigManager.getPluginConfig().getBoolean("dimensions.end.bypassable");
			for (Player player : DimensionPausePlugin.plugin.getServer().getOnlinePlayers()) {
				if (player.getWorld().getEnvironment().equals(World.Environment.THE_END) && !canBypass(player, bypassable)) {
					kickToWorld(player);

					// Send the player the proper title for the environment they tried to access
					boolean sendTitle = ConfigManager.getPluginConfig().getBoolean("dimensions." + environment + ".alert.title.enabled");
					boolean sendChat = ConfigManager.getPluginConfig().getBoolean("dimensions." + environment + ".alert.chat.enabled");

					if (sendTitle) {
						player.showTitle(CommandFeedback.getTitleForDimension(dimension));
					}

					if (sendChat) {
						player.sendMessage(CommandFeedback.getChatForDimension(dimension));
					}
				}
			}
		}
	}

	private void kickToWorld(Player player) {
		if (player.getBedSpawnLocation() != null && ConfigManager.getPluginConfig().getBoolean("try-bed-first")) {
			player.teleport(player.getBedSpawnLocation());
		} else {
			World world = Bukkit.getWorld(ConfigManager.getPluginConfig().getString("kick-world"));
			if (world == null) {
				DimensionPausePlugin.plugin.getLogger().warning("IMPORTANT MESSAGE! The Nether has been paused, but at least one player is still in it. The world set in config was not obtainable, so we cannot teleport players out of the Nether. Please intervene!");
				return;
			}

			player.teleport(world.getSpawnLocation());
		}
	}

	public boolean getState(World.Environment dimension) {
		if (dimension.equals(World.Environment.NETHER)) {
			return netherPaused;
		} else if (dimension.equals(World.Environment.THE_END)) {
			return endPaused;
		} else {
			return false;
		}
	}

	public boolean canBypass(Player player, boolean bypassableFlag) {
		if (player.isOp()) return true;
		if (!bypassableFlag) return false;
		return player.hasPermission("dimensionpause.bypass");
	}

	private void alertOfStateChange(Collection<? extends Player> players, World.Environment environment, boolean newState) {
		// Get a string value for the dimension. This is useful later on.
		String env = environment.equals(World.Environment.NETHER) ? "nether" : "end";

		if (!ConfigManager.getPluginConfig().getBoolean("dimensions." + env + ".alert.on-toggle.enabled")) return;

		for (Player player : players) {
			player.sendMessage(CommandFeedback.getToggleMessageForDimension(environment, newState));
		}

	}

}
