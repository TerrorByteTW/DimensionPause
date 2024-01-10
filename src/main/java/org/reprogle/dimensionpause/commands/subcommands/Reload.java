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

package org.reprogle.dimensionpause.commands.subcommands;

import org.bukkit.entity.Player;
import org.reprogle.dimensionpause.ConfigManager;
import org.reprogle.dimensionpause.commands.CommandFeedback;
import org.reprogle.dimensionpause.commands.SubCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Reload implements SubCommand {
	@Override
	public String getName() {
		return "reload";
	}

	@Override
	public void perform(Player p, String[] args) {
		try {
			ConfigManager.getPluginConfig().reload();
			ConfigManager.getPluginConfig().save();

			ConfigManager.getLanguageFile().reload();
			ConfigManager.getLanguageFile().save();

			p.sendMessage(CommandFeedback.sendCommandFeedback("reload"));

		} catch (IOException e) {
			// Nothing
		}
	}

	@Override
	public List<String> getSubcommands(Player p, String[] args) {
		return Collections.emptyList();
	}

	@Override
	public List<String> getRequiredPermissions() {
		List<String> permissions = new ArrayList<>();
		permissions.add("dimensionpause.reload");
		return permissions;
	}
}
