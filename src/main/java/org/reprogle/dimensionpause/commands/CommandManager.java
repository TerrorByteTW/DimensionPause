package org.reprogle.dimensionpause.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reprogle.dimensionpause.commands.subcommands.Reload;
import org.reprogle.dimensionpause.commands.subcommands.State;
import org.reprogle.dimensionpause.commands.subcommands.Toggle;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Singleton
public class CommandManager implements TabExecutor {

    private final CommandFeedback commandFeedback;

    @Getter
    @Inject
    private Set<SubCommand> subcommands;

    @Inject
    public CommandManager(CommandFeedback commandFeedback) {
        this.commandFeedback = commandFeedback;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!sender.hasPermission("dimensionpause.commands")) {
            sender.sendMessage(commandFeedback.sendCommandFeedback("nopermission", null, null));
            return false;
        }

        if (args.length > 0) {
            // For each subcommand in the subcommands array list, check if the argument is
            // the same as the command.
            // If so, run said subcommand
            for (SubCommand subcommand : subcommands) {
                if (args[0].equalsIgnoreCase(subcommand.getName())) {
                    if (!checkPermissions(sender, subcommand)) {
                        sender.sendMessage(commandFeedback.sendCommandFeedback("nopermission", null, null));
                        return false;
                    }

                    subcommand.perform(sender, args);
                    return true;
                }
            }

            sender.sendMessage(commandFeedback.sendCommandFeedback("usage", null, null));
        } else {
            sender.sendMessage(commandFeedback.sendCommandFeedback("usage", null, null));
        }

        return false;
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!(sender.hasPermission("dimensionpause.commands") || sender.isOp()))
            return null;

        if (args.length == 1) {
            ArrayList<String> subcommandsTabComplete = new ArrayList<>();

            // Copy each partial match to the subcommands list
            StringUtil.copyPartialMatches(args[0], List.of(subcommands.stream().map(SubCommand::getName).toArray(String[]::new)), subcommandsTabComplete);

            return subcommandsTabComplete;
        } else if (args.length >= 2) {
            // If the argument is the 2nd one or more, return the subcommands for that
            // subcommand
            for (SubCommand subcommand : subcommands) {
                // Check if the first argument equals the command in the current interation
                if (args[0].equalsIgnoreCase(subcommand.getName())) {
                    // Create a new array and copy partial matches of the current argument.
                    // getSubcommands can actually handle more than one subcommand per
                    // root command, meaning if the argument length is 3 or 4 or 5, it can handle
                    // those accordingly.
                    ArrayList<String> subcommandsTabComplete = new ArrayList<>();

                    StringUtil.copyPartialMatches(args[args.length - 1], subcommand.getSubcommands(sender, args),
                            subcommandsTabComplete);

                    return subcommandsTabComplete;
                }
            }
        }

        return null;
    }

    /**
     * Check if the Player has the permissions necessary to run the subcommand
     *
     * @param sender     The CommandSender to check
     * @param subcommand The subcommand we're checking
     */
    private boolean checkPermissions(CommandSender sender, SubCommand subcommand) {
        boolean allowed = false;

        if (subcommand.getRequiredPermissions().isEmpty())
            return true;

        for (String permission : subcommand.getRequiredPermissions()) {
            if (sender.hasPermission(permission)) {
                allowed = true;
                break;
            }
        }

        return allowed;
    }
}
