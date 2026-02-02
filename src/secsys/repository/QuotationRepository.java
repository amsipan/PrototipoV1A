package secsys.repository;

import secsys.config.DbConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuotationRepository {

    private static final String[] EDITABLE_STATES = new String[]{"Borrador", "Revision", "Revisión"};

    // ========= DTO =========
    public static class QuoteRow {
        public UUID cotizacionId;
        public Long numero; // JDBC puede traer bigint como Long
        public String estado;

        public String rucPotencial;
        public String nombreEmpresa;

        public UUID detalleId;
        public String servicioPrincipal;
        public String descripcionServicio;

        public BigDecimal descuentoTotal;
        public BigDecimal subtotalSinIva;
        public BigDecimal ivaValor;
        public BigDecimal total;

        public BigDecimal subtotalServicioBase;

        public String actualizadoEn;
    }

    public static final class CreateQuoteRequest {
        public String rucPotencial;
        public String nombreEmpresa;
        public LocalDate fechaGeneracion;
        public LocalDate vigenciaHasta;
        public String estado;
        public int descuentoPct;

        public String servicio;
        public String descripcion;

        // OJO: en KB4 y SMF aquí viene el TOTAL ya calculado por la UI (según tu regla)
        public BigDecimal precioUnitario;
        public int cantidad;

        public BigDecimal precioGestion;
        public boolean incluirGestion;
    }

    private Connection openConn() throws Exception {
        DbConfig cfg = DbConfig.fromEnv();
        return DriverManager.getConnection(cfg.jdbcUrl(), cfg.user(), cfg.password());
    }

    // ========= LISTAR =========
    public List<QuoteRow> listEditableByRuc(String rucPotencial) throws Exception {
        if (rucPotencial == null || !rucPotencial.matches("^\\d{13}$")) {
            throw new IllegalArgumentException("RUC inválido (13 dígitos).");
        }

        String sql =
                "select " +
                        "  c.cotizacion_id, c.numero, c.estado, c.ruc_potencial, c.nombre_empresa, " +
                        "  c.descuento_total, c.subtotal_sin_iva, c.iva_valor, c.total, c.actualizado_en, " +
                        "  d.detalle_id, d.servicio, d.descripcion, " +
                        "  coalesce(s.sum_subtotal, 0) as subtotal_servicio_base " +
                        "from sgsis.cotizacion c " +
                        "left join sgsis.cotizacion_detalle d " +
                        "  on d.cotizacion_id = c.cotizacion_id and d.orden = 1 " +
                        "left join ( " +
                        "  select cotizacion_id, sum(coalesce(subtotal,0)) as sum_subtotal " +
                        "  from sgsis.cotizacion_detalle " +
                        "  group by cotizacion_id " +
                        ") s on s.cotizacion_id = c.cotizacion_id " +
                        "where c.ruc_potencial = ? " +
                        "  and c.estado = any(?) " +
                        "order by c.actualizado_en desc nulls last, c.creado_en desc;";

        List<QuoteRow> out = new ArrayList<>();

        try (Connection cn = openConn();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, rucPotencial);
            Array arr = cn.createArrayOf("text", EDITABLE_STATES);
            ps.setArray(2, arr);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    QuoteRow r = new QuoteRow();
                    r.cotizacionId = (UUID) rs.getObject("cotizacion_id");

                    Object nObj = rs.getObject("numero");
                    r.numero = (nObj == null) ? null : ((Number) nObj).longValue();

                    r.estado = rs.getString("estado");
                    r.rucPotencial = rs.getString("ruc_potencial");
                    r.nombreEmpresa = rs.getString("nombre_empresa");

                    r.detalleId = (UUID) rs.getObject("detalle_id");
                    r.servicioPrincipal = rs.getString("servicio");
                    r.descripcionServicio = rs.getString("descripcion");

                    r.descuentoTotal = bd(rs, "descuento_total");
                    r.subtotalSinIva = bd(rs, "subtotal_sin_iva");
                    r.ivaValor = bd(rs, "iva_valor");
                    r.total = bd(rs, "total");

                    r.subtotalServicioBase = bd(rs, "subtotal_servicio_base");

                    Timestamp ts = rs.getTimestamp("actualizado_en");
                    r.actualizadoEn = (ts == null) ? "-" : ts.toString();

                    out.add(r);
                }
            }
        }

        return out;
    }

    // ========= UPDATE DESCRIPCIÓN =========
    public boolean updateServiceDescription(String rucPotencial, UUID detalleId, String newDesc) throws Exception {
        if (rucPotencial == null || !rucPotencial.matches("^\\d{13}$")) {
            throw new IllegalArgumentException("RUC inválido (13 dígitos).");
        }

        validateDescription(newDesc);

        if (detalleId == null) {
            throw new IllegalArgumentException("Detalle inválido.");
        }

        String sql =
                "update sgsis.cotizacion_detalle d " +
                        "set descripcion = ? " +
                        "from sgsis.cotizacion c " +
                        "where d.detalle_id = ? " +
                        "  and d.cotizacion_id = c.cotizacion_id " +
                        "  and c.ruc_potencial = ? " +
                        "  and c.estado = any(?)";

        try (Connection cn = openConn();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, newDesc.trim());
            ps.setObject(2, detalleId);
            ps.setString(3, rucPotencial);
            Array arr = cn.createArrayOf("text", EDITABLE_STATES);
            ps.setArray(4, arr);

            return ps.executeUpdate() > 0;
        }
    }

    // ========= UPDATE DESCUENTO =========
    public boolean updateDiscountAndTotals(String rucPotencial, UUID cotizacionId, int descuentoPct) throws Exception {
        if (rucPotencial == null || !rucPotencial.matches("^\\d{13}$")) {
            throw new IllegalArgumentException("RUC inválido (13 dígitos).");
        }
        if (cotizacionId == null) {
            throw new IllegalArgumentException("Cotización inválida.");
        }

        if (descuentoPct < 0) throw new IllegalArgumentException("El descuento debe ser positivo");
        if (descuentoPct > 99) throw new IllegalArgumentException("Descuento inválido");

        try (Connection cn = openConn()) {
            cn.setAutoCommit(false);

            try {
                if (!isEditableQuote(cn, rucPotencial, cotizacionId)) {
                    cn.rollback();
                    return false;
                }

                // Se recalcula SIEMPRE desde SUM(subtotal) de detalle (y tus subtotales ya respetan KB4/SMF)
                BigDecimal subtotalBase = fetchSubtotalBase(cn, cotizacionId);
                BigDecimal ivaRate = fetchIvaRate(cn);

                BigDecimal factor = BigDecimal.ONE.subtract(
                        new BigDecimal(descuentoPct).divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP)
                );

                BigDecimal subtotalSinIva = subtotalBase.multiply(factor).setScale(2, RoundingMode.HALF_UP);
                BigDecimal ivaValor = subtotalSinIva.multiply(ivaRate).setScale(2, RoundingMode.HALF_UP);
                BigDecimal total = subtotalSinIva.add(ivaValor).setScale(2, RoundingMode.HALF_UP);

                String upd =
                        "update sgsis.cotizacion " +
                                "set descuento_total = ?, subtotal_sin_iva = ?, iva_valor = ?, total = ?, actualizado_en = now() " +
                                "where cotizacion_id = ? and ruc_potencial = ? and estado = any(?)";

                try (PreparedStatement ps = cn.prepareStatement(upd)) {
                    ps.setBigDecimal(1, new BigDecimal(descuentoPct).setScale(2, RoundingMode.HALF_UP));
                    ps.setBigDecimal(2, subtotalSinIva);
                    ps.setBigDecimal(3, ivaValor);
                    ps.setBigDecimal(4, total);
                    ps.setObject(5, cotizacionId);
                    ps.setString(6, rucPotencial);
                    Array arr = cn.createArrayOf("text", EDITABLE_STATES);
                    ps.setArray(7, arr);

                    if (ps.executeUpdate() <= 0) {
                        cn.rollback();
                        return false;
                    }
                }

                cn.commit();
                return true;

            } catch (RuntimeException | SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    // ========= CREAR COTIZACIÓN (KB4/SMF: NO multiplicar por cantidad) =========
    public void createQuotation(CreateQuoteRequest req) throws Exception {
        if (req == null) throw new IllegalArgumentException("Request inválido.");

        if (req.rucPotencial == null || !req.rucPotencial.matches("^\\d{13}$")) {
            throw new IllegalArgumentException("RUC inválido (13 dígitos).");
        }

        String empresa = (req.nombreEmpresa == null) ? "" : req.nombreEmpresa.trim();
        if (empresa.length() < 3 || empresa.length() > 100) {
            throw new IllegalArgumentException("Nombre de la empresa inválido (3 a 100 caracteres).");
        }

        if (req.fechaGeneracion == null) throw new IllegalArgumentException("Fecha de generación inválida.");
        if (req.vigenciaHasta == null) throw new IllegalArgumentException("Fecha de vencimiento inválida.");
        if (req.vigenciaHasta.isBefore(req.fechaGeneracion)) {
            throw new IllegalArgumentException("La fecha de vencimiento no puede ser menor a la fecha de generación.");
        }

        String servicio = (req.servicio == null) ? "" : req.servicio.trim();
        if (servicio.isBlank() || "Seleccione".equalsIgnoreCase(servicio)) {
            throw new IllegalArgumentException("Debe seleccionar un tipo de servicio válido.");
        }

        validateDescription(req.descripcion);

        int dPct = req.descuentoPct;
        if (dPct < 0) dPct = 0;
        if (dPct > 99) dPct = 99;

        BigDecimal precioServicio = (req.precioUnitario == null) ? BigDecimal.ZERO : req.precioUnitario;
        if (precioServicio.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Precio del servicio inválido.");

        BigDecimal precioGestion = (req.precioGestion == null) ? BigDecimal.ZERO : req.precioGestion;
        if (precioGestion.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Precio de gestión inválido.");

        boolean incluirGestion = req.incluirGestion && precioGestion.compareTo(BigDecimal.ZERO) > 0;

        // ======= REGLA: KB4 y SMF vienen como TOTAL ya calculado =======
        boolean isKB4 = isServiceKB4(servicio);
        boolean isSMF = isServiceSMF(servicio);

        // Cantidad “efectiva” para guardar
        int cantidadServicio = req.cantidad;
        if (isSMF) {
            // SMF: cantidad debe ser 1 sí o sí
            cantidadServicio = 1;
        } else {
            // para KB4: puede quedar referencial (usuarios), pero no afecta el subtotal
            if (cantidadServicio <= 0) cantidadServicio = 1;
        }

        // Subtotal del servicio:
        // KB4 o SMF => subtotal = precioServicio (NO multiplicar)
        // (Si en el futuro hay otros servicios que sí dependan de cantidad, aquí lo cambias)
        BigDecimal subtotalServicio = precioServicio.setScale(2, RoundingMode.HALF_UP);

        // Base para descuento: subtotal servicio + gestión (si aplica)
        BigDecimal subtotalBase = subtotalServicio.add(incluirGestion ? precioGestion : BigDecimal.ZERO);

        try (Connection cn = openConn()) {
            cn.setAutoCommit(false);

            try {
                BigDecimal ivaRate = fetchIvaRate(cn);

                BigDecimal factor = BigDecimal.ONE.subtract(
                        new BigDecimal(dPct).divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP)
                );

                BigDecimal subtotalSinIva = subtotalBase.multiply(factor).setScale(2, RoundingMode.HALF_UP);
                BigDecimal ivaValor = subtotalSinIva.multiply(ivaRate).setScale(2, RoundingMode.HALF_UP);
                BigDecimal total = subtotalSinIva.add(ivaValor).setScale(2, RoundingMode.HALF_UP);

                // No insertes "numero" -> lo da DEFAULT nextval(...) en BD
                final String sqlCot =
                        "insert into sgsis.cotizacion (" +
                                "  ruc_potencial, nombre_empresa, fecha_generacion, vigencia_hasta, estado, " +
                                "  descuento_total, subtotal_sin_iva, iva_valor, total, creado_en, actualizado_en" +
                                ") values (?, ?, ?, ?, ?, ?, ?, ?, ?, now(), now()) " +
                                "returning cotizacion_id";

                UUID cotId;
                try (PreparedStatement ps = cn.prepareStatement(sqlCot)) {
                    ps.setString(1, req.rucPotencial);
                    ps.setString(2, empresa);
                    ps.setDate(3, Date.valueOf(req.fechaGeneracion));
                    ps.setDate(4, Date.valueOf(req.vigenciaHasta));
                    ps.setString(5, (req.estado == null || req.estado.isBlank()) ? "Borrador" : req.estado.trim());
                    ps.setBigDecimal(6, new BigDecimal(dPct).setScale(2, RoundingMode.HALF_UP));
                    ps.setBigDecimal(7, subtotalSinIva);
                    ps.setBigDecimal(8, ivaValor);
                    ps.setBigDecimal(9, total);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("No se retornó cotizacion_id.");
                        cotId = (UUID) rs.getObject(1);
                    }
                }

                final String sqlDet =
                        "insert into sgsis.cotizacion_detalle (" +
                                "  cotizacion_id, orden, servicio, descripcion, cantidad, precio_unitario, subtotal" +
                                ") values (?, ?, ?, ?, ?, ?, ?)";

                // Detalle principal (orden=1)
                try (PreparedStatement ps = cn.prepareStatement(sqlDet)) {
                    ps.setObject(1, cotId);
                    ps.setInt(2, 1);
                    ps.setString(3, servicio);
                    ps.setString(4, req.descripcion.trim());
                    ps.setInt(5, cantidadServicio);
                    ps.setBigDecimal(6, precioServicio.setScale(2, RoundingMode.HALF_UP));
                    ps.setBigDecimal(7, subtotalServicio);
                    ps.executeUpdate();
                }

                // Gestión (orden=2) opcional: cantidad SIEMPRE 1
                if (incluirGestion) {
                    try (PreparedStatement ps = cn.prepareStatement(sqlDet)) {
                        ps.setObject(1, cotId);
                        ps.setInt(2, 2);
                        ps.setString(3, "Gestión");
                        ps.setString(4, "Precio de gestión");
                        ps.setInt(5, 1);
                        ps.setBigDecimal(6, precioGestion.setScale(2, RoundingMode.HALF_UP));
                        ps.setBigDecimal(7, precioGestion.setScale(2, RoundingMode.HALF_UP));
                        ps.executeUpdate();
                    }
                }

                cn.commit();

            } catch (RuntimeException | SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    // ========= HELPERS =========

    private boolean isEditableQuote(Connection cn, String rucPotencial, UUID cotizacionId) throws SQLException {
        String sql =
                "select 1 from sgsis.cotizacion " +
                        "where cotizacion_id = ? and ruc_potencial = ? and estado = any(?)";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, cotizacionId);
            ps.setString(2, rucPotencial);
            Array arr = cn.createArrayOf("text", EDITABLE_STATES);
            ps.setArray(3, arr);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private BigDecimal fetchSubtotalBase(Connection cn, UUID cotizacionId) throws SQLException {
        String sql =
                "select coalesce(sum(coalesce(subtotal,0)), 0) as subtotal_base " +
                        "from sgsis.cotizacion_detalle " +
                        "where cotizacion_id = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, cotizacionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return bd(rs, "subtotal_base");
                return BigDecimal.ZERO;
            }
        }
    }

    private BigDecimal fetchIvaRate(Connection cn) throws SQLException {
        String sql =
                "select valor from sgsis.parametro_config where clave = 'fin.iva' limit 1";

        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) return BigDecimal.ZERO;

            BigDecimal raw;
            try {
                raw = new BigDecimal(rs.getString("valor"));
            } catch (Exception ignored) {
                return BigDecimal.ZERO;
            }

            // si viene como 15 en vez de 0.15
            if (raw.compareTo(BigDecimal.ONE) > 0) {
                raw = raw.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
            }

            if (raw.compareTo(BigDecimal.ZERO) < 0) return BigDecimal.ZERO;
            if (raw.compareTo(BigDecimal.ONE) > 0) return BigDecimal.ONE;

            return raw;
        }
    }

    private boolean isServiceKB4(String servicio) {
        if (servicio == null) return false;
        String s = servicio.toLowerCase();
        // Ajusta si tu texto exacto es distinto
        return s.contains("kb4") || s.contains("knowbe4");
    }

    private boolean isServiceSMF(String servicio) {
        if (servicio == null) return false;
        String s = servicio.toLowerCase();
        // Ajusta si tu texto exacto es distinto
        return s.contains("smf");
    }

    private void validateDescription(String desc) {
        String d = (desc == null) ? "" : desc.trim();

        if (d.length() < 3) {
            throw new IllegalArgumentException("La descripción debe tener al menos 3 caracteres.");
        }
        if (d.length() > 250) {
            throw new IllegalArgumentException("La descripción no puede exceder 250 caracteres.");
        }
        if (!d.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9 .,#\\-]{3,250}$")) {
            throw new IllegalArgumentException("Descripción inválida. Solo letras/números y símbolos . , # -");
        }
    }

    private static BigDecimal bd(ResultSet rs, String col) throws SQLException {
        BigDecimal v = rs.getBigDecimal(col);
        return (v == null) ? BigDecimal.ZERO : v;
    }

    // ========= rcot8v1.1: consultar cotizaciones por RUC potencial =========
public List<QuoteRow> listByPotentialRucDetailed(String rucPotencial) throws Exception {
    if (rucPotencial == null || rucPotencial.trim().isEmpty()) {
        throw new IllegalArgumentException("Ingrese el RUC del potencial cliente");
    }
    String ruc = rucPotencial.trim();
    if (!ruc.matches("^\\d{13}$")) {
        throw new IllegalArgumentException("Ingrese el RUC del potencial cliente");
    }

    // Primero: validar existencia del potencial cliente
    // Ajusta el nombre de tabla/columna si tu modelo lo usa distinto.
    if (!existsPotentialClientByRuc(ruc)) {
        throw new IllegalArgumentException("Potencial cliente no encontrado");
    }

    // Listado detallado (cabecera + detalle orden 1)
    String sql =
            "select " +
            " c.cotizacion_id, c.numero, c.estado, c.ruc_potencial, c.nombre_empresa, " +
            " c.descuento_total, c.subtotal_sin_iva, c.iva_valor, c.total, c.actualizado_en, " +
            " d.detalle_id, d.servicio, d.descripcion, " +
            " coalesce(s.sum_subtotal, 0) as subtotal_servicio_base " +
            "from sgsis.cotizacion c " +
            "left join sgsis.cotizacion_detalle d on d.cotizacion_id = c.cotizacion_id and d.orden = 1 " +
            "left join ( " +
            "   select cotizacion_id, sum(coalesce(subtotal,0)) as sum_subtotal " +
            "   from sgsis.cotizacion_detalle group by cotizacion_id " +
            ") s on s.cotizacion_id = c.cotizacion_id " +
            "where c.ruc_potencial = ? " +
            "order by c.fecha_generacion desc nulls last, c.creado_en desc;";

    List<QuoteRow> out = new ArrayList<>();

    try (Connection cn = openConn();
         PreparedStatement ps = cn.prepareStatement(sql)) {

        ps.setString(1, ruc);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                QuoteRow q = new QuoteRow();
                q.cotizacionId = (UUID) rs.getObject("cotizacion_id");

                Object nObj = rs.getObject("numero");
                q.numero = (nObj == null) ? null : ((Number) nObj).longValue();

                q.estado = rs.getString("estado");
                q.rucPotencial = rs.getString("ruc_potencial");
                q.nombreEmpresa = rs.getString("nombre_empresa");

                q.detalleId = (UUID) rs.getObject("detalle_id");
                q.servicioPrincipal = rs.getString("servicio");
                q.descripcionServicio = rs.getString("descripcion");

                q.descuentoTotal = bd(rs, "descuento_total");
                q.subtotalSinIva = bd(rs, "subtotal_sin_iva");
                q.ivaValor = bd(rs, "iva_valor");
                q.total = bd(rs, "total");
                q.subtotalServicioBase = bd(rs, "subtotal_servicio_base");

                Timestamp ts = rs.getTimestamp("actualizado_en");
                q.actualizadoEn = (ts == null) ? "-" : ts.toString();

                out.add(q);
            }
        }
    }

    return out;
}

// ========= rcot9v1.1: consultar cotizaciones por periodo =========
public List<QuoteRow> listByDateRangeDetailed(LocalDate fechaInicio, LocalDate fechaFin) throws Exception {
    if (fechaInicio == null || fechaFin == null) {
        throw new IllegalArgumentException("Debe seleccionar la fecha de inicio y fin.");
    }
    // Validación lógica (correcta): fin no puede ser menor que inicio
    if (fechaFin.isBefore(fechaInicio)) {
        throw new IllegalArgumentException("La fecha de fin no puede ser mayor a la fecha de inicio");
    }

    String sql =
            "select " +
            " c.cotizacion_id, c.numero, c.estado, c.ruc_potencial, c.nombre_empresa, " +
            " c.descuento_total, c.subtotal_sin_iva, c.iva_valor, c.total, c.actualizado_en, " +
            " d.detalle_id, d.servicio, d.descripcion, " +
            " coalesce(s.sum_subtotal, 0) as subtotal_servicio_base " +
            "from sgsis.cotizacion c " +
            "left join sgsis.cotizacion_detalle d on d.cotizacion_id = c.cotizacion_id and d.orden = 1 " +
            "left join ( " +
            "   select cotizacion_id, sum(coalesce(subtotal,0)) as sum_subtotal " +
            "   from sgsis.cotizacion_detalle group by cotizacion_id " +
            ") s on s.cotizacion_id = c.cotizacion_id " +
            "where c.fecha_generacion between ? and ? " +
            "order by c.fecha_generacion desc nulls last, c.creado_en desc;";

    List<QuoteRow> out = new ArrayList<>();

    try (Connection cn = openConn();
         PreparedStatement ps = cn.prepareStatement(sql)) {

        ps.setDate(1, Date.valueOf(fechaInicio));
        ps.setDate(2, Date.valueOf(fechaFin));

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                QuoteRow q = new QuoteRow();
                q.cotizacionId = (UUID) rs.getObject("cotizacion_id");

                Object nObj = rs.getObject("numero");
                q.numero = (nObj == null) ? null : ((Number) nObj).longValue();

                q.estado = rs.getString("estado");
                q.rucPotencial = rs.getString("ruc_potencial");
                q.nombreEmpresa = rs.getString("nombre_empresa");

                q.detalleId = (UUID) rs.getObject("detalle_id");
                q.servicioPrincipal = rs.getString("servicio");
                q.descripcionServicio = rs.getString("descripcion");

                q.descuentoTotal = bd(rs, "descuento_total");
                q.subtotalSinIva = bd(rs, "subtotal_sin_iva");
                q.ivaValor = bd(rs, "iva_valor");
                q.total = bd(rs, "total");
                q.subtotalServicioBase = bd(rs, "subtotal_servicio_base");

                Timestamp ts = rs.getTimestamp("actualizado_en");
                q.actualizadoEn = (ts == null) ? "-" : ts.toString();

                out.add(q);
            }
        }
    }

    return out;
}

// ======= helper existencia potencial cliente =======
private boolean existsPotentialClientByRuc(String ruc) throws Exception {
    // ⚠️ Ajusta esta consulta según tu modelo real.
    // Si tú NO tienes tabla de potencial_cliente, puedes validar solo con cotizacion:
    // "select 1 from sgsis.cotizacion where ruc_potencial=? limit 1"
    String sql = "select 1 from sgsis.potencial_cliente where ruc = ? limit 1";

    try (Connection cn = openConn();
         PreparedStatement ps = cn.prepareStatement(sql)) {

        ps.setString(1, ruc);
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    } catch (SQLException ex) {
        // Fallback si no existe la tabla potencial_cliente en tu esquema
        String fallback = "select 1 from sgsis.cotizacion where ruc_potencial = ? limit 1";
        try (Connection cn2 = openConn();
             PreparedStatement ps2 = cn2.prepareStatement(fallback)) {
            ps2.setString(1, ruc);
            try (ResultSet rs2 = ps2.executeQuery()) {
                return rs2.next();
            }
        }
    }
}

public boolean markQuotationAsRevision(UUID cotizacionId) throws Exception {
    if (cotizacionId == null) throw new IllegalArgumentException("Cotización inválida.");

    // Solo cambia Borrador -> Revision
    String sql =
            "update sgsis.cotizacion " +
            "set estado = 'Revision', actualizado_en = now() " +
            "where cotizacion_id = ? and estado = 'Borrador'";

    try (Connection cn = openConn();
         PreparedStatement ps = cn.prepareStatement(sql)) {

        ps.setObject(1, cotizacionId);
        return ps.executeUpdate() > 0;
    }
}

public boolean markQuotationAsAccepted(UUID cotizacionId) throws Exception {
    if (cotizacionId == null) throw new IllegalArgumentException("Cotización inválida.");

    // Aceptada desde Borrador o Revision
    String sql =
            "update sgsis.cotizacion " +
            "set estado = 'Aceptada', actualizado_en = now() " +
            "where cotizacion_id = ? and estado in ('Borrador', 'Revision')";

    try (Connection cn = openConn();
         PreparedStatement ps = cn.prepareStatement(sql)) {

        ps.setObject(1, cotizacionId);
        return ps.executeUpdate() > 0;
    }
}

public boolean markQuotationAsRejected(UUID cotizacionId) throws Exception {
    if (cotizacionId == null) throw new IllegalArgumentException("Cotización inválida.");

    // Rechazada desde Borrador o Revision
    String sql =
            "update sgsis.cotizacion " +
            "set estado = 'Rechazada', actualizado_en = now() " +
            "where cotizacion_id = ? and estado in ('Borrador', 'Revision')";

    try (Connection cn = openConn();
         PreparedStatement ps = cn.prepareStatement(sql)) {

        ps.setObject(1, cotizacionId);
        return ps.executeUpdate() > 0;
    }
}



}
