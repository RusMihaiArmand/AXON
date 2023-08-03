package ro.axon.dot.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface EmployeeRepository extends
    JpaRepository<EmployeeEty, String>,
    QuerydslPredicateExecutor<EmployeeEty> {

  List<EmployeeEty> findEmployeeByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String name1, String name2);

}
