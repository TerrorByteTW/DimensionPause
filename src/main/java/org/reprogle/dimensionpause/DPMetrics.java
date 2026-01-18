package org.reprogle.dimensionpause;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class DPMetrics {
    DPMetrics(JavaPlugin plugin) {
        Metrics metrics = new Metrics(plugin, 19032);
//        metrics.addCustomChart(new AdvancedPie("dimensions_disabled", () -> {
//            boolean netherEnabled = DimensionPausePlugin.ds.getState(World.Environment.NETHER);
//            boolean endEnabled = DimensionPausePlugin.ds.getState(World.Environment.THE_END);
//
//            Map<String, Integer> valueMap = new HashMap<>();
//            valueMap.put("The End", endEnabled ? 1 : 0);
//            valueMap.put("Nether", netherEnabled ? 1 : 0);
//            return valueMap;
//        }));
    }
}
