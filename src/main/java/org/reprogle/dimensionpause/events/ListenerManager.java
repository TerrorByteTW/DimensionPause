package org.reprogle.dimensionpause.events;

import com.google.inject.Inject;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ListenerManager {

    private final JavaPlugin plugin;

    @Inject
    PlayerJoinEventListener playerJoinEventListener;
    @Inject
    PlayerTeleportEventListener playerTeleportEventListener;
    @Inject
    PlayerInteractEventListener playerInteractEventListener;
    @Inject
    PortalCreateEventListener portalCreateEventListener;
    @Inject
    PlayerPortalEventListener playerPortalEventListener;
    @Inject
    WorldLoadEventListener worldLoadEventListener;

    @Inject
    ListenerManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Set's up all the listeners in the entire plugin
     */
    public void setupListeners() {
        PluginManager pm = plugin.getServer().getPluginManager();
        final List<Listener> listeners = new ArrayList<>(List.of(
            playerJoinEventListener,
            playerTeleportEventListener,
            playerInteractEventListener,
            portalCreateEventListener,
            playerPortalEventListener,
            worldLoadEventListener));
        listeners.forEach(event -> pm.registerEvents(event, plugin));
    }

}
