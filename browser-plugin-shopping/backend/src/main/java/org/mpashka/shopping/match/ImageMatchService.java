package org.mpashka.shopping.match;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.mpashka.shopping.normalize.Variant;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 6: fetch variant photos, perceptual-hash them, and cluster variants whose photos
 * are near-identical (same manufacturer image reused across marketplaces) into match
 * groups. Downloads run OUTSIDE transactions; clustering is a single transactional pass.
 */
@ApplicationScoped
public class ImageMatchService {

    private static final Logger LOG = Logger.getLogger(ImageMatchService.class);

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /** Many image CDNs (Wikimedia, marketplace CDNs) 403 requests without a browser UA. */
    private static final String USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) "
                    + "Chrome/124.0 Safari/537.36 ShoppingCollector/0.1";

    public record ImageRef(Long id, String url) {
    }

    @Transactional
    List<ImageRef> claimUnhashed(int limit) {
        return VariantImage.<VariantImage>find("phash is null and fetchError is null")
                .page(0, limit)
                .list().stream()
                .map(vi -> new ImageRef(vi.id, vi.url))
                .toList();
    }

    @Transactional
    void store(Long id, Long phash, String error) {
        VariantImage vi = VariantImage.findById(id);
        if (vi == null) return;
        vi.phash = phash;
        vi.fetchError = error == null ? null : error.substring(0, Math.min(error.length(), 512));
        vi.fetchedAt = Instant.now();
    }

    private Long fetchAndHash(String url) throws IOException, InterruptedException {
        HttpResponse<byte[]> r = HTTP.send(
                HttpRequest.newBuilder(URI.create(url))
                        .timeout(Duration.ofSeconds(20))
                        .header("User-Agent", USER_AGENT)
                        .header("Accept", "image/avif,image/webp,image/png,image/*,*/*")
                        .GET().build(),
                HttpResponse.BodyHandlers.ofByteArray());
        if (r.statusCode() / 100 != 2) throw new IOException("HTTP " + r.statusCode());
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(r.body()));
        if (img == null) throw new IOException("unsupported/undecodable image");
        return ImageHash.pHash(img);
    }

    /** Download + hash up to {@code limit} pending images. Returns how many were hashed. */
    public int hashPending(int limit) {
        int hashed = 0;
        for (ImageRef ref : claimUnhashed(limit)) {
            try {
                store(ref.id(), fetchAndHash(ref.url()), null);
                hashed++;
            } catch (Exception e) {
                store(ref.id(), null, String.valueOf(e.getMessage()));
                LOG.debugf("hash failed for %s: %s", ref.url(), e.getMessage());
            }
        }
        return hashed;
    }

    /**
     * Cluster variants by photo similarity via union-find over all hashed images: any two
     * variants sharing a pair of images within {@code threshold} Hamming distance join the
     * same group. O(n²) over images — fine for a personal corpus. Returns the group count.
     */
    @Transactional
    public int recomputeMatches(int threshold) {
        record Img(long variantId, long phash) {
        }
        List<Img> imgs = VariantImage.<VariantImage>find("phash is not null").list().stream()
                .map(vi -> new Img(vi.variant.id, vi.phash))
                .toList();

        Map<Long, Long> parent = new HashMap<>();
        for (Img i : imgs) parent.putIfAbsent(i.variantId(), i.variantId());

        for (int a = 0; a < imgs.size(); a++) {
            for (int b = a + 1; b < imgs.size(); b++) {
                if (imgs.get(a).variantId() == imgs.get(b).variantId()) continue;
                if (ImageHash.hamming(imgs.get(a).phash(), imgs.get(b).phash()) <= threshold) {
                    union(parent, imgs.get(a).variantId(), imgs.get(b).variantId());
                }
            }
        }

        // Assign each variant its cluster root; leave photo-less variants untouched.
        List<Long> roots = new ArrayList<>();
        for (Long variantId : parent.keySet()) {
            long root = find(parent, variantId);
            Variant v = Variant.findById(variantId);
            if (v != null) v.matchGroupId = root;
            if (!roots.contains(root)) roots.add(root);
        }
        LOG.infof("recomputed matches: %d variants → %d groups", parent.size(), roots.size());
        return roots.size();
    }

    private static long find(Map<Long, Long> parent, long x) {
        long root = x;
        while (parent.get(root) != root) root = parent.get(root);
        while (parent.get(x) != root) {
            long next = parent.get(x);
            parent.put(x, root);
            x = next;
        }
        return root;
    }

    private static void union(Map<Long, Long> parent, long a, long b) {
        parent.put(find(parent, a), find(parent, b));
    }
}
