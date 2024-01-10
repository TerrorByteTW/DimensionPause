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
						&& !canBypass(player, bypassable) && !isAllowedWorld(player, World.Environment.NETHER)) {
					kickToWorld(player, dimension);
				}
			}
		} else if (currentEndState) {
			boolean bypassable = ConfigManager.getPluginConfig().getBoolean("dimensions.end.bypassable");
			for (Player player : DimensionPausePlugin.plugin.getServer().getOnlinePlayers()) {
				if (player.getWorld().getEnvironment().equals(World.Environment.THE_END)
						&& !canBypass(player, bypassable) && !isAllowedWorld(player, World.Environment.THE_END)) {
					kickToWorld(player, dimension);
				}
			}
		}
	}

	private void kickToWorld(Player player, World.Environment dimension) {
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

			if (!success) {
				DimensionPausePlugin.plugin.getLogger()
						.warning("IMPORTANT MESSAGE! A world has been paused, but at least one player is still in it ( "
								+ player.getName()
								+ "). This player doesn't have a bed, and none of the worlds in the kick-worlds were obtainable, so we cannot teleport players out of the world. Please intervene!");
				return;
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
	}

	// Get the state of a current dimension.
	public boolean getState(World.Environment dimension, World world) {
		if (dimension.equals(World.Environment.NETHER)) {
			return ConfigManager.getPluginConfig().getBoolean("dimensions.nether.paused");
		} else if (dimension.equals(World.Environment.THE_END)) {
			return ConfigManager.getPluginConfig().getBoolean("dimensions.end.paused");
		} else {
			return false;
		}
	}

	public boolean addWorldToFilter(World world) {
		return true;
	}

	public boolean removeWorldFromFilter(World world) {
		return true;
	}

	/**
	 * This method checks whether or not a world is allowed to be entered by a
	 * player
	 * 
	 * @param player    The player to check
	 * @param dimension The dimension of the world
	 * @return True if the world is allowed to be entered, false if not
	 */
	// TODO: Make this work. This is so confusing LMAO
	public boolean isAllowedWorld(Player player, World.Environment dimension) {
		World world = player.getWorld();
		List<String> worlds = ConfigManager.getPluginConfig().getStringList("kick-world");
		if (worlds.isEmpty()) {
			return true;
		}

		boolean inverted = false;
		boolean isInWorld = false;

		// If Inverted, the filters are the only worlds where it's DISABLED. Otherwise,
		// those are the only worlds where it's ENABLED
		String env = dimension.equals(World.Environment.NETHER) ? "nether" : "end";
		inverted = ConfigManager.getPluginConfig().getBoolean("dimensions." + env + ".worlds.invert");

		// If the player is in a world, we then return inverted. Inverted has the
		// benefit of tracking not only
		for (String w : worlds) {
			if (w.equals(world.getName())) {
				isInWorld = true;
			}
		}

		return !inverted;
	}

	public boolean canBypass(Player player, boolean bypassableFlag) {
		if (player.isOp())
			return true;
		if (!bypassableFlag)
			return false;
		return player.hasPermission("dimensionpause.bypass");
	}

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
