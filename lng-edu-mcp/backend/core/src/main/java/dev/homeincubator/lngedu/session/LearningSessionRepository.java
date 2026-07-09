// @tag:domain-model @tag:request-id
package dev.homeincubator.lngedu.session;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LearningSessionRepository extends JpaRepository<LearningSession, UUID> {

    Optional<LearningSession> findByRequestId(String requestId);

    List<LearningSession> findByUserIdAndStartedAtBetween(UUID userId, Instant from, Instant to);
}
