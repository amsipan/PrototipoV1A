package secsys.db;

import secsys.config.DbConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DbConnection {

    private final DbConfig cfg;


    public DbConnection(DbConfig cfg) {
        this.cfg = cfg;
    }

    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(cfg.jdbcUrl(), cfg.user(), cfg.password());

        // Refuerzo de schema por si el driver/URL no lo aplica como esperas
        try (Statement st = conn.createStatement()) {
            st.execute("SET search_path TO " + cfg.schema());
        }

        return conn;
    }
}
