package ro.axon.dot.domain.persistence;

import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import ro.axon.dot.domain.entity.LegallyDaysOffEty;
import ro.axon.dot.domain.repositories.LegallyDaysOffRepository;

@Component
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
