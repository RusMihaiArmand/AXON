package ro.axon.dot.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface TeamRepository extends
    JpaRepository<TeamEty, Long>,
    QuerydslPredicateExecutor<TeamEty> {

    List<TeamEty> findByStatus(Status status);
}
