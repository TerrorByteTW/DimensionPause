package org.reprogle.dimensionpause.utils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.dimensionpause.commands.CommandFeedback;

import java.time.Instant;
import java.util.Collection;
import java.util.logging.Level;

import org.bukkit.Location;
import org.reprogle.dimensionpause.store.TrackedWorldsRepository;

@Singleton
public class DimensionState {

    @Inject
    private BytePluginConfig config;
    @Inject
    private JavaPlugin plugin;
    @Inject
    private CommandFeedback commandFeedback;
    @Inject
    private TrackedWorldsRepository repo;
    @Inject
    DimensionExpirationTimer timer;

    /**
     * A very simple method that toggles a dimensions state, does not allow expiration times
     *
     * @param world     The world to toggle
     * @param dimension The dimension in the world to toggle
     */
    public void setDimensionState(World world, World.Environment dimension) {
        setDimensionState(world, dimension, null, State.TOGGLE);
    }

    /**
     * Sets a world's dimension with the given state
     *
     * @param world     The world to switch state
     * @param dimension The dimension to switch state
     * @param state     The state to set the dimension to
     */
    public void setDimensionState(World world, World.Environment dimension, State state) {
        setDimensionState(world, dimension, null, state);
    }

    /**
     * Toggle a world with a given expiration time. If an expiration time is supplied and not null, the world will be force disabled
     *
     * @param world          The world to toggle
     * @param dimension      The dimension to toggle for that world
     * @param expirationTime The time in which the world is re-enabled
     */
    public void setDimensionState(World world, World.Environment dimension, @Nullable Instant expirationTime) {
        setDimensionState(world, dimension, expirationTime, expirationTime == null ? State.TOGGLE : State.DISABLED);
    }

    /**
     * Toggles the dimension for a given world, and kicks players from that world's dimension if there is anyone in the world when it was disabled
     *
     * @param world          The world to toggle
     * @param dimension      The dimension of the world to toggle
     * @param expirationTime An optional expiration date/time which will automatically allow players to re-enter the world once past
     */
    public void setDimensionState(World world, World.Environment dimension, @Nullable Instant expirationTime, State state) {
        Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();

        String worldName = world.getName();

        boolean worldDimensionEnabled;

        switch (state) {
            case DISABLED -> worldDimensionEnabled = repo.setWorld(worldName, dimension, false, expirationTime).enabled();
            case ENABLED -> worldDimensionEnabled = repo.setWorld(worldName, dimension, true, expirationTime).enabled();
            default -> {
                worldDimensionEnabled = repo.isWorldEnabled(worldName, dimension).enabled();
                worldDimensionEnabled = repo.setWorld(worldName, dimension, !worldDimensionEnabled, expirationTime).enabled();
            }
        }

        timer.refresh();

        alertOfStateChange(players, worldName, dimension, worldDimensionEnabled);

        // Check if the world is now disabled
        if (!worldDimensionEnabled) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (player.getWorld().getEnvironment().equals(dimension) && !canBypass(player, world, dimension))
                    kickToWorld(player, dimension);
            }
        }
    }

    /**
     * A helper method to kick a player to a world
     *
     * @param player    The Player being kicked
     * @param dimension The dimension the player was kicked FROM
     */
    public void kickToWorld(Player player, World.Environment dimension) {
        Location loc = player.getRespawnLocation();

        if (config.config().getBoolean("try-bed-first") && loc != null) {
            player.teleportAsync(loc);
        } else {
            World world = Bukkit.getWorld(config.config().getString("kick-world"));

            // We can't teleport the player if the kick-world is invalid, so we must return null
            if (world == null) {
                plugin.getLogger().log(Level.WARNING, "IMPORTANT MESSAGE! A world has been paused, but at least one player is still in it ({0}). This player doesn't have a valid respawn location, and the kick-world configured in config was not obtainable, so we cannot teleport players out of the world. Please intervene!", player.getName());
                return;
            }

            // Teleport the player asynchronously (Folia) to the kick-world's spawn
            player.teleportAsync(world.getSpawnLocation());
        }

        // Alert the player of the teleport
        alertPlayer(player, dimension);

    }

    /**
     * Gets the state of a world's dimension for the given world
     *
     * @param world     The world to check
     * @param dimension The dimension of the world to check
     * @return A {@link TrackedWorldsRepository.WorldPauseStatus} record containing whether the world is enabled, and its expiration time if applicable.
     * Will always return false if a world or world's dimension doesn't exist or isn't in the DB
     */
    public TrackedWorldsRepository.WorldPauseStatus getState(World world, World.Environment dimension) {
        return getState(world.getName(), dimension);
    }

    /**
     * Gets the state of a world's dimension for the given world
     *
     * @param world     The world name to check
     * @param dimension The dimension of the world to check
     * @return A {@link TrackedWorldsRepository.WorldPauseStatus} record containing whether the world is enabled, and its expiration time if applicable.
     * Will always return false if a world or world's dimension doesn't exist or isn't in the DB
     */
    public TrackedWorldsRepository.WorldPauseStatus getState(String world, World.Environment dimension) {
        return repo.isWorldEnabled(world, dimension);
    }

    /**
     * Tests if a player can bypass pauses
     *
     * @param player        The player traveling
     * @param world         The world the player is in. This should always be an overworld
     * @param toEnvironment The environment of the world the player would be going to
     * @return True if bypassable
     */
    public boolean canBypass(Player player, World world, World.Environment toEnvironment) {
        if (player.isOp()) return true;
        if (player.hasPermission("dimensionpause.*")) return true;
        return player.hasPermission("dimensionpause.bypass." + world.getName() + "." + (toEnvironment.equals(World.Environment.NETHER) ? "nether" : "end"));
    }

    /**
     * Alerts a player of a world's dimension being paused or unpaused via chat and/or title if they attempt to enter
     *
     * @param player    The player to alert
     * @param dimension The dimension that was paused (World doesn't matter here)
     */
    public void alertPlayer(Player player, World.Environment dimension) {
        String env = dimension.equals(World.Environment.NETHER) ? "nether" : "end";
        boolean sendTitle = config.config().getBoolean("dimensions." + env + ".alert.title");
        boolean sendChat = config.config().getBoolean("dimensions." + env + ".alert.chat");

        if (sendTitle) {
            player.showTitle(commandFeedback.getTitleForDimension(dimension));
        }

        if (sendChat) {
            player.sendMessage(commandFeedback.getDimensionIsPausedMessage(dimension));
        }
    }

    /**
     * Alerts players via a chat message of a world's dimension being toggled
     *
     * @param players     The player to alert
     * @param world       The world that was toggled
     * @param environment The dimension that was toggled
     * @param newState    The updated state of the dimension
     */
    private void alertOfStateChange(Collection<? extends Player> players, String world, World.Environment environment, boolean newState) {
        // Get a string value for the dimension. This is useful later on.
        String env = environment.equals(World.Environment.NETHER) ? "nether" : "end";

        if (!config.config().getBoolean("dimensions." + env + ".alert.on-toggle")) return;

        for (Player player : players) {
            player.sendMessage(commandFeedback.getStateChangedMessage(world, environment, newState));
        }
    }

    public enum State {
        /**
         * Indicates a Dimension that is enabled
         */
        ENABLED,
        /**
         * Indicates a dimension that is disabled
         */
        DISABLED,
        /**
         * Indicates a dimension whose state will be flipped, used in conjunction with the setDimension method
         */
        TOGGLE
    }
}
