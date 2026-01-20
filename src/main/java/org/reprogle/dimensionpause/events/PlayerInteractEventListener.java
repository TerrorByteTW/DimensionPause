package org.reprogle.dimensionpause.events;

import com.google.inject.Inject;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.dimensionpause.utils.DimensionState;
import org.reprogle.dimensionpause.commands.CommandFeedback;

public class PlayerInteractEventListener implements Listener {
    @Inject
    DimensionState state;
    @Inject
    BytePluginConfig config;
    @Inject
    CommandFeedback commandFeedback;

    @EventHandler()
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (!event.getClickedBlock().getType().equals(Material.END_PORTAL_FRAME)) return;
        if (event.getMaterial() != Material.ENDER_EYE) return;

        World world = event.getPlayer().getWorld();
        if (state.getState(world, World.Environment.THE_END).enabled()) return;

        if (state.canBypass(event.getPlayer(), world, World.Environment.THE_END)) return;
        event.setCancelled(true);
        Player p = event.getPlayer();

        boolean sendTitle = config.config().getBoolean("dimensions.end.alert.title");
        boolean sendChat = config.config().getBoolean("dimensions.end.alert.chat");

        if (sendTitle) {
            p.showTitle(commandFeedback.getTitleForDimension(World.Environment.THE_END));
        }

        if (sendChat) {
            p.sendMessage(commandFeedback.getDimensionIsPausedMessage(World.Environment.THE_END));
        }
    }
}
