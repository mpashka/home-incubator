package org.mpashka.shopping.normalize;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Reduces a raw capture payload to a compact, clean offer list before it goes to the LLM.
 * Raw Yandex Market search payloads are ~2 MB entity graphs (≈750K tokens, far over the
 * model's 262K context). We reverse-engineered where the product data lives and extract
 * just that, cutting 2 MB → a few KB. The raw capture stays intact in the DB.
 *
 * Yandex Market: each product card is an object carrying `baobabPayload` (title, price,
 * marketSku/modelId ids, isCrossBorder = imported-from-abroad) and `productPayload`
 * (specs, gallery images). We emit one compact offer per card.
 *
 * Ozon payloads are small enough (≤ ~200 KB) to pass through; a final char cap guards the rest.
 */
@ApplicationScoped
public class PayloadTrimmer {

    private static final Logger LOG = Logger.getLogger(PayloadTrimmer.class);

    private static final int MAX_CHARS = 500_000;
    private static final int MAX_IMAGES_PER_OFFER = 4;

    @Inject
    ObjectMapper mapper;

    public String trim(JsonNode payload, String marketplace) {
        if (payload == null) return "{}";

        if ("yandex_market".equals(marketplace)) {
            ArrayNode offers = mapper.createArrayNode();
            collectYmOffers(payload, offers);
            if (!offers.isEmpty()) {
                ObjectNode out = mapper.createObjectNode();
                out.set("offers", offers);
                return cap(out.toString(), marketplace);
            }
            // No product cards (filters/suggest/empty resolve) — send a tiny stub.
            return "{\"offers\":[]}";
        }

        // Ozon and anything else: pass through, size-capped.
        return cap(payload.toString(), marketplace);
    }

    /** Walk the YM graph and pull one compact offer per product card. */
    private void collectYmOffers(JsonNode node, ArrayNode out) {
        if (node.isObject()) {
            JsonNode bb = node.get("baobabPayload");
            if (bb != null && bb.hasNonNull("title") && bb.has("price")) {
                out.add(ymOffer(bb, node.get("productPayload")));
                return; // don't descend into a card we've captured
            }
            node.forEach(child -> collectYmOffers(child, out));
        } else if (node.isArray()) {
            node.forEach(child -> collectYmOffers(child, out));
        }
    }

    private ObjectNode ymOffer(JsonNode bb, JsonNode pp) {
        ObjectNode o = mapper.createObjectNode();
        o.set("title", bb.get("title"));
        o.set("price", bb.get("price"));
        copy(bb, o, "marketSku");
        copy(bb, o, "modelId");
        copy(bb, o, "shopId");
        if (bb.has("isCrossBorder")) o.set("crossBorder", bb.get("isCrossBorder"));

        // Reconstruct a stable product URL so offers can be de-duplicated / price-tracked.
        if (bb.hasNonNull("modelId")) {
            String url = "https://market.yandex.ru/product--/" + bb.get("modelId").asText();
            if (bb.hasNonNull("marketSku")) url += "?sku=" + bb.get("marketSku").asText();
            o.put("url", url);
        }

        if (pp != null && pp.isObject()) {
            if (pp.has("specs")) o.set("specs", pp.get("specs"));
            Set<String> imgs = new LinkedHashSet<>();
            collectImages(pp.get("gallery"), imgs);
            if (!imgs.isEmpty()) {
                ArrayNode arr = o.putArray("images");
                imgs.stream().limit(MAX_IMAGES_PER_OFFER).forEach(arr::add);
            }
        }
        return o;
    }

    private static void copy(JsonNode src, ObjectNode dst, String field) {
        if (src.has(field)) dst.set(field, src.get(field));
    }

    /** Collect image URLs (YM avatars have no file extension). */
    private static void collectImages(JsonNode node, Set<String> out) {
        if (node == null) return;
        if (node.isTextual()) {
            String s = node.asText();
            if (s.contains("avatars.mds.yandex.net") || s.contains("/get-mpic/")) {
                out.add(s.startsWith("//") ? "https:" + s : s);
            }
        } else if (node.isArray() || node.isObject()) {
            node.forEach(c -> collectImages(c, out));
        }
    }

    private String cap(String s, String marketplace) {
        if (s.length() > MAX_CHARS) {
            LOG.warnf("payload capped %d → %d chars (%s)", s.length(), MAX_CHARS, marketplace);
            return s.substring(0, MAX_CHARS);
        }
        return s;
    }
}
