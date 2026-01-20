package secsys.dto;

import java.time.LocalDateTime;

public class PlanningActivityDTO {
    public LocalDateTime fechaInicio;
    public LocalDateTime fechaFin;
    public String actividad;
    public String descripcion;
    public String estado; // Pendiente | En_progreso | Completada | Cancelada
}
