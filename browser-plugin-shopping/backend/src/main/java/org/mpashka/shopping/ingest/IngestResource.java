package org.mpashka.shopping.ingest;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Ingest endpoint the extension outbox POSTs to. Persists raw captures verbatim,
 * idempotently by client-supplied id. Phase 5 will hand the stored captures to
 * the LLM normalizer (RawCapture -> ProductModel / Variant / Offer).
 */
@Path("/api/ingest")
public class IngestResource {

    private static final Logger LOG = Logger.getLogger(IngestResource.class);

    public record IngestRequest(List<RawCapture> captures) {
    }

    /**
     * @param accepted total captures in the request
     * @param stored   how many were newly persisted (the rest were duplicates)
     */
    public record IngestResponse(int accepted, int stored) {
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public IngestResponse ingest(IngestRequest request) {
        List<RawCapture> captures = request.captures() == null ? List.of() : request.captures();
        int stored = 0;
        for (RawCapture c : captures) {
            if (c.id() == null || CaptureEntity.findById(c.id()) != null) {
                continue; // idempotent: skip missing id / already stored
            }
            CaptureEntity.from(c).persist();
            stored++;
        }
        LOG.infof("ingest: accepted=%d stored=%d", captures.size(), stored);
        return new IngestResponse(captures.size(), stored);
    }

    /** Quick sanity endpoint: total captures stored so far. */
    @GET
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON)
    public long count() {
        return CaptureEntity.count();
    }
}
