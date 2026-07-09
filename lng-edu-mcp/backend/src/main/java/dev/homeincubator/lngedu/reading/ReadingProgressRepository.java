// @tag:domain-model @tag:reading-block
package dev.homeincubator.lngedu.reading;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, UUID> {

    Optional<ReadingProgress> findByUserIdAndBookId(UUID userId, UUID bookId);
}
