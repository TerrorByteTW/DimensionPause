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

package org.reprogle.dimensionpause.events;

import org.bukkit.Bukkit;
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

	PlayerInteractEventListener() {
		// Private constructor to hide the implicit public one
	}

	@EventHandler()
	public static void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null
				&& event.getClickedBlock().getType().equals(Material.END_PORTAL_FRAME)) {

			World world = Bukkit.getWorld(event.getPlayer().getWorld() + "_the_end");
			if (world == null)
				return;

			if (!DimensionPausePlugin.ds.getState(World.Environment.THE_END)
					|| DimensionPausePlugin.ds.isAllowedWorld(world))
				return;

			boolean bypassable = ConfigManager.getPluginConfig().getBoolean("dimensions.end.bypassable");

			if (DimensionPausePlugin.ds.canBypass(event.getPlayer(), bypassable))
				return;

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
