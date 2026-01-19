package org.reprogle.dimensionpause.store;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.reprogle.dimensionpause.DimensionPausePlugin;
import org.reprogle.dimensionpause.store.patches.SQLitePatch;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Singleton
public class SQLite extends Database {
    private final DimensionPausePlugin plugin;
    private final Logger logger;

    private final List<SQLitePatch> patches = new ArrayList<>();
    private final int DB_VERSION = 1;

    private final String SQLITE_CREATE_WORLDS_TABLE = "CREATE TABLE IF NOT EXISTS dimensionpause_worlds (" +
            "`world` VARCHAR NOT NULL," +
            "`dimension` VARCHAR NOT NULL," +
            "`enabled` INTEGER NOT NULL," +
            "`updatedAt` DATE NOT NULL," +
            "`expiresAt` BIGINT NULL," +
            "PRIMARY KEY (`world`, `dimension`)" +
            ")";

    private final String SET_PRAGMA = "PRAGMA user_version = " + DB_VERSION + ";";

    @Inject
    public SQLite(DimensionPausePlugin plugin, Logger logger) {
        super(plugin);
        this.logger = logger;
        this.plugin = plugin;

        connection = getSQLConnection();
        try (Statement s = connection.createStatement()) {
            PreparedStatement ps = connection.prepareStatement("PRAGMA user_version;");
            ResultSet rs = ps.executeQuery();
            int userVersion = rs.getInt("user_version");

            boolean upgradeNecessary = checkIfUpgradeNecessary(connection, userVersion);
            if (!upgradeNecessary) {
                s.executeUpdate(SQLITE_CREATE_WORLDS_TABLE);
            } else {
                for (SQLitePatch patch : patches) {
                    // We're gonna close and reopen the connection for every patch to ensure a fresh connection and no locks
                    if (!connection.isClosed()) connection.close();

                    // Only apply the patch if the current version of the DB is less than the version of the DB patch
                    if (userVersion < patch.patchedIn()) {
                        // Apply the patch
                        connection = getSQLConnection();
                        patch.update(connection, logger);
                    }
                }
            }
        } catch (SQLException e) {
            logger.severe("SQLException occurred while creating SQLite connection: " + e.getMessage());
            logger.severe("Full stack" + Arrays.toString(e.getStackTrace()));
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                logger.severe("Failed to close SQLite Connection: " + e);
            }
        }

        connection = getSQLConnection();
        try (Statement s = connection.createStatement()) {
            s.executeUpdate(SET_PRAGMA);
        } catch (
                SQLException e) {
            logger.severe("SQLException occurred while creating SQLite connection: " + e.getMessage());
            logger.severe("Full stack" + Arrays.toString(e.getStackTrace()));
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                logger.severe("Failed to close SQLite Connection: " + e);
            }
        }
    }

    public Connection getSQLConnection() {
        File dataFolder = new File(plugin.getDataFolder(), "dimensionpause.db");
        if (!dataFolder.exists()) {
            try {
                boolean success = dataFolder.createNewFile();
                if (success) {
                    logger.info("Created data folder");
                } else {
                    logger.severe("Could not create data folder!");
                }
            } catch (IOException e) {
                logger.severe("Could not create dimensionpause.db file");
            }
        }

        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return connection;

        } catch (SQLException e) {
            logger.severe("SQLite exception on initialize: " + e);
        } catch (ClassNotFoundException e) {
            logger.severe("SQLite JDBC Library not found. Please install this on your host to use SQLite: " + e);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }

        return null;
    }

    public boolean checkIfUpgradeNecessary(Connection connection, int userVersion) {
        boolean alreadyInitialized;
        boolean tablesExist;

        alreadyInitialized = userVersion >= DB_VERSION;

        // Then we check if any tables exist at all in the DB
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%';");
            ResultSet rs = ps.executeQuery();
            tablesExist = rs.next();
        } catch (SQLException e) {
            tablesExist = false;
        }

        return (!alreadyInitialized && tablesExist);
    }
}
