package org.mpashka.shopping.ingest;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Mirror of the extension's RawCapture contract (see extension/src/shared/types.ts).
 * The {@code payload} is kept as an opaque JSON tree — the LLM normalizer owns
 * interpretation, and keeping it verbatim lets us re-parse when marketplace
 * schemas change.
 *
 * @param id          stable client-side uuid; used for idempotent ingest
 * @param marketplace "ozon" | "yandex_market"
 * @param kind        "search" | "product" | "unknown"
 * @param pageUrl     page the capture was observed on
 * @param requestUrl  request URL that returned the payload (network captures)
 * @param searchQuery best-effort query associated with the capture
 * @param capturedAt  epoch millis on the client
 * @param source      "network" | "dom"
 * @param payload     verbatim marketplace payload
 * @param images      absolute image URLs referenced by the capture
 */
public record RawCapture(
        String id,
        String marketplace,
        String kind,
        String pageUrl,
        String requestUrl,
        String searchQuery,
        long capturedAt,
        String source,
        JsonNode payload,
        List<String> images
) {
}
