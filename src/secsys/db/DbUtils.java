package secsys.db;

import java.sql.*;

public final class DbUtils {
    private DbUtils() {}

    public static void closeQuietly(AutoCloseable c) {
        if (c == null) return;
        try { c.close(); } catch (Exception ignored) {}
    }

    public static void rollbackQuietly(Connection conn) {
        if (conn == null) return;
        try { conn.rollback(); } catch (SQLException ignored) {}
    }

    public static void setAutoCommitQuietly(Connection conn, boolean autoCommit) {
        if (conn == null) return;
        try { conn.setAutoCommit(autoCommit); } catch (SQLException ignored) {}
    }
}
