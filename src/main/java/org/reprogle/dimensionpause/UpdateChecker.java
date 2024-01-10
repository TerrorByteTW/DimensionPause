package org.reprogle.dimensionpause;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public record UpdateChecker(Plugin plugin, String link) {

	/**
	 * Grabs the version number from the link provided
	 *
	 * @param consumer The consumer function
	 */
	public void getVersion(final Consumer<String> consumer) {
		Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
			try (InputStream inputStream = new URL(this.link).openStream();
					Scanner scanner = new Scanner(inputStream)) {
				if (scanner.hasNext()) {
					consumer.accept(scanner.next());
				}
			} catch (IOException exception) {
				plugin.getLogger().info("Unable to check for updates: " + exception.getMessage());
			}
		});
	}

}
