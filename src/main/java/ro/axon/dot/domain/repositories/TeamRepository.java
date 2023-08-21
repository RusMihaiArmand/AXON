package ro.axon.dot.domain.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ro.axon.dot.domain.entity.TeamEty;
import ro.axon.dot.domain.enums.TeamStatus;

public interface TeamRepository extends JpaRepository<TeamEty, Long>, QuerydslPredicateExecutor<TeamEty> {
  List<TeamEty> findByStatus(TeamStatus status);
}
