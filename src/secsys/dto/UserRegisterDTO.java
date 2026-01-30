package secsys.dto;

public class UserRegisterDTO {
    public String cedula;        // 10 dígitos
    public String nombres;       // 2 nombres, sin tildes/ñ
    public String apellidos;     // 2 apellidos, sin tildes/ñ
    public String username;      // ascii
    public String correo;        // @segadvice.com
    public String rol;           // Administrador|Gerente|Presidente|Empleado Operativo
    public String passwordPlain; // texto plano (se hashea en BD)
    public String estado;        // Activo|Inactivo (opcional: si null => Activo)
}
