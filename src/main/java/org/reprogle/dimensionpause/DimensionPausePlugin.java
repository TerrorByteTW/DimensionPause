package org.reprogle.dimensionpause;

import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;
import org.reprogle.dimensionpause.commands.CommandManager;
import org.reprogle.dimensionpause.events.ListenerManager;

public final class DimensionPausePlugin extends JavaPlugin {
    public static DimensionPausePlugin plugin;

    public static DimensionState ds = null;

    @Override
    public void onEnable() {
        plugin = this;
        ConfigManager.setupConfig(this);
        Metrics metrics = new Metrics(this, 19032);

        getCommand("dimensionpause").setExecutor(new CommandManager());
        ListenerManager.setupListeners(this);

        ds = new DimensionState(this);
        getLogger().info("Dimension Pause has been loaded");
    }

    @Override
    public void onDisable() {
        getLogger().info("Dimension Pause is shutting down");
    }
}
