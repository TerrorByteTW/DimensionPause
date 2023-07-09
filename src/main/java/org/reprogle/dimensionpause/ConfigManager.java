package org.reprogle.dimensionpause;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private static YamlDocument config;

    /**
     * Sets up the plugin config and saves it to private variables for use later.
     * Will shut down the plugin if there are
     * any IOExceptions as these config files are non-negotiable in the function of
     * this plugin.
     *
     * @param plugin The DimensionPause Plugin object
     */
    public static void setupConfig(Plugin plugin) {
        plugin.getLogger().info("Attempting to load config files...");
        try {
            config = YamlDocument.create(new File(plugin.getDataFolder(), "config.yml"),
                    plugin.getResource("config.yml"), GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS).build());

            config.update();
            config.save();
        } catch (IOException e){
            plugin.getLogger().severe(
                    "Could not create/load plugin config, disabling! Please alert the plugin author with the following info: "
                            + e);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }

        plugin.getLogger().info("Successfully loaded all plugin config files!");
    }

    /**
     * Returns the plugin config object
     *
     * @return The YamlDocument object
     */
    public static YamlDocument getPluginConfig() {
        return config;
    }
}
