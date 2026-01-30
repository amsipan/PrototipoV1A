package secsys.repository;

import secsys.db.DbConnection;
import secsys.db.DbException;
import secsys.dto.UsuarioCreateDTO;
import secsys.dto.UsuarioInfoDTO;

import java.sql.*;
import java.util.UUID;

public class UsuarioRepository extends BaseRepository {

    public UsuarioRepository(DbConnection provider) {
        super(provider);
    }

    // =========================
    // INSERT
    // =========================
    public UUID insert(UsuarioCreateDTO dto) {
        if (dto == null) throw new DbException("DTO null.");
        if (dto.cedula == null || dto.cedula.isBlank()) throw new DbException("cedula obligatoria.");
        if (dto.nombres == null || dto.nombres.isBlank()) throw new DbException("nombres obligatorios.");
        if (dto.apellidos == null || dto.apellidos.isBlank()) throw new DbException("apellidos obligatorios.");
        if (dto.username == null || dto.username.isBlank()) throw new DbException("username obligatorio.");
        if (dto.correo == null || dto.correo.isBlank()) throw new DbException("correo obligatorio.");
        if (dto.rol == null || dto.rol.isBlank()) throw new DbException("rol obligatorio.");
        if (dto.password == null || dto.password.isBlank()) throw new DbException("password obligatorio.");

        final String sql =
                "INSERT INTO sgsis.usuario " +
                "(cedula, nombres, apellidos, username, correo, rol, password_hash, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, public.crypt(?::text, public.gen_salt('bf')), 'Activo') " +
                "RETURNING usuario_id";

        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dto.cedula.trim());
            ps.setString(2, dto.nombres.trim());
            ps.setString(3, dto.apellidos.trim());
            ps.setString(4, dto.username.trim());
            ps.setString(5, dto.correo.trim());
            ps.setString(6, dto.rol.trim());
            ps.setString(7, dto.password);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new DbException("No se devolvió usuario_id.");
                return (UUID) rs.getObject(1);
            }

        } catch (SQLException ex) {
            throw new DbException("Error insertando usuario: " + ex.getMessage(), ex);
        }
    }

    // =========================
    // AUTHENTICATE (username case-insensitive + estado Activo)
    // =========================
    public UsuarioInfoDTO authenticate(String username, String password, String rol) {
        if (username == null || username.isBlank()) return null;
        if (password == null || password.isBlank()) return null;
        if (rol == null || rol.isBlank()) return null;

        final String sql =
                "SELECT usuario_id, cedula, nombres, apellidos, username, correo, rol, estado " +
                "FROM sgsis.usuario " +
                "WHERE lower(username) = lower(?) " +
                "  AND password_hash = public.crypt(?::text, password_hash) " +
                "  AND lower(rol) = lower(?)" +
                "  AND estado = 'Activo' " +
                "LIMIT 1";

        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ps.setString(2, password);
            ps.setString(3, rol.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                UsuarioInfoDTO u = new UsuarioInfoDTO();
                u.usuarioId = (UUID) rs.getObject("usuario_id");
                u.cedula = rs.getString("cedula");
                u.nombres = rs.getString("nombres");
                u.apellidos = rs.getString("apellidos");
                u.username = rs.getString("username");
                u.correo = rs.getString("correo");
                u.rol = rs.getString("rol");
                u.estado = rs.getString("estado");
                return u;
            }

        } catch (SQLException ex) {
            throw new DbException("Error autenticando usuario: " + ex.getMessage(), ex);
        }
    }

    // =========================
    // FIND BY USERNAME (case-insensitive)
    // =========================
    public UsuarioInfoDTO findByUsernameIgnoreCase(String username) {
        if (username == null || username.isBlank()) return null;

        final String sql =
                "SELECT usuario_id, cedula, nombres, apellidos, username, correo, rol, estado " +
                "FROM sgsis.usuario " +
                "WHERE lower(username) = lower(?) " +
                "LIMIT 1";

        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                UsuarioInfoDTO u = new UsuarioInfoDTO();
                u.usuarioId = (UUID) rs.getObject("usuario_id");
                u.cedula = rs.getString("cedula");
                u.nombres = rs.getString("nombres");
                u.apellidos = rs.getString("apellidos");
                u.username = rs.getString("username");
                u.correo = rs.getString("correo");
                u.rol = rs.getString("rol");
                u.estado = rs.getString("estado");
                return u;
            }

        } catch (SQLException ex) {
            throw new DbException("Error consultando usuario: " + ex.getMessage(), ex);
        }
    }

    // =========================
    // UPDATE (no modifica cedula/nombres/apellidos/username)
    // passwordPlainOrNull: si null/blank => no cambia contraseña
    // =========================
    public void updateEditableFields(UUID usuarioId, String correo, String rol, String passwordPlainOrNull) {
        if (usuarioId == null) throw new DbException("usuarioId es obligatorio.");
        if (correo == null || correo.isBlank()) throw new DbException("correo es obligatorio.");
        if (rol == null || rol.isBlank()) throw new DbException("rol es obligatorio.");

        final boolean changePass = passwordPlainOrNull != null && !passwordPlainOrNull.isBlank();

        final String sqlNoPass =
                "UPDATE sgsis.usuario SET correo = ?, rol = ? WHERE usuario_id = ?";

        final String sqlWithPass =
                "UPDATE sgsis.usuario SET correo = ?, rol = ?, " +
                "password_hash = public.crypt(?::text, public.gen_salt('bf')) " +
                "WHERE usuario_id = ?";

        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(changePass ? sqlWithPass : sqlNoPass)) {

            ps.setString(1, correo.trim());
            ps.setString(2, rol.trim());

            if (changePass) {
                ps.setString(3, passwordPlainOrNull);
                ps.setObject(4, usuarioId);
            } else {
                ps.setObject(3, usuarioId);
            }

            int updated = ps.executeUpdate();
            if (updated == 0) throw new DbException("No se actualizó ningún registro (usuario_id no existe).");

        } catch (SQLException ex) {
            throw new DbException("Error actualizando usuario: " + ex.getMessage(), ex);
        }
    }

    // (Opcional) si luego quieres activar/inactivar usuarios desde GUI
    public void setEstado(UUID usuarioId, String estado) {
        if (usuarioId == null) throw new DbException("usuarioId obligatorio.");
        if (estado == null || estado.isBlank()) throw new DbException("estado obligatorio.");

        final String sql = "UPDATE sgsis.usuario SET estado = ? WHERE usuario_id = ?";

        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, estado.trim());
            ps.setObject(2, usuarioId);

            int updated = ps.executeUpdate();
            if (updated == 0) throw new DbException("No se actualizó ningún registro (usuario_id no existe).");

        } catch (SQLException ex) {
            throw new DbException("Error actualizando estado: " + ex.getMessage(), ex);
        }
    }

    public boolean existsByUsername(String username) {
        if (username == null || username.isBlank()) return false;

        final String sql =
                "SELECT 1 " +
                "FROM sgsis.usuario " +
                "WHERE lower(username) = lower(?) " +
                "LIMIT 1";

        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException ex) {
            throw new DbException("Error verificando usuario: " + ex.getMessage(), ex);
        }
    }


    public boolean existsByCorreo(String correo) {
        if (correo == null || correo.isBlank()) return false;

        final String sql =
                "SELECT 1 " +
                "FROM sgsis.usuario " +
                "WHERE lower(correo) = lower(?) " +
                "LIMIT 1";

        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, correo.trim());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException ex) {
            throw new DbException("Error verificando correo: " + ex.getMessage(), ex);
        }
    }


}
