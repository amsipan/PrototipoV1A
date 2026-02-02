package secsys.repository;

import secsys.config.DbConfig;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SystemSettingsRepository {

    private Connection openConn() throws Exception {
        DbConfig cfg = DbConfig.fromEnv();
        return DriverManager.getConnection(cfg.jdbcUrl(), cfg.user(), cfg.password());
    }

    public BigDecimal getIvaRate() throws Exception {

        final String sql =
                "SELECT valor " +
                "FROM sgsis.parametro_config " +
                "WHERE clave = ? " +
                "LIMIT 1";

        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "fin.iva");

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                BigDecimal v = rs.getBigDecimal("valor");
                if (v == null) return null;

                if (v.compareTo(BigDecimal.ONE) > 0) {
                    v = v.divide(new BigDecimal("100"), 6, BigDecimal.ROUND_HALF_UP);
                }

                if (v.compareTo(BigDecimal.ZERO) < 0 || v.compareTo(BigDecimal.ONE) > 0) return null;

                return v;
            }
        }
    }

    // ✅ NUEVO: obtener doc_id de cabecera para cotización (NO QUEMADO)
    public String getPdfHeaderDocId() throws Exception {

        // 1) Primero intenta por parámetro
        final String sqlParam =
                "SELECT valor " +
                "FROM sgsis.parametro_config " +
                "WHERE clave = ? " +
                "LIMIT 1";

        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sqlParam)) {

            ps.setString(1, "doc.pdf_last_id");

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String v = rs.getString("valor");
                    if (v != null && !v.trim().isEmpty()) return v.trim();
                }
            }
        }

        // 2) Si no hay parámetro, toma el último doc_id real en plantilla_cabecera_pdf
        final String sqlLast =
                "SELECT doc_id " +
                "FROM sgsis.plantilla_cabecera_pdf " +
                "ORDER BY creado_en DESC " +
                "LIMIT 1";

        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sqlLast);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) return null;
            String docId = rs.getString("doc_id");
            return (docId == null) ? null : docId.trim();
        }
    }
}
