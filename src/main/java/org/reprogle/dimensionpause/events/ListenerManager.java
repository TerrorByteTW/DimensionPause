package org.reprogle.dimensionpause.events;

import com.google.inject.Inject;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.reprogle.dimensionpause.ConfigManager;
import org.reprogle.dimensionpause.DimensionPausePlugin;

import java.util.ArrayList;
import java.util.List;

public class ListenerManager {

    private final DimensionPausePlugin plugin;

    @Inject
    PlayerSpawnLocationEventListener playerSpawnLocationEventListener;
    @Inject
    PlayerJoinEventListener playerJoinEventListener;
    @Inject
    PlayerTeleportEventListener playerTeleportEventListener;
    @Inject
    PlayerInteractEventListener playerInteractEventListener;
    @Inject
    PortalCreateEventListener portalCreateEventListener;
    @Inject
    EntityPortalEnterEventListener entityPortalEnterEventListener;

    @Inject
    ListenerManager(DimensionPausePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Set's up all the listeners in the entire plugin
     */
    public void setupListeners() {
        final List<Listener> listeners = new ArrayList<>(List.of(playerSpawnLocationEventListener,
                playerJoinEventListener, playerTeleportEventListener, playerInteractEventListener,
                portalCreateEventListener, entityPortalEnterEventListener));
        PluginManager pm = plugin.getServer().getPluginManager();
        listeners.forEach(event -> pm.registerEvents(event, plugin));
    }

}
