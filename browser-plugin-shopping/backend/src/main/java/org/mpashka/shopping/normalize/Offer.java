package org.mpashka.shopping.normalize;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * A concrete purchase option for a {@link Variant}: one seller on one marketplace
 * at a price. This is the row the AI optimizer ultimately compares. Price changes
 * are appended to {@link PriceHistory}; the offer keeps the latest values.
 */
@Entity
@Table(name = "offer")
public class Offer extends PanacheEntity {

    @ManyToOne(optional = false)
    public Variant variant;

    public String marketplace;
    public String seller;

    @Column(length = 2048)
    public String url;

    public Double price;
    /** Ozon "green"/card price where present. */
    public Double greenPrice;
    public String currency;

    /** The key differentiators of the RF market: cheap "slow from China" listings. */
    public Boolean deliveryFromChina;
    public Boolean globalFirmware;

    public Instant lastSeenAt;

    /** marketplace|url identifies the offer across captures. */
    @Column(unique = true, length = 2100)
    public String dedupKey;

    static String keyOf(String marketplace, String url) {
        return (marketplace + "|" + url).toLowerCase().trim();
    }
}
