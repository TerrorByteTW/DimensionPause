package org.reprogle.dimensionpause.events;

import com.google.inject.Inject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.reprogle.dimensionpause.utils.DimensionExpirationTimer;

public class WorldLoadEvent implements Listener {
    @Inject
    DimensionExpirationTimer timer;

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        timer.refresh();
    }
}
