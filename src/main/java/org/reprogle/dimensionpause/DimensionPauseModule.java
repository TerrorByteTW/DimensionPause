package org.reprogle.dimensionpause;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import org.reprogle.bytelib.commands.CommandRegistration;
import org.reprogle.dimensionpause.commands.CommandFeedback;
import org.reprogle.dimensionpause.commands.DPCommandRegistration;
import org.reprogle.dimensionpause.commands.SubCommand;
import org.reprogle.dimensionpause.commands.subcommands.Reload;
import org.reprogle.dimensionpause.commands.subcommands.State;
import org.reprogle.dimensionpause.commands.subcommands.Toggle;
import org.reprogle.bytelib.boot.lifecycle.PluginLifecycle;

public final class DimensionPauseModule extends AbstractModule {

    @Override
    protected void configure() {
        // Core services
        bind(CommandFeedback.class).in(Singleton.class);

        // Subcommands
        Multibinder<SubCommand> subcommandBinder = Multibinder.newSetBinder(binder(), SubCommand.class);
        subcommandBinder.addBinding().to(Reload.class);
        subcommandBinder.addBinding().to(State.class);
        subcommandBinder.addBinding().to(Toggle.class);

        // Lifecycle hooks
        Multibinder<PluginLifecycle> lifecycles = Multibinder.newSetBinder(binder(), PluginLifecycle.class);
        lifecycles.addBinding().to(DimensionPauseLifecycle.class);

        // Commands
        Multibinder.newSetBinder(binder(), CommandRegistration.class)
                .addBinding()
                .to(DPCommandRegistration.class);
    }
}

