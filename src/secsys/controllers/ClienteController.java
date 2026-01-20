package secsys.controllers;

import secsys.dto.ClienteCreateDTO;
import secsys.dto.ClienteInfoDTO;
import secsys.services.ClienteService;

import java.util.UUID;

public class ClienteController {
    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    public UUID registrar(ClienteCreateDTO dto) {
        return service.registrarCliente(dto);
    }

    public ClienteInfoDTO consultarPorRuc(String ruc) {
        return service.consultarPorRuc(ruc);
    }
}
