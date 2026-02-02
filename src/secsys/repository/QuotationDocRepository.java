package secsys.repository;

import secsys.db.DbConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuotationDocRepository extends BaseRepository {

    public QuotationDocRepository(DbConnection provider) {
        super(provider);
    }

    // ===== DTOs =====
    public static class QuotationHeaderDTO {
        public UUID cotizacionId;
        public Integer numero;
        public String nombreEmpresa;
        public String rucPotencial;
        public String estado;
        public BigDecimal descuentoTotal;
        public BigDecimal subtotalSinIva;
        public BigDecimal ivaValor;
        public BigDecimal total;
        public LocalDateTime actualizadoEn;
    }

    public static class QuotationDetailDTO {
        public String servicio;
        public String descripcion;
        public BigDecimal precioUnitario;
        public BigDecimal cantidad;
        public BigDecimal subtotal;
        public Integer orden;
    }

    public static class PdfTemplateDTO {
        public UUID plantillaId;
        public String docId;
        public String archivoNombre;
        public byte[] archivoPdf;
        public String sha256;
    }

    // ====== Cabecera documental (PDF en tabla) ======
    public PdfTemplateDTO findHeaderTemplateByDocId(String docId) throws Exception {
        String sql = "select plantilla_id, doc_id, archivo_nombre, archivo_pdf, sha256 " +
                "from sgsis.plantilla_cabecera_pdf where doc_id = ?";

        try (Connection cn = provider.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, docId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                PdfTemplateDTO t = new PdfTemplateDTO();
                t.plantillaId = (UUID) rs.getObject("plantilla_id");
                t.docId = rs.getString("doc_id");
                t.archivoNombre = rs.getString("archivo_nombre");
                t.archivoPdf = rs.getBytes("archivo_pdf");
                t.sha256 = rs.getString("sha256");
                return t;
            }
        }
    }

    // ✅ Última cotización por RUC
    public QuotationHeaderDTO findLatestQuotationByRuc(String ruc) throws Exception {
        String sql =
                "select cotizacion_id, numero, nombre_empresa, ruc_potencial, estado, " +
                "       descuento_total, subtotal_sin_iva, iva_valor, total, actualizado_en " +
                "from sgsis.cotizacion " +
                "where ruc_potencial = ? " +
                "order by actualizado_en desc nulls last, creado_en desc " +
                "limit 1";

        try (Connection cn = provider.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, ruc);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                QuotationHeaderDTO q = new QuotationHeaderDTO();
                q.cotizacionId = (UUID) rs.getObject("cotizacion_id");

                Object numObj = rs.getObject("numero");
                q.numero = (numObj == null) ? null : ((Number) numObj).intValue();

                q.nombreEmpresa = rs.getString("nombre_empresa");
                q.rucPotencial = rs.getString("ruc_potencial");
                q.estado = rs.getString("estado");
                q.descuentoTotal = rs.getBigDecimal("descuento_total");
                q.subtotalSinIva = rs.getBigDecimal("subtotal_sin_iva");
                q.ivaValor = rs.getBigDecimal("iva_valor");
                q.total = rs.getBigDecimal("total");

                Timestamp ts = rs.getTimestamp("actualizado_en");
                q.actualizadoEn = (ts == null) ? null : ts.toLocalDateTime();

                return q;
            }
        }
    }

    // ====== Detalle de la cotización ======
    public List<QuotationDetailDTO> listDetails(UUID cotizacionId) throws Exception {
        String sql =
                "select servicio, descripcion, precio_unitario, cantidad, subtotal, orden " +
                "from sgsis.cotizacion_detalle " +
                "where cotizacion_id = ? " +
                "order by orden asc";

        List<QuotationDetailDTO> out = new ArrayList<>();

        try (Connection cn = provider.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setObject(1, cotizacionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    QuotationDetailDTO d = new QuotationDetailDTO();
                    d.servicio = rs.getString("servicio");
                    d.descripcion = rs.getString("descripcion");
                    d.precioUnitario = rs.getBigDecimal("precio_unitario");
                    d.cantidad = rs.getBigDecimal("cantidad");
                    d.subtotal = rs.getBigDecimal("subtotal");

                    Object ord = rs.getObject("orden");
                    d.orden = (ord == null) ? null : ((Number) ord).intValue();

                    out.add(d);
                }
            }
        }
        return out;
    }

    // ====== Registro del evento de envío ======
    public void logEmailSent(UUID cotizacionId, String rucPotencial, String emailDestino) throws Exception {
        String sql =
                "insert into sgsis.evento_notificacion (evento_id, cotizacion_id, cliente_id, email_destino, tipo, creado_en) " +
                "values (gen_random_uuid(), ?, ?, ?, 'ENVIO_COTIZACION', now())";

        try (Connection cn = provider.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setObject(1, cotizacionId);
            ps.setString(2, rucPotencial);   // <- aquí guardas el RUC en cliente_id si tu tabla lo usa así
            ps.setString(3, emailDestino);
            ps.executeUpdate();
        }
    }

    public void logEmailSentSafe(UUID cotizacionId, String rucPotencial, String emailDestino) throws Exception {
        try {
            String sql =
                    "insert into sgsis.evento_notificacion (evento_id, cotizacion_id, ruc_potencial, email_destino, tipo, creado_en) " +
                    "values (gen_random_uuid(), ?, ?, ?, 'ENVIO_COTIZACION', now())";

            try (Connection cn = provider.getConnection();
                 PreparedStatement ps = cn.prepareStatement(sql)) {

                ps.setObject(1, cotizacionId);
                ps.setString(2, rucPotencial);
                ps.setString(3, emailDestino);
                ps.executeUpdate();
            }
        } catch (Exception ignore) {
            // silencio a propósito
        }
    }
}
