package org.reprogle.dimensionpause;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.dimensionpause.events.ListenerManager;
import org.reprogle.dimensionpause.store.TrackedWorldsRepository;
import org.reprogle.dimensionpause.utils.DimensionExpirationTimer;
import org.reprogle.bytelib.boot.lifecycle.PluginLifecycle;

public class DimensionPauseLifecycle implements PluginLifecycle {
    private final JavaPlugin plugin;
    private final ComponentLogger logger;

    private final ListenerManager listenerManager;
    private final DimensionExpirationTimer timer;
    private final TrackedWorldsRepository trackedWorldsRepository;

    @Inject
    public DimensionPauseLifecycle(
            JavaPlugin plugin,
            ComponentLogger logger,
            ListenerManager listenerManager,
            DimensionExpirationTimer timer,
            TrackedWorldsRepository trackedWorldsRepository
    ) {
        this.plugin = plugin;
        this.logger = logger;
        this.listenerManager = listenerManager;
        this.timer = timer;
        this.trackedWorldsRepository = trackedWorldsRepository;
    }

    @Override
    public void onEnable() {
        new DPMetrics(plugin);

        trackedWorldsRepository.createSchema();

        listenerManager.setupListeners();

        logger.info("Dimension Pause has been loaded");

        if (plugin.getDescription().getVersion().contains("SNAPSHOT")) {
            plugin.getServer().getConsoleSender().sendMessage(
                    Component.text("You are running a SNAPSHOT version of DimensionPause. Support will not be provided!", NamedTextColor.RED)
            );
        } else {
            new UpdateChecker(plugin, "https://raw.githubusercontent.com/TerrorByteTW/DimensionPause/master/version.txt")
                    .getVersion(latest -> {
                        if (Integer.parseInt(latest.replace(".", "")) >
                                Integer.parseInt(plugin.getDescription().getVersion().replace(".", ""))) {
                            plugin.getServer().getConsoleSender().sendMessage(
                                    Component.text("There is a new update available for DimensionPause: " + latest +
                                            ". Please download for the latest features and security updates!", NamedTextColor.RED)
                            );
                        } else {
                            plugin.getServer().getConsoleSender().sendMessage(
                                    Component.text("You are on the latest version of DimensionPause!", NamedTextColor.GREEN)
                            );
                        }
                    });
        }

        if (isFolia()) {
            plugin.getServer().getConsoleSender().sendMessage(
                    Component.text("Welcome to Folia!!!! It is assumed you know what you're doing, since Folia is not yet standard. While DimensionPause can run on Folia, it is not yet officially endorsed by the developer, and is also not actively tested. Be wary when using it for now, and report any bugs in Honeypot caused by Folia to the developer!")
            );
        }

        timer.refresh();
    }

    @Override
    public void onDisable() {
        logger.info("Dimension Pause is shutting down");
    }

    private boolean isFolia() {
        return Bukkit.getServer().getName().startsWith("Folia");
    }
}
