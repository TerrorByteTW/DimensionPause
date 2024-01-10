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

package org.reprogle.dimensionpause;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.dimensionpause.commands.CommandFeedback;
import org.reprogle.dimensionpause.commands.CommandManager;
import org.reprogle.dimensionpause.events.ListenerManager;

// Shut up SonarLint
@SuppressWarnings({ "java:S1104", "java:S1119", "java:S2696", "java:S1444" })
public final class DimensionPausePlugin extends JavaPlugin {
	public static DimensionPausePlugin plugin;

	public static DimensionState ds = null;

	@Override
	public void onEnable() {
		plugin = this;
		ConfigManager.setupConfig(this);
		new Metrics(this, 19032);

		getCommand("dimensionpause").setExecutor(new CommandManager());
		ListenerManager.setupListeners(this);

		ds = new DimensionState(this);
		getLogger().info("Dimension Pause has been loaded");

		new UpdateChecker(this, "https://raw.githubusercontent.com/TerrorByteTW/DimensionPause/master/version.txt")
				.getVersion(latest -> {
					// noinspection UnstableApiUsage
					if (Integer.parseInt(latest.replace(".", "")) > Integer
							.parseInt(this.getPluginMeta().getVersion().replace(".", ""))) {
						Component updateMessage = Component.text()
								.append(CommandFeedback.getChatPrefix())
								.append(Component.text(" "))
								.append(Component.text(
										"There is a new update available: " + latest
												+ ". Please download for the latest features and security updates!",
										NamedTextColor.RED))
								.build();

						getServer().getConsoleSender().sendMessage(updateMessage);
					} else {
						Component noUpdateMessage = Component.text()
								.append(CommandFeedback.getChatPrefix())
								.append(Component.text(" "))
								.append(Component.text("You are on the latest version of DimensionPause!",
										NamedTextColor.GREEN))
								.build();

						getServer().getConsoleSender().sendMessage(noUpdateMessage);
					}
				});
	}

	@Override
	public void onDisable() {
		getLogger().info("Dimension Pause is shutting down");
	}
}
