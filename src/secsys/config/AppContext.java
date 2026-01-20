package secsys.config;

import secsys.db.DbConnection;
import secsys.db.TransactionManager;
import secsys.repository.PingRepository;
import secsys.services.PingService;

public final class AppContext {

    private final DbConfig dbConfig;
    private final DbConnection dbConnection;
    private final TransactionManager txManager;

    private final PingRepository pingRepository;
    private final PingService pingService;

    public AppContext() {
        this.dbConfig = DbConfig.fromEnv();
        this.dbConnection = new DbConnection(dbConfig);
        this.txManager = new TransactionManager(dbConnection);

        this.pingRepository = new PingRepository(dbConnection);
        this.pingService = new PingService(pingRepository);
    }

    public DbConnection db() { return dbConnection; }
    public TransactionManager tx() { return txManager; }

    public PingService pingService() { return pingService; }
}
