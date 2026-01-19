package org.reprogle.dimensionpause.events;

import com.google.inject.Inject;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.reprogle.dimensionpause.utils.ConfigManager;
import org.reprogle.dimensionpause.DimensionPausePlugin;
import org.reprogle.dimensionpause.utils.DimensionState;
import org.reprogle.dimensionpause.commands.CommandFeedback;
import org.reprogle.dimensionpause.store.Database;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerPortalEventListener implements Listener {

    @Inject
    ConfigManager configManager;
    @Inject
    CommandFeedback commandFeedback;
    @Inject
    DimensionState state;
    @Inject
    DimensionPausePlugin plugin;

    private final Set<UUID> playersBeingHandled = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPortalEnter(org.bukkit.event.player.PlayerPortalEvent event) {
        // We only care if they're teleporting from the overworld, as exiting the Nether is always okay, and
        // End Portals can't be created without commands in the Nether and vice-versa
        // The PlayerTeleportEventListener will handle fixing this if the player is somehow teleported to the End from the Nether and vice-versa
        if (event.getPlayer().getWorld().getEnvironment() != World.Environment.NORMAL) return;
        Player player = event.getPlayer();

        World.Environment environmentTo = event.getTo().getWorld().getEnvironment();
        String stringifiedEnv = environmentTo == World.Environment.NETHER ? "nether" : "end";
        Database.WorldPauseStatus status = state.getState(player.getWorld(), environmentTo);
        if (status.enabled()) return;
        if (state.canBypass(player, player.getWorld(), environmentTo)) return;

        event.setCancelled(true);

        // Apply the corresponding "bounce-back" effect depending on the environment (Nether portals are vertical, End Portals are horizontal, so they require different math)
        if (configManager.getPluginConfig().getBoolean("dimensions." + stringifiedEnv + ".bounce-back")) {
            switch (environmentTo) {
                case NETHER -> netherBounceback(player, event.getFrom());
                case THE_END -> endBounceback(player);
                default -> {
                    // Do nothing because we don't care
                }
            }
        }

        boolean sendTitle = configManager.getPluginConfig().getBoolean("dimensions." + stringifiedEnv + ".alert.title");
        boolean sendChat = configManager.getPluginConfig().getBoolean("dimensions." + stringifiedEnv + ".alert.chat");

        if (sendTitle) {
            player.showTitle(commandFeedback.getTitleForDimension(environmentTo));
        }

        if (sendChat) {
            player.sendMessage(commandFeedback.getDimensionIsPausedMessage(environmentTo));
        }

    }

    private void netherBounceback(Player player, Location portalLocation) {
        // Ensure this event is not already being handled
        if (playersBeingHandled.contains(player.getUniqueId())) {
            return;
        }
        playersBeingHandled.add(player.getUniqueId());

        // Send the player back a bit to emphasize the pause
        // Modified from https://github.com/Multiverse/Multiverse-NetherPortals/blob/7a46c67f0a06064fe7f0e4f7b99aa00afc0c5e25/src/main/java/com/onarandombox/MultiverseNetherPortals/listeners/MVNPEntityListener.java#L77-L127
        double newVecX;
        double newVecZ;
        double strength = 1.3;

        Block block = portalLocation.getBlock();

        // determine portal orientation by checking if the block to the west/east is also a nether portal block
        if (block.getRelative(BlockFace.WEST).getType() == Material.NETHER_PORTAL || block.getRelative(BlockFace.EAST).getType() == Material.NETHER_PORTAL) {
            newVecX = 0;
            // we add 0.5 to the location of the block to get the center
            if (player.getLocation().getZ() < block.getLocation().getZ() + 0.5) {
                // Entered from the North
                newVecZ = -1 * strength;
            } else {
                // Entered from the South
                newVecZ = 1 * strength;
            }
        } else {
            newVecZ = 0;
            // we add 0.5 to the location of the block to get the center
            if (player.getLocation().getX() < block.getLocation().getX() + 0.5) {
                // Entered from the West
                newVecX = -1 * strength;
            } else {
                // Entered from the East
                newVecX = 1 * strength;
            }
        }

        player.setVelocity(new Vector(newVecX, .5, newVecZ));

        // Delay the velocity and removal of the player from the set
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 5, false, false));
            playersBeingHandled.remove(player.getUniqueId());
        }, 1L); // 1 tick or 1/20 of a second
    }

    private void endBounceback(Player player) {
        // Ensure this event is not already being handled
        if (playersBeingHandled.contains(player.getUniqueId())) {
            return;
        }

        playersBeingHandled.add(player.getUniqueId());

        float yaw = player.getLocation().getYaw();

        double radians = Math.toRadians(yaw);

        double x = Math.sin(radians);
        double z = -Math.cos(radians);

        Vector knockbackDirection = new Vector(x, 0.7, z);

        knockbackDirection.multiply(0.9);
        player.setVelocity(knockbackDirection);

        // Delay the velocity and removal of the player from the set
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 5, false, false));
            playersBeingHandled.remove(player.getUniqueId());
        }, 5L); // 1 tick or 1/20 of a second
    }


    // Small event listener to handle cases such as fall damage
    @EventHandler
    public void onPlayerDamageEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;

        if (playersBeingHandled.contains(p.getUniqueId())) event.setCancelled(true);
    }

}
