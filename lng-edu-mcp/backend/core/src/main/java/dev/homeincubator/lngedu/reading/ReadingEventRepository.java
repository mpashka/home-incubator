// @tag:domain-model @tag:reading-block @tag:request-id
package dev.homeincubator.lngedu.reading;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ReadingEventRepository extends JpaRepository<ReadingEvent, UUID> {

    Optional<ReadingEvent> findByRequestId(String requestId);

    /** Total characters read by a learner within a UTC instant window (0 when none). */
    @Query("select coalesce(sum(re.charsRead), 0) from ReadingEvent re "
            + "where re.userId = :userId and re.occurredAt >= :from and re.occurredAt < :to")
    long sumCharsRead(@Param("userId") UUID userId,
                      @Param("from") Instant from,
                      @Param("to") Instant to);

    /** Number of reading blocks (events) a learner recorded within a UTC instant window. */
    long countByUserIdAndOccurredAtGreaterThanEqualAndOccurredAtLessThan(UUID userId, Instant from, Instant to);
}
