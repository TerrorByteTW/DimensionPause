package org.reprogle.dimensionpause;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.reprogle.dimensionpause.commands.CommandFeedback;

import java.io.IOException;
import java.util.Collection;

public class DimensionState {

	// Suppress ConstantValue warning for netherPause and endPaused, because that's not true due to #toggleDimension
	public DimensionState(Plugin plugin) {
		boolean netherState = ConfigManager.getPluginConfig().getBoolean("dimensions.nether.paused");
		boolean endState = ConfigManager.getPluginConfig().getBoolean("dimensions.end.paused");

		plugin.getLogger().info("The Nether is currently " + (netherState ? "paused" : "active") + " and the End is currently " + (endState ? "paused" : "active") + ".");
		plugin.getLogger().info("You may change is at any time by running /dimensionpause toggle [end | nether] in-game\n");
		plugin.getLogger().info("Disabling any dimension will teleport out players currently in that dimension. See config for more info");
	}

	public void toggleDimension(World.Environment dimension) {
		Collection<? extends Player> players = DimensionPausePlugin.plugin.getServer().getOnlinePlayers();

		boolean currentNetherState = ConfigManager.getPluginConfig().getBoolean("dimensions.nether.paused");
		boolean currentEndState = ConfigManager.getPluginConfig().getBoolean("dimensions.end.paused");

		// This method requires a Dimension enum, and since there's only two, if it's not one then it's the other
		if (dimension.equals(World.Environment.NETHER)) {
			currentNetherState = !currentNetherState;
			try {
				ConfigManager.getPluginConfig().set("dimensions.nether.paused", currentNetherState);
				ConfigManager.getPluginConfig().save();
			} catch (IOException e) {
				DimensionPausePlugin.plugin.getLogger().warning(CommandFeedback.sendCommandFeedback("io-exception").toString());
			}

			alertOfStateChange(players, dimension, currentNetherState);
		} else {
			currentEndState = !currentEndState;
			try {
				ConfigManager.getPluginConfig().set("dimensions.end.paused", currentEndState);
				ConfigManager.getPluginConfig().save();
			} catch (IOException e) {
				DimensionPausePlugin.plugin.getLogger().warning(CommandFeedback.sendCommandFeedback("io-exception").toString());
			}
			alertOfStateChange(players, dimension, currentEndState);
		}

		if (currentNetherState) {
			boolean bypassable = ConfigManager.getPluginConfig().getBoolean("dimensions.nether.bypassable");
			for (Player player : DimensionPausePlugin.plugin.getServer().getOnlinePlayers()) {
				if (player.getWorld().getEnvironment().equals(World.Environment.NETHER) && !canBypass(player, bypassable)) {
					kickToWorld(player, dimension);
				}
			}
		} else if (currentEndState) {
			boolean bypassable = ConfigManager.getPluginConfig().getBoolean("dimensions.end.bypassable");
			for (Player player : DimensionPausePlugin.plugin.getServer().getOnlinePlayers()) {
				if (player.getWorld().getEnvironment().equals(World.Environment.THE_END) && !canBypass(player, bypassable)) {
					kickToWorld(player, dimension);
				}
			}
		}
	}

	private void kickToWorld(Player player, World.Environment dimension) {
		if (player.getBedSpawnLocation() != null && ConfigManager.getPluginConfig().getBoolean("try-bed-first")) {
			player.teleport(player.getBedSpawnLocation());
		} else {
			World world = Bukkit.getWorld(ConfigManager.getPluginConfig().getString("kick-world"));
			if (world == null) {
				DimensionPausePlugin.plugin.getLogger().warning("IMPORTANT MESSAGE! A world has been paused, but at least one player is still in it ( " + player.getName() + "). This player doesn't have a bed, and the kick-world configured in config was not obtainable, so we cannot teleport players out of the world. Please intervene!");
				return;
			}

			player.teleport(world.getSpawnLocation());
		}

		// Send the player the proper title for the environment they tried to access
		boolean sendTitle = ConfigManager.getPluginConfig().getBoolean("dimensions." + (dimension.equals(World.Environment.NETHER) ? "nether" : "end") + ".alert.title.enabled");
		boolean sendChat = ConfigManager.getPluginConfig().getBoolean("dimensions." + (dimension.equals(World.Environment.NETHER) ? "nether" : "end") + ".alert.chat.enabled");

		if (sendTitle) {
			player.showTitle(CommandFeedback.getTitleForDimension(dimension));
		}

		if (sendChat) {
			player.sendMessage(CommandFeedback.getChatForDimension(dimension));
		}
	}

	public boolean getState(World.Environment dimension) {
		if (dimension.equals(World.Environment.NETHER)) {
			return ConfigManager.getPluginConfig().getBoolean("dimensions.nether.paused");
		} else if (dimension.equals(World.Environment.THE_END)) {
			return ConfigManager.getPluginConfig().getBoolean("dimensions.end.paused");
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
