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

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.reprogle.dimensionpause.ConfigManager;
import org.reprogle.dimensionpause.DimensionPausePlugin;
import org.reprogle.dimensionpause.commands.CommandFeedback;

public class PlayerChangedWorldEventListener implements Listener {

	PlayerChangedWorldEventListener() {
		// Private constructor to hide the implicit public one
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public static void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
		// Grab the environment and the player. If the player is teleporting to the
		// overworld, ignore it
		World.Environment env = event.getPlayer().getWorld().getEnvironment();
		Player p = event.getPlayer();
		if (env.equals(World.Environment.NORMAL) || DimensionPausePlugin.ds.isAllowedWorld(p.getWorld()))
			return;

		// Grab the bypassable values for the nether and end.
		boolean netherBypass = ConfigManager.getPluginConfig().getBoolean("dimensions.nether.bypassable");
		boolean endBypass = ConfigManager.getPluginConfig().getBoolean("dimensions.end.bypassable");

		// If the environment the player is teleporting to is disabled, do the following
		if (DimensionPausePlugin.ds.getState(env)) {

			// If the player can bypass the environment, quit processing
			if (DimensionPausePlugin.ds.canBypass(p, env.equals(World.Environment.NETHER) ? netherBypass : endBypass))
				return;

			if (!DimensionPausePlugin.ds.kickToWorld(p, env, true))
				// If we are unable to do any of the fallback, force them back to the world they
				// came from
				p.teleport(event.getFrom().getSpawnLocation());

			// Send the player the proper title for the environment they tried to access
			String environment = env.equals(World.Environment.NETHER) ? "nether" : "end";
			boolean sendTitle = ConfigManager.getPluginConfig()
					.getBoolean("dimensions." + environment + ".alert.title.enabled");
			boolean sendChat = ConfigManager.getPluginConfig()
					.getBoolean("dimensions." + environment + ".alert.chat.enabled");

			if (sendTitle) {
				p.showTitle(CommandFeedback.getTitleForDimension(env));
			}

			if (sendChat) {
				p.sendMessage(CommandFeedback.getChatForDimension(env));
			}
		}
	}

}