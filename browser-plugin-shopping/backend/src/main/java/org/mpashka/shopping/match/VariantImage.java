package org.mpashka.shopping.match;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.mpashka.shopping.normalize.Variant;

import java.time.Instant;

/**
 * One photo of a {@link Variant}, plus its perceptual hash once fetched. The matcher
 * compares hashes across variants to detect the same physical product on different
 * marketplaces.
 */
@Entity
@Table(name = "variant_image")
public class VariantImage extends PanacheEntity {

    @ManyToOne(optional = false)
    public Variant variant;

    @Column(unique = true, length = 2048)
    public String url;

    /** 64-bit pHash; null until fetched, stays null if {@link #fetchError} is set. */
    public Long phash;

    public Instant fetchedAt;

    @Column(length = 512)
    public String fetchError;

    public static VariantImage findOrCreate(Variant variant, String url) {
        VariantImage vi = find("url", url).firstResult();
        if (vi == null) {
            vi = new VariantImage();
            vi.variant = variant;
            vi.url = url;
            vi.persist();
        }
        return vi;
    }
}
