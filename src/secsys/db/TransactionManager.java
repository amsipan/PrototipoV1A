package secsys.db;

import secsys.db.DbException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

public final class TransactionManager {

    private final DbConnection provider;

    public TransactionManager(DbConnection provider) {
        this.provider = provider;
    }

    public <T> T inTx(Function<Connection, T> fn) {
        try (Connection conn = provider.getConnection()) {
            boolean prevAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                T result = fn.apply(conn);
                conn.commit();
                conn.setAutoCommit(prevAuto);
                return result;
            } catch (Exception e) {
                try { conn.rollback(); } catch (SQLException ignored) {}
                throw e;
            }
        } catch (Exception e) {
            throw new DbException("Error en transacción: " + e.getMessage(), e);
        }
    }
}
