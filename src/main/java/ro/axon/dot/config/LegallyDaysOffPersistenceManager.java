package ro.axon.dot.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import ro.axon.dot.domain.LegallyDaysOffEty;
import ro.axon.dot.domain.LegallyDaysOffRepository;

@RequiredArgsConstructor
public class LegallyDaysOffPersistenceManager {

  private final LegallyDaysOffRepository legallyDaysOffRepository;


  @Cacheable(value = "legallyDaysOff")
  public List<LegallyDaysOffEty> getAllLegallyDaysOffFromDb() {
    return legallyDaysOffRepository.findAll();
  }

}
