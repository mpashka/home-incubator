package org.mpashka.shopping.normalize;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * One offer as extracted by the LLM from a raw capture. This is the structured-output
 * schema Claude fills — field descriptions guide the extraction. The backend then
 * clusters these into {@link ProductModel} → {@link Variant} → {@link Offer}.
 */
public record NormalizedOffer(
        @JsonPropertyDescription("Canonical model name without colour/memory, e.g. 'Redmi Pad Pro 12.1'")
        String modelTitle,
        @JsonPropertyDescription("Screen diagonal in inches, e.g. 12.1")
        Double screenInches,
        @JsonPropertyDescription("SoC / chipset as stated, e.g. 'Snapdragon 8 Gen 3'; null if unknown")
        String soc,
        @JsonPropertyDescription("Colour of this listing, e.g. 'Graphite'; null if unknown")
        String color,
        @JsonPropertyDescription("RAM in GB; null if unknown")
        Integer ramGb,
        @JsonPropertyDescription("Storage in GB; null if unknown")
        Integer storageGb,
        @JsonPropertyDescription("'ozon' or 'yandex_market'")
        String marketplace,
        @JsonPropertyDescription("Seller/shop name; null if unknown")
        String seller,
        @JsonPropertyDescription("Listed price as a number, no currency symbol")
        Double price,
        @JsonPropertyDescription("Ozon green/card price if present; otherwise null")
        Double greenPrice,
        @JsonPropertyDescription("Currency code, e.g. 'RUB'")
        String currency,
        @JsonPropertyDescription("True if the listing indicates slow delivery from China")
        Boolean deliveryFromChina,
        @JsonPropertyDescription("True if the listing mentions global firmware / global version")
        Boolean globalFirmware,
        @JsonPropertyDescription("Absolute product URL")
        String url,
        @JsonPropertyDescription("Marketplace model id if present in the payload (e.g. modelId); groups sellers of the same model. Null if unknown.")
        String sourceModelId,
        @JsonPropertyDescription("Absolute image URLs for THIS listing; empty if none")
        java.util.List<String> imageUrls) {
}
