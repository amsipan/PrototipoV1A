package secsys.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordUtil {

    private static final SecureRandom RNG = new SecureRandom();

    // Parámetros recomendables sin librerías externas
    private static final int SALT_BYTES = 16;
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;

    private PasswordUtil() {}

    public static String newSaltBase64() {
        byte[] salt = new byte[SALT_BYTES];
        RNG.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashPBKDF2Base64(String password, String saltBase64) {
        if (password == null) password = "";
        byte[] salt = Base64.getDecoder().decode(saltBase64);

        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar hash PBKDF2: " + e.getMessage(), e);
        }
    }

    public static boolean verify(String password, String saltBase64, String expectedHashBase64) {
        String hash = hashPBKDF2Base64(password, saltBase64);
        return constantTimeEquals(hash, expectedHashBase64);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int res = 0;
        for (int i = 0; i < a.length(); i++) res |= (a.charAt(i) ^ b.charAt(i));
        return res == 0;
    }
}
