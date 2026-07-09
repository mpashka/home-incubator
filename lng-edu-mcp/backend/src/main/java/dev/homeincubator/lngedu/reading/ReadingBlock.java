// @tag:reading-block
package dev.homeincubator.lngedu.reading;

/**
 * Result of assembling one dynamic reading block: the block text and the absolute
 * character offset just past it (exclusive). Feeding {@code endOffset} back as the next
 * start offset yields gap-free, overlap-free sequential reading.
 */
public record ReadingBlock(String text, int endOffset) {
}
