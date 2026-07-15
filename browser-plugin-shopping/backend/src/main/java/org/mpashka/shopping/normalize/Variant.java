package org.mpashka.shopping.normalize;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A concrete configuration of a {@link ProductModel}: colour + RAM/ROM. Photos
 * live here (a colour's images), which is also what the phase-6 image matcher
 * will compare across marketplaces.
 */
@Entity
@Table(name = "variant")
public class Variant extends PanacheEntity {

    @ManyToOne(optional = false)
    public ProductModel model;

    public String color;
    public Integer ramGb;
    public Integer storageGb;

    /** model|color|ram|storage, unique within a model. */
    @Column(unique = true, length = 256)
    public String dedupKey;

    /**
     * Set by the image matcher (phase 6): variants sharing near-identical photos get the
     * same group id, flagging the same physical product even when their names/keys differ.
     */
    public Long matchGroupId;

    static String keyOf(Long modelId, String color, Integer ramGb, Integer storageGb) {
        return (modelId + "|" + color + "|" + ramGb + "|" + storageGb)
                .toLowerCase()
                .replaceAll("\\s+", " ")
                .trim();
    }

    static Variant findOrCreate(ProductModel model, String color, Integer ramGb, Integer storageGb) {
        String key = keyOf(model.id, color, ramGb, storageGb);
        Variant v = find("dedupKey", key).firstResult();
        if (v == null) {
            v = new Variant();
            v.model = model;
            v.color = color;
            v.ramGb = ramGb;
            v.storageGb = storageGb;
            v.dedupKey = key;
            v.persist();
        }
        return v;
    }
}
