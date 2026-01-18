package org.reprogle.dimensionpause.commands.subcommands;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.reprogle.dimensionpause.DimensionState;
import org.reprogle.dimensionpause.commands.CommandFeedback;
import org.reprogle.dimensionpause.commands.SubCommand;

import java.util.ArrayList;
import java.util.List;

public class Toggle implements SubCommand {
    @Inject
    CommandFeedback commandFeedback;
    @Inject
    DimensionState state;

    @Override
    public String getName() {
        return "toggle";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length >= 3 && (args[2].equalsIgnoreCase("end") || args[2].equalsIgnoreCase("nether"))) {
            String world = args[1];
            String dimension = args[2].toLowerCase();
            World.Environment environment = dimension.equalsIgnoreCase("nether") ? World.Environment.NETHER : World.Environment.THE_END;
            state.toggleDimension(world, environment, null);
            sender.sendMessage(commandFeedback.sendCommandFeedback("newstate", world, dimension));
        } else {
            sender.sendMessage(commandFeedback.sendCommandFeedback("usage", null, null));
        }
    }

    @Override
    public List<String> getSubcommands(CommandSender sender, String[] args) {
        List<String> subcommands = new ArrayList<>();

        // We are already in argument 1 of the command, hence why this is a subcommand
        // class. Argument 2 is the
        // subcommand for the subcommand,
        // aka /dimensionpause state <WORLD <- This arg> <dimension>
        // Same with argument 3
        // aka /dimensionpause state <world> <DIMENSION <- This arg>

        if (args.length == 2) {
            Bukkit.getWorlds().forEach(world -> {
                if (world.getEnvironment().equals(World.Environment.NORMAL))
                    subcommands.add(world.getName());
            });
        } else if (args.length == 3) {
            subcommands.add("nether");
            subcommands.add("end");
        }
        return subcommands;
    }

    @Override
    public List<String> getRequiredPermissions() {
        List<String> permissions = new ArrayList<>();
        permissions.add("dimensionpause.toggle");
        return permissions;
    }
}
