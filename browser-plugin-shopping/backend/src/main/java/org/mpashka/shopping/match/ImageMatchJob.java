package org.mpashka.shopping.match;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Drains pending variant photos into perceptual hashes and recomputes match groups.
 * Scheduled (guarded by `shopping.match.enabled`), plus a manual `POST /api/match/run`.
 */
@ApplicationScoped
@Path("/api/match")
public class ImageMatchJob {

    @Inject
    ImageMatchService service;

    @ConfigProperty(name = "shopping.match.enabled", defaultValue = "false")
    boolean enabled;

    @ConfigProperty(name = "shopping.match.batch-size", defaultValue = "50")
    int batchSize;

    /** Max Hamming distance (of 64 bits) to treat two photos as the same picture. */
    @ConfigProperty(name = "shopping.match.threshold", defaultValue = "10")
    int threshold;

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Scheduled(every = "{shopping.match.interval:10m}")
    void scheduled() {
        if (enabled) runOnce();
    }

    @POST
    @Path("/run")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Integer> run() {
        return runOnce();
    }

    /** Hash a batch of pending photos, then recluster. Returns {hashed, groups}. */
    public Map<String, Integer> runOnce() {
        if (!running.compareAndSet(false, true)) {
            return Map.of("hashed", 0, "groups", -1);
        }
        try {
            int hashed = service.hashPending(batchSize);
            int groups = service.recomputeMatches(threshold);
            return Map.of("hashed", hashed, "groups", groups);
        } finally {
            running.set(false);
        }
    }
}
