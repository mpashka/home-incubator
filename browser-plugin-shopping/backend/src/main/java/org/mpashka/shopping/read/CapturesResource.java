package org.mpashka.shopping.read;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.mpashka.shopping.ingest.CaptureEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Read API for the Vue viewer. Until LLM normalization (phase 5) exists, the
 * viewer browses raw captures directly: a paginated, filterable list plus a
 * detail endpoint that returns the verbatim payload.
 */
@Path("/api/captures")
@Produces(MediaType.APPLICATION_JSON)
public class CapturesResource {

    /** One row in the captures list. Keeps the payload out for a light response. */
    public record CaptureSummaryDto(
            String id,
            String marketplace,
            String kind,
            String searchQuery,
            String pageUrl,
            long capturedAt,
            int imageCount,
            String thumbnail
    ) {
        static CaptureSummaryDto of(CaptureEntity e) {
            List<String> imgs = e.images == null ? List.of() : e.images;
            return new CaptureSummaryDto(
                    e.id, e.marketplace, e.kind, e.searchQuery, e.pageUrl,
                    e.capturedAt, imgs.size(), imgs.isEmpty() ? null : imgs.get(0));
        }
    }

    public record CapturePage(List<CaptureSummaryDto> items, long total, int page, int size) {
    }

    @GET
    public CapturePage list(
            @QueryParam("marketplace") String marketplace,
            @QueryParam("kind") String kind,
            @QueryParam("q") String q,
            @QueryParam("page") @jakarta.ws.rs.DefaultValue("0") int page,
            @QueryParam("size") @jakarta.ws.rs.DefaultValue("50") int size) {

        // Build a dynamic filter; empty params are ignored.
        StringBuilder query = new StringBuilder("1=1");
        List<Object> params = new ArrayList<>();
        if (marketplace != null && !marketplace.isBlank()) {
            params.add(marketplace);
            query.append(" and marketplace = ?").append(params.size());
        }
        if (kind != null && !kind.isBlank()) {
            params.add(kind);
            query.append(" and kind = ?").append(params.size());
        }
        if (q != null && !q.isBlank()) {
            params.add("%" + q.toLowerCase() + "%");
            query.append(" and lower(searchQuery) like ?").append(params.size());
        }

        var find = CaptureEntity.find(query.toString(), Sort.by("receivedAt").descending(), params.toArray());
        long total = find.count();
        List<CaptureSummaryDto> items = find.page(Page.of(page, size))
                .<CaptureEntity>list().stream()
                .map(CaptureSummaryDto::of)
                .toList();
        return new CapturePage(items, total, page, size);
    }

    /** Full capture including the verbatim payload. */
    @GET
    @Path("/{id}")
    public CaptureEntity detail(@PathParam("id") String id) {
        CaptureEntity e = CaptureEntity.findById(id);
        if (e == null) throw new NotFoundException();
        return e;
    }
}
