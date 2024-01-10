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
import org.reprogle.dimensionpause.commands.CommandFeedback;
import org.reprogle.dimensionpause.commands.SubCommand;

import java.util.ArrayList;
import java.util.List;

public class State implements SubCommand {
	@Override
	public String getName() {
		return "state";
	}

	@Override
	public void perform(Player p, String[] args) {
		if (args.length >= 2) {
			if (args[1].equalsIgnoreCase("nether") || args[1].equalsIgnoreCase("end")) {
				p.sendMessage(CommandFeedback.sendCommandFeedback("state", args[1].toLowerCase()));
			} else {
				p.sendMessage(CommandFeedback.sendCommandFeedback("usage"));
			}
		} else {
			p.sendMessage(CommandFeedback.sendCommandFeedback("usage"));
		}
	}

	@Override
	public List<String> getSubcommands(Player p, String[] args) {
		List<String> subcommands = new ArrayList<>();

		// We are already in argument 1 of the command, hence why this is a subcommand
		// class. Argument 2 is the
		// subcommand for the subcommand,
		// aka /dimensionpause state <THIS ONE>

		if (args.length == 2) {
			subcommands.add("nether");
			subcommands.add("end");
		}
		return subcommands;
	}

	@Override
	public List<String> getRequiredPermissions() {
		List<String> permissions = new ArrayList<>();
		permissions.add("dimensionpause.state");
		return permissions;
	}
}
