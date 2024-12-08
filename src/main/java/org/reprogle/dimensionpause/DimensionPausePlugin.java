package org.reprogle.dimensionpause;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.reprogle.dimensionpause.commands.CommandFeedback;
import org.reprogle.dimensionpause.commands.CommandManager;
import org.reprogle.dimensionpause.events.ListenerManager;

public final class DimensionPausePlugin extends JavaPlugin {
	public static DimensionPausePlugin plugin;

	public static DimensionState ds = null;

	@Override
	public void onEnable() {
		plugin = this;
		ConfigManager.setupConfig(this);
		new Metrics(this, 19032);

		CommandManager manager = new CommandManager();

		getCommand("dimensionpause").setExecutor(manager);
		ListenerManager.setupListeners(this);

		ds = new DimensionState(this);
		getLogger().info("Dimension Pause has been loaded");

		new UpdateChecker(this, "https://raw.githubusercontent.com/TerrorByteTW/DimensionPause/master/version.txt").getVersion(latest -> {
            if (Integer.parseInt(latest.replace(".", "")) > Integer.parseInt(this.getDescription().getVersion().replace(".", ""))) {
				Component updateMessage = Component.text()
						.append(CommandFeedback.getChatPrefix())
						.append(Component.text(" "))
						.append(Component.text("There is a new update available: " + latest + ". Please download for the latest features and security updates!", NamedTextColor.RED))
						.build();

				getServer().getConsoleSender().sendMessage(updateMessage);
			} else {
				Component noUpdateMessage = Component.text()
						.append(CommandFeedback.getChatPrefix())
						.append(Component.text(" "))
						.append(Component.text("You are on the latest version of DimensionPause!", NamedTextColor.GREEN))
						.build();

				getServer().getConsoleSender().sendMessage(noUpdateMessage);
			}
		});
	}

	@Override
	public void onDisable() {
		getLogger().info("Dimension Pause is shutting down");
	}
}
