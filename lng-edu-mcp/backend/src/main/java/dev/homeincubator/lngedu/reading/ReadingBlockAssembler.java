// @tag:reading-block
package dev.homeincubator.lngedu.reading;

import org.springframework.stereotype.Component;

/**
 * Pure, side-effect-free assembler for a single reading block.
 *
 * <p>Given the book text, a start character offset and the user's per-language
 * {@code block_min_words}/{@code block_max_words} window, it returns the block text plus the
 * absolute end offset. It has no Spring/DB dependencies and is fully unit-testable.
 *
 * <p>Boundary rules:
 * <ol>
 *   <li>Words are maximal runs of non-whitespace characters.</li>
 *   <li>If the remaining text has at most {@code maxWords} words, the whole remainder is
 *       returned (it ends at the natural end-of-text boundary).</li>
 *   <li>Otherwise the block must be cut. If a sentence/paragraph boundary falls within
 *       {@code [minWords, maxWords]} words, the block ends at the <em>latest</em> such
 *       boundary (largest block that still ends cleanly).</li>
 *   <li>If no boundary falls in the window, the block is hard-cut right after the
 *       {@code maxWords}-th word.</li>
 *   <li>The end offset never exceeds the text length; a start at/after the end yields an
 *       empty block.</li>
 * </ol>
 *
 * <p>The returned text always equals {@code text.substring(start, endOffset)}, so
 * sequential calls (each starting at the previous {@code endOffset}) cover the whole text
 * with no gaps or overlaps.
 */
@Component
public class ReadingBlockAssembler {

    public ReadingBlock assemble(String text, int startOffset, int minWords, int maxWords) {
        if (text == null) {
            throw new IllegalArgumentException("text must not be null");
        }
        int n = text.length();

        // Defensive normalisation of the word window.
        int max = Math.max(maxWords, 1);
        int min = Math.max(minWords, 1);
        if (min > max) {
            min = max;
        }

        int start = clamp(startOffset, 0, n);
        if (start >= n) {
            return new ReadingBlock("", n);
        }

        int i = start;
        int words = 0;
        int bestBoundaryEnd = -1; // latest sentence/paragraph boundary within [min, max]
        int hardCapEnd = -1;      // end just past the max-th word
        boolean exhausted = false;

        while (true) {
            // Skip whitespace before the next word.
            while (i < n && Character.isWhitespace(text.charAt(i))) {
                i++;
            }
            if (i >= n) {
                exhausted = true;
                break;
            }

            // Consume one word.
            while (i < n && !Character.isWhitespace(text.charAt(i))) {
                i++;
            }
            int wordEnd = i; // exclusive
            words++;

            if (words >= min && words <= max
                    && (endsSentence(text, wordEnd) || paragraphBreakAfter(text, wordEnd))) {
                bestBoundaryEnd = wordEnd; // keep the latest qualifying boundary
            }

            if (words >= max) {
                hardCapEnd = wordEnd;
                break;
            }
        }

        int endOffset;
        if (exhausted) {
            // Everything remaining fits within the window: return the whole remainder.
            endOffset = n;
        } else if (bestBoundaryEnd >= 0) {
            endOffset = bestBoundaryEnd;
        } else {
            endOffset = hardCapEnd; // hard cut at maxWords
        }

        return new ReadingBlock(text.substring(start, endOffset), endOffset);
    }

    /** True if the word ending at {@code wordEnd} (exclusive) closes a sentence. */
    private static boolean endsSentence(String text, int wordEnd) {
        int j = wordEnd - 1;
        // Skip trailing closing quotes/brackets, e.g. .")  or  ?»
        while (j >= 0 && isClosingWrapper(text.charAt(j))) {
            j--;
        }
        if (j < 0) {
            return false;
        }
        char c = text.charAt(j);
        return c == '.' || c == '!' || c == '?' || c == '…'; // …
    }

    private static boolean isClosingWrapper(char c) {
        return c == '"' || c == '\'' || c == ')' || c == ']' || c == '}'
                || c == '”' /* ” */ || c == '’' /* ’ */
                || c == '»' /* » */;
    }

    /** True if the whitespace run following {@code wordEnd} contains a blank line. */
    private static boolean paragraphBreakAfter(String text, int wordEnd) {
        int newlines = 0;
        for (int j = wordEnd; j < text.length(); j++) {
            char c = text.charAt(j);
            if (c == '\n') {
                newlines++;
                if (newlines >= 2) {
                    return true;
                }
            } else if (!Character.isWhitespace(c)) {
                break;
            }
        }
        return false;
    }

    private static int clamp(int value, int lo, int hi) {
        return Math.max(lo, Math.min(hi, value));
    }
}
