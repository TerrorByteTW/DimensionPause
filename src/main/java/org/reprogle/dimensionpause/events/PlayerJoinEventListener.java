package org.reprogle.dimensionpause.events;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.reprogle.dimensionpause.DimensionPausePlugin;
import org.reprogle.dimensionpause.DimensionState;

public class PlayerJoinEventListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (!DimensionState.alertPlayers.contains(player.getUniqueId())) {
            return;
        }
        
        World world = player.getWorld();
        
        DimensionPausePlugin.ds.alertPlayer(player, world.getEnvironment());
        DimensionState.alertPlayers.remove(player.getUniqueId());
    }

}
