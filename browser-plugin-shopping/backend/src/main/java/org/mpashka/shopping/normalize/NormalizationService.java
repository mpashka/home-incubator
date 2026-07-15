package org.mpashka.shopping.normalize;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.mpashka.shopping.ingest.CaptureEntity;
import org.mpashka.shopping.match.VariantImage;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Persistence side of normalization: reads un-normalized captures, and upserts the
 * LLM's offers into ProductModel → Variant → Offer with price history. The slow LLM
 * call lives in {@link CaptureNormalizer} and runs OUTSIDE these transactions.
 */
@ApplicationScoped
public class NormalizationService {

    private static final Logger LOG = Logger.getLogger(NormalizationService.class);

    @Inject
    CaptureNormalizer normalizer;

    @Inject
    PayloadTrimmer trimmer;

    /** Fetch a batch of un-normalized captures as detached inputs (transactional read). */
    @Transactional
    public List<NormalizeInput> claimBatch(int limit) {
        return CaptureEntity.<CaptureEntity>find("normalized = false", Sort.by("receivedAt"))
                .page(Page.ofSize(limit))
                .list().stream()
                .map(c -> new NormalizeInput(
                        c.id, c.marketplace, c.kind, c.searchQuery,
                        trimmer.trim(c.payload, c.marketplace)))
                .toList();
    }

    /** Upsert one capture's offers and mark it normalized. */
    @Transactional
    public void persist(String captureId, List<NormalizedOffer> offers) {
        for (NormalizedOffer o : offers) {
            if (o.url() == null || o.marketplace() == null || o.modelTitle() == null) continue;
            upsertOffer(o);
        }
        CaptureEntity c = CaptureEntity.findById(captureId);
        if (c != null) c.normalized = true;
    }

    /** Yandex Market URLs are `market.yandex.ru/product--<slug>/<modelId>`. */
    private static final Pattern YM_MODEL_ID = Pattern.compile("/product--[^/]*/(\\d+)");

    /**
     * The authoritative model id. Prefer the LLM's `sourceModelId`, but fall back to parsing it
     * from the product URL — the LLM copies the URL (which we build with the id) far more
     * reliably than a standalone field, so this makes model dedup robust.
     */
    private static String modelIdOf(NormalizedOffer o) {
        if (o.sourceModelId() != null && !o.sourceModelId().isBlank()) return o.sourceModelId();
        if (o.url() != null) {
            Matcher m = YM_MODEL_ID.matcher(o.url());
            if (m.find()) return m.group(1);
        }
        return null;
    }

    private void upsertOffer(NormalizedOffer o) {
        ProductModel model = ProductModel.findOrCreate(
                o.modelTitle(), o.screenInches(), o.soc(), modelIdOf(o));
        Variant variant = Variant.findOrCreate(model, o.color(), o.ramGb(), o.storageGb());

        // Register the listing's photos for later perceptual-hash matching.
        if (o.imageUrls() != null) {
            for (String imgUrl : o.imageUrls()) {
                if (imgUrl != null && !imgUrl.isBlank()) VariantImage.findOrCreate(variant, imgUrl);
            }
        }

        String key = Offer.keyOf(o.marketplace(), o.url());
        Offer offer = Offer.find("dedupKey", key).firstResult();
        boolean priceChanged;
        if (offer == null) {
            offer = new Offer();
            offer.dedupKey = key;
            offer.variant = variant;
            offer.marketplace = o.marketplace();
            offer.url = o.url();
            priceChanged = true;
        } else {
            priceChanged = !Objects.equals(offer.price, o.price())
                    || !Objects.equals(offer.greenPrice, o.greenPrice());
        }

        offer.seller = o.seller();
        offer.price = o.price();
        offer.greenPrice = o.greenPrice();
        offer.currency = o.currency();
        offer.deliveryFromChina = o.deliveryFromChina();
        offer.globalFirmware = o.globalFirmware();
        offer.lastSeenAt = Instant.now();
        offer.persist();

        if (priceChanged) {
            PriceHistory ph = new PriceHistory();
            ph.offer = offer;
            ph.price = o.price();
            ph.greenPrice = o.greenPrice();
            ph.observedAt = offer.lastSeenAt;
            ph.persist();
        }
    }

    /** Orchestrate: LLM-normalize (no tx) then persist (tx). Returns offers stored. */
    public int process(NormalizeInput input) {
        List<NormalizedOffer> offers;
        try {
            offers = normalizer.normalize(input);
        } catch (RuntimeException e) {
            LOG.errorf(e, "normalize failed for capture %s", input.id());
            return 0;
        }
        persist(input.id(), offers);
        return offers.size();
    }
}
