package org.reprogle.dimensionpause.commands;

import com.google.inject.Inject;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.title.Title;
import org.bukkit.World;
import org.reprogle.dimensionpause.utils.ConfigManager;
import org.reprogle.dimensionpause.utils.DimensionState;
import org.reprogle.dimensionpause.store.Database;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class CommandFeedback {

    public static final MiniMessage mm = MiniMessage.miniMessage();

    @Inject
    private ConfigManager configManager;
    @Inject
    private DimensionState state;

    /**
     * A helper class which helps to reduce boilerplate player.sendMessage code by providing the strings to send instead
     * of having to copy and paste them.
     *
     * @param feedback The string to send back
     * @return The Feedback string
     */
    public Component sendCommandFeedback(String feedback, @Nullable World world, @Nullable String dimension) {
        Component feedbackMessage;
        YamlDocument languageFile = configManager.getLanguageFile();
        World.Environment environment = null;
        if (dimension != null && (dimension.equalsIgnoreCase("end") || dimension.equalsIgnoreCase("nether")))
            environment = (dimension.equalsIgnoreCase("nether") ? World.Environment.NETHER : World.Environment.THE_END);

        final Component untilComponent = mm.deserialize(languageFile.getString("state.until"));

        switch (feedback.toLowerCase()) {
            case "usage" -> {
                final Component prefixComponent = mm.deserialize(languageFile.getString("state.until"));
                feedbackMessage = Component.text().content("\n \n \n \n \n \n-----------------------\n \n").color(NamedTextColor.WHITE)
                        .append(prefixComponent).append(Component.text(" "))
                        .append(Component.text("Need help?\n \n", NamedTextColor.WHITE))
                        .append(Component.text("  /dimensionpause ", NamedTextColor.WHITE)).append(Component.text("toggle <world> <end | nether> [<weeks>w][<days>d][<hours>h][<minutes>m][<seconds>s] \n", NamedTextColor.GRAY))
                        .append(Component.text("  /dimensionpause ", NamedTextColor.WHITE)).append(Component.text("state <world> <end | nether> \n", NamedTextColor.GRAY))
                        .append(Component.text("  /dimensionpause ", NamedTextColor.WHITE)).append(Component.text("reload \n \n", NamedTextColor.GRAY))
                        .append(Component.text("-----------------------", NamedTextColor.WHITE))
                        .build();
            }
            case "nopermission" -> feedbackMessage = deserialize(languageFile.getString("no-permission"), false, null);
            case "reload" -> feedbackMessage = deserialize(languageFile.getString("reload"), false, null);
            case "io-exception" -> feedbackMessage = deserialize(languageFile.getString("io-exception"), false, null);
            case "newstate" -> {
                if (environment == null || world == null) {
                    feedbackMessage = deserialize(languageFile.getString("toggled.default"), false, null);
                } else {
                    Database.WorldPauseStatus worldState = state.getState(world, environment);

                    TextComponent.Builder builder = Component.text().append(deserialize(languageFile.getString("toggled." + dimension), worldState.enabled(), world.getName()));

                    // Only output the expiration time if disabled
                    if (worldState.expiresAt() != null && !worldState.enabled()) {
                        builder.append(Component.text(" "))
                                .append(untilComponent)
                                .append(Component.text(" "))
                                .append(Component.text(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC).format(worldState.expiresAt())))
                                .append(Component.text(" UTC"));
                    }

                    feedbackMessage = builder.build();
                }
            }
            case "state" -> {
                if (environment == null || world == null) return Component.empty();

                Database.WorldPauseStatus worldState = state.getState(world, environment);
                TextComponent.Builder builder = Component.text().append(deserialize(languageFile.getString("state." + dimension), worldState.enabled(), world.getName()));

                if (worldState.expiresAt() != null && !worldState.enabled()) {
                    builder.append(Component.text(" "))
                            .append(untilComponent)
                            .append(Component.text(" "))
                            .append(Component.text(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC).format(worldState.expiresAt())))
                            .append(Component.text(" UTC"));
                }

                feedbackMessage = builder.build();
            }
            default -> feedbackMessage = deserialize(languageFile.getString("unknown-error"), false, null);
        }

        return feedbackMessage;
    }

    public Title getTitleForDimension(World.Environment env) {
        String environment = env.equals(World.Environment.NETHER) ? "nether" : "end";

        final Component mainTitle = Component.text().append(mm.deserialize(configManager.getLanguageFile().getString("alert." + environment + ".title.title"))).build();
        final Component subtitle = Component.text().append(mm.deserialize(configManager.getLanguageFile().getString("alert." + environment + ".title.subtitle"))).build();

        final Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500));
        return Title.title(mainTitle, subtitle, times);
    }

    public Title getTitleForTeleport(int secondsRemaining, boolean fadeInTitle) {
        NamedTextColor numColor = (secondsRemaining >= 3) ? NamedTextColor.GOLD
                : (secondsRemaining == 2) ? NamedTextColor.RED
                : NamedTextColor.DARK_RED;

        final Component mainTitle = Component.text().append(mm.deserialize(configManager.getLanguageFile().getString("preteleport.title"))).build();
        final Component subtitle = Component.text()
                .append(mm.deserialize(configManager.getLanguageFile().getString("preteleport.subtitle")))
                .append(Component.text(secondsRemaining, numColor)).build();

        return Title.title(
                mainTitle,
                subtitle,
                Title.Times.times(fadeInTitle ? Duration.ofMillis(500) : Duration.ZERO, Duration.ofMillis(1000), Duration.ZERO)
        );
    }

    public Component getDimensionIsPausedMessage(World.Environment env) {
        String environment = env.equals(World.Environment.NETHER) ? "nether" : "end";
        return deserialize(configManager.getLanguageFile().getString("alert." + environment + ".chat"), false, null);
    }

    public Component getStateChangedMessage(World world, World.Environment env, boolean enabled) {
        return getStateChangedMessage(world.getName(), env, enabled);
    }

    public Component getStateChangedMessage(String world, World.Environment env, boolean enabled) {
        String environment = env.equals(World.Environment.NETHER) ? "nether" : "end";
        return deserialize(configManager.getLanguageFile().getString("alert." + environment + ".on-toggle"), enabled, world);
    }

    private Component deserialize(String serializedString, boolean worldState, String worldName) {
        final Component prefixComponent = mm.deserialize(configManager.getLanguageFile().getString("prefix"));
        final Component pausedComponent = mm.deserialize(configManager.getLanguageFile().getString("state.paused"));
        final Component unpausedComponent = mm.deserialize(configManager.getLanguageFile().getString("state.unpaused"));

        return mm.deserialize(serializedString,
                Placeholder.component(
                        "prefix",
                        prefixComponent
                ),
                Placeholder.component(
                        "state",
                        worldState ? unpausedComponent : pausedComponent
                ),
                Placeholder.component(
                        "world",
                        Component.text(worldName != null ? worldName : "", NamedTextColor.BLUE)
                )
        );
    }
}
