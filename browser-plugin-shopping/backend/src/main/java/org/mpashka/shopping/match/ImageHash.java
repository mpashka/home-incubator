package org.mpashka.shopping.match;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * DCT-based perceptual hash (pHash). Reduces an image to a 64-bit signature that is
 * stable under resize/recompression/minor edits, so the same manufacturer photo reused
 * by different marketplaces hashes to the same (or near) value. Compare two hashes with
 * {@link #hamming(long, long)}: a small distance means "same picture".
 *
 * Pure Java (AWT only) — deterministic and unit-testable offline. Semantic matching of
 * genuinely different photos (angles, backgrounds) is a future embeddings upgrade.
 */
public final class ImageHash {

    private static final int SIZE = 32; // DCT input size
    private static final int LOW = 8;   // low-frequency block kept

    private ImageHash() {
    }

    public static long pHash(BufferedImage src) {
        double[] gray = toGray32(src);
        double[][] dct = dct2d(gray);

        // Average of the top-left LOW×LOW block, excluding the DC term [0][0].
        double sum = 0;
        int n = 0;
        for (int y = 0; y < LOW; y++) {
            for (int x = 0; x < LOW; x++) {
                if (x == 0 && y == 0) continue;
                sum += dct[y][x];
                n++;
            }
        }
        double avg = sum / n;

        long hash = 0L;
        int bit = 0;
        for (int y = 0; y < LOW; y++) {
            for (int x = 0; x < LOW; x++) {
                if (x == 0 && y == 0) continue;
                if (dct[y][x] > avg) hash |= (1L << bit);
                bit++;
            }
        }
        return hash;
    }

    /** Number of differing bits between two hashes (0 = identical, 64 = opposite). */
    public static int hamming(long a, long b) {
        return Long.bitCount(a ^ b);
    }

    private static double[] toGray32(BufferedImage src) {
        BufferedImage g = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = g.createGraphics();
        graphics.drawImage(src, 0, 0, SIZE, SIZE, null);
        graphics.dispose();

        double[] out = new double[SIZE * SIZE];
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                out[y * SIZE + x] = g.getRaster().getSample(x, y, 0);
            }
        }
        return out;
    }

    /** Naive separable 2D DCT-II over a SIZE×SIZE grid. */
    private static double[][] dct2d(double[] in) {
        double[][] out = new double[SIZE][SIZE];
        double c0 = Math.sqrt(1.0 / SIZE);
        double c = Math.sqrt(2.0 / SIZE);
        for (int u = 0; u < SIZE; u++) {
            for (int v = 0; v < SIZE; v++) {
                double sum = 0;
                for (int y = 0; y < SIZE; y++) {
                    for (int x = 0; x < SIZE; x++) {
                        sum += in[y * SIZE + x]
                                * Math.cos(((2 * x + 1) * u * Math.PI) / (2 * SIZE))
                                * Math.cos(((2 * y + 1) * v * Math.PI) / (2 * SIZE));
                    }
                }
                double cu = (u == 0) ? c0 : c;
                double cv = (v == 0) ? c0 : c;
                out[v][u] = cu * cv * sum;
            }
        }
        return out;
    }
}
