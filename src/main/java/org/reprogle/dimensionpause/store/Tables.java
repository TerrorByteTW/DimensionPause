package org.reprogle.dimensionpause.store;

import org.bukkit.World;
import org.reprogle.bytelib.db.api.SqlType;
import org.reprogle.bytelib.db.api.Table;

import java.time.Instant;

public final class Tables {
    public static final Table WORLDS = Table.of("worlds");
    public static final Table.Column<String> world = WORLDS.col("world", SqlType.TEXT);
    public static final Table.Column<World.Environment> dimension = WORLDS.col("dimension", PaperSqlTypes.ENV);
    public static final Table.Column<Boolean> enabled = WORLDS.col("enabled", SqlType.BOOLEAN);
    public static final Table.Column<String> updatedAt = WORLDS.col("updatedAt", SqlType.TEXT);
    public static final Table.Column<Instant> expiresAt = WORLDS.col("expiresAt", PaperSqlTypes.INSTANT);

    private Tables() {}
}
