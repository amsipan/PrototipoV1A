package secsys.controllers;

import secsys.dto.ClienteCreateDTO;
import secsys.dto.ClienteInfoDTO;
import secsys.services.ClienteService;

import java.util.List;
import java.util.UUID;

public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }
    
    public UUID registrarCliente (ClienteCreateDTO cliente){
        return service.registrarCliente(cliente);
    }

    public ClienteInfoDTO consultarPorRuc(String ruc) { 
        return service.consultarPorRuc(ruc);
    }

    // NUEVO
    public List<ClienteInfoDTO> consultarPorRazonSocial(String razonSocial) {
        return service.consultarPorRazonSocial(razonSocial);
    }
}
