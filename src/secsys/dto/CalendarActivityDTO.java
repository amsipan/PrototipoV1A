package secsys.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class CalendarActivityDTO {
    public UUID actividadId;
    public UUID planificacionId;
    public UUID clienteId;

    public String ruc;
    public String razonSocial;

    public String version;
    public String tipoServicio;

    public OffsetDateTime fechaInicio;
    public OffsetDateTime fechaFin;

    public String actividad;
    public String descripcion;
    public String estado;

    public String colorHex; // #RRGGBB
}
