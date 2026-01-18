package org.reprogle.dimensionpause;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;
import org.reprogle.dimensionpause.commands.CommandFeedback;
import org.reprogle.dimensionpause.commands.SubCommand;
import org.reprogle.dimensionpause.commands.subcommands.Reload;
import org.reprogle.dimensionpause.commands.subcommands.State;
import org.reprogle.dimensionpause.commands.subcommands.Toggle;

public class DimensionPauseModule extends AbstractModule {
    private final DimensionPausePlugin plugin;
    private final ConfigManager configManager;
    private final CommandFeedback commandFeedback;

    public DimensionPauseModule(DimensionPausePlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        configManager.setupConfig(plugin);

        this.commandFeedback = new CommandFeedback();
    }

    @Override
    protected void configure() {
        // The lifeline of the entire DI system is the plugin object itself
        bind(DimensionPausePlugin.class).toInstance(plugin);
        bind(ConfigManager.class).toInstance(configManager);
        bind(CommandFeedback.class).toInstance(commandFeedback);

        Multibinder<SubCommand> subcommandBinder = Multibinder.newSetBinder(binder(), SubCommand.class);
        subcommandBinder.addBinding().to(Reload.class);
        subcommandBinder.addBinding().to(State.class);
        subcommandBinder.addBinding().to(Toggle.class);
    }

    public Injector createInjector() {
        return Guice.createInjector(this);
    }
}
