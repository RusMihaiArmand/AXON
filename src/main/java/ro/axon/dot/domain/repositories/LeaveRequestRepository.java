package ro.axon.dot.domain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ro.axon.dot.domain.entity.LeaveRequestEty;

public interface LeaveRequestRepository extends
        JpaRepository<LeaveRequestEty, Long>,
        QuerydslPredicateExecutor<LeaveRequestEty> {
}