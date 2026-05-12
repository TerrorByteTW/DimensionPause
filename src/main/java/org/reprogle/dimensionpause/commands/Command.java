package org.reprogle.dimensionpause.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public final class Command implements BasicCommand {

    private final String rootPermission;
    private final String usageMessageKey;
    private final String noPermissionKey;

    private final CommandFeedback feedback;
    private final Set<SubCommand> subcommands;

    public Command(
            String rootPermission,
            String usageMessageKey,
            String noPermissionKey,
            CommandFeedback feedback,
            Set<SubCommand> subcommands
    ) {
        this.rootPermission = rootPermission;
        this.usageMessageKey = usageMessageKey;
        this.noPermissionKey = noPermissionKey;
        this.feedback = feedback;
        this.subcommands = subcommands;
    }

    @Override
    public void execute(CommandSourceStack source, String @NonNull [] args) {
        CommandSender sender = source.getSender();

        // Keep your existing behavior: show a message rather than just hiding the command
        if (!sender.hasPermission(rootPermission)) {
            sender.sendMessage(feedback.sendCommandFeedback(noPermissionKey, null, null));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(feedback.sendCommandFeedback(usageMessageKey, null, null));
            return;
        }

        final String sub = args[0];

        for (SubCommand subcommand : subcommands) {
            if (!sub.equalsIgnoreCase(subcommand.getName())) continue;

            if (!hasAnyPermission(sender, subcommand.getRequiredPermissions())) {
                sender.sendMessage(feedback.sendCommandFeedback(noPermissionKey, null, null));
                return;
            }

            // IMPORTANT: keep the old contract: args[0] is the subcommand name
            subcommand.perform(sender, args);
            return;
        }

        sender.sendMessage(feedback.sendCommandFeedback(usageMessageKey, null, null));
    }

    @Override
    public @NonNull Collection<String> suggest(CommandSourceStack source, String @NonNull [] args) {
        CommandSender sender = source.getSender();

        if (!sender.hasPermission(rootPermission) && !sender.isOp()) {
            return List.of();
        }

        // args[] here is "including repeated spaces" according to Paper;
        // but for typical behavior you can treat it like normal args for partial matching.
        if (args.length <= 1) {
            String token = args.length == 0 ? "" : args[0];

            List<String> names = subcommands.stream().map(SubCommand::getName).toList();
            List<String> matches = new ArrayList<>();
            StringUtil.copyPartialMatches(token, names, matches);
            return matches;
        }

        // Delegate to the subcommand itself for deeper suggestions
        for (SubCommand subcommand : subcommands) {
            if (!args[0].equalsIgnoreCase(subcommand.getName())) continue;

            List<String> subs = subcommand.getSubcommands(sender, args);
            if (subs == null) subs = List.of();

            String last = args[args.length - 1];
            List<String> matches = new ArrayList<>();
            StringUtil.copyPartialMatches(last, subs, matches);
            return matches;
        }

        return List.of();
    }

    private static boolean hasAnyPermission(CommandSender sender, List<String> perms) {
        if (perms == null || perms.isEmpty()) return true;
        for (String perm : perms) {
            if (sender.hasPermission(perm)) return true;
        }
        return false;
    }
}
