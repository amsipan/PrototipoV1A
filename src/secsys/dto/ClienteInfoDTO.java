package secsys.dto;

import java.time.LocalDate;
import java.util.UUID;

public class ClienteInfoDTO {
    public UUID clienteId;
    public String ruc;
    public String razonSocial;
    public String direccion;
    public String representanteLegal;
    public String telefono;
    public String correo;
    public String sector;
    public String tamano;
    public String estado;
    public LocalDate fechaInicioContrato;
    public LocalDate fechaFinContrato;
}
