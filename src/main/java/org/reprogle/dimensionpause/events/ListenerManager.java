package org.reprogle.dimensionpause.events;

import org.bukkit.plugin.Plugin;

public class ListenerManager {

	/**
	 * Set's up all the listeners in the entire plugin
	 *
	 * @param plugin The Honeypot plugin instance
	 */
	public static void setupListeners(Plugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(new PlayerTeleportEventListener(), plugin);
		plugin.getServer().getPluginManager().registerEvents(new PlayerInteractEventListener(), plugin);
		plugin.getServer().getPluginManager().registerEvents(new PortalCreateEventListener(), plugin);
		plugin.getServer().getPluginManager().registerEvents(new EntityPortalEnterEventListener(), plugin);
	}

}
