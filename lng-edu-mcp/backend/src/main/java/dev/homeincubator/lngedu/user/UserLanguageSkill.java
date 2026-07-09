// @tag:domain-model @tag:reading-block
package dev.homeincubator.lngedu.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "user_language_skills",
        uniqueConstraints = @UniqueConstraint(name = "uq_user_language", columnNames = {"user_id", "language"})
)
public class UserLanguageSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "language", nullable = false)
    private String language;

    @Column(name = "level", nullable = false)
    private String level;

    @Column(name = "block_min_words", nullable = false)
    private int blockMinWords;

    @Column(name = "block_max_words", nullable = false)
    private int blockMaxWords;

    @Column(name = "target_unknown_ratio")
    private BigDecimal targetUnknownRatio;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UserLanguageSkill() {
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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getBlockMinWords() {
        return blockMinWords;
    }

    public void setBlockMinWords(int blockMinWords) {
        this.blockMinWords = blockMinWords;
    }

    public int getBlockMaxWords() {
        return blockMaxWords;
    }

    public void setBlockMaxWords(int blockMaxWords) {
        this.blockMaxWords = blockMaxWords;
    }

    public BigDecimal getTargetUnknownRatio() {
        return targetUnknownRatio;
    }

    public void setTargetUnknownRatio(BigDecimal targetUnknownRatio) {
        this.targetUnknownRatio = targetUnknownRatio;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
