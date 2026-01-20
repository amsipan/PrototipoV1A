package secsys.repository;

import secsys.dto.ClienteBasicDTO;
import secsys.dto.ClienteCreateDTO;
import secsys.dto.ClienteInfoDTO;
import secsys.db.DbConnection;
import secsys.db.DbException;
import secsys.db.DbUtils;

import java.sql.*;
import java.text.Normalizer;
import java.time.LocalDate;
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
                dto.fechaInicioContrato = (ini == null) ? null : ini.toLocalDate();
                dto.fechaFinContrato = (fin == null) ? null : fin.toLocalDate();

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

    /**
     * Actualiza un cliente por cliente_id.
     * - NO actualiza el RUC (queda bloqueado en la UI).
     */
    public void updateById(UUID clienteId,
                           String razonSocial,
                           String direccion,
                           String representanteLegal,
                           String telefono,
                           String correo,
                           String sector,
                           String tamano,
                           String estado,
                           LocalDate fechaInicioContrato,
                           LocalDate fechaFinContrato) {

        if (clienteId == null) throw new DbException("clienteId es obligatorio.");
        if (razonSocial == null || razonSocial.isBlank()) throw new DbException("razonSocial es obligatorio.");
        if (sector == null || sector.isBlank()) throw new DbException("sector es obligatorio.");
        if (tamano == null || tamano.isBlank()) throw new DbException("tamano es obligatorio.");
        if (estado == null || estado.isBlank()) throw new DbException("estado es obligatorio.");
        if (fechaInicioContrato == null) throw new DbException("fechaInicioContrato es obligatoria.");
        if (fechaFinContrato == null) throw new DbException("fechaFinContrato es obligatoria.");
        

        String razonSocialN = nfcTrim(razonSocial);
        String sectorN = nfcTrim(sector);
        String tamanoN = nfcTrim(tamano);
        String estadoN = nfcTrim(estado);
        
        final String sql =
                "UPDATE sgsis.cliente SET " +
                "razon_social = ?, " +
                "direccion = ?, " +
                "representante_legal = ?, " +
                "telefono = ?, " +
                "correo = ?, " +
                "sector = ?, " +
                "tamano = ?, " +
                "estado = ?, " +
                "fecha_inicio_contrato = ?, " +
                "fecha_fin_contrato = ? " +
                "WHERE cliente_id = ?";

        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, razonSocialN);
            ps.setString(2, emptyToNullNfc(direccion));
            ps.setString(3, emptyToNullNfc(representanteLegal));
            ps.setString(4, emptyToNullNfc(telefono));
            ps.setString(5, emptyToNullNfc(correo));
            ps.setString(6, sectorN);
            ps.setString(7, tamanoN);
            ps.setString(8, estadoN);
            ps.setDate(9, Date.valueOf(fechaInicioContrato));
            ps.setDate(10, Date.valueOf(fechaFinContrato));
            ps.setObject(11, clienteId);

            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new DbException("No se actualizó ningún registro. cliente_id no existe.");
            }

        } catch (SQLException ex) {
            throw new DbException("Error actualizando cliente: " + ex.getMessage(), ex);
        }
    }


    private static String nfcTrim(String s) {
        if (s == null) return null;
        String t = s.trim();
        // OJO: si quieres también limpiar espacios raros (NBSP), descomenta:
        // t = t.replace('\u00A0', ' ').trim();
        return Normalizer.normalize(t, Normalizer.Form.NFC);
    }

    private static String emptyToNullNfc(String s) {
        if (s == null) return null;
        String t = nfcTrim(s);
        return (t == null || t.isBlank()) ? null : t;
    }
    
}
