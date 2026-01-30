package secsys.services;

import secsys.db.DbException;
import secsys.dto.UserRegisterDTO;

import java.text.Normalizer;

public final class UsuarioValidator {

    private UsuarioValidator() {}

    public static void validateRegister(UserRegisterDTO dto) {
        if (dto == null) throw new DbException("DTO null.");

        dto.cedula = trim(dto.cedula);
        dto.nombres = normalizeSpaces(trim(dto.nombres));
        dto.apellidos = normalizeSpaces(trim(dto.apellidos));
        dto.username = trim(dto.username);
        dto.correo = trim(dto.correo);
        dto.rol = trim(dto.rol);
        dto.passwordPlain = dto.passwordPlain == null ? "" : dto.passwordPlain;

        if (!isCedula10(dto.cedula)) throw new DbException("Cédula inválida: debe tener 10 dígitos.");
        if (dto.username.isBlank()) throw new DbException("Nombre de usuario es obligatorio.");
        if (!dto.username.matches("^[A-Za-z0-9._-]{4,30}$")) {
            throw new DbException("Nombre de usuario inválido: use 4-30 caracteres (A-Z, a-z, 0-9, . _ -).");
        }

        // Nombres / Apellidos: ASCII sin tildes ni ñ
        validateAsciiNoAccentsNoEnye(dto.nombres, "Nombres");
        validateAsciiNoAccentsNoEnye(dto.apellidos, "Apellidos");

        // Debe tener 2 palabras mínimo
        if (countWords(dto.nombres) < 2) throw new DbException("Nombres debe tener al menos 2 nombres.");
        if (countWords(dto.apellidos) < 2) throw new DbException("Apellidos debe tener al menos 2 apellidos.");

        // Correo institucional
        if (dto.correo.isBlank()) throw new DbException("Correo es obligatorio.");
        if (!dto.correo.matches("(?i)^[A-Za-z0-9._%+-]+@segadvice\\.com$")) {
            throw new DbException("Correo inválido: debe ser institucional @segadvice.com.");
        }

        // Rol
        if (!isValidRol(dto.rol)) {
            throw new DbException("Rol inválido. Use: Administrador, Gerente, Presidente, Empleado Operativo.");
        }

        // Password mínima
        if (dto.passwordPlain.isBlank()) throw new DbException("Contraseña es obligatoria.");
        if (dto.passwordPlain.length() < 6) throw new DbException("Contraseña demasiado corta (mínimo 6 caracteres).");

        // Estado
        if (dto.estado != null && !dto.estado.isBlank()) {
            dto.estado = trim(dto.estado);
            if (!dto.estado.equals("Activo") && !dto.estado.equals("Inactivo")) {
                throw new DbException("Estado inválido. Use: Activo o Inactivo.");
            }
        } else {
            dto.estado = "Activo";
        }
    }

    public static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static boolean isCedula10(String s) {
        return s != null && s.matches("^[0-9]{10}$");
    }

    private static int countWords(String s) {
        if (s == null) return 0;
        String t = s.trim();
        if (t.isEmpty()) return 0;
        return t.split("\\s+").length;
    }

    private static void validateAsciiNoAccentsNoEnye(String s, String field) {
        if (s == null || s.isBlank()) throw new DbException(field + " es obligatorio.");

        // Rechaza ñ/Ñ explícitamente
        if (s.indexOf('ñ') >= 0 || s.indexOf('Ñ') >= 0) {
            throw new DbException(field + " no debe contener 'ñ'. Use solo letras sin tildes.");
        }

        // Rechaza tildes/diacríticos: si cambia al quitar diacríticos, entonces tenía tildes
        String nfd = Normalizer.normalize(s, Normalizer.Form.NFD);
        String stripped = nfd.replaceAll("\\p{M}+", ""); // elimina marcas diacríticas
        if (!stripped.equals(s)) {
            throw new DbException(field + " no debe contener tildes. Ej: 'Jose' en lugar de 'José'.");
        }

        // Solo letras y espacios (ASCII)
        if (!s.matches("^[A-Za-z]+( [A-Za-z]+)*$")) {
            throw new DbException(field + " inválido: solo letras A-Z y espacios (sin símbolos).");
        }
    }

    private static boolean isValidRol(String rol) {
        return "Administrador".equals(rol)
                || "Gerente".equals(rol)
                || "Presidente".equals(rol)
                || "Empleado Operativo".equals(rol);
    }

    private static String normalizeSpaces(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s{2,}", " ");
    }
}
