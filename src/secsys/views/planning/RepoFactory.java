package secsys.views.planning;

import secsys.db.DbConnection;
import secsys.repository.ClienteRepository;
import secsys.repository.PlanningRepository;
import secsys.repository.UsuarioRepository;

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
}
