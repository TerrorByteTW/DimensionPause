package org.reprogle.dimensionpause;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.reprogle.dimensionpause.commands.CommandFeedback;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;

public class DimensionState {

	public static final Set<UUID> alertPlayers = new HashSet<>();

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
					kickToWorld(player, dimension, true);
				}
			}
		}
		
		if (currentEndState) {
			boolean bypassable = ConfigManager.getPluginConfig().getBoolean("dimensions.end.bypassable");
			for (Player player : DimensionPausePlugin.plugin.getServer().getOnlinePlayers()) {
				if (player.getWorld().getEnvironment().equals(World.Environment.THE_END) && !canBypass(player, bypassable)) {
					kickToWorld(player, dimension, true);
				}
			}
		}
	}

	@Nullable
	public Location kickToWorld(Player player, World.Environment dimension, boolean teleport) {
		Location bedSpawn = player.getBedSpawnLocation();
		Location loc;

		if (ConfigManager.getPluginConfig().getBoolean("try-bed-first") && bedSpawn != null) {
			if (teleport) player.teleport(bedSpawn);
			loc = player.getBedSpawnLocation();
		} else {
			World world = Bukkit.getWorld(ConfigManager.getPluginConfig().getString("kick-world"));
			if (world == null) {
				DimensionPausePlugin.plugin.getLogger().log(Level.WARNING, "IMPORTANT MESSAGE! A world has been paused, but at least one player is still in it ( {0}). This player doesn''t have a bed, and the kick-world configured in config was not obtainable, so we cannot teleport players out of the world. Please intervene!", player.getName());
				return null;
			}

			player.teleport(world.getSpawnLocation());
			loc = world.getSpawnLocation();
		}

		if (teleport) {
			alertPlayer(player, dimension);
		}

		return loc;
	}

	public boolean getState(World.Environment dimension) {
            return switch (dimension) {
                case NETHER -> ConfigManager.getPluginConfig().getBoolean("dimensions.nether.paused");
                case THE_END -> ConfigManager.getPluginConfig().getBoolean("dimensions.end.paused");
                default -> false;
            };
	}

	public boolean canBypass(Player player, boolean bypassableFlag) {
		if (player.isOp()) return true;
		if (!bypassableFlag) return false;
		return player.hasPermission("dimensionpause.bypass");
	}

	public void alertPlayer(Player player, World.Environment dimension) {
		String env = dimension.equals(World.Environment.NETHER) ? "nether" : "end";
		boolean sendTitle = ConfigManager.getPluginConfig().getBoolean("dimensions." + env + ".alert.title.enabled");
		boolean sendChat = ConfigManager.getPluginConfig().getBoolean("dimensions." + env + ".alert.chat.enabled");
		
		if (sendTitle) {
			player.showTitle(CommandFeedback.getTitleForDimension(dimension));
		}
		
		if (sendChat) {
			player.sendMessage(CommandFeedback.getChatForDimension(dimension));
		}
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
