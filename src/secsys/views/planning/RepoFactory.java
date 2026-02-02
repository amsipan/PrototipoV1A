package secsys.views.planning;

import secsys.db.DbConnection;
import secsys.repository.ClienteRepository;
import secsys.repository.PlanningRepository;
import secsys.repository.UsuarioRepository;

// ✅ estos 2 repos NO usan DbConnection en tu proyecto (usan DbConfig.fromEnv())
// así que RepoFactory solo los "construye" y ya.
import secsys.repository.PlatformLicensingRepository;
import secsys.repository.SystemSettingsRepository;

public final class RepoFactory {

    private static DbConnection db;

    private RepoFactory() {}

    public static void init(DbConnection dbConnection) {
        db = dbConnection;
    }

    private static void ensureInit() {
        if (db == null) {
            throw new IllegalStateException("RepoFactory no inicializado. Llama RepoFactory.init(dbConnection) al iniciar la app.");
        }
    }

    public static ClienteRepository clienteRepository() {
        ensureInit();
        return new ClienteRepository(db);
    }

    public static PlanningRepository planningRepository() {
        ensureInit();
        return new PlanningRepository(db);
    }

    public static UsuarioRepository usuarioRepository() {
        ensureInit();
        return new UsuarioRepository(db);
    }

    // ✅ NUEVOS (no dependen de DbConnection)
    public static SystemSettingsRepository systemSettingsRepository() {
        return new SystemSettingsRepository();
    }

    public static PlatformLicensingRepository platformLicensingRepository() {
        return new PlatformLicensingRepository();
    }
}
