package secsys.repository;

import secsys.dto.ClienteBasicDTO;
import secsys.dto.ClienteCreateDTO;
import secsys.dto.ClienteInfoDTO;
import secsys.db.DbConnection;
import secsys.db.DbException;
import secsys.db.DbUtils;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClienteRepository extends BaseRepository {

    public ClienteRepository(DbConnection provider) {
        super(provider);
    }

    public UUID insert(ClienteCreateDTO dto) {

        String sql =
                "INSERT INTO sgsis.cliente (" +
                        "ruc, razon_social, direccion, representante_legal, telefono, correo, sector, tamano, fecha_inicio_contrato, fecha_fin_contrato" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                        "RETURNING cliente_id";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = provider.getConnection();
            ps = conn.prepareStatement(sql);

            ps.setString(1, dto.ruc);
            ps.setString(2, dto.razonSocial);
            ps.setString(3, dto.direccion);
            ps.setString(4, dto.representanteLegal);
            ps.setString(5, dto.telefono);
            ps.setString(6, dto.correo);
            ps.setString(7, dto.sector);
            ps.setString(8, dto.tamano);

            ps.setDate(9, Date.valueOf(dto.fechaInicioContrato));
            ps.setDate(10, Date.valueOf(dto.fechaFinContrato));

            rs = ps.executeQuery();
            if (rs.next()) return (UUID) rs.getObject(1);

            throw new DbException("No se devolvió cliente_id al insertar.");
        } catch (SQLException ex) {
            throw new DbException("Error insertando cliente: " + ex.getMessage(), ex);
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(conn);
        }
    }

    public ClienteInfoDTO findByRucExact(String ruc) throws Exception {
    final String sql =
            "SELECT cliente_id, ruc, razon_social " +
            "FROM sgsis.cliente " +
            "WHERE ruc = ? " +
            "LIMIT 1";

    try (Connection conn = provider.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, ruc);

        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return null;

            ClienteInfoDTO dto = new ClienteInfoDTO();
            dto.clienteId = (UUID) rs.getObject("cliente_id");
            dto.ruc = rs.getString("ruc");
            dto.razonSocial = rs.getString("razon_social");
            return dto;
        }
    }
}


    public ClienteInfoDTO findByRuc(String ruc) {
        String sql =
                "SELECT cliente_id, ruc, razon_social, direccion, representante_legal, telefono, correo, " +
                "       sector, tamano, estado, fecha_inicio_contrato, fecha_fin_contrato " +
                "FROM sgsis.cliente " +
                "WHERE ruc = ? " +
                "LIMIT 1";

        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ruc);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                ClienteInfoDTO dto = new ClienteInfoDTO();
                dto.clienteId = (UUID) rs.getObject("cliente_id");
                dto.ruc = rs.getString("ruc");
                dto.razonSocial = rs.getString("razon_social");
                dto.direccion = rs.getString("direccion");
                dto.representanteLegal = rs.getString("representante_legal");
                dto.telefono = rs.getString("telefono");
                dto.correo = rs.getString("correo");
                dto.sector = rs.getString("sector");
                dto.tamano = rs.getString("tamano");
                dto.estado = rs.getString("estado");

                Date ini = rs.getDate("fecha_inicio_contrato");
                Date fin = rs.getDate("fecha_fin_contrato");
                dto.fechaInicioContrato = (ini == null) ? null : ((java.sql.Date) ini).toLocalDate();
                dto.fechaFinContrato = (fin == null) ? null : ((java.sql.Date) fin).toLocalDate();

                return dto;
            }

        } catch (SQLException ex) {
            throw new DbException("Error consultando cliente por RUC: " + ex.getMessage(), ex);
        }
    }

    public ClienteBasicDTO findBasicByRuc(String ruc) {
        final String sql = "SELECT cliente_id, razon_social FROM sgsis.cliente WHERE ruc = ? LIMIT 1";

        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ruc);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                ClienteBasicDTO dto = new ClienteBasicDTO();
                dto.clienteId = (UUID) rs.getObject("cliente_id");
                dto.razonSocial = rs.getString("razon_social");
                return dto;
            }

        } catch (SQLException ex) {
            throw new DbException("Error consultando cliente por RUC: " + ex.getMessage(), ex);
        }
    }

    // ==========================================================
    // ✅ NUEVO: buscar por razón social (CONTiene) ignorando caso (ILIKE)
    // ==========================================================
    public List<ClienteInfoDTO> findByRazonSocialLikeIgnoreCase(String razonSocialFragmento) {
        if (razonSocialFragmento == null || razonSocialFragmento.trim().isEmpty()) {
            throw new DbException("razonSocial es obligatoria.");
        }

        final String sql =
                "SELECT cliente_id, ruc, razon_social, direccion, representante_legal, telefono, correo, " +
                "       sector, tamano, estado, fecha_inicio_contrato, fecha_fin_contrato " +
                "FROM sgsis.cliente " +
                "WHERE razon_social ILIKE ('%' || ? || '%') " +
                "ORDER BY razon_social ASC, ruc ASC";

        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, razonSocialFragmento.trim());

            List<ClienteInfoDTO> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ClienteInfoDTO dto = new ClienteInfoDTO();
                    dto.clienteId = (UUID) rs.getObject("cliente_id");
                    dto.ruc = rs.getString("ruc");
                    dto.razonSocial = rs.getString("razon_social");
                    dto.direccion = rs.getString("direccion");
                    dto.representanteLegal = rs.getString("representante_legal");
                    dto.telefono = rs.getString("telefono");
                    dto.correo = rs.getString("correo");
                    dto.sector = rs.getString("sector");
                    dto.tamano = rs.getString("tamano");
                    dto.estado = rs.getString("estado");

                    Date ini = rs.getDate("fecha_inicio_contrato");
                    Date fin = rs.getDate("fecha_fin_contrato");
                    dto.fechaInicioContrato = (ini == null) ? null : ((java.sql.Date) ini).toLocalDate();
                    dto.fechaFinContrato = (fin == null) ? null : ((java.sql.Date) fin).toLocalDate();

                    out.add(dto);
                }
            }
            return out;

        } catch (SQLException ex) {
            throw new DbException("Error consultando cliente por razón social: " + ex.getMessage(), ex);
        }
    }

    // ==========================================================
    // ✅ NUEVO: actualizar por cliente_id (lo usa ClientUpdatePanel)
    // ==========================================================
    public void updateById(UUID clienteId,
                           String direccion,
                           String representanteLegal,
                           String telefono,
                           String correo,
                           String estado) {

        if (clienteId == null) throw new DbException("clienteId es obligatorio.");
        if (estado == null || estado.isBlank()) throw new DbException("estado es obligatorio.");

        final String sql =
                "UPDATE sgsis.cliente SET " +
                "direccion = ?, " +
                "representante_legal = ?, " +
                "telefono = ?, " +
                "correo = ?, " +
                "estado = ? " +
                "WHERE cliente_id = ?";

        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emptyToNull(direccion));
            ps.setString(2, emptyToNull(representanteLegal));
            ps.setString(3, emptyToNull(telefono));
            ps.setString(4, emptyToNull(correo));
            ps.setString(5, estado.trim());
            ps.setObject(6, clienteId);

            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new DbException("No se actualizó ningún registro. cliente_id no existe.");
            }

        } catch (SQLException ex) {
            throw new DbException("Error actualizando cliente: " + ex.getMessage(), ex);
        }
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String x = s.trim();
        return x.isEmpty() ? null : x;
    }

    public List<ClienteBasicDTO> findBasicByRazonSocialLikeIgnoreCase(String razon) {
        
        if (razon == null) razon = "";
        String term = razon.trim();
        if (term.isEmpty()) return java.util.Collections.emptyList();
        
        final String sql =
                "SELECT cliente_id, razon_social " +
                "FROM sgsis.cliente " +
                "WHERE razon_social ILIKE ('%' || ? || '%') " +
                "ORDER BY razon_social ASC";
        
        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, term);
            
            List<ClienteBasicDTO> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ClienteBasicDTO dto = new ClienteBasicDTO();
                    dto.clienteId = (UUID) rs.getObject("cliente_id");
                    dto.razonSocial = rs.getString("razon_social");
                    out.add(dto);
                }
            }
            return out;
        
        } catch (SQLException ex) {
            throw new DbException("Error consultando cliente por razón social: " + ex.getMessage(), ex);
        }
    }

}
