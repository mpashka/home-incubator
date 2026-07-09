// @tag:domain-model @tag:reading-block @tag:request-id
package dev.homeincubator.lngedu.reading;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

/**
 * One recorded reading step: the block delivered by {@code get_next_chunk} whose result the
 * learner reported. Makes comprehension durable and lets daily stats derive chars/blocks read.
 */
@Entity
@Table(
        name = "reading_events",
        uniqueConstraints = @UniqueConstraint(name = "uq_reading_event_request_id", columnNames = "request_id")
)
public class ReadingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "book_id", nullable = false)
    private UUID bookId;

    // NULL when the event is not tied to a session.
    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "start_offset", nullable = false)
    private int startOffset;

    @Column(name = "end_offset", nullable = false)
    private int endOffset;

    @Column(name = "chars_read", nullable = false)
    private int charsRead;

    // 'understood' / 'partial' / 'unclear'.
    @Column(name = "comprehension", nullable = false)
    private String comprehension;

    // Idempotency key for record_chunk_result (@tag:request-id).
    @Column(name = "request_id", updatable = false)
    private String requestId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected ReadingEvent() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getBookId() {
        return bookId;
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }

    public int getCharsRead() {
        return charsRead;
    }

    public void setCharsRead(int charsRead) {
        this.charsRead = charsRead;
    }

    public String getComprehension() {
        return comprehension;
    }

    public void setComprehension(String comprehension) {
        this.comprehension = comprehension;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}
