package secsys.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlanningUploadDTO {
    public UUID clienteId;

    // Cabecera CSV (Recomendación A, sin estadoAvance)
    public String tipoServicio; // debe coincidir con tu dominio d_tipo_servicio
    public String version;      // vX.Y
    public String estado;       // Activa | Inactiva (vigencia planificación)
    public String colorHex;     // #RRGGBB

    // Archivo
    public String fileName;
    public byte[] fileBytes;

    // Actividades
    public List<PlanningActivityDTO> actividades = new ArrayList<>();
}
