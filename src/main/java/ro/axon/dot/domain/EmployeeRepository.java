package ro.axon.dot.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface EmployeeRepository extends
    JpaRepository<EmployeeEty, String>,
    QuerydslPredicateExecutor<EmployeeEty> {

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
