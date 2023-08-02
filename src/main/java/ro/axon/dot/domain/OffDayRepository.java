package ro.axon.dot.domain;

import java.sql.Date;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface OffDayRepository extends
    JpaRepository<OffDayEty, Date>,
    QuerydslPredicateExecutor<OffDayEty> {

}
