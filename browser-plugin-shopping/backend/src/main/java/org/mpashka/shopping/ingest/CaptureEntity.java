package org.mpashka.shopping.ingest;

import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

/**
 * A raw capture persisted verbatim. The client-supplied {@code id} is the primary
 * key, which makes ingest idempotent: re-sending the same capture is a no-op.
 *
 * The marketplace payload and image list are stored as JSONB so they survive
 * schema drift and stay queryable for the later LLM normalization step.
 */
@Entity
@Table(name = "raw_capture")
public class CaptureEntity extends PanacheEntityBase {

    @Id
    public String id;

    public String marketplace;
    public String kind;

    @Column(length = 2048)
    public String pageUrl;

    @Column(length = 2048)
    public String requestUrl;

    public String searchQuery;
    public String source;

    /** Epoch millis on the client when captured. */
    public long capturedAt;

    /** Server time when this capture was accepted. */
    public Instant receivedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    public JsonNode payload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    public List<String> images;

    /** Set once normalization has consumed this capture (phase 5). */
    public boolean normalized;

    static CaptureEntity from(RawCapture c) {
        CaptureEntity e = new CaptureEntity();
        e.id = c.id();
        e.marketplace = c.marketplace();
        e.kind = c.kind();
        e.pageUrl = c.pageUrl();
        e.requestUrl = c.requestUrl();
        e.searchQuery = c.searchQuery();
        e.source = c.source();
        e.capturedAt = c.capturedAt();
        e.receivedAt = Instant.now();
        e.payload = c.payload();
        e.images = c.images();
        e.normalized = false;
        return e;
    }
}
