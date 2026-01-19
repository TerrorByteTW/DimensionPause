package org.reprogle.dimensionpause.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldUtils {
    public static World getOverworld(World world) {
        World.Environment env = world.getEnvironment();

        if (env == World.Environment.NORMAL) {
            return world;
        }

        String name = world.getName();

        if (env == World.Environment.NETHER && name.endsWith("_nether")) {
            name = name.substring(0, name.length() - "_nether".length());
        } else if (env == World.Environment.THE_END && name.endsWith("_the_end")) {
            name = name.substring(0, name.length() - "_the_end".length());
        }

        return Bukkit.getWorld(name);
    }
}
