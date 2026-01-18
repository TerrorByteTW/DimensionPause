package org.reprogle.dimensionpause.events;

import com.google.inject.Inject;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.reprogle.dimensionpause.ConfigManager;
import org.reprogle.dimensionpause.DimensionPausePlugin;
import org.reprogle.dimensionpause.DimensionState;
import org.reprogle.dimensionpause.commands.CommandFeedback;

public class PlayerInteractEventListener implements Listener {
    @Inject
    DimensionState state;
    @Inject
    ConfigManager configManager;
    @Inject
    CommandFeedback commandFeedback;

    @EventHandler()
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && event.getClickedBlock().getType().equals(Material.END_PORTAL_FRAME)) {
            World world = event.getPlayer().getWorld();
            if (state.getState(world, World.Environment.THE_END).enabled()) return;

            boolean bypassable = configManager.getPluginConfig().getBoolean("dimensions.end.bypassable");

            if (state.canBypass(event.getPlayer(), bypassable)) return;
            event.setCancelled(true);
            Player p = event.getPlayer();

            boolean sendTitle = configManager.getPluginConfig().getBoolean("dimensions.end.alert.title.enabled");
            boolean sendChat = configManager.getPluginConfig().getBoolean("dimensions.end.alert.chat.enabled");

            if (sendTitle) {
                p.showTitle(commandFeedback.getTitleForDimension(World.Environment.THE_END));
            }

            if (sendChat) {
                p.sendMessage(commandFeedback.getChatForDimension(World.Environment.THE_END));
            }
        }
    }
}
