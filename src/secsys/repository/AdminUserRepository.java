package secsys.repository;

import secsys.config.DbConfig;
import secsys.dto.UsuarioInfoDTO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminUserRepository {

    public static class UserRow {
        public UUID usuarioId;
        public String cedula;
        public String apellidos;
        public String nombres;
        public String username;
        public String correo;
        public String rol;
        public String estado;
    }

    private Connection openConn() throws Exception {
        DbConfig cfg = DbConfig.fromEnv();
        return DriverManager.getConnection(cfg.jdbcUrl(), cfg.user(), cfg.password());
    }

    // ==========================
    // LISTAR activos
    // ==========================
    public List<UserRow> searchActiveAll() throws Exception {
        final String sql =
                "SELECT usuario_id, cedula, apellidos, nombres, username, correo, rol, estado " +
                "FROM sgsis.usuario " +
                "WHERE estado = 'Activo' " +
                "ORDER BY apellidos, nombres";

        List<UserRow> out = new ArrayList<>();

        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) out.add(mapRow(rs));
        }
        return out;
    }

    // ==========================
    // ACTIVOS por username (COINCIDENCIA LIKE)
    // ==========================
    public List<UserRow> searchActiveByUsernameLike(String usernameLike) throws Exception {
        final String sql =
                "SELECT usuario_id, cedula, apellidos, nombres, username, correo, rol, estado " +
                "FROM sgsis.usuario " +
                "WHERE estado = 'Activo' AND lower(username) LIKE lower(?) " +
                "ORDER BY username ASC " +
                "LIMIT 50";

        List<UserRow> out = new ArrayList<>();

        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + usernameLike + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapRow(rs));
            }
        }
        return out;
    }

    // ==========================
    // ACTIVOS por cédula (MATCH EXACTO)
    // ==========================
    public List<UserRow> searchActiveByCedulaExact(String cedula) throws Exception {
        final String sql =
                "SELECT usuario_id, cedula, apellidos, nombres, username, correo, rol, estado " +
                "FROM sgsis.usuario " +
                "WHERE estado = 'Activo' AND cedula = ? " +
                "ORDER BY username ASC";

        List<UserRow> out = new ArrayList<>();

        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cedula);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapRow(rs));
            }
        }
        return out;
    }

    // ==========================
    // (Opcional) Métodos que ya tenías
    // ==========================
    public List<UserRow> searchByUsernameLike(String usernameLike) throws Exception {
        final String sql =
                "SELECT usuario_id, cedula, apellidos, nombres, username, correo, rol, estado " +
                "FROM sgsis.usuario " +
                "WHERE lower(username) LIKE lower(?) " +
                "ORDER BY username ASC " +
                "LIMIT 50";

        List<UserRow> out = new ArrayList<>();

        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + usernameLike + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapRow(rs));
            }
        }
        return out;
    }

    public List<UsuarioInfoDTO> findUserInfoByUsernameLike(String usernameLike) throws Exception {
        final String sql =
                "SELECT usuario_id, cedula, apellidos, nombres, username, correo, rol, estado " +
                "FROM sgsis.usuario " +
                "WHERE lower(username) LIKE lower(?) " +
                "ORDER BY username ASC " +
                "LIMIT 50";
        
        List<UsuarioInfoDTO> out = new ArrayList<>();
        
        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, "%" + usernameLike + "%");
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UsuarioInfoDTO dto = new UsuarioInfoDTO();
                    dto.usuarioId = (UUID) rs.getObject("usuario_id");
                    dto.cedula = rs.getString("cedula");
                    dto.apellidos = rs.getString("apellidos");
                    dto.nombres = rs.getString("nombres");
                    dto.username = rs.getString("username");
                    dto.correo = rs.getString("correo");
                    dto.rol = rs.getString("rol");
                    dto.estado = rs.getString("estado");
                    out.add(dto);
                }
            }
        }
        return out;
    }


    public List<UserRow> searchByCedulaExact(String cedula) throws Exception {
        final String sql =
                "SELECT usuario_id, cedula, apellidos, nombres, username, correo, rol, estado " +
                "FROM sgsis.usuario " +
                "WHERE cedula = ? " +
                "ORDER BY username ASC";

        List<UserRow> out = new ArrayList<>();

        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cedula);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapRow(rs));
            }
        }
        return out;
    }

    public int disableUser(UUID usuarioId) throws Exception {
        final String sql =
                "UPDATE sgsis.usuario " +
                "SET estado = 'Inactivo', actualizado_en = now() " +
                "WHERE usuario_id = ?";

        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, usuarioId);
            return ps.executeUpdate();
        }
    }

    private static UserRow mapRow(ResultSet rs) throws Exception {
        UserRow r = new UserRow();
        r.usuarioId = (UUID) rs.getObject("usuario_id");
        r.cedula = rs.getString("cedula");
        r.apellidos = rs.getString("apellidos");
        r.nombres = rs.getString("nombres");
        r.username = rs.getString("username");
        r.correo = rs.getString("correo");
        r.rol = rs.getString("rol");
        r.estado = rs.getString("estado");
        return r;
    }

    public UsuarioInfoDTO findUserInfoByCedulaExact(String cedula) throws Exception {
        final String sql =
                "SELECT usuario_id, cedula, apellidos, nombres, username, correo, rol, estado " +
                "FROM sgsis.usuario " +
                "WHERE cedula = ? " +
                "LIMIT 1";

        try (Connection conn = openConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cedula);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                UsuarioInfoDTO dto = new UsuarioInfoDTO();
                dto.usuarioId = (UUID) rs.getObject("usuario_id");
                dto.cedula = rs.getString("cedula");
                dto.apellidos = rs.getString("apellidos");
                dto.nombres = rs.getString("nombres");
                dto.username = rs.getString("username");
                dto.correo = rs.getString("correo");
                dto.rol = rs.getString("rol");
                dto.estado = rs.getString("estado");
                return dto;
            }
        }
    }

}
