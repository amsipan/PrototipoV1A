package secsys.dto;

import java.util.UUID;

public class UsuarioInfoDTO {
    public UUID usuarioId;
    public String cedula;
    public String nombres;
    public String apellidos;
    public String username;
    public String correo;
    public String rol;
    public String estado; // Activo | Inactivo
}
