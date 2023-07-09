package org.reprogle.dimensionpause;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class DimensionState {

	private boolean netherPaused = false;
	private boolean endPaused = false;

	// Suppress ConstantValue warning for netherPause and endPaused, because that's not true due to #toggleDimension
	public DimensionState(Plugin plugin) {
		boolean netherPaused = ConfigManager.getPluginConfig().getBoolean("dimensions.nether.paused");
		boolean endPaused = ConfigManager.getPluginConfig().getBoolean("dimensions.end.paused");

		plugin.getLogger().info("The Nether is currently " + (netherPaused ? "paused" : "active") + " and the End is currently" + (endPaused ? "paused" : "active") + ".");
		plugin.getLogger().info("You may change is at any time by running /dimensionpause toggle [end | nether] in-game");
		plugin.getLogger().info("Disabling any dimension will teleport players currently in that dimension to their spawn point, be that a bed");
	}

	public void toggleDimension(World.Environment dimension) {
		// This method requires a Dimension enum, and since there's only two, if it's not one then it's the other
		if (dimension.equals(World.Environment.NETHER)) {
			netherPaused = !netherPaused;
		} else {
			endPaused = !endPaused;
		}

		if (netherPaused) {
			boolean bypassable = ConfigManager.getPluginConfig().getBoolean("dimensions.nether.bypassable");
			for (Player player : DimensionPausePlugin.plugin.getServer().getOnlinePlayers()) {
				if (player.getWorld().getEnvironment().equals(World.Environment.NETHER)) {
					if(!canBypass(player, bypassable)) kickToWorld(player);
				}
			}
		} else if (endPaused) {
			boolean bypassable = ConfigManager.getPluginConfig().getBoolean("dimensions.end.bypassable");
			for (Player player : DimensionPausePlugin.plugin.getServer().getOnlinePlayers()) {
				if (player.getWorld().getEnvironment().equals(World.Environment.THE_END)) {
					if(!canBypass(player, bypassable)) kickToWorld(player);
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
		// This method requires a Dimension enum, and since there's only two, if it's not one then it's the other
		if (dimension.equals(World.Environment.NETHER)) {
			return netherPaused;
		} else {
			return endPaused;
		}
	}

	public boolean canBypass(Player player, boolean bypassableFlag) {
		if (player.isOp()) return true;
		if (!bypassableFlag) return false;
		return player.hasPermission("dimensionpause.end");
	}

}
