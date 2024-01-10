/*
 * DimensionPause is a tool for dynamic dimension locking
 * Copyright TerrorByte (c) 2022-2024
 * Copyright DimensionPause Contributors (c) 2022-2024
 * 
 * This program is free software: You can redistribute it and/or modify it under the terms of the Mozilla Public License 2.0
 * as published by the Mozilla under the Mozilla Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including, without limitation,
 * warranties that the Covered Software is free of defects, merchantable, fit for a particular purpose or non-infringing.
 * See the MPL 2.0 license for more details.
 * 
 * For a full copy of the license in its entirety, please visit <https://www.mozilla.org/en-US/MPL/2.0/>
 */

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
import java.util.Arrays;
import java.util.List;

public class ConfigManager {
    private static YamlDocument config;
    private static YamlDocument languageFile;

    private static final List<String> languages = Arrays.asList("en_US");

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
        } catch (IOException e) {
            plugin.getLogger().severe(
                    "Could not create/load plugin config, disabling! Please alert the plugin author with the following info: "
                            + e);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }

        String language = config.getString("language");
        if (!(languages.contains(language))
                && Boolean.FALSE.equals(config.getBoolean("bypass-language-check"))) {
            plugin.getLogger().warning("Language is currently set to " + language
                    + ". This language is currently not supported, defaulting to en_US.");
            language = "en_US";
        }

        try {
            languageFile = YamlDocument.create(new File(new File(plugin.getDataFolder(), "lang"), language + ".yml"),
                    plugin.getResource("lang/" + language + ".yml"),
                    GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("language-version"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS).build());

            languageFile.update();
            languageFile.save();

            plugin.getLogger().info("Language set to: " + language);
        } catch (IOException e) {
            plugin.getLogger().severe(
                    "Could not load language file, disabling! Please alert the plugin author with the following info:"
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

    /**
     * Returns the plugin language file
     *
     * @return The YamlDocument object
     */
    public static YamlDocument getLanguageFile() {
        return languageFile;
    }
}
