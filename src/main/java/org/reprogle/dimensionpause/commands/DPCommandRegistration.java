package org.reprogle.dimensionpause.commands;

import com.google.inject.Inject;
import io.papermc.paper.command.brigadier.Commands;
import org.reprogle.bytelib.commands.CommandRegistration;

import java.util.List;
import java.util.Set;

public final class DPCommandRegistration implements CommandRegistration {
    private final CommandFeedback feedback;
    private final Set<SubCommand> subcommands;

    @Inject
    public DPCommandRegistration(CommandFeedback feedback, Set<SubCommand> subcommands) {
        this.feedback = feedback;
        this.subcommands = subcommands;
    }

    @Override
    public void register(Commands commands) {
        commands.register(
                "dimensionpause",
                "Pauses dimensions per-world",
                List.of("dp"),
                new Command(
                        "dimensionpause.commands",
                        "usage",
                        "nopermission",
                        feedback,
                        subcommands
                )
        );
    }
}
