package org.mpashka.shopping.normalize;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Periodically drains un-normalized captures through the LLM normalizer. Disabled by
 * default (no API key in dev); enable with `shopping.normalize.enabled=true`. A guard
 * prevents overlapping runs since each LLM call is slow.
 */
@ApplicationScoped
public class NormalizationJob {

    private static final Logger LOG = Logger.getLogger(NormalizationJob.class);

    @Inject
    NormalizationService service;

    @ConfigProperty(name = "shopping.normalize.enabled", defaultValue = "false")
    boolean enabled;

    @ConfigProperty(name = "shopping.normalize.batch-size", defaultValue = "10")
    int batchSize;

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Scheduled(every = "{shopping.normalize.interval:5m}")
    void scheduled() {
        if (!enabled) return;
        runOnce();
    }

    /** Process one batch. Returns the number of captures processed. Safe to call manually. */
    public int runOnce() {
        if (!running.compareAndSet(false, true)) {
            LOG.debug("normalization already running; skipping");
            return 0;
        }
        try {
            List<NormalizeInput> batch = service.claimBatch(batchSize);
            int total = 0;
            for (NormalizeInput input : batch) {
                total += service.process(input);
            }
            if (!batch.isEmpty()) {
                LOG.infof("normalization batch: %d captures, %d offers", batch.size(), total);
            }
            return batch.size();
        } finally {
            running.set(false);
        }
    }
}
