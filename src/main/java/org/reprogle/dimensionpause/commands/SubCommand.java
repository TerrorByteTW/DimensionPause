package org.reprogle.dimensionpause.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand {

	/**
	 * Gets the name of the command
	 *
	 * @return The String name
	 */
	String getName();

	/**
	 * Performs the command
	 *
	 * @param sender The CommandSender running the command
	 * @param args   Any arguments to pass
	 */
	void perform(CommandSender sender, String[] args);

	/**
	 * Gets all subcommands of the main command if any (Such as with the create or
	 * remove command)
	 *
	 * @param sender The CommandSender running the command
	 * @param args   Any arguments to pass
	 * @return A list of all subcommands as strings
	 */
	List<String> getSubcommands(CommandSender sender, String[] args);

	/**
	 * Gets the required permissions to run the command. May be multiple
	 *
	 * @return A list of all subcommands as strings
	 */
	List<String> getRequiredPermissions();
}
