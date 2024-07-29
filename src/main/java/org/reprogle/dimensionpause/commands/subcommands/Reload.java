package org.reprogle.dimensionpause.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.reprogle.dimensionpause.ConfigManager;
import org.reprogle.dimensionpause.commands.CommandFeedback;
import org.reprogle.dimensionpause.commands.SubCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Reload implements SubCommand {
	@Override
	public String getName() {
		return "reload";
	}

	@Override
	public void perform(CommandSender sender, String[] args) {
		try {
		ConfigManager.getPluginConfig().reload();
		ConfigManager.getPluginConfig().save();

		ConfigManager.getLanguageFile().reload();
		ConfigManager.getLanguageFile().save();

		sender.sendMessage(CommandFeedback.sendCommandFeedback("reload"));

		} catch (IOException e) {
			// Nothing
		}
	}

	@Override
	public List<String> getSubcommands(CommandSender sender, String[] args) {
		return null;
	}

	@Override
	public List<String> getRequiredPermissions() {
		List<String> permissions = new ArrayList<>();
		permissions.add("dimensionpause.reload");
		return permissions;
	}
}
