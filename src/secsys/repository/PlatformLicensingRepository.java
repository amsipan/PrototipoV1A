package secsys.repository;

import secsys.config.DbConfig;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlatformLicensingRepository {

    // ==========================
    // Exceptions
    // ==========================
    public static class DuplicatePlatformIdException extends Exception {
        public DuplicatePlatformIdException(String msg) { super(msg); }
    }

    // ==========================
    // DTO / Row
    // ==========================
    public static class PlatformLicenseRow {
        public UUID licenciamientoId;
        public String plataformaCodigo;       // KB4 / SMF
        public String nombreLicenciamiento;

        // KnowBe4 (USA)
        public Integer numeroUsuarios;
        public BigDecimal costoAnualPorUsuario;

        // Smartfense (LATAM)
        public BigDecimal costoAnualTotal;
        public String url;

        public String creadoEn;

        // Útil para JComboBox (muestra algo legible)
        @Override
        public String toString() {
            String p = (plataformaCodigo == null) ? "-" : plataformaCodigo.trim().toUpperCase();
            if ("KB4".equals(p)) return nombreLicenciamiento + " (KB4)";
            if ("SMF".equals(p)) return nombreLicenciamiento + " (SMF)";
            return nombreLicenciamiento + " (" + p + ")";
        }
    }

    // ==========================
    // DB
    // ==========================
    private Connection openConn() throws Exception {
        DbConfig cfg = DbConfig.fromEnv();
        return DriverManager.getConnection(cfg.jdbcUrl(), cfg.user(), cfg.password());
    }

    // ==========================
    // Helpers
    // ==========================
    private static PlatformLicenseRow mapRow(ResultSet rs) throws Exception {
        PlatformLicenseRow r = new PlatformLicenseRow();
        r.licenciamientoId = (UUID) rs.getObject("licenciamiento_id");
        r.plataformaCodigo = rs.getString("plataforma_codigo");
        r.nombreLicenciamiento = rs.getString("nombre_licenciamiento");

        Object nu = rs.getObject("numero_usuarios");
        r.numeroUsuarios = (nu == null) ? null : ((Number) nu).intValue();

        r.costoAnualPorUsuario = rs.getBigDecimal("costo_anual_por_usuario");
        r.costoAnualTotal = rs.getBigDecimal("costo_anual_total");
        r.url = rs.getString("url");

        try {
            Object ce = rs.getObject("creado_en");
            r.creadoEn = (ce == null) ? null : String.valueOf(ce);
        } catch (Exception ignore) {}

        return r;
    }

    // ==========================
    // Duplicado por (plataforma + nombre exacto)
    // ==========================
    public boolean existsByPlatformAndName(String platformCode, String licenseName) throws Exception {
        final String sql =
                "SELECT 1 " +
                "FROM sgsis.plataforma_licenciamiento " +
                "WHERE plataforma_codigo = ? AND lower(nombre_licenciamiento) = lower(?) " +
                "LIMIT 1";

        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, platformCode);
            ps.setString(2, licenseName);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ==========================
    // Insert KnowBe4 (KB4)
    // ==========================
    public void insertKnowBe4(String licenseName, int users, BigDecimal costPerUser) throws Exception {

        final String platformCode = "KB4";

        if (existsByPlatformAndName(platformCode, licenseName)) {
            throw new DuplicatePlatformIdException("Identificador de plataforma duplicado");
        }

        final String sql =
                "INSERT INTO sgsis.plataforma_licenciamiento " +
                "(plataforma_codigo, nombre_licenciamiento, numero_usuarios, costo_anual_por_usuario, costo_anual_total, url) " +
                "VALUES (?, ?, ?, ?, NULL, NULL)";

        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, platformCode);
            ps.setString(2, licenseName);
            ps.setInt(3, users);
            ps.setBigDecimal(4, costPerUser);

            ps.executeUpdate();
        }
    }

    // ==========================
    // Insert Smartfense (SMF)
    // ==========================
    public void insertSmartfense(String licenseName, BigDecimal costAnnualTotal, String url) throws Exception {

        final String platformCode = "SMF";

        if (existsByPlatformAndName(platformCode, licenseName)) {
            throw new DuplicatePlatformIdException("Identificador de plataforma duplicado");
        }

        final String sql =
                "INSERT INTO sgsis.plataforma_licenciamiento " +
                "(plataforma_codigo, nombre_licenciamiento, numero_usuarios, costo_anual_por_usuario, costo_anual_total, url) " +
                "VALUES (?, ?, NULL, NULL, ?, ?)";

        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, platformCode);
            ps.setString(2, licenseName);
            ps.setBigDecimal(3, costAnnualTotal);
            ps.setString(4, url);

            ps.executeUpdate();
        }
    }

    // ==========================
    // LIKE por nombre (coincidencia)
    // ==========================
    public List<PlatformLicenseRow> searchByLicenseNameLike(String licenseLike) throws Exception {

        final String sql =
                "SELECT licenciamiento_id, plataforma_codigo, nombre_licenciamiento, " +
                "       numero_usuarios, costo_anual_por_usuario, costo_anual_total, url, creado_en " +
                "FROM sgsis.plataforma_licenciamiento " +
                "WHERE lower(nombre_licenciamiento) LIKE lower(?) " +
                "ORDER BY nombre_licenciamiento ASC " +
                "LIMIT 50";

        List<PlatformLicenseRow> out = new ArrayList<>();

        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + licenseLike + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapRow(rs));
            }
        }

        return out;
    }


    public PlatformLicenseRow findById(UUID licenciamientoId) throws Exception {

        final String sql =
                "SELECT licenciamiento_id, plataforma_codigo, nombre_licenciamiento, " +
                "       numero_usuarios, costo_anual_por_usuario, costo_anual_total, url, creado_en " +
                "FROM sgsis.plataforma_licenciamiento " +
                "WHERE licenciamiento_id = ? " +
                "LIMIT 1";

        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, licenciamientoId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapRow(rs);
            }
        }
    }

    public int updateKnowBe4All(UUID licenciamientoId, int numeroUsuarios, BigDecimal costoAnualPorUsuario)
            throws Exception {

        final String sql =
                "UPDATE sgsis.plataforma_licenciamiento " +
                "SET numero_usuarios = ?, " +
                "    costo_anual_por_usuario = ?, " +
                "    costo_anual_total = NULL, " +
                "    url = NULL " +
                "WHERE licenciamiento_id = ? " +
                "  AND upper(plataforma_codigo) = 'KB4'";

        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, numeroUsuarios);
            ps.setBigDecimal(2, costoAnualPorUsuario);
            ps.setObject(3, licenciamientoId);

            return ps.executeUpdate();
        }
    }

    public int updateSmartfenseAll(UUID licenciamientoId, BigDecimal costoAnualTotal) throws Exception {

        final String sql =
                "UPDATE sgsis.plataforma_licenciamiento " +
                "SET costo_anual_total = ?, " +
                "    numero_usuarios = NULL, " +
                "    costo_anual_por_usuario = NULL " +
                "WHERE licenciamiento_id = ? " +
                "  AND upper(plataforma_codigo) = 'SMF'";

        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, costoAnualTotal);
            ps.setObject(2, licenciamientoId);

            return ps.executeUpdate();
        }
    }

    public List<PlatformLicenseRow> listByPlatformCode(String platformCode) throws Exception {

        final String sql =
                "SELECT licenciamiento_id, plataforma_codigo, nombre_licenciamiento, " +
                "       numero_usuarios, costo_anual_por_usuario, costo_anual_total, url " +
                "FROM sgsis.plataforma_licenciamiento " +
                "WHERE upper(plataforma_codigo) = upper(?) " +
                "ORDER BY nombre_licenciamiento ASC " +
                "LIMIT 200";

        List<PlatformLicenseRow> out = new ArrayList<>();

        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, platformCode);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapRow(rs));
            }
        }

        return out;
    }

}
