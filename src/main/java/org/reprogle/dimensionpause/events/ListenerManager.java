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

import org.bukkit.plugin.Plugin;

public class ListenerManager {

	private ListenerManager() {
		// Private constructor to hide the implicit public one
	}

	/**
	 * Set's up all the listeners in the entire plugin
	 *
	 * @param plugin The Honeypot plugin instance
	 */
	public static void setupListeners(Plugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(new PlayerChangedWorldEventListener(), plugin);
		plugin.getServer().getPluginManager().registerEvents(new PlayerInteractEventListener(), plugin);
		plugin.getServer().getPluginManager().registerEvents(new PortalCreateEventListener(), plugin);
	}

}
