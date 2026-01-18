package org.reprogle.dimensionpause.commands;

import com.google.inject.Inject;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.World;
import org.reprogle.dimensionpause.ConfigManager;
import org.reprogle.dimensionpause.DimensionState;
import org.reprogle.dimensionpause.store.Database;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class CommandFeedback {

    public static final MiniMessage mm = MiniMessage.miniMessage();

    @Inject
    private ConfigManager configManager;
    @Inject
    private DimensionState state;

    /**
     * Return the chat prefix object from config
     *
     * @return The chat prefix, preformatted with color and other modifiers
     */
    public Component getChatPrefix() {
        return mm.deserialize(Objects.requireNonNull(configManager.getLanguageFile().getString("prefix")));
    }

    /**
     * A helper class which helps to reduce boilerplate player.sendMessage code by providing the strings to send instead
     * of having to copy and paste them.
     *
     * @param feedback The string to send back
     * @return The Feedback string
     */
    public Component sendCommandFeedback(String feedback, @Nullable String world, @Nullable String dimension) {
        Component feedbackMessage;
        Component chatPrefix = getChatPrefix();
        YamlDocument languageFile = configManager.getLanguageFile();
        World.Environment environment = null;
        if (dimension != null && (dimension.equalsIgnoreCase("end") || dimension.equalsIgnoreCase("nether")))
            environment = (dimension.equalsIgnoreCase("nether") ? World.Environment.NETHER : World.Environment.THE_END);

        switch (feedback.toLowerCase()) {
            case "usage" ->
                    feedbackMessage = Component.text().content("\n \n \n \n \n \n-----------------------\n \n").color(NamedTextColor.WHITE)
                            .append(chatPrefix).append(Component.text(" "))
                            .append(Component.text("Need help?\n \n", NamedTextColor.WHITE))
                            .append(Component.text("  /dimensionpause ", NamedTextColor.WHITE)).append(Component.text("toggle [end | nether] \n", NamedTextColor.GRAY))
                            .append(Component.text("  /dimensionpause ", NamedTextColor.WHITE)).append(Component.text("state [end | nether] \n", NamedTextColor.GRAY))
                            .append(Component.text("  /dimensionpause ", NamedTextColor.WHITE)).append(Component.text("reload \n \n", NamedTextColor.GRAY))
                            .append(Component.text("-----------------------", NamedTextColor.WHITE))
                            .build();
            case "nopermission" -> feedbackMessage = Component.text().append(chatPrefix)
                    .append(Component.text(" "))
                    .append(mm.deserialize(languageFile.getString("no-permission")))
                    .build();
            case "reload" -> feedbackMessage = Component.text().append(chatPrefix)
                    .append(Component.text(" "))
                    .append(mm.deserialize(languageFile.getString("reload")))
                    .build();
            case "io-exception" -> feedbackMessage = Component.text().append(chatPrefix)
                    .append(Component.text(" "))
                    .append(mm.deserialize(languageFile.getString("io-exception")))
                    .build();
            case "newstate" -> {
                Component pausedComponent = mm.deserialize(languageFile.getString("state.paused"));
                Component unpausedComponent = mm.deserialize(languageFile.getString("state.unpaused"));
                if (environment == null) {
                    feedbackMessage = Component.text().append(chatPrefix)
                            .append(Component.text(" "))
                            .append(mm.deserialize(languageFile.getString("toggled.default")))
                            .build();
                } else {
                    Database.WorldPauseStatus worldState = state.getState(world, environment);

                    TextComponent.Builder builder = Component.text().append(chatPrefix)
                            .append(Component.text(" "))
                            .append(mm.deserialize(languageFile.getString("toggled." + dimension)))
                            .append(!worldState.enabled() ? pausedComponent : unpausedComponent);

                    if (worldState.expiresAt() != null) {
                        Component untilComponent = mm.deserialize(languageFile.getString("state.until"));
                        builder.append(untilComponent)
                                .append(Component.text(worldState.expiresAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                                .append(Component.text(" UTC"));
                    }

                    feedbackMessage = builder.build();
                }
            }
            case "state" -> {
                Component pausedComponent = mm.deserialize(languageFile.getString("state.paused"));
                Component unpausedComponent = mm.deserialize(languageFile.getString("state.unpaused"));
                if (environment == null) return Component.empty();

                Database.WorldPauseStatus worldState = state.getState(world, environment);
                TextComponent.Builder builder = Component.text().append(chatPrefix)
                        .append(Component.text(" "))
                        .append(mm.deserialize(languageFile.getString("state." + dimension)))
                        .append(!worldState.enabled() ? pausedComponent : unpausedComponent);

                if (worldState.expiresAt() != null) {
                    Component untilComponent = mm.deserialize(languageFile.getString("state.until"));
                    builder.append(untilComponent)
                            .append(Component.text(worldState.expiresAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                            .append(Component.text(" UTC"));
                }

                feedbackMessage = builder.build();
            }
            default -> feedbackMessage = Component.text().append(chatPrefix)
                    .append(Component.text(" "))
                    .append(mm.deserialize(languageFile.getString("unknown-error")))
                    .build();
        }

        return feedbackMessage;
    }

    public Title getTitleForDimension(World.Environment env) {
        String environment = env.equals(World.Environment.NETHER) ? "nether" : "end";

        final Component mainTitle = Component.text().append(mm.deserialize(configManager.getPluginConfig().getString("dimensions." + environment + ".alert.title.title"))).build();
        final Component subtitle = Component.text().append(mm.deserialize(configManager.getPluginConfig().getString("dimensions." + environment + ".alert.title.subtitle"))).build();

        final Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500));
        return Title.title(mainTitle, subtitle, times);
    }

    public Component getChatForDimension(World.Environment env) {
        String environment = env.equals(World.Environment.NETHER) ? "nether" : "end";
        return Component.text()
                .append(getChatPrefix())
                .append(mm.deserialize(configManager.getPluginConfig().getString("dimensions." + environment + ".alert.chat.message")))
                .build();
    }

    public Component getToggleMessageForDimension(World world, World.Environment env, boolean newState) {
        return getToggleMessageForDimension(world.getName(), env, newState);
    }

    public Component getToggleMessageForDimension(String world, World.Environment env, boolean newState) {
        String environment = env.equals(World.Environment.NETHER) ? "nether" : "end";
        String stateParsed = newState ? "<red>paused</red>" : "<green>unpaused</green>";
        String worldFmtd = "<blue>" + world + "<reset>";

        String preparsedText = configManager.getPluginConfig().getString("dimensions." + environment + ".alert.on-toggle.message").replace("%world%", worldFmtd).replace("%state%", stateParsed);
        return Component.text()
                .append(getChatPrefix())
                .append(Component.text(" "))
                .append(mm.deserialize(preparsedText))
                .build();
    }
}
