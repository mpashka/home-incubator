package org.mpashka.shopping.normalize;

/** Shared prompt text so every provider extracts the same shape. */
final class Prompts {

    private Prompts() {
    }

    static final String SYSTEM = """
            You extract structured tablet offers from raw marketplace API/DOM payloads
            (Ozon, Yandex Market). Return ONLY tablets. For each distinct listing produce
            one offer. Normalize the model name to a canonical form without colour/memory.
            Infer `deliveryFromChina` and `globalFirmware` from any delivery/description text.
            Include the listing's image URLs in `imageUrls` (used later to match the same
            product across marketplaces). Use null for anything you cannot determine.
            Do not invent values.
            """;

    /**
     * JSON shape for providers that lack enforced structured output (e.g. Yandex via
     * `response_format: json_object`). Anthropic uses schema-enforced structured output and
     * does not need this.
     */
    static final String JSON_SHAPE = """
            Respond with ONLY a JSON object of this exact shape, no prose, no markdown fences:
            {"offers":[{
              "modelTitle": string,        // canonical name without colour/memory
              "screenInches": number|null,
              "soc": string|null,          // e.g. "Snapdragon 8 Gen 3"
              "color": string|null,
              "ramGb": integer|null,
              "storageGb": integer|null,
              "marketplace": "ozon"|"yandex_market",
              "seller": string|null,
              "price": number|null,
              "greenPrice": number|null,   // Ozon card price if present
              "currency": string|null,     // e.g. "RUB"
              "deliveryFromChina": boolean|null,
              "globalFirmware": boolean|null,
              "url": string,
              "imageUrls": [string]        // may be empty
            }]}
            """;

    static String userMessage(NormalizeInput c) {
        return """
                Marketplace: %s
                Page kind: %s
                Search query: %s
                Raw payload follows (JSON):
                %s
                """.formatted(
                c.marketplace(), c.kind(),
                c.searchQuery() == null ? "" : c.searchQuery(),
                c.payloadJson() == null ? "{}" : c.payloadJson());
    }
}
