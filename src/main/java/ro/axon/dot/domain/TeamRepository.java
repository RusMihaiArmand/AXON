package ro.axon.dot.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface TeamRepository extends
    JpaRepository<TeamEty, Long>,
    QuerydslPredicateExecutor<TeamEty> {

  List<TeamEty> findByStatus(TeamStatus status);
}
