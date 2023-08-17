package ro.axon.dot.domain.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ro.axon.dot.domain.entity.EmployeeEty;

public interface EmployeeRepository extends
    JpaRepository<EmployeeEty, String>,
    QuerydslPredicateExecutor<EmployeeEty> {

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
  Optional<EmployeeEty> findEmployeeByUsername(String username);
}
