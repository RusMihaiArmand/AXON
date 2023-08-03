package ro.axon.dot.domain;

import java.sql.Date;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface LegallyDaysOffRepository extends
    JpaRepository<LegallyDaysOffEty, Date>,
    QuerydslPredicateExecutor<LegallyDaysOffEty> {

}
