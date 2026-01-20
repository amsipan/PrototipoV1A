package secsys.repository;

import secsys.db.DbConnection;
import secsys.db.DbException;
import secsys.dto.CalendarActivityDTO;
import secsys.dto.PlanningActivityDTO;
import secsys.dto.PlanningSummaryDTO;
import secsys.dto.PlanningUploadDTO;
import secsys.dto.PlanningUploadFileDTO;

import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlanningRepository extends BaseRepository {

    public PlanningRepository(DbConnection provider) {
        super(provider);
    }

    public UUID insertUpload(PlanningUploadDTO dto) {

        final String sqlPlan =
                "INSERT INTO sgsis.planificacion " +
                "(cliente_id, tipo_servicio, version, fecha_inicio, fecha_fin, archivo_csv_nombre, color, estado_vigencia) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "RETURNING planificacion_id";

        final String sqlAct =
                "INSERT INTO sgsis.planificacion_actividad " +
                "(planificacion_id, fecha_inicio, fecha_fin, actividad, descripcion, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        final String sqlUpload =
                "INSERT INTO sgsis.planificacion_upload (planificacion_id, nombre_archivo, contenido) " +
                "VALUES (?, ?, ?)";

        if (dto == null) throw new DbException("DTO null.");
        if (dto.clienteId == null) throw new DbException("clienteId es obligatorio.");
        if (dto.tipoServicio == null || dto.tipoServicio.isBlank()) throw new DbException("tipoServicio es obligatorio.");
        if (dto.version == null || dto.version.isBlank()) throw new DbException("version es obligatoria.");
        if (dto.estado == null || dto.estado.isBlank()) throw new DbException("estado_vigencia es obligatorio (Activa|Inactiva).");
        if (dto.colorHex == null || dto.colorHex.isBlank()) throw new DbException("color es obligatorio (#RRGGBB).");
        if (dto.fileName == null || dto.fileName.isBlank()) throw new DbException("fileName es obligatorio.");
        if (dto.fileBytes == null || dto.fileBytes.length == 0) throw new DbException("fileBytes es obligatorio.");
        if (dto.actividades == null || dto.actividades.isEmpty()) throw new DbException("No hay actividades para insertar.");

        java.time.LocalDate minDate = null;
        java.time.LocalDate maxDate = null;

        for (PlanningActivityDTO a : dto.actividades) {
            if (a == null || a.fechaInicio == null || a.fechaFin == null) continue;
            var di = a.fechaInicio.toLocalDate();
            var df = a.fechaFin.toLocalDate();
            if (minDate == null || di.isBefore(minDate)) minDate = di;
            if (maxDate == null || df.isAfter(maxDate)) maxDate = df;
        }

        if (minDate == null || maxDate == null) {
            throw new DbException("No se pudo determinar fecha_inicio/fecha_fin desde las actividades.");
        }

        try (Connection conn = provider.getConnection()) {
            conn.setAutoCommit(false);

            UUID planId;

            try (PreparedStatement psPlan = conn.prepareStatement(sqlPlan)) {
                psPlan.setObject(1, dto.clienteId);
                psPlan.setString(2, dto.tipoServicio);
                psPlan.setString(3, dto.version);
                psPlan.setDate(4, Date.valueOf(minDate));
                psPlan.setDate(5, Date.valueOf(maxDate));
                psPlan.setString(6, dto.fileName);
                psPlan.setString(7, dto.colorHex);
                psPlan.setString(8, dto.estado);

                try (ResultSet rs = psPlan.executeQuery()) {
                    if (!rs.next()) throw new SQLException("No se retornó planificacion_id.");
                    planId = (UUID) rs.getObject(1);
                }
            }

            try (PreparedStatement psAct = conn.prepareStatement(sqlAct)) {
                for (PlanningActivityDTO a : dto.actividades) {
                    OffsetDateTime ini = a.fechaInicio.atOffset(ZoneOffset.UTC);
                    OffsetDateTime fin = a.fechaFin.atOffset(ZoneOffset.UTC);

                    psAct.setObject(1, planId);
                    psAct.setObject(2, ini);
                    psAct.setObject(3, fin);
                    psAct.setString(4, a.actividad);
                    psAct.setString(5, a.descripcion);
                    psAct.setString(6, a.estado);
                    psAct.addBatch();
                }
                psAct.executeBatch();
            }

            try (PreparedStatement psUp = conn.prepareStatement(sqlUpload)) {
                psUp.setObject(1, planId);
                psUp.setString(2, dto.fileName);
                psUp.setBytes(3, dto.fileBytes);
                psUp.executeUpdate();
            }

            conn.commit();
            return planId;

        } catch (SQLException ex) {
            throw new DbException("Error subiendo planificación: " + ex.getMessage(), ex);
        }
    }

    public List<PlanningSummaryDTO> findByClienteId(UUID clienteId) {
        final String sql =
                "SELECT planificacion_id, cliente_id, tipo_servicio, version, fecha_inicio, fecha_fin, " +
                "       estado_vigencia, color, archivo_csv_nombre " +
                "FROM sgsis.planificacion " +
                "WHERE cliente_id = ? " +
                "ORDER BY fecha_inicio DESC, fecha_fin DESC, version DESC";

        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, clienteId);

            List<PlanningSummaryDTO> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PlanningSummaryDTO dto = new PlanningSummaryDTO();
                    dto.planificacionId = (UUID) rs.getObject("planificacion_id");
                    dto.clienteId = (UUID) rs.getObject("cliente_id");
                    dto.tipoServicio = rs.getString("tipo_servicio");
                    dto.version = rs.getString("version");

                    Date di = rs.getDate("fecha_inicio");
                    Date df = rs.getDate("fecha_fin");
                    dto.fechaInicio = (di == null) ? null : di.toLocalDate();
                    dto.fechaFin = (df == null) ? null : df.toLocalDate();

                    dto.estadoVigencia = rs.getString("estado_vigencia");
                    dto.colorHex = rs.getString("color");
                    dto.archivoCsvNombre = rs.getString("archivo_csv_nombre");

                    out.add(dto);
                }
            }
            return out;

        } catch (SQLException ex) {
            throw new DbException("Error consultando planificaciones: " + ex.getMessage(), ex);
        }
    }

    public PlanningSummaryDTO findLatestByClienteId(UUID clienteId) {
        final String sql =
                "SELECT planificacion_id, cliente_id, tipo_servicio, version, fecha_inicio, fecha_fin, " +
                "       estado_vigencia, color, archivo_csv_nombre " +
                "FROM sgsis.planificacion " +
                "WHERE cliente_id = ? " +
                "ORDER BY fecha_inicio DESC, fecha_fin DESC, version DESC " +
                "LIMIT 1";

        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, clienteId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                PlanningSummaryDTO dto = new PlanningSummaryDTO();
                dto.planificacionId = (UUID) rs.getObject("planificacion_id");
                dto.clienteId = (UUID) rs.getObject("cliente_id");
                dto.tipoServicio = rs.getString("tipo_servicio");
                dto.version = rs.getString("version");

                Date di = rs.getDate("fecha_inicio");
                Date df = rs.getDate("fecha_fin");
                dto.fechaInicio = (di == null) ? null : di.toLocalDate();
                dto.fechaFin = (df == null) ? null : df.toLocalDate();

                dto.estadoVigencia = rs.getString("estado_vigencia");
                dto.colorHex = rs.getString("color");
                dto.archivoCsvNombre = rs.getString("archivo_csv_nombre");
                return dto;
            }

        } catch (SQLException ex) {
            throw new DbException("Error consultando última planificación: " + ex.getMessage(), ex);
        }
    }

    public PlanningUploadFileDTO getUploadByPlanificacionId(UUID planificacionId) {
        final String sql =
                "SELECT u.planificacion_id, u.nombre_archivo, u.contenido " +
                "FROM sgsis.planificacion_upload u " +
                "WHERE u.planificacion_id = ? " +
                "ORDER BY u.creado_en DESC " +
                "LIMIT 1";

        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, planificacionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                PlanningUploadFileDTO dto = new PlanningUploadFileDTO();
                dto.planificacionId = (UUID) rs.getObject("planificacion_id");
                dto.fileName = rs.getString("nombre_archivo");
                dto.fileBytes = rs.getBytes("contenido");
                return dto;
            }

        } catch (SQLException ex) {
            throw new DbException("Error obteniendo CSV por planificación: " + ex.getMessage(), ex);
        }
    }

    // ============================================================
    // ✅ NUEVO: Actividades del calendario (solo semana actual)
    // Considera planificaciones ACTIVAS y trae color/version
    // ============================================================
    public List<CalendarActivityDTO> findCalendarActivitiesBetween(OffsetDateTime from, OffsetDateTime to) {
        final String sql =
                "SELECT a.actividad_id, a.planificacion_id, p.cliente_id, " +
                "       c.ruc, c.razon_social, " +
                "       p.version, p.tipo_servicio, p.color, " +
                "       a.fecha_inicio, a.fecha_fin, a.actividad, a.descripcion, a.estado " +
                "FROM sgsis.planificacion_actividad a " +
                "JOIN sgsis.planificacion p ON p.planificacion_id = a.planificacion_id " +
                "JOIN sgsis.cliente c ON c.cliente_id = p.cliente_id " +
                "WHERE p.estado_vigencia = 'Activa' " +
                "  AND a.fecha_inicio >= ? " +
                "  AND a.fecha_inicio < ? " +
                "ORDER BY a.fecha_inicio ASC";

        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, from);
            ps.setObject(2, to);

            List<CalendarActivityDTO> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CalendarActivityDTO d = new CalendarActivityDTO();
                    d.actividadId = (UUID) rs.getObject("actividad_id");
                    d.planificacionId = (UUID) rs.getObject("planificacion_id");
                    d.clienteId = (UUID) rs.getObject("cliente_id");

                    d.ruc = rs.getString("ruc");
                    d.razonSocial = rs.getString("razon_social");

                    d.version = rs.getString("version");
                    d.tipoServicio = rs.getString("tipo_servicio");
                    d.colorHex = rs.getString("color");

                    d.fechaInicio = rs.getObject("fecha_inicio", OffsetDateTime.class);
                    d.fechaFin = rs.getObject("fecha_fin", OffsetDateTime.class);

                    d.actividad = rs.getString("actividad");
                    d.descripcion = rs.getString("descripcion");
                    d.estado = rs.getString("estado");

                    out.add(d);
                }
            }
            return out;

        } catch (SQLException ex) {
            throw new DbException("Error consultando actividades del calendario: " + ex.getMessage(), ex);
        }
    }

    // ============================================================
    // ✅ NUEVO: Cambiar estado de actividad
    // ============================================================
    public void updateActivityEstado(UUID actividadId, String nuevoEstado) {
        final String sql =
                "UPDATE sgsis.planificacion_actividad " +
                "SET estado = ? " +
                "WHERE actividad_id = ?";

        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setObject(2, actividadId);
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new DbException("Error actualizando estado de actividad: " + ex.getMessage(), ex);
        }
    }

    // ============================================================
    // ✅ NUEVO: Insertar actividad manual para el cliente,
    // asociándola a la planificación ACTIVA del cliente.
    // ============================================================
    public UUID insertManualActivityToActivePlan(UUID clienteId,
                                                 OffsetDateTime inicio,
                                                 OffsetDateTime fin,
                                                 String actividad,
                                                 String descripcion,
                                                 String estado) {

        final String sqlPlan =
                "SELECT planificacion_id " +
                "FROM sgsis.planificacion " +
                "WHERE cliente_id = ? AND estado_vigencia = 'Activa' " +
                "ORDER BY version DESC " +
                "LIMIT 1";

        final String sqlIns =
                "INSERT INTO sgsis.planificacion_actividad " +
                "(planificacion_id, fecha_inicio, fecha_fin, actividad, descripcion, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "RETURNING actividad_id";

        try (Connection conn = provider.getConnection()) {
            conn.setAutoCommit(false);

            UUID planId;

            try (PreparedStatement ps = conn.prepareStatement(sqlPlan)) {
                ps.setObject(1, clienteId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new DbException("El cliente no tiene planificación Activa para agregar actividades.");
                    }
                    planId = (UUID) rs.getObject("planificacion_id");
                }
            }

            UUID actId;
            try (PreparedStatement ps = conn.prepareStatement(sqlIns)) {
                ps.setObject(1, planId);
                ps.setObject(2, inicio);
                ps.setObject(3, fin);
                ps.setString(4, actividad);
                ps.setString(5, descripcion);
                ps.setString(6, estado);

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    actId = (UUID) rs.getObject(1);
                }
            }

            conn.commit();
            return actId;

        } catch (SQLException ex) {
            throw new DbException("Error insertando actividad manual: " + ex.getMessage(), ex);
        }
    }
}
