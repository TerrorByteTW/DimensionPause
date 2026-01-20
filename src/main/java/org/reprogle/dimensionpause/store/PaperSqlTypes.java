package org.reprogle.dimensionpause.store;

import org.bukkit.World;
import org.reprogle.bytelib.db.api.SqlType;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;

public final class PaperSqlTypes {
    public static final SqlType<World.Environment> ENV = new SqlType<>() {
        public void bind(PreparedStatement ps, int index, World.Environment value) throws SQLException {
            ps.setString(index, value.toString());
        }

        public World.Environment read(ResultSet rs, String column) throws SQLException {
            String s = rs.getString(column);
            return World.Environment.valueOf(s);
        }
    };

    public static final SqlType<Instant> INSTANT = new SqlType<>() {
        public void bind(PreparedStatement ps, int index, Instant value) throws SQLException {
            if (value != null) ps.setLong(4, value.toEpochMilli());
            else ps.setNull(4, Types.BIGINT);
        }

        public @Nullable Instant read(ResultSet rs, String column) throws SQLException {
            Object raw = rs.getObject(column);
            Long expiresAtMs = (raw instanceof Number n) ? n.longValue() : null;
            return expiresAtMs != null ? Instant.ofEpochMilli(expiresAtMs) : null;
        }
    };
}
