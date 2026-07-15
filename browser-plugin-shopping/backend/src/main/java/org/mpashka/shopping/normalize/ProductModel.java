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
     * Dedup key. A marketplace `modelId` (when the LLM extracted one) is authoritative — it
     * groups every seller of the same model regardless of title/SoC/screen wording. Otherwise
     * fall back to a normalized title (SoC/screen are LLM-variable, so they are NOT in the key,
     * to avoid splitting one model into several).
     */
    static String keyOf(String title, String sourceModelId) {
        if (sourceModelId != null && !sourceModelId.isBlank()) {
            return "id:" + sourceModelId.trim();
        }
        String t = title == null ? "" : title;
        return "t:" + t.toLowerCase().replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
    }

    static ProductModel findOrCreate(String title, Double screenInches, String soc, String sourceModelId) {
        String key = keyOf(title, sourceModelId);
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
