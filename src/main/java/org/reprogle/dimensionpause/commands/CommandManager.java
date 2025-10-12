package org.reprogle.dimensionpause.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reprogle.dimensionpause.commands.subcommands.Reload;
import org.reprogle.dimensionpause.commands.subcommands.State;
import org.reprogle.dimensionpause.commands.subcommands.Toggle;

import java.util.ArrayList;
import java.util.List;

public class CommandManager implements TabExecutor {

    private final ArrayList<SubCommand> subcommands = new ArrayList<>();

    public CommandManager() {
        subcommands.add(new Toggle());
        subcommands.add(new Reload());
        subcommands.add(new State());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!sender.hasPermission("dimensionpause.commands")) {
            sender.sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
            return false;
        }

        if (args.length > 0) {
            // For each subcommand in the subcommands array list, check if the argument is
            // the same as the command.
            // If so, run said subcommand
            for (SubCommand subcommand : subcommands) {
                if (args[0].equalsIgnoreCase(subcommand.getName())) {
                    if (!checkPermissions(sender, subcommand)) {
                        sender.sendMessage(CommandFeedback.sendCommandFeedback("nopermission"));
                        return false;
                    }

                    subcommand.perform(sender, args);
                    return true;
                }
            }

            sender.sendMessage(CommandFeedback.sendCommandFeedback("usage"));
        } else {
            sender.sendMessage(CommandFeedback.sendCommandFeedback("usage"));
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
