package org.reprogle.dimensionpause.store;

import org.bukkit.World;
import org.reprogle.dimensionpause.DimensionPausePlugin;

import javax.annotation.Nullable;
import java.sql.*;
import java.time.Instant;
import java.util.Arrays;

public abstract class Database {
    private static final String WORLD_TABLE = "dimensionpause_worlds";
    private static final String SELECT = "SELECT enabled, expiresAt FROM ";
    private static final String INSERT_INTO = "INSERT INTO ";
    private static final String WHERE = " WHERE world = ? AND dimension = ? LIMIT 1;";

    final DimensionPausePlugin plugin;
    Connection connection;

    protected Database(DimensionPausePlugin plugin) {
        this.plugin = plugin;
    }

    public abstract Connection getSQLConnection();

    public WorldPauseStatus setWorld(String world, World.Environment dimension, boolean enabled, @Nullable Instant expiresAt) {

        try (Connection c = getSQLConnection(); PreparedStatement ps = c.prepareStatement(
                INSERT_INTO + WORLD_TABLE +
                        " (world, dimension, enabled, updatedAt, expiresAt) " +
                        "VALUES (?, ?, ?, datetime('now'), ?) " +
                        "ON CONFLICT(world, dimension) DO UPDATE SET " +
                        "enabled = excluded.enabled, " +
                        "updatedAt = datetime('now'), " +
                        "expiresAt = CASE " +
                        "  WHEN excluded.expiresAt IS NOT NULL THEN excluded.expiresAt " +
                        "  WHEN excluded.enabled = 1 THEN NULL " +
                        "  ELSE expiresAt " +
                        "END"
        )) {
            try {

                ps.setString(1, world);
                ps.setString(2, dimension.toString());
                ps.setInt(3, enabled ? 1 : 0);

                if (expiresAt != null) ps.setLong(4, expiresAt.toEpochMilli());
                else ps.setNull(4, Types.BIGINT);

                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error while executing create SQL statement: " + e);
                plugin.getLogger().severe(Arrays.toString(e.getStackTrace()));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close SQL Database connection: " + e);
        }

        return new WorldPauseStatus(enabled, expiresAt);
    }

    public WorldPauseStatus isWorldEnabled(String world, World.Environment dimension) {
        try (Connection c = getSQLConnection(); PreparedStatement ps = c.prepareStatement(SELECT + WORLD_TABLE + WHERE)) {
            ps.setString(1, world);
            ps.setString(2, dimension.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return new WorldPauseStatus(true, null);
                }

                boolean enabled = rs.getInt("enabled") == 1;
                Object raw = rs.getObject("expiresAt");
                Long expiresAtMs = (raw instanceof Number n) ? n.longValue() : null;
                Instant expiresAt = expiresAtMs != null ? Instant.ofEpochMilli(expiresAtMs) : null;

                enabled = enabled && (expiresAt == null || expiresAt.isBefore(Instant.now()));

                return new WorldPauseStatus(enabled, expiresAt);
            } catch (SQLException e) {
                plugin.getLogger().severe("Error while executing create SQL statement: " + e);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close SQL Database connection: " + e);
        }

        return new WorldPauseStatus(true, null);
    }

    public record WorldPauseStatus(
            boolean enabled,
            Instant expiresAt
    ) {
    }
}
