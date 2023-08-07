package ro.axon.dot.config;

import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import ro.axon.dot.domain.LegallyDaysOffEty;
import ro.axon.dot.domain.LegallyDaysOffRepository;

public class LegallyDaysOffPersistenceManager {

  private final LegallyDaysOffRepository legallyDaysOffRepository;

  public LegallyDaysOffPersistenceManager(LegallyDaysOffRepository legallyDaysOffRepository) {
    this.legallyDaysOffRepository = legallyDaysOffRepository;
  }

  @Cacheable(value = "legallyDaysOff")
  public List<LegallyDaysOffEty> getAllLegallyDaysOffFromDb() {
    return legallyDaysOffRepository.findAll();
  }

}
