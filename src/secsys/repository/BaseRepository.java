package secsys.repository;

import secsys.db.ConnectionProvider;
import secsys.db.DbConnection;

public abstract class BaseRepository {
    protected final DbConnection provider;

    protected BaseRepository(DbConnection provider2) {
        this.provider = provider2;
    }
}
