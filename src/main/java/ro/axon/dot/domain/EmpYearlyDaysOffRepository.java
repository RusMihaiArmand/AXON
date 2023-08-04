package ro.axon.dot.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface EmpYearlyDaysOffRepository extends
    JpaRepository<EmpYearlyDaysOffEty, Long>,
    QuerydslPredicateExecutor<EmpYearlyDaysOffEty> {

}
