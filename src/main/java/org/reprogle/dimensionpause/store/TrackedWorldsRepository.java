package org.reprogle.dimensionpause.store;

import com.google.inject.Inject;
import org.bukkit.World;
import org.reprogle.bytelib.db.api.Param;
import org.reprogle.bytelib.db.api.RowMapper;
import org.reprogle.bytelib.db.sqlite.SqliteDatabase;

import javax.annotation.Nullable;
import java.time.Instant;

public final class TrackedWorldsRepository {
    private static final RowMapper<TrackedWorld> TRACKED_WORLD_MAPPER =
            row -> new TrackedWorld(
                    row.string("world"),
                    row.get("dimension", PaperSqlTypes.ENV),
                    row.bool("enabled"),
                    row.get("expiresAt", PaperSqlTypes.INSTANT)
            );

    @Inject
    SqliteDatabase db;

    public void createSchema() {
        // The enabled column is backed by the SqlType of BOOLEAN, which converts to a 1 or 0 on the DB end
        // The updatedAt column is backed by the SqlType of STRING, which will simply put in a string of the date. This is only used for auditing purposes
        // The expiresAt column is backed by the SqlType of INSTANT, which stores the epoch millis in the column
        db.execute("""
                CREATE TABLE IF NOT EXISTS dimensionpause_worlds (
                `world` VARCHAR NOT NULL,
                `dimension` VARCHAR NOT NULL,
                `enabled` INTEGER NOT NULL,
                `updatedAt` DATE NOT NULL,
                `expiresAt` BIGINT NULL,
                PRIMARY KEY (`world`, `dimension`)
                )
                """);
    }

    public WorldPauseStatus setWorld(String world, World.Environment dimension, boolean enabled, @Nullable Instant expiresAt) {
        return db.transaction(tx -> {
            tx.execute("""
                    INSERT INTO dimensionpause_worlds (world, dimension, enabled, updatedAt, expiresAt)
                    VALUES (?, ?, ?, datetime('now'), ?)
                    ON CONFLICT(world, dimension) DO UPDATE SET
                    enabled = excluded.enabled,
                    updatedAt = datetime('now'),
                    expiresAt = CASE
                     WHEN excluded.expiresAt IS NOT NULL THEN excluded.expiresAt
                     WHEN excluded.enabled = 1 THEN NULL
                     ELSE expiresAt
                    END
                    """, Param.text(world), Tables.dimension.param(dimension), Param.bool(enabled), Tables.expiresAt.param(expiresAt));

            return new WorldPauseStatus(enabled, expiresAt);
        });
    }

    public WorldPauseStatus isWorldEnabled(String world, World.Environment dimension) {
        WorldPauseStatus status = db.queryOne("""
                        SELECT enabled, expiresAt
                        FROM dimensionpause_worlds
                        WHERE world = ? AND dimension = ?;
                        """,
                row -> new WorldPauseStatus(
                        row.bool("enabled"),
                        row.get("expiresAt", PaperSqlTypes.INSTANT)
                ),
                Param.text(world), Tables.dimension.param(dimension));

        if (status == null) return new WorldPauseStatus(true, null);
        return status;
    }

    public record WorldPauseStatus(
            boolean enabled,
            Instant expiresAt
    ) {
    }
}
