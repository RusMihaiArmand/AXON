package ro.axon.dot.domain.repositories;

import java.sql.Date;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ro.axon.dot.domain.entity.LegallyDaysOffEty;

public interface LegallyDaysOffRepository extends
    JpaRepository<LegallyDaysOffEty, Date>,
    QuerydslPredicateExecutor<LegallyDaysOffEty> {

}
