package secsys.dto;

import java.time.LocalDate;

public class ClienteCreateDTO {
    public String ruc;
    public String razonSocial;
    public String direccion;
    public String representanteLegal;
    public String telefono;
    public String correo;
    public String sector;   // Comercial, Industrial, Servicios, Tecnológico, Otro
    public String tamano;   // Microempresa, Pequeña, Mediana, Grande

    // Contrato (dentro del cliente)
    public LocalDate fechaInicioContrato;
    public LocalDate fechaFinContrato;
}
