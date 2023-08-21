package ro.axon.dot.domain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ro.axon.dot.domain.entity.EmpYearlyDaysOffEty;

public interface EmpYearlyDaysOffRepository extends
    JpaRepository<EmpYearlyDaysOffEty, Long>,
    QuerydslPredicateExecutor<EmpYearlyDaysOffEty> {

}
