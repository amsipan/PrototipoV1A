package secsys.dto;

import java.util.UUID;

public class UsuarioSessionDTO {
    public UUID usuarioId;
    public String username;
    public String rol;
    public String nombres;
    public String apellidos;

    public String nombreCompleto() {
        String n = (nombres == null) ? "" : nombres.trim();
        String a = (apellidos == null) ? "" : apellidos.trim();
        return (n + " " + a).trim();
    }
}
