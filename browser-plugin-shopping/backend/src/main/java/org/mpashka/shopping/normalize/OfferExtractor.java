package org.mpashka.shopping.normalize;

import java.util.List;

/**
 * Provider-agnostic extraction of {@link NormalizedOffer}s from a raw capture. Implementations
 * name themselves via {@link #provider()}; {@link CaptureNormalizer} picks one by config.
 */
public interface OfferExtractor {

    /** Provider id matched against `shopping.llm.provider`, e.g. "yandex" or "anthropic". */
    String provider();

    List<NormalizedOffer> extract(NormalizeInput input);
}
