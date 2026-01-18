package org.reprogle.dimensionpause.commands.subcommands;

import com.google.inject.Inject;
import org.bukkit.command.CommandSender;
import org.reprogle.dimensionpause.ConfigManager;
import org.reprogle.dimensionpause.commands.CommandFeedback;
import org.reprogle.dimensionpause.commands.SubCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Reload implements SubCommand {
	@Inject
	ConfigManager configManager;
	@Inject
	CommandFeedback commandFeedback;

	@Override
	public String getName() {
		return "reload";
	}

	@Override
	public void perform(CommandSender sender, String[] args) {
		try {
		configManager.getPluginConfig().reload();
		configManager.getPluginConfig().save();

		configManager.getLanguageFile().reload();
		configManager.getLanguageFile().save();

		sender.sendMessage(commandFeedback.sendCommandFeedback("reload", null, null));

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
