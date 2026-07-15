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

    static String keyOf(String title, Double screenInches, String soc) {
        return (title + "|" + screenInches + "|" + soc)
                .toLowerCase()
                .replaceAll("\\s+", " ")
                .trim();
    }

    static ProductModel findOrCreate(String title, Double screenInches, String soc) {
        String key = keyOf(title, screenInches, soc);
        ProductModel m = find("dedupKey", key).firstResult();
        if (m == null) {
            m = new ProductModel();
            m.dedupKey = key;
            m.title = title;
            m.screenInches = screenInches;
            m.soc = soc;
            m.persist();
        }
        return m;
    }
}
