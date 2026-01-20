package secsys.services;

import secsys.dto.PlanningUploadDTO;
import secsys.repository.PlanningRepository;

import java.io.ByteArrayInputStream;
import java.util.UUID;

public class PlanningService {

    private final PlanningRepository repo;
    private final PlanningCsvParser parser = new PlanningCsvParser();

    public PlanningService(PlanningRepository repo) {
        this.repo = repo;
    }

    public UUID uploadPlanning(PlanningUploadDTO dto) {
        if (dto == null) throw new IllegalArgumentException("DTO null.");
        if (dto.fileBytes == null || dto.fileBytes.length == 0) {
            throw new IllegalArgumentException("Archivo CSV vacío.");
        }

        // Parse + valida (llena tipoServicio, version, estado, colorHex y actividades)
        parser.parse(new ByteArrayInputStream(dto.fileBytes), dto);

        // Insert transaccional
        return repo.insertUpload(dto);
    }
}
