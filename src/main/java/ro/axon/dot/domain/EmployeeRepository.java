package ro.axon.dot.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface EmployeeRepository extends
    JpaRepository<EmployeeEty, String>,
    QuerydslPredicateExecutor<EmployeeEty> {

    List<EmployeeEty> findByUsername(String username);
    List<EmployeeEty> findByEmail(String email);
}
