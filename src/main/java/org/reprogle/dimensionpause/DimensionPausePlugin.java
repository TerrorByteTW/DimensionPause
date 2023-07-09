package org.reprogle.dimensionpause;

import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;

public final class DimensionPausePlugin extends JavaPlugin {
    public static DimensionPausePlugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        ConfigManager.setupConfig(this);
        Metrics metrics = new Metrics(this, 19032);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
