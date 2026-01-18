package org.reprogle.dimensionpause.events;

import com.google.inject.Inject;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.reprogle.dimensionpause.DimensionState;

public class PlayerJoinEventListener implements Listener {
    @Inject
    DimensionState state;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (!state.alertPlayers.contains(player.getUniqueId())) {
            return;
        }
        
        World world = player.getWorld();
        
        state.alertPlayer(player, world.getEnvironment());
        state.alertPlayers.remove(player.getUniqueId());
    }

}
