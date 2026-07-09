// @tag:domain-model @tag:reading-block
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

@Entity
@Table(
        name = "reading_progress",
        uniqueConstraints = @UniqueConstraint(name = "uq_user_book", columnNames = {"user_id", "book_id"})
)
public class ReadingProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "book_id", nullable = false)
    private UUID bookId;

    // Character offset into book_texts.content, not a chunk index.
    @Column(name = "position_char", nullable = false)
    private int positionChar;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ReadingProgress() {
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

    public int getPositionChar() {
        return positionChar;
    }

    public void setPositionChar(int positionChar) {
        this.positionChar = positionChar;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
