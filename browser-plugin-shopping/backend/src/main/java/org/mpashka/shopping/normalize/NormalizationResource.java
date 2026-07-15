package org.mpashka.shopping.normalize;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

/**
 * Read API for normalized data plus a manual normalization trigger. Feeds the Vue
 * viewer's model view (ProductModel → Variant → Offer).
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class NormalizationResource {

    @Inject
    NormalizationJob job;

    public record OfferDto(String marketplace, String seller, Double price, Double greenPrice,
                           String currency, Boolean deliveryFromChina, Boolean globalFirmware, String url) {
    }

    public record VariantDto(String color, Integer ramGb, Integer storageGb, Long matchGroupId,
                             List<OfferDto> offers) {
    }

    public record ModelDto(Long id, String title, Double screenInches, String soc, List<VariantDto> variants) {
    }

    /** Trigger one normalization batch now (bypasses the enabled flag / schedule). */
    @POST
    @Path("/normalize/run")
    public java.util.Map<String, Integer> run() {
        return java.util.Map.of("processed", job.runOnce());
    }

    @GET
    @Path("/models")
    @Transactional
    public List<ModelDto> models() {
        return ProductModel.<ProductModel>listAll().stream().map(m -> {
            List<VariantDto> variants = Variant.<Variant>list("model", m).stream().map(v -> {
                List<OfferDto> offers = Offer.<Offer>list("variant", v).stream()
                        .map(o -> new OfferDto(o.marketplace, o.seller, o.price, o.greenPrice,
                                o.currency, o.deliveryFromChina, o.globalFirmware, o.url))
                        .toList();
                return new VariantDto(v.color, v.ramGb, v.storageGb, v.matchGroupId, offers);
            }).toList();
            return new ModelDto(m.id, m.title, m.screenInches, m.soc, variants);
        }).toList();
    }
}
