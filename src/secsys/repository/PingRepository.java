package secsys.repository;

import secsys.db.DbConnection;
import secsys.db.DbException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PingRepository extends BaseRepository {

    public PingRepository(DbConnection provider) {
        super(provider);
    }

    public boolean ping() {
        final String sql = "SELECT 1";
        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next();
        } catch (Exception e) {
            throw new DbException("No se pudo hacer ping a la base: " + e.getMessage(), e);
        }
    }
}
