package org.reprogle.dimensionpause;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.reprogle.dimensionpause.commands.CommandFeedback;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;
import org.reprogle.dimensionpause.store.Database;
import org.reprogle.dimensionpause.store.SQLite;

@Singleton
public class DimensionState {

    public final Set<UUID> alertPlayers = new HashSet<>();
    @Inject
    private ConfigManager configManager;
    @Inject
    private DimensionPausePlugin plugin;
    @Inject
    private CommandFeedback commandFeedback;
    @Inject
    private SQLite db;

    public void toggleDimension(World world, World.Environment dimension, @Nullable LocalDate expirationTime) {
        toggleDimension(world.getName(), dimension, expirationTime);
    }

    public void toggleDimension(String world, World.Environment dimension, @Nullable LocalDate expirationTime) {
        Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();

        boolean worldDimensionEnabled = db.isWorldEnabled(world, dimension).enabled();
        db.setWorld(world, dimension, !worldDimensionEnabled, expirationTime);

        alertOfStateChange(players, world, dimension, !worldDimensionEnabled);

        if (!worldDimensionEnabled) {
            boolean bypassable = configManager.getPluginConfig().getBoolean("dimensions." + (dimension.equals(World.Environment.NETHER) ? "nether" : "end") + ".bypassable");
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (player.getWorld().getEnvironment().equals(dimension) && !canBypass(player, bypassable)) {
                    kickToWorld(player, dimension, true);
                }
            }
        }
    }

    /**
     * A helper method to kick a player to a world, OR to get the location of the place they'd be spawned at (In the case of Async events)
     *
     * @param player    The Player being kicked
     * @param dimension The dimension the player was kicked FROM
     * @param teleport  Whether to teleport the player once the respawn location is confirmed
     * @return The Location the player was/will be teleported to
     */
    @Nullable
    public Location kickToWorld(Player player, World.Environment dimension, boolean teleport) {
        Location loc = player.getRespawnLocation();

        if (configManager.getPluginConfig().getBoolean("try-bed-first") && loc != null) {
            if (teleport) player.teleportAsync(loc);
        } else {
            World world = Bukkit.getWorld(configManager.getPluginConfig().getString("kick-world"));

            // We can't teleport the player if the kick-world is invalid, so we must return null
            if (world == null) {
                plugin.getLogger().log(Level.WARNING, "IMPORTANT MESSAGE! A world has been paused, but at least one player is still in it ({0}). This player doesn't have a valid respawn location, and the kick-world configured in config was not obtainable, so we cannot teleport players out of the world. Please intervene!", player.getName());
                return null;
            }

            // Teleport the player asynchronously (Folia) to the kick-world's spawn
            if (teleport) player.teleportAsync(world.getSpawnLocation());
            loc = world.getSpawnLocation();
        }

        // If we teleported the player, alert them
        if (teleport) {
            alertPlayer(player, dimension);
        }

        return loc;
    }

    public Database.WorldPauseStatus getState(World world, World.Environment dimension) {
        return getState(world.getName(), dimension);
    }

    public Database.WorldPauseStatus getState(String world, World.Environment dimension) {
        return db.isWorldEnabled(world, dimension);
    }

    public boolean canBypass(Player player, boolean bypassableFlag) {
        if (player.isOp()) return true;
        if (!bypassableFlag) return false;
        return player.hasPermission("dimensionpause.bypass");
    }

    public void alertPlayer(Player player, World.Environment dimension) {
        String env = dimension.equals(World.Environment.NETHER) ? "nether" : "end";
        boolean sendTitle = configManager.getPluginConfig().getBoolean("dimensions." + env + ".alert.title.enabled");
        boolean sendChat = configManager.getPluginConfig().getBoolean("dimensions." + env + ".alert.chat.enabled");

        if (sendTitle) {
            player.showTitle(commandFeedback.getTitleForDimension(dimension));
        }

        if (sendChat) {
            player.sendMessage(commandFeedback.getChatForDimension(dimension));
        }
    }

    private void alertOfStateChange(Collection<? extends Player> players, String world, World.Environment environment, boolean newState) {
        // Get a string value for the dimension. This is useful later on.
        String env = environment.equals(World.Environment.NETHER) ? "nether" : "end";

        if (!configManager.getPluginConfig().getBoolean("dimensions." + env + ".alert.on-toggle.enabled")) return;

        for (Player player : players) {
            player.sendMessage(commandFeedback.getToggleMessageForDimension(world, environment, newState));
        }

    }
}
