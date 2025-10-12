package org.reprogle.dimensionpause.commands.subcommands;

import org.bukkit.command.CommandSender;
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
	public void perform(CommandSender sender, String[] args) {
		if (args.length >= 2) {
			switch (args[1].toLowerCase()) {
				case "nether", "end" -> sender.sendMessage(CommandFeedback.sendCommandFeedback("state", args[1].toLowerCase()));
				default -> sender.sendMessage(CommandFeedback.sendCommandFeedback("usage"));
			}
		} else {
			sender.sendMessage(CommandFeedback.sendCommandFeedback("usage"));
		}
	}

	@Override
	public List<String> getSubcommands(CommandSender sender, String[] args) {
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
