package org.reprogle.dimensionpause;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import java.util.function.Consumer;

public record UpdateChecker(Plugin plugin, String link) {
    // Reusable HTTP Client so we don't pay performance overhead and don't build new clients every time we need them
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /**
     * Grabs the version number from the link provided
     *
     * @param consumer The consumer function
     */
    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(this.link))
                        .GET()
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200 && !response.body().isEmpty()) {
                    try (Scanner scanner = new Scanner(response.body())) {
                        if (scanner.hasNext()) {
                            consumer.accept(scanner.next());
                        }
                    }
                } else {
                    plugin.getLogger().info("Unable to check for updates: HTTP " + response.statusCode());
                }
            } catch (IOException | InterruptedException e) {
                plugin.getLogger().info("Unable to check for updates: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        });
    }

}
