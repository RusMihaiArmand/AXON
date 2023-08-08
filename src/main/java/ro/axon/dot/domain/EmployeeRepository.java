package ro.axon.dot.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface EmployeeRepository extends
    JpaRepository<EmployeeEty, String>,
    QuerydslPredicateExecutor<EmployeeEty> {

  List<EmployeeEty> findEmployeeByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String name1, String name2);
  Optional<EmployeeEty> findEmployeeByUsername(String username);
}
