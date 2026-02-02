package secsys.services;

import secsys.repository.QuotationRepository;
import java.time.LocalDate;
import java.util.List;

public class QuotationQueryService {

    private final QuotationRepository repo;

    public QuotationQueryService(QuotationRepository repo) {
        this.repo = repo;
    }

    public List<QuotationRepository.QuoteRow> findByPotentialRuc(String ruc) throws Exception {
        return repo.listByPotentialRucDetailed(ruc);
    }

    public List<QuotationRepository.QuoteRow> findByPeriod(LocalDate start, LocalDate end) throws Exception {
        return repo.listByDateRangeDetailed(start, end);
    }
}
