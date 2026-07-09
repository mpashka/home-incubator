// @tag:domain-model @tag:request-id
package dev.homeincubator.lngedu.vocabulary;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "word_events",
        uniqueConstraints = @UniqueConstraint(name = "uq_word_event_request_id", columnNames = "request_id")
)
public class WordEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "vocabulary_item_id", nullable = false)
    private UUID vocabularyItemId;

    // NULL when the event is not tied to a session.
    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "context")
    private String context;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    // Optional idempotency key (@tag:request-id).
    @Column(name = "request_id", updatable = false)
    private String requestId;

    protected WordEvent() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getVocabularyItemId() {
        return vocabularyItemId;
    }

    public void setVocabularyItemId(UUID vocabularyItemId) {
        this.vocabularyItemId = vocabularyItemId;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
