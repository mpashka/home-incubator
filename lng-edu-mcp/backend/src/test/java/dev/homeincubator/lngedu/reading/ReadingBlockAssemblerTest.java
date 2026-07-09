package dev.homeincubator.lngedu.reading;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Pure unit tests for the reading-window algorithm (@tag:reading-block). No Spring/DB. */
class ReadingBlockAssemblerTest {

    private final ReadingBlockAssembler assembler = new ReadingBlockAssembler();

    @Test
    void emptyText_returnsEmptyBlockAtZero() {
        ReadingBlock block = assembler.assemble("", 0, 5, 20);
        assertThat(block.text()).isEmpty();
        assertThat(block.endOffset()).isZero();
    }

    @Test
    void textShorterThanMin_returnsWholeText() {
        String text = "One two three";
        ReadingBlock block = assembler.assemble(text, 0, 5, 20);
        assertThat(block.text()).isEqualTo(text);
        assertThat(block.endOffset()).isEqualTo(text.length());
    }

    @Test
    void prefersSentenceBoundaryWithinWindow() {
        // Boundaries after word 3 ("cc.") and word 6 ("ff."); many more words follow.
        String text = "Aa bb cc. Dd ee ff. Gg hh ii jj kk ll mm nn oo pp.";
        ReadingBlock block = assembler.assemble(text, 0, 3, 8);
        // Latest sentence boundary within [3,8] words is after "ff.".
        assertThat(block.text()).isEqualTo("Aa bb cc. Dd ee ff.");
        assertThat(text.substring(block.endOffset())).startsWith(" Gg");
    }

    @Test
    void prefersParagraphBoundaryWithinWindow() {
        String text = "Aa bb cc\n\nDd ee ff gg hh ii jj kk ll mm nn oo";
        ReadingBlock block = assembler.assemble(text, 0, 2, 8);
        // Blank line after "cc" is a paragraph boundary within the window.
        assertThat(block.text()).isEqualTo("Aa bb cc");
    }

    @Test
    void hardCutAtMaxWordsWhenNoBoundary() {
        String text = "a b c d e f g h i j k l";
        ReadingBlock block = assembler.assemble(text, 0, 3, 5);
        assertThat(block.text()).isEqualTo("a b c d e");
        assertThat(block.endOffset()).isEqualTo("a b c d e".length());
    }

    @Test
    void startAtEnd_returnsEmptyBlock() {
        String text = "Some words here.";
        ReadingBlock block = assembler.assemble(text, text.length(), 3, 10);
        assertThat(block.text()).isEmpty();
        assertThat(block.endOffset()).isEqualTo(text.length());
    }

    @Test
    void startBeyondEnd_isClampedAndEmpty() {
        String text = "Some words here.";
        ReadingBlock block = assembler.assemble(text, text.length() + 50, 3, 10);
        assertThat(block.text()).isEmpty();
        assertThat(block.endOffset()).isEqualTo(text.length());
    }

    @Test
    void neverExceedsTextLength() {
        String text = "one two three four five";
        ReadingBlock block = assembler.assemble(text, 0, 100, 200);
        assertThat(block.endOffset()).isEqualTo(text.length());
        assertThat(block.text()).isEqualTo(text);
    }

    @Test
    void sequentialCallsCoverWholeTextWithoutGapsOrOverlaps() {
        String text = "Alpha beta gamma. Delta epsilon zeta eta theta.\n\n"
                + "Iota kappa lambda mu nu xi omicron pi rho sigma tau upsilon phi chi psi omega. "
                + "Extra tail words without any terminal punctuation to force a hard cut here";

        StringBuilder reconstructed = new StringBuilder();
        int offset = 0;
        int guard = 0;
        while (offset < text.length()) {
            ReadingBlock block = assembler.assemble(text, offset, 4, 9);
            // Each block must make forward progress.
            assertThat(block.endOffset()).isGreaterThan(offset);
            // Text equals the exact contiguous slice -> no gaps, no overlaps.
            assertThat(block.text()).isEqualTo(text.substring(offset, block.endOffset()));
            reconstructed.append(block.text());
            offset = block.endOffset();
            assertThat(++guard).isLessThan(1000);
        }
        assertThat(reconstructed.toString()).isEqualTo(text);
        assertThat(offset).isEqualTo(text.length());
    }

    @Test
    void handlesTrailingWhitespaceRemainder() {
        String text = "word one two.   ";
        ReadingBlock first = assembler.assemble(text, 0, 1, 5);
        // Whole remainder (3 words) fits within max -> returns everything including trailing spaces.
        assertThat(first.endOffset()).isEqualTo(text.length());
        assertThat(first.text()).isEqualTo(text);
        ReadingBlock next = assembler.assemble(text, first.endOffset(), 1, 5);
        assertThat(next.text()).isEmpty();
    }
}
