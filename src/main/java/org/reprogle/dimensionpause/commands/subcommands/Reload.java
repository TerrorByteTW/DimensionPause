package org.reprogle.dimensionpause.commands.subcommands;

import com.google.inject.Inject;
import org.bukkit.command.CommandSender;
import org.reprogle.bytelib.config.BytePluginConfig;
import org.reprogle.dimensionpause.commands.CommandFeedback;
import org.reprogle.dimensionpause.commands.SubCommand;
import org.reprogle.dimensionpause.utils.DimensionExpirationTimer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Reload implements SubCommand {
    @Inject
    BytePluginConfig config;
    @Inject
    CommandFeedback commandFeedback;
    @Inject
    DimensionExpirationTimer timer;

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        try {
            config.config().reload();
            timer.refresh();

            sender.sendMessage(commandFeedback.sendCommandFeedback("reload", null, null));

        } catch (IOException e) {
            // Nothing
        }
    }

    @Override
    public List<String> getSubcommands(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public List<String> getRequiredPermissions() {
        List<String> permissions = new ArrayList<>();
        permissions.add("dimensionpause.reload");
        return permissions;
    }
}
