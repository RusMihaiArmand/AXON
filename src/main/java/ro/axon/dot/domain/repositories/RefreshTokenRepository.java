package ro.axon.dot.domain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.axon.dot.domain.entity.RefreshTokenEty;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEty, String> {
}
