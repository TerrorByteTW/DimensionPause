package org.reprogle.dimensionpause.commands;

import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.World;
import org.reprogle.dimensionpause.ConfigManager;
import org.reprogle.dimensionpause.DimensionPausePlugin;

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
							.append(Component.text("Need help?\n", NamedTextColor.WHITE))
							.append(Component.text("  /dimensionpause ", NamedTextColor.WHITE)).append(Component.text("toggle [end | nether]\n", NamedTextColor.GRAY))
							.append(Component.text("-----------------------", NamedTextColor.WHITE))
							.build();
			case "nopermission" ->
					feedbackMessage = Component.text().append(chatPrefix)
							.append(Component.text(" "))
							.append(mm.deserialize(languageFile.getString("no-permission")))
							.build();
			case "newstate" -> {
				if (dimension.length > 0 && dimension[0].equals("nether")) {
					feedbackMessage = Component.text().append(chatPrefix)
							.append(Component.text(" "))
							.append(mm.deserialize(languageFile.getString("toggled.nether")))
							.append(Component.text(DimensionPausePlugin.ds.getState(World.Environment.THE_END) ? "paused" : "unpaused", NamedTextColor.BLUE))
							.build();
				} else if (dimension.length > 0 && dimension[0].equals("end")) {
					feedbackMessage = Component.text().append(chatPrefix)
							.append(Component.text(" "))
							.append(mm.deserialize(languageFile.getString("toggled.end")))
							.append(Component.text(DimensionPausePlugin.ds.getState(World.Environment.THE_END) ? "paused" : "unpaused", NamedTextColor.BLUE))
							.build();
				} else {
					feedbackMessage = Component.text().append(chatPrefix)
							.append(Component.text(" "))
							.append(mm.deserialize(languageFile.getString("toggled.default")))
							.build();
				}
			}
			default ->
					feedbackMessage = Component.text().append(chatPrefix)
							.append(Component.text(" "))
							.append(mm.deserialize(languageFile.getString("unknown-error")))
							.build();
		}

		return feedbackMessage;
	}

}
