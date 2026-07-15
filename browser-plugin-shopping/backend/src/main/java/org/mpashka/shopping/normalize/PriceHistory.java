package org.mpashka.shopping.normalize;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/** One observed price point for an {@link Offer}, appended each time it changes. */
@Entity
@Table(name = "price_history")
public class PriceHistory extends PanacheEntity {

    @ManyToOne(optional = false)
    public Offer offer;

    public Double price;
    public Double greenPrice;
    public Instant observedAt;
}
