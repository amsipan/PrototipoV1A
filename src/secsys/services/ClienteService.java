package secsys.services;

import secsys.dto.ClienteCreateDTO;
import secsys.dto.ClienteInfoDTO;
import secsys.repository.ClienteRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ClienteService {
    private final ClienteRepository repo;

    public ClienteService(ClienteRepository repo) {
        this.repo = repo;
    }

    public UUID registrarCliente(ClienteCreateDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Datos vacíos.");

        if (dto.ruc == null || !dto.ruc.trim().matches("^[0-9]{13}$"))
            throw new IllegalArgumentException("RUC inválido (debe tener 13 dígitos).");

        if (dto.razonSocial == null || dto.razonSocial.trim().length() < 3)
            throw new IllegalArgumentException("Razón social inválida.");

        if (dto.direccion == null || dto.direccion.trim().length() < 5)
            throw new IllegalArgumentException("Dirección inválida.");

        if (dto.representanteLegal == null || dto.representanteLegal.trim().length() < 3)
            throw new IllegalArgumentException("Representante legal inválido.");

        if (dto.telefono == null || !dto.telefono.trim().matches("^[0-9]{10}$"))
            throw new IllegalArgumentException("Teléfono inválido (debe tener 10 dígitos).");

        if (dto.correo == null || !dto.correo.trim().contains("@"))
            throw new IllegalArgumentException("Correo inválido.");

        if (dto.sector == null || dto.sector.equalsIgnoreCase("Seleccione"))
            throw new IllegalArgumentException("Seleccione un sector.");

        if (dto.tamano == null || dto.tamano.equalsIgnoreCase("Seleccione"))
            throw new IllegalArgumentException("Seleccione un tamaño de empresa.");

        // Fechas
        LocalDate ini = dto.fechaInicioContrato;
        LocalDate fin = dto.fechaFinContrato;

        if (ini == null) throw new IllegalArgumentException("Seleccione la fecha de inicio de contrato.");
        if (fin == null) throw new IllegalArgumentException("Seleccione la fecha de fin de contrato.");
        if (!fin.isAfter(ini)) throw new IllegalArgumentException("La fecha fin debe ser mayor a la fecha inicio.");

        return repo.insert(dto);
    }

    public ClienteInfoDTO consultarPorRuc(String ruc) {
        return repo.findByRuc(ruc);
    }

    public List<ClienteInfoDTO> consultarPorRazonSocial(String razonSocial) {
        return repo.findByRazonSocialLikeIgnoreCase(razonSocial);
    }
}
