package org.reprogle.dimensionpause.events;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.util.Vector;
import org.reprogle.dimensionpause.ConfigManager;
import org.reprogle.dimensionpause.DimensionPausePlugin;
import org.reprogle.dimensionpause.commands.CommandFeedback;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EntityPortalEnterEventListener implements Listener {

    private final Set<UUID> playersBeingHandled = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityPortalEnter(EntityPortalEnterEvent event) {
        // Check if the event is a player, if nether bounce-back option is enabled, and if the nether is currently paused
        if (!(event.getEntity() instanceof Player) || !ConfigManager.getPluginConfig().getBoolean("dimensions.nether.bounce-back") || !DimensionPausePlugin.ds.getState(World.Environment.NETHER)) {
            return;
        }

        Player p = (Player) event.getEntity();
        Location currentLocation = event.getLocation().set(event.getLocation().getBlockX(), event.getLocation().getBlockY(), event.getLocation().getBlockZ());

        if (currentLocation.getBlock().getType() != Material.NETHER_PORTAL) {
            return;
        }

        // If the player can bypass the environment, quit processing
        if (DimensionPausePlugin.ds.canBypass(p, ConfigManager.getPluginConfig().getBoolean("dimensions.nether.bypassable"))) {
            return;
        }

        // Ensure this event is not already being handled
        if (playersBeingHandled.contains(p.getUniqueId())) {
            return;
        }
        playersBeingHandled.add(p.getUniqueId());

        // Send the player back a bit to emphasize the pause
        // Modified from https://github.com/Multiverse/Multiverse-NetherPortals/blob/7a46c67f0a06064fe7f0e4f7b99aa00afc0c5e25/src/main/java/com/onarandombox/MultiverseNetherPortals/listeners/MVNPEntityListener.java#L77-L127
        double newVecX;
        double newVecZ;
        double strength = 1;

        Block block = currentLocation.getBlock();
        // determine portal orientation by checking if the block to the west/east is also a nether portal block
        if (block.getRelative(BlockFace.WEST).getType() == Material.NETHER_PORTAL || block.getRelative(BlockFace.EAST).getType() == Material.NETHER_PORTAL) {
            newVecX = 0;
            // we add 0.5 to the location of the block to get the center
            if (p.getLocation().getZ() < block.getLocation().getZ() + 0.5) {
                // Entered from the North
                newVecZ = -1 * strength;
            } else {
                // Entered from the South
                newVecZ = 1 * strength;
            }
        } else {
            newVecZ = 0;
            // we add 0.5 to the location of the block to get the center
            if (p.getLocation().getX() < block.getLocation().getX() + 0.5) {
                // Entered from the West
                newVecX = -1 * strength;
            } else {
                // Entered from the East
                newVecX = 1 * strength;
            }
        }

        p.teleport(p.getLocation().clone().add(newVecX, .2, newVecZ));

        // Delay the velocity and removal of the player from the set
        DimensionPausePlugin.plugin.getServer().getScheduler().runTaskLater(DimensionPausePlugin.plugin, () -> {
            p.setVelocity(new Vector(newVecX, .6, newVecZ));
            playersBeingHandled.remove(p.getUniqueId());
        }, 1L); // 1 tick or 1/20 of a second

        boolean sendTitle = ConfigManager.getPluginConfig().getBoolean("dimensions.nether.alert.title.enabled");
        boolean sendChat = ConfigManager.getPluginConfig().getBoolean("dimensions.nether.alert.chat.enabled");

        if (sendTitle) {
            p.showTitle(CommandFeedback.getTitleForDimension(World.Environment.NETHER));
        }

        if (sendChat) {
            p.sendMessage(CommandFeedback.getChatForDimension(World.Environment.NETHER));
        }
    }

}
