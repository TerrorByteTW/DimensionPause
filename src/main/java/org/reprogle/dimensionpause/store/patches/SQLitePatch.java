package org.reprogle.dimensionpause.store.patches;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public interface SQLitePatch {

    /**
     * The patch to apply
     *
     * @param c      The connection
     * @param logger The logger to log any potential errors
     * @throws SQLException Thrown if an error occurs
     */
    void update(Connection c, Logger logger) throws SQLException;

    /**
     * The user_version pragma that the database patch applies to. This allows us to ignore unnecessary patches
     *
     * @return user_version of patch
     */
    int patchedIn();
}
