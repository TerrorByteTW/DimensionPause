package org.reprogle.dimensionpause.commands;

import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.World;
import org.reprogle.dimensionpause.ConfigManager;
import org.reprogle.dimensionpause.DimensionPausePlugin;

import java.time.Duration;
import java.util.Objects;

public class CommandFeedback {

	public static MiniMessage mm = MiniMessage.miniMessage();

	/**
	 * Return the chat prefix object from config
	 *
	 * @return The chat prefix, preformatted with color and other modifiers
	 */
	public static Component getChatPrefix() {
		return mm.deserialize(Objects.requireNonNull(ConfigManager.getLanguageFile().getString("prefix")));
	}

	/**
	 * A helper class which helps to reduce boilerplate player.sendMessage code by providing the strings to send instead
	 * of having to copy and paste them.
	 *
	 * @param feedback The string to send back
	 * @return The Feedback string
	 */
	public static Component sendCommandFeedback(String feedback, String... dimension) {
		Component feedbackMessage;
		Component chatPrefix = getChatPrefix();
		YamlDocument languageFile = ConfigManager.getLanguageFile();

		switch (feedback.toLowerCase()) {
			case "usage" ->
					feedbackMessage = Component.text().content("\n \n \n \n \n \n-----------------------\n \n").color(NamedTextColor.WHITE)
							.append(chatPrefix).append(Component.text(" "))
							.append(Component.text("Need help?", NamedTextColor.WHITE))
							.append(Component.text("\n  /dimensionpause ", NamedTextColor.WHITE)).append(Component.text("toggle [end | nether]", NamedTextColor.GRAY))
							.append(Component.text("\n-----------------------", NamedTextColor.WHITE))
							.build();
			case "nopermission" -> feedbackMessage = Component.text().append(chatPrefix)
					.append(Component.text(" "))
					.append(mm.deserialize(languageFile.getString("no-permission")))
					.build();
			case "newstate" -> {
				Component pausedComponent = Component.text("paused").color(NamedTextColor.RED);
				Component unpausedComponent = Component.text("unpaused").color(NamedTextColor.GREEN);

				if (dimension.length > 0 && dimension[0].equals("nether")) {
					feedbackMessage = Component.text().append(chatPrefix)
							.append(Component.text(" "))
							.append(mm.deserialize(languageFile.getString("toggled.nether")))
							.append(DimensionPausePlugin.ds.getState(World.Environment.NETHER) ? pausedComponent : unpausedComponent)
							.build();
				} else if (dimension.length > 0 && dimension[0].equals("end")) {
					feedbackMessage = Component.text().append(chatPrefix)
							.append(Component.text(" "))
							.append(mm.deserialize(languageFile.getString("toggled.end")))
							.append(DimensionPausePlugin.ds.getState(World.Environment.THE_END) ? pausedComponent : unpausedComponent)
							.build();
				} else {
					feedbackMessage = Component.text().append(chatPrefix)
							.append(Component.text(" "))
							.append(mm.deserialize(languageFile.getString("toggled.default")))
							.build();
				}
			}
			default -> feedbackMessage = Component.text().append(chatPrefix)
					.append(Component.text(" "))
					.append(mm.deserialize(languageFile.getString("unknown-error")))
					.build();
		}

		return feedbackMessage;
	}

	public static Title getTitleForDimension(World.Environment env) {
		String environment = env.equals(World.Environment.NETHER) ? "nether" : "end";

		final Component mainTitle = Component.text().append(mm.deserialize(ConfigManager.getPluginConfig().getString("dimensions." + environment + ".alert.title.title"))).build();
		final Component subtitle = Component.text().append(mm.deserialize(ConfigManager.getPluginConfig().getString("dimensions." + environment + ".alert.title.subtitle"))).build();

		final Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500));
		return Title.title(mainTitle, subtitle, times);
	}

	public static Component getChatForDimension(World.Environment env) {
		String environment = env.equals(World.Environment.NETHER) ? "nether" : "end";
		return Component.text()
				.append(getChatPrefix())
				.append(mm.deserialize(ConfigManager.getPluginConfig().getString("dimensions." + environment + ".alert.chat.message")))
				.build();
	}

	public static Component getToggleMessageForDimension(World.Environment env, boolean newState) {
		String environment = env.equals(World.Environment.NETHER) ? "nether" : "end";
		String stateParsed = newState ? "<red>paused</red>" : "<green>unpaused</green>";

		String preparsedText = ConfigManager.getPluginConfig().getString("dimensions." + environment + ".alert.on-toggle.message").replace("%state%", stateParsed);
		return Component.text()
				.append(getChatPrefix())
				.append(Component.text(" "))
				.append(mm.deserialize(preparsedText))
				.build();
	}
}
