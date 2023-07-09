package org.reprogle.dimensionpause.events;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.reprogle.dimensionpause.ConfigManager;
import org.reprogle.dimensionpause.DimensionPausePlugin;
import org.reprogle.dimensionpause.commands.CommandFeedback;

public class PlayerInteractEventListener implements Listener {

	@EventHandler()
	public static void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && event.getClickedBlock().getType().equals(Material.END_PORTAL_FRAME)) {
			if(!DimensionPausePlugin.ds.getState(World.Environment.THE_END)) return;

			boolean bypassable = ConfigManager.getPluginConfig().getBoolean("dimensions.end.bypassable");

			if (DimensionPausePlugin.ds.getState(World.Environment.THE_END)) {
				if (DimensionPausePlugin.ds.canBypass(event.getPlayer(), bypassable)) return;
				event.setCancelled(true);
				Player p = event.getPlayer();

				boolean sendTitle = ConfigManager.getPluginConfig().getBoolean("dimensions.end.alert.title.enabled");
				boolean sendChat = ConfigManager.getPluginConfig().getBoolean("dimensions.end.alert.chat.enabled");

				if (sendTitle) {
					p.showTitle(CommandFeedback.getTitleForDimension(World.Environment.THE_END));
				}

				if (sendChat) {
					p.sendMessage(CommandFeedback.getChatForDimension(World.Environment.THE_END));
				}
			}
		}
	}
}
