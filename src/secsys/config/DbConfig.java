package secsys.config;

import java.util.Objects;

public final class DbConfig {
    private final String host;
    private final int port;
    private final String dbName;
    private final String user;
    private final String password;
    private final String schema;
    private final boolean ssl;

    private DbConfig(String host, int port, String dbName, String user, String password, String schema, boolean ssl) {
        this.host = Objects.requireNonNull(host);
        this.port = port;
        this.dbName = Objects.requireNonNull(dbName);
        this.user = Objects.requireNonNull(user);
        this.password = password == null ? "" : password;
        this.schema = (schema == null || schema.isBlank()) ? "sgsis" : schema.trim();
        this.ssl = ssl;
    }

    /** Carga configuración desde variables de entorno con fallback para dev local */
    public static DbConfig fromEnv() {
        String host = env("DB_HOST", "localhost");
        int port = envInt("DB_PORT", 5432);
        String dbName = env("DB_NAME", "sgsis");
        String user = env("DB_USER", "postgres");
        String pass = env("DB_PASS", "221003");
        String schema = env("DB_SCHEMA", "sgsis");
        boolean ssl = envBool("DB_SSL", false);
        return new DbConfig(host, port, dbName, user, pass, schema, ssl);
    }

    /** URL JDBC con currentSchema para trabajar con un único esquema */
    public String jdbcUrl() {
        // currentSchema=sgsis evita escribir "sgsis." en cada query
        String base = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
        String params = "?currentSchema=" + schema + "&ssl=" + ssl;
        return base + params;
    }

    public String user() { return user; }
    public String password() { return password; }
    public String schema() { return schema; }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v.trim();
    }

    private static int envInt(String key, int def) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) return def;
        try { return Integer.parseInt(v.trim()); }
        catch (NumberFormatException ex) { return def; }
    }

    private static boolean envBool(String key, boolean def) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) return def;
        v = v.trim().toLowerCase();
        return v.equals("true") || v.equals("1") || v.equals("yes") || v.equals("y");
    }
}
