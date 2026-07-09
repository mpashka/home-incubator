// @tag:domain-model @tag:reading-block
package dev.homeincubator.lngedu.book;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "book_texts")
public class BookText {

    // Shares its PK with books.id (1:1); no generated value here.
    @Id
    @Column(name = "book_id", nullable = false, updatable = false)
    private UUID bookId;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "length_chars", nullable = false)
    private int lengthChars;

    public BookText() {
    }

    public UUID getBookId() {
        return bookId;
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getLengthChars() {
        return lengthChars;
    }

    public void setLengthChars(int lengthChars) {
        this.lengthChars = lengthChars;
    }
}
