package org.reprogle.dimensionpause.commands;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.bukkit.World;
import org.reprogle.bytelib.config.Translator;
import org.reprogle.dimensionpause.store.TrackedWorldsRepository;
import org.reprogle.dimensionpause.utils.DimensionState;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class CommandFeedback {

    @Inject
    Translator translator;
    @Inject
    DimensionState state;

    /**
     * A helper class which helps to reduce boilerplate player.sendMessage code by providing the strings to send instead
     * of having to copy and paste them.
     *
     * @param feedback The string to send back
     * @return The Feedback string
     */
    public Component sendCommandFeedback(String feedback, @Nullable World world, @Nullable String dimension) {
        Component feedbackMessage;
        World.Environment environment = null;
        if (dimension != null && (dimension.equalsIgnoreCase("end") || dimension.equalsIgnoreCase("nether")))
            environment = (dimension.equalsIgnoreCase("nether") ? World.Environment.NETHER : World.Environment.THE_END);

        final Component untilComponent = translator.tr("state.until");

        switch (feedback.toLowerCase()) {
            case "usage" -> {
                final Component prefixComponent = translator.tr("prefix");
                feedbackMessage = Component.text().content("\n \n \n \n \n \n-----------------------\n \n").color(NamedTextColor.WHITE)
                        .append(prefixComponent).append(Component.text(" "))
                        .append(Component.text("Need help?\n \n", NamedTextColor.WHITE))
                        .append(Component.text("  /dimensionpause ", NamedTextColor.WHITE)).append(Component.text("toggle <world> <end | nether> [<weeks>w][<days>d][<hours>h][<minutes>m][<seconds>s] \n", NamedTextColor.GRAY))
                        .append(Component.text("  /dimensionpause ", NamedTextColor.WHITE)).append(Component.text("state <world> <end | nether> \n", NamedTextColor.GRAY))
                        .append(Component.text("  /dimensionpause ", NamedTextColor.WHITE)).append(Component.text("reload \n \n", NamedTextColor.GRAY))
                        .append(Component.text("-----------------------", NamedTextColor.WHITE))
                        .build();
            }
            case "nopermission" ->
                    feedbackMessage = translator.tr("no-permission", stateResolver(false), worldResolver(null));
            case "reload" -> feedbackMessage = translator.tr("reload", stateResolver(false), worldResolver(null));
            case "io-exception" ->
                    feedbackMessage = translator.tr("io-exception", stateResolver(false), worldResolver(null));
            case "newstate" -> {
                if (environment == null || world == null) {
                    feedbackMessage = translator.tr("toggled.default", stateResolver(false), worldResolver(null));
                } else {
                    TrackedWorldsRepository.WorldPauseStatus worldState = state.getState(world, environment);

                    TextComponent.Builder builder = Component.text().append(translator.tr("toggled." + dimension, stateResolver(worldState.enabled()), worldResolver(world.getName())));

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

                TrackedWorldsRepository.WorldPauseStatus worldState = state.getState(world, environment);
                TextComponent.Builder builder = Component.text().append(translator.tr("state." + dimension, stateResolver(worldState.enabled()), worldResolver(world.getName())));

                if (worldState.expiresAt() != null && !worldState.enabled()) {
                    builder.append(Component.text(" "))
                            .append(untilComponent)
                            .append(Component.text(" "))
                            .append(Component.text(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC).format(worldState.expiresAt())))
                            .append(Component.text(" UTC"));
                }

                feedbackMessage = builder.build();
            }
            default -> feedbackMessage = translator.tr("unknown-error", stateResolver(false), worldResolver(null));
        }

        return feedbackMessage;
    }

    public Title getTitleForDimension(World.Environment env) {
        return translator.title("alert." + (env.equals(World.Environment.NETHER) ? "nether" : "end") + ".title",
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500)));
    }

    public Title getTitleForTeleport(int secondsRemaining, boolean fadeInTitle) {
        NamedTextColor numColor = (secondsRemaining >= 3) ? NamedTextColor.GOLD
                : (secondsRemaining == 2) ? NamedTextColor.RED
                : NamedTextColor.DARK_RED;

        final Component mainTitle = Component.text().append(translator.tr("preteleport.title")).build();
        final Component subtitle = Component.text()
                .append(translator.tr("preteleport.subtitle"))
                .append(Component.text(" "))
                .append(Component.text(secondsRemaining, numColor)).build();

        return Title.title(
                mainTitle,
                subtitle,
                Title.Times.times(fadeInTitle ? Duration.ofMillis(500) : Duration.ZERO, Duration.ofMillis(1000), Duration.ZERO)
        );
    }

    public Component getDimensionIsPausedMessage(World.Environment env) {
        String environment = env.equals(World.Environment.NETHER) ? "nether" : "end";
        return translator.tr("alert." + environment + ".chat", stateResolver(false), worldResolver(null));
    }

    public Component getStateChangedMessage(World world, World.Environment env, boolean enabled) {
        return getStateChangedMessage(world.getName(), env, enabled);
    }

    public Component getStateChangedMessage(String world, World.Environment env, boolean enabled) {
        String environment = env.equals(World.Environment.NETHER) ? "nether" : "end";
        return translator.tr("alert." + environment + ".on-toggle", stateResolver(enabled), worldResolver(world));
    }

    private TagResolver stateResolver(boolean worldState) {
        final Component pausedComponent = translator.tr("state.paused");
        final Component unpausedComponent = translator.tr("state.unpaused");
        return Placeholder.component("state", worldState ? unpausedComponent : pausedComponent);
    }

    private TagResolver worldResolver(String worldName) {
        return Placeholder.component("world", Component.text(worldName != null ? worldName : "", NamedTextColor.BLUE));
    }
}
