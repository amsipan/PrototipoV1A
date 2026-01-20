package secsys.views.planning;

import secsys.db.DbConnection;
import secsys.repository.ClienteRepository;
import secsys.repository.PlanningRepository;

import java.util.Objects;

public final class RepoFactory {

    private static volatile DbConnection provider;

    private RepoFactory() {}

    public static void init(DbConnection dbProvider) {
        provider = Objects.requireNonNull(dbProvider);
    }

    public static PlanningRepository planningRepository() {
        if (provider == null) throw new IllegalStateException("RepoFactory no inicializado. Llama RepoFactory.init(dbConnection) al iniciar la app.");
        return new PlanningRepository(provider);
    }

    public static ClienteRepository clienteRepository() {
        if (provider == null) throw new IllegalStateException("RepoFactory no inicializado. Llama RepoFactory.init(dbConnection) al iniciar la app.");
        return new ClienteRepository(provider);
    }
}
