// @tag:domain-model @tag:request-id
package dev.homeincubator.lngedu.vocabulary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WordEventRepository extends JpaRepository<WordEvent, UUID> {

    Optional<WordEvent> findByRequestId(String requestId);

    List<WordEvent> findByVocabularyItemId(UUID vocabularyItemId);
}
