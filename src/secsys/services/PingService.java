package secsys.services;

import secsys.repository.PingRepository;

public class PingService {

    private final PingRepository repo;

    public PingService(PingRepository repo) {
        this.repo = repo;
    }

    public boolean isDbAlive() {
        return repo.ping();
    }


}
