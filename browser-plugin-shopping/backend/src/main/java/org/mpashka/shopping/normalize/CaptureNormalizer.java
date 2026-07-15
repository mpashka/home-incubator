package org.mpashka.shopping.normalize;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Facade over the pluggable {@link OfferExtractor}s. The provider is chosen by
 * `shopping.llm.provider` (default "yandex"); "anthropic" is kept available. Persistence
 * lives in {@link NormalizationService}; this class only turns a raw capture into offers.
 */
@ApplicationScoped
public class CaptureNormalizer {

    private static final Logger LOG = Logger.getLogger(CaptureNormalizer.class);

    @Inject
    Instance<OfferExtractor> extractors;

    @ConfigProperty(name = "shopping.llm.provider", defaultValue = "yandex")
    String provider;

    private OfferExtractor selected;

    @PostConstruct
    void init() {
        for (OfferExtractor e : extractors) {
            if (e.provider().equalsIgnoreCase(provider)) {
                selected = e;
                break;
            }
        }
        if (selected == null) {
            throw new IllegalStateException("No OfferExtractor for shopping.llm.provider=" + provider);
        }
        LOG.infof("LLM provider: %s", selected.provider());
    }

    public List<NormalizedOffer> normalize(NormalizeInput input) {
        return selected.extract(input);
    }
}
