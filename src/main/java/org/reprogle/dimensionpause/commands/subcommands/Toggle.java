package org.reprogle.dimensionpause.commands.subcommands;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.reprogle.dimensionpause.DimensionPausePlugin;
import org.reprogle.dimensionpause.commands.CommandFeedback;
import org.reprogle.dimensionpause.commands.SubCommand;

import java.util.ArrayList;
import java.util.List;

public class Toggle implements SubCommand {
	@Override
	public String getName() {
		return "toggle";
	}

	@Override
	public void perform(Player p, String[] args) {
		if (args.length >= 2) {
			switch (args[1].toLowerCase()) {
				case "nether" -> DimensionPausePlugin.ds.toggleDimension(World.Environment.NETHER);
				case "end" -> DimensionPausePlugin.ds.toggleDimension(World.Environment.THE_END);
				default -> p.sendMessage(CommandFeedback.sendCommandFeedback("usage"));
			}
			p.sendMessage(CommandFeedback.sendCommandFeedback("newstate", args[1].toLowerCase()));
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
		// aka /dimensionpause bypass <THIS ONE>

		if (args.length == 2) {
			subcommands.add("nether");
			subcommands.add("end");
		}
		return subcommands;
	}

	@Override
	public List<String> getRequiredPermissions() {
		List<String> permissions = new ArrayList<>();
		permissions.add("dimensionpause.toggle");
		return permissions;
	}
}
