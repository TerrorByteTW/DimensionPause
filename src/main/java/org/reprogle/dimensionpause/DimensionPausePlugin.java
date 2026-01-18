package org.reprogle.dimensionpause;

import com.google.inject.Inject;
import com.google.inject.Injector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.dimensionpause.commands.CommandFeedback;
import org.reprogle.dimensionpause.commands.CommandManager;
import org.reprogle.dimensionpause.events.ListenerManager;

public final class DimensionPausePlugin extends JavaPlugin {

    @Inject
    private CommandFeedback commandFeedback;
    @Inject
    private ListenerManager listenerManager;
    @Inject
    private CommandManager commandManager;

    private Injector injector;

    @Override
    public void onLoad() {
        ConfigManager configManager = new ConfigManager();
        DimensionPauseModule module = new DimensionPauseModule(this, configManager);
        injector = module.createInjector();
        injector.injectMembers(this);
    }

    @Override
    public void onEnable() {
        new DPMetrics(this);

        getCommand("dimensionpause").setExecutor(this.commandManager);
        listenerManager.setupListeners();

        getLogger().info("Dimension Pause has been loaded");

        if (this.getDescription().getVersion().contains("SNAPSHOT")) {
            Component updateMessage = Component.text()
                    .append(commandFeedback.getChatPrefix())
                    .append(Component.text(" "))
                    .append(Component.text("You are running a SNAPSHOT version of DimensionPause. Support will not be provided!", NamedTextColor.RED))
                    .build();

            getServer().getConsoleSender().sendMessage(updateMessage);
        } else {
            new UpdateChecker(this, "https://raw.githubusercontent.com/TerrorByteTW/DimensionPause/master/version.txt").getVersion(latest -> {
                if (Integer.parseInt(latest.replace(".", "")) > Integer.parseInt(this.getDescription().getVersion().replace(".", ""))) {
                    Component updateMessage = Component.text()
                            .append(commandFeedback.getChatPrefix())
                            .append(Component.text(" "))
                            .append(Component.text("There is a new update available: " + latest + ". Please download for the latest features and security updates!", NamedTextColor.RED))
                            .build();

                    getServer().getConsoleSender().sendMessage(updateMessage);
                } else {
                    Component noUpdateMessage = Component.text()
                            .append(commandFeedback.getChatPrefix())
                            .append(Component.text(" "))
                            .append(Component.text("You are on the latest version of DimensionPause!", NamedTextColor.GREEN))
                            .build();

                    getServer().getConsoleSender().sendMessage(noUpdateMessage);
                }
            });
        }

        if (isFolia()) {
            getServer().getConsoleSender().sendMessage(
                    Component.text("Welcome to Folia!!!! It is assumed you know what you're doing, since Folia is not yet standard. While DimensionPause can run on Folia, it is not yet officially endorsed by the developer, and is also not actively tested. Be wary when using it for now, and report any bugs in Honeypot caused by Folia to the developer!"));
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Dimension Pause is shutting down");
    }

    public Injector getInjector() {
        return injector;
    }

    private boolean isFolia() {
        return Bukkit.getServer().getName().startsWith("Folia");
    }
}
