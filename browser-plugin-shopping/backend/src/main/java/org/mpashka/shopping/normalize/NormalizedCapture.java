package org.mpashka.shopping.normalize;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** Top-level structured-output shape: all tablet offers found in one capture. */
public record NormalizedCapture(
        @JsonPropertyDescription("Every distinct tablet offer found in the payload. Empty if none.")
        List<NormalizedOffer> offers) {
}
