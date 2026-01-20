package secsys.dto;

import java.time.LocalDate;
import java.util.UUID;

public class PlanningSummaryDTO {
    public UUID planificacionId;
    public UUID clienteId;

    public String tipoServicio;     // d_tipo_servicio
    public String version;          // d_version_vxy
    public LocalDate fechaInicio;   // date
    public LocalDate fechaFin;      // date

    public String estadoVigencia;   // d_estado_vigencia_planificacion (Activa|Inactiva)
    public String colorHex;         // d_color_hex

    public String archivoCsvNombre; // varchar(120)

    // Conveniencia para mostrar
    public String displayRango() {
        String ini = (fechaInicio == null) ? "-" : fechaInicio.toString();
        String fin = (fechaFin == null) ? "-" : fechaFin.toString();
        return ini + "  →  " + fin;
    }
}
