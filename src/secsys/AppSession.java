package secsys;

import secsys.dto.UsuarioInfoDTO;

public final class AppSession {

    private static UsuarioInfoDTO current;

    private AppSession() {}

    public static void set(UsuarioInfoDTO session) {
        current = session;
    }

    public static UsuarioInfoDTO get() {
        return current;
    }

    public static String role() {
        return current == null ? null : current.rol;
    }

    public static boolean isOperative() {
        String r = role();
        return r != null && r.equalsIgnoreCase("Empleado Operativo");
    }

    public static void clear() {
        current = null;
    }
}
