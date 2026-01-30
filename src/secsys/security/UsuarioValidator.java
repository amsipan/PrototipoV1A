package secsys.security;

import secsys.dto.UsuarioCreateDTO;

public final class UsuarioValidator {

    private UsuarioValidator() {}

    public static void validateOrThrow(UsuarioCreateDTO dto, String confirmPassword) {

        if (dto == null) throw new IllegalArgumentException("DTO null.");

        if (isBlank(dto.cedula)) throw new IllegalArgumentException("La cédula es obligatoria.");
        if (isBlank(dto.apellidos)) throw new IllegalArgumentException("Los apellidos son obligatorios.");
        if (isBlank(dto.nombres)) throw new IllegalArgumentException("Los nombres son obligatorios.");
        if (isBlank(dto.username)) throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        if (isBlank(dto.correo)) throw new IllegalArgumentException("El correo es obligatorio.");
        if (isBlank(dto.rol)) throw new IllegalArgumentException("El rol es obligatorio.");

        if (isBlank(dto.password)) throw new IllegalArgumentException("La contraseña es obligatoria.");
        if (dto.password.length() < 6) throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");

        if (confirmPassword == null) confirmPassword = "";
        if (!dto.password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Las contraseñas no coinciden.");
        }

        // correo institucional
        String mail = dto.correo.trim().toLowerCase();
        if (!mail.endsWith("@segadvice.com")) {
            throw new IllegalArgumentException("El correo debe ser institucional (@segadvice.com).");
        }

        // username sin tildes/ñ (tu regla)
        String u = dto.username.trim();
        if (!u.matches("^[A-Za-z0-9._-]+$")) {
            throw new IllegalArgumentException("El usuario solo puede contener letras sin tildes/ñ, números y . _ -");
        }

        // nombres/apellidos sin tildes/ñ (tu regla)
        if (!dto.nombres.matches("^[A-Za-z ]+$") || !dto.apellidos.matches("^[A-Za-z ]+$")) {
            throw new IllegalArgumentException("Nombres y apellidos no deben contener tildes ni ñ (solo letras y espacios).");
        }

        // roles válidos
        if (!(dto.rol.equals("Administrador") ||
              dto.rol.equals("Gerente") ||
              dto.rol.equals("Presidente") ||
              dto.rol.equals("Empleado Operativo"))) {
            throw new IllegalArgumentException("Rol no válido.");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
