// @tag:domain-model
package dev.homeincubator.lngedu.vocabulary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VocabularyItemRepository extends JpaRepository<VocabularyItem, UUID> {

    Optional<VocabularyItem> findByUserIdAndLanguageAndLemma(UUID userId, String language, String lemma);

    List<VocabularyItem> findByUserIdAndLanguage(UUID userId, String language);

    /** New words a learner added within a time window (for daily stats). */
    long countByUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(UUID userId, Instant from, Instant to);
}
