package secsys.repository;

import secsys.config.DbConfig;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HexFormat;

public class SettingsRepository {

    private Connection openConn() throws Exception {
        DbConfig cfg = DbConfig.fromEnv();
        return DriverManager.getConnection(cfg.jdbcUrl(), cfg.user(), cfg.password());
    }

    // ==========================
    // PARAMETROS (IVA, MONEDA)
    // ==========================
    public void upsertParam(String key, String value) throws Exception {
        final String sql =
                "INSERT INTO sgsis.parametro_config (clave, valor, actualizado_en) " +
                "VALUES (?, ?, now()) " +
                "ON CONFLICT (clave) DO UPDATE " +
                "SET valor = EXCLUDED.valor, actualizado_en = now()";

        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        }
    }

    public String getParam(String key) throws Exception {
        final String sql = "SELECT valor FROM sgsis.parametro_config WHERE clave = ? LIMIT 1";
        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getString("valor");
            }
        }
    }

    // ==========================
    // PLANTILLA CABECERA PDF
    // ==========================
    public void upsertPdfTemplate(String docId, String fileName, byte[] pdfBytes) throws Exception {
        String sha = sha256Hex(pdfBytes);

        final String sql =
                "INSERT INTO sgsis.plantilla_cabecera_pdf (doc_id, archivo_nombre, archivo_pdf, sha256, creado_en) " +
                "VALUES (?, ?, ?, ?, now()) " +
                "ON CONFLICT (doc_id) DO UPDATE SET " +
                "archivo_nombre = EXCLUDED.archivo_nombre, " +
                "archivo_pdf = EXCLUDED.archivo_pdf, " +
                "sha256 = EXCLUDED.sha256, " +
                "creado_en = now()";

        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, docId);
            ps.setString(2, fileName);
            ps.setBytes(3, pdfBytes);
            ps.setString(4, sha);

            ps.executeUpdate();
        }
    }

    public String getPdfTemplateSha(String docId) throws Exception {
        final String sql = "SELECT sha256 FROM sgsis.plantilla_cabecera_pdf WHERE doc_id = ? LIMIT 1";
        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, docId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getString("sha256");
            }
        }
    }

    // ==========================
    // RESPALDOS
    // ==========================
    public void saveBackupFrequency(String freq) throws Exception {
        // dejamos 1 sola fila “activa”
        final String sqlDel = "DELETE FROM sgsis.respaldo_programacion";
        final String sqlIns =
                "INSERT INTO sgsis.respaldo_programacion (frecuencia, actualizado_en) VALUES (?, now())";

        try (Connection conn = openConn()) {
            try (PreparedStatement ps1 = conn.prepareStatement(sqlDel)) {
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement(sqlIns)) {
                ps2.setString(1, freq);
                ps2.executeUpdate();
            }
        }
    }

    public String getBackupFrequency() throws Exception {
        final String sql =
                "SELECT frecuencia FROM sgsis.respaldo_programacion " +
                "ORDER BY actualizado_en DESC LIMIT 1";

        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return null;
            return rs.getString("frecuencia");
        }
    }

    public void logBackupExecution(String tipo, String estado, String mensaje) throws Exception {
        final String sql =
                "INSERT INTO sgsis.respaldo_ejecucion (tipo, estado, mensaje, creado_en) " +
                "VALUES (?, ?, ?, now())";

        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipo);
            ps.setString(2, estado);
            ps.setString(3, mensaje);
            ps.executeUpdate();
        }
    }

    // ==========================
    // Utils
    // ==========================
    private static String sha256Hex(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] dig = md.digest(data);
        return HexFormat.of().formatHex(dig);
    }
}
