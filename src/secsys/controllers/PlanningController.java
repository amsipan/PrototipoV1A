package secsys.controllers;

import secsys.dto.PlanningUploadDTO;
import secsys.services.PlanningService;

import java.util.UUID;

public class PlanningController {

    private final PlanningService service;

    public PlanningController(PlanningService service) {
        this.service = service;
    }

    /**
     * Sube una planificación desde un CSV:
     * - valida y parsea el CSV
     * - inserta planificacion + actividades + upload en una transacción
     */
    public UUID uploadPlanning(PlanningUploadDTO dto) {
        return service.uploadPlanning(dto);
    }

    
}
