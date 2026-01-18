package org.reprogle.dimensionpause.store;

import org.bukkit.World;
import org.reprogle.dimensionpause.DimensionPausePlugin;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    public void setWorld(String world, World.Environment dimension, boolean enabled, @Nullable LocalDate expiresAt) {
        Connection c = null;
        PreparedStatement ps = null;

        try {
            c = getSQLConnection();
            ps = c.prepareStatement(
                    INSERT_INTO + WORLD_TABLE +
                            " (world, dimension, enabled, updatedAt, expiresAt) " +
                            "VALUES (?, ?, ?, datetime('now'), ?) " +
                            "ON CONFLICT(world, dimension) DO UPDATE SET " +
                            "enabled = excluded.enabled, " +
                            "updatedAt = datetime('now'), " +
                            "expiresAt = CASE " +
                            "WHEN excluded.expiresAt IS NOT NULL THEN excluded.expiresAt " +
                            "WHEN excluded.enabled = 1 AND expiresAt IS NOT NULL AND expiresAt <= datetime('now') THEN NULL " +
                            "ELSE expiresAt " +
                            "END"
            );

            ps.setString(1, world);
            ps.setString(2, dimension.toString());
            ps.setInt(3, enabled ? 1 : 0);
            ps.setObject(4, expiresAt);

            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error while executing create SQL statement on block table: " + e);
        } finally {
            try {
                if (ps != null) ps.close();
                if (c != null) c.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to close SQL Database connection: " + e);
            }
        }
    }

    public WorldPauseStatus isWorldEnabled(String world, World.Environment dimension) {
        try (Connection c = getSQLConnection(); PreparedStatement ps = c.prepareStatement(SELECT + WORLD_TABLE + WHERE)) {
            ps.setString(1, world);
            ps.setString(2, dimension.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return new WorldPauseStatus(false, null);
                }

                boolean enabled = rs.getInt("enabled") == 1;
                LocalDateTime expiresAt = null;
                String expiresRaw = rs.getString("expiresAt");
                if (expiresRaw != null) {
                    expiresAt = LocalDateTime.parse(expiresRaw, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }

                enabled = enabled || (expiresAt != null && expiresAt.isBefore(LocalDateTime.now()));

                return new WorldPauseStatus(enabled, expiresAt);
            } catch (SQLException e) {
                plugin.getLogger().severe("Error while executing create SQL statement on block table: " + e);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close SQL Database connection: " + e);
        }

        return new WorldPauseStatus(false, null);
    }

    public record WorldPauseStatus(
            boolean enabled,
            LocalDateTime expiresAt
    ) {
    }
}
