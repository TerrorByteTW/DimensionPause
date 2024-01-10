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
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;
import org.reprogle.dimensionpause.ConfigManager;
import org.reprogle.dimensionpause.DimensionPausePlugin;
import org.reprogle.dimensionpause.commands.CommandFeedback;

public class PortalCreateEventListener implements Listener {

	PortalCreateEventListener() {
		// Private constructor to hide the implicit public one
	}

	/**
	 * This event attempts to cancel portal creation events. However,
	 * 
	 * @param event
	 */
	@EventHandler()
	public static void onPortalCreateEvent(PortalCreateEvent event) {
		// We only want to disable the portal creation if a player lights it
		if (!(event.getEntity() instanceof Player p))
			return;

		// We obviously don't want to disable portal create events *in* the Nether
		if (p.getWorld().getEnvironment().equals(World.Environment.NETHER))
			return;

		// It is assumed that the nether world will be named "_nether". HOWEVER, this
		// will not always be the case.
		// Because of how Spigot works, it is possible to generate a Nether world with a
		// different name. This method will not always work, but that's okay because we
		// block attempts to teleport into nether worlds anyway
		World world = Bukkit.getWorld(p.getWorld() + "_nether");
		if (world == null)
			return;

		// Check if the Nether is disabled at all or, if it is enabled, whether the
		// world is allowed or not anyway
		if (!DimensionPausePlugin.ds.getState(World.Environment.NETHER)
				|| DimensionPausePlugin.ds.isAllowedWorld(world))
			return;

		// Check if the nether is bypassable
		boolean bypassable = ConfigManager.getPluginConfig().getBoolean("dimensions.nether.bypassable");
		if (DimensionPausePlugin.ds.canBypass(p, bypassable))
			return;

		// We only want to check create reason of FIRE, because the other two,
		// END_PLATFORM, and NETHER_PAIR, should never be cancelled
		if (event.getReason().equals(PortalCreateEvent.CreateReason.FIRE)) {

			// Block portal creation
			event.setCancelled(true);

			// Send the player the Nether title and chat messages, if configured
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
}
