/*
 * DimensionPause is a tool for dynamic dimension locking
 * Copyright TerrorByte (c) 2022-2024
 * Copyright DimensionPause Contributors (c) 2022-2024
 * 
 * This program is free software: You can redistribute it and/or modify it under the terms of the Mozilla Public License 2.0
 * as published by the Mozilla under the Mozilla Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including, without limitation,
 * warranties that the Covered Software is free of defects, merchantable, fit for a particular purpose or non-infringing.
 * See the MPL 2.0 license for more details.
 * 
 * For a full copy of the license in its entirety, please visit <https://www.mozilla.org/en-US/MPL/2.0/>
 */
package org.reprogle.dimensionpause;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.reprogle.dimensionpause.commands.CommandFeedback;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("java:S1192")
/**
 * The dimension state class is used to manage the state of the dimensions. It
 * also handles kicking players out of the dimensions if they are disabled.
 * - The constructor simply alerts console of the current states of dimensions
 * on load
 * - The toggleDimension method will toggle the state of the dimension
 * provided
 * - The getState method will return the state of the dimension provided
 * - The canBypass method will return whether or not the player can bypass
 * the dimension provided
 * - The kickToWorld method will kick the player to the world specified in
 * the config
 * - The alertOfStateChange method will alert the server of the state change
 * of the dimension provided
 */
public class DimensionState {

	/**
	 * Create the DimensionState class. This constructor isn't extremely necessary,
	 * but is just meant to alert console of the current states of the dimensions on
	 * load.
	 * 
	 * @param plugin The plugin object representing DimensionPause
	 */
	@SuppressWarnings("java:S2629")
	public DimensionState(Plugin plugin) {
		boolean netherState = ConfigManager.getPluginConfig().getBoolean("dimensions.nether.paused");
		boolean endState = ConfigManager.getPluginConfig().getBoolean("dimensions.end.paused");
		List<String> filterListEnd = ConfigManager.getPluginConfig().getStringList("dimensions.end.worlds.world-list");
		List<String> filterListNether = ConfigManager.getPluginConfig()
				.getStringList("dimensions.nether.worlds.world-list");

		StringBuilder filterListEndString = new StringBuilder();
		for (int i = 0; i < filterListEnd.size(); i++) {
			filterListEndString.append(filterListEnd.get(i));
			if (i < filterListEnd.size() - 1) {
				filterListEndString.append(", ");
			}
		}

		StringBuilder filterListNetherString = new StringBuilder();
		for (int i = 0; i < filterListNether.size(); i++) {
			filterListNetherString.append(filterListNether.get(i));
			if (i < filterListNether.size() - 1) {
				filterListNetherString.append(", ");
			}
		}

		plugin.getLogger().info("The Nether is currently " + (netherState ? "paused" : "active")
				+ " and the End is currently " + (endState ? "paused" : "active") + ".");

		plugin.getLogger().info("Current worlds in the End dimension filter: " + filterListEndString.toString());
		plugin.getLogger().info("Current worlds in the Nether dimension filter: " + filterListNetherString.toString());
		plugin.getLogger()
				.info("The End dimension filter is currently " + (Boolean.TRUE.equals(
						ConfigManager.getPluginConfig().getBoolean("dimensions.end.worlds.invert")) ? "inverted"
								: "not inverted"));
		plugin.getLogger().info("The Nether dimension filter is currently " + (Boolean.TRUE.equals(
				ConfigManager.getPluginConfig().getBoolean("dimensions.nether.worlds.invert")) ? "inverted"
						: "not inverted"));

		plugin.getLogger()
				.info("You may change is at any time by running /dimensionpause toggle [end | nether] in-game\n");
		plugin.getLogger().info(
				"Disabling any dimension will teleport out players currently in that dimension. See config for more info");
	}

	/**
	 * Toggle a given dimension
	 * 
	 * @param dimension
	 */
	@SuppressWarnings("java:S3776")
	public void toggleDimension(World.Environment dimension) {
		Collection<? extends Player> players = DimensionPausePlugin.plugin.getServer().getOnlinePlayers();

		boolean currentNetherState = ConfigManager.getPluginConfig().getBoolean("dimensions.nether.paused");
		boolean currentEndState = ConfigManager.getPluginConfig().getBoolean("dimensions.end.paused");

		// This method requires a Dimension enum, and since there's only two that this
		// plugin uses, if it's not one then it's the other
		if (dimension.equals(World.Environment.NETHER)) {
			currentNetherState = !currentNetherState;
			try {
				ConfigManager.getPluginConfig().set("dimensions.nether.paused", currentNetherState);
				ConfigManager.getPluginConfig().save();
			} catch (IOException e) {
				DimensionPausePlugin.plugin.getLogger()
						.warning(CommandFeedback.sendCommandFeedback("io-exception").toString());
			}

			alertOfStateChange(players, dimension, currentNetherState);
		} else {
			currentEndState = !currentEndState;
			try {
				ConfigManager.getPluginConfig().set("dimensions.end.paused", currentEndState);
				ConfigManager.getPluginConfig().save();
			} catch (IOException e) {
				DimensionPausePlugin.plugin.getLogger()
						.warning(CommandFeedback.sendCommandFeedback("io-exception").toString());
			}
			alertOfStateChange(players, dimension, currentEndState);
		}

		if (currentNetherState) {
			boolean bypassable = ConfigManager.getPluginConfig().getBoolean("dimensions.nether.bypassable");
			for (Player player : DimensionPausePlugin.plugin.getServer().getOnlinePlayers()) {
				if (player.getWorld().getEnvironment().equals(World.Environment.NETHER)
						&& !canBypass(player, bypassable)
						&& !isAllowedWorld(player.getWorld())) {
					kickToWorld(player, dimension, false);
				}
			}
		} else if (currentEndState) {
			boolean bypassable = ConfigManager.getPluginConfig().getBoolean("dimensions.end.bypassable");
			for (Player player : DimensionPausePlugin.plugin.getServer().getOnlinePlayers()) {
				if (player.getWorld().getEnvironment().equals(World.Environment.THE_END)
						&& !canBypass(player, bypassable)
						&& !isAllowedWorld(player.getWorld())) {
					kickToWorld(player, dimension, false);
				}
			}
		}
	}

	/**
	 * A legacy method to support the new fromPlayerChangeEvent parameter in the
	 * kickToWorld method
	 * 
	 * @param player    The player to kick
	 * @param dimension The dimension they were kicked from
	 * @return True if the player was able to be successfully teleported out
	 * @Deprecated Use {@link #kickToWorld(Player, World.Environment, boolean
	 *             fromPlayerChangeEvent)} instead
	 */
	public boolean kickToWorld(Player player, World.Environment dimension) {
		return kickToWorld(player, dimension, false);
	}

	/**
	 * Kick a player out of the world they're currently in if it is disabled or if
	 * they attempt to join a world that is disabled
	 * 
	 * @param player    The player to kick
	 * @param dimension The dimension they're *traveling to*, not necessarily in,
	 *                  however it can be both. This method is used in multiple
	 *                  places
	 * @return True if the player was able to be successfully teleported out
	 */
	@SuppressWarnings("java:S3776")
	public boolean kickToWorld(Player player, World.Environment dimension, boolean fromPlayerChangeEvent) {
		if (player.getBedSpawnLocation() != null && ConfigManager.getPluginConfig().getBoolean("try-bed-first")) {
			player.teleport(player.getBedSpawnLocation());
		} else {
			boolean success = false;
			List<String> worlds = ConfigManager.getPluginConfig().getStringList("kick-world");
			for (String world : worlds) {
				World tWorld = Bukkit.getWorld(world);
				if (tWorld != null) {
					player.teleport(tWorld.getSpawnLocation());
					success = true;
					break;
				}
			}

			if (!success && !fromPlayerChangeEvent) {
				DimensionPausePlugin.plugin.getLogger()
						.warning("IMPORTANT MESSAGE! A world has been paused, but at least one player is still in it ( "
								+ player.getName()
								+ "). This player doesn't have a bed, and none of the worlds in the kick-worlds were obtainable, so we cannot teleport players out of the world. Please intervene!");
				return false;
			} else if (!success) {
				DimensionPausePlugin.plugin.getLogger()
						.warning("IMPORTANT MESSAGE! A world has been paused, but at least one player is still in it ( "
								+ player.getName()
								+ "). This player doesn't have a bed, and none of the worlds in the kick-worlds were obtainable, so we are trying to teleport them back to where they came from. Please double check your config!");
				return false;
			}
		}

		// Send the player the proper title for the environment they tried to access
		// Before we actually do that, first check if we are even supposed to send that
		// message. If not, just don't lol.
		boolean sendTitle = ConfigManager.getPluginConfig().getBoolean("dimensions."
				+ (dimension.equals(World.Environment.NETHER) ? "nether" : "end") + ".alert.title.enabled");
		boolean sendChat = ConfigManager.getPluginConfig().getBoolean("dimensions."
				+ (dimension.equals(World.Environment.NETHER) ? "nether" : "end") + ".alert.chat.enabled");

		if (sendTitle) {
			player.showTitle(CommandFeedback.getTitleForDimension(dimension));
		}

		if (sendChat) {
			player.sendMessage(CommandFeedback.getChatForDimension(dimension));
		}

		return true;
	}

	/**
	 * Get's the state of a world
	 * 
	 * @param world The world to check
	 * @return Whether or not a world is disabled
	 */
	public boolean getState(World.Environment dimension) {
		if (dimension.equals(World.Environment.NETHER)) {
			return ConfigManager.getPluginConfig().getBoolean("dimensions.nether.paused");
		} else if (dimension.equals(World.Environment.THE_END)) {
			return ConfigManager.getPluginConfig().getBoolean("dimensions.end.paused");
		} else {
			return false;
		}
	}

	/**
	 * Adds a world to the world filter
	 * 
	 * @param world The world to add
	 */
	public void addWorldToFilter(World world) {
		World.Environment dimension = world.getEnvironment();
		String env = dimension.equals(World.Environment.NETHER) ? "nether" : "end";
		List<String> worlds = ConfigManager.getPluginConfig().getStringList("dimensions." + env + ".worlds.world-list");

		if (!worlds.contains(world.getName())) {
			worlds.add(world.getName());
			ConfigManager.getPluginConfig().set("dimensions." + env + ".worlds.world-list", worlds);
		}
	}

	/**
	 * Removes a world from the filter list
	 * 
	 * @param world The world to remove
	 */
	public void removeWorldFromFilter(World world) {
		World.Environment dimension = world.getEnvironment();
		String env = dimension.equals(World.Environment.NETHER) ? "nether" : "end";
		List<String> worlds = ConfigManager.getPluginConfig().getStringList("dimensions." + env + ".worlds.world-list");

		if (worlds.contains(world.getName())) {
			worlds.remove(world.getName());
			ConfigManager.getPluginConfig().set("dimensions." + env + ".worlds.world-list", worlds);
		}
	}

	/**
	 * This method checks whether or not a world is currently in the filter list
	 * 
	 * @param world The world to check
	 * @return True if the world is allowed to be entered, false if not
	 */
	public boolean isAllowedWorld(World world) {
		World.Environment dimension = world.getEnvironment();
		String env = dimension.equals(World.Environment.NETHER) ? "nether" : "end";
		List<String> worlds = ConfigManager.getPluginConfig().getStringList("dimensions." + env + ".worlds.world-list");
		if (worlds.isEmpty()) {
			return true;
		}

		boolean inverted = false;
		boolean allowed = false;

		// If Inverted, the filters are the only worlds where it's DISABLED. Otherwise,
		// those are the only worlds where it's ENABLED
		inverted = ConfigManager.getPluginConfig().getBoolean("dimensions." + env + ".worlds.invert");

		// If the player is in a world, we then return inverted. Inverted has the
		// benefit of tracking not only
		for (String w : worlds) {
			if (w.equals(world.getName())) {
				allowed = true;
			}
		}

		return !inverted ? allowed : !allowed;
	}

	/**
	 * A simple utility method that checks whether or not a player can bypass a
	 * given dimension
	 * 
	 * @param player         The player to check
	 * @param bypassableFlag Whether or not the dimension is bypassable in config
	 * @return True if the player can bypass, false if not
	 */
	public boolean canBypass(Player player, boolean bypassableFlag) {
		if (player.isOp())
			return true;
		if (!bypassableFlag)
			return false;
		return player.hasPermission("dimensionpause.bypass");
	}

	/**
	 * This method alerts players of the state change of the dimension provided
	 * 
	 * @param players     A list of player objects to iterate through
	 * @param environment The dimension that was changed
	 * @param newState    Whether or not the dimension is paused or not paused.
	 */
	private void alertOfStateChange(Collection<? extends Player> players, World.Environment environment,
			boolean newState) {
		// Get a string value for the dimension. This is useful later on.
		String env = environment.equals(World.Environment.NETHER) ? "nether" : "end";

		if (Boolean.FALSE
				.equals(ConfigManager.getPluginConfig().getBoolean("dimensions." + env + ".alert.on-toggle.enabled")))
			return;

		for (Player player : players) {
			player.sendMessage(CommandFeedback.getToggleMessageForDimension(environment, newState));
		}

	}

}
