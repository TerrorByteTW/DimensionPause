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

package org.reprogle.dimensionpause.commands;

import org.bukkit.entity.Player;

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
	 * @param p    The Player running the command
	 * @param args Any arguments to pass
	 */
	void perform(Player p, String[] args);

	/**
	 * Gets all subcommands of the main command if any (Such as with the create or
	 * remove command)
	 *
	 * @param p    The Player running the command
	 * @param args Any arguments to pass
	 * @return A list of all subcommands as strings
	 */
	List<String> getSubcommands(Player p, String[] args);

	/**
	 * Gets the required permissions to run the command. May be multiple
	 *
	 * @return A list of all subcommands as strings
	 */
	List<String> getRequiredPermissions();
}
