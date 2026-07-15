package org.mpashka.shopping.normalize;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * A tablet model, e.g. "Redmi Pad Pro 12.1". Distinct listings across variants,
 * sellers, and marketplaces collapse into one ProductModel. The LLM proposes the
 * canonical title/SoC/screen; {@link #dedupKey} is what we match on to avoid
 * duplicates.
 */
@Entity
@Table(name = "product_model")
public class ProductModel extends PanacheEntity {

    /** Lowercased, whitespace-collapsed identity: title|soc|screen. Unique. */
    @Column(unique = true, length = 512)
    public String dedupKey;

    public String title;
    public Double screenInches;
    public String soc;

    /**
     * Dedup key = normalized canonical title, for BOTH marketplaces. This groups the same
     * tablet across sellers, "from China" vs local listings, and Ozon vs Yandex Market — which
     * is the whole point (compare all prices for one model). SoC/screen are NOT in the key
     * (LLM-variable → would split one model); nor is a marketplace `modelId` (Yandex assigns a
     * distinct one per configuration/batch → would over-split). The LLM is prompted to keep the
     * title canonical (brand once, no colour/memory).
     */
    static String keyOf(String title) {
        String t = title == null ? "" : title;
        return t.toLowerCase().replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
    }

    static ProductModel findOrCreate(String title, Double screenInches, String soc) {
        String key = keyOf(title);
        ProductModel m = find("dedupKey", key).firstResult();
        if (m == null) {
            m = new ProductModel();
            m.dedupKey = key;
            m.title = title;
            m.screenInches = screenInches;
            m.soc = soc;
            m.persist();
        } else {
            // Enrich: fill attributes the first capture couldn't determine.
            if (m.screenInches == null && screenInches != null) m.screenInches = screenInches;
            if (m.soc == null && soc != null) m.soc = soc;
            if ((m.title == null || m.title.isBlank()) && title != null) m.title = title;
        }
        return m;
    }
}
