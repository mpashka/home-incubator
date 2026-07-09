package dev.homeincubator.lngedu.book;

import java.util.UUID;

/** Book metadata plus an optional learner reading-progress view. Transport-agnostic DTO. */
public record BookSummary(
        UUID id,
        String title,
        String language,
        String author,
        String source,
        Progress progress) {

    /** Present only when a learner was supplied. */
    public record Progress(int positionChar, int lengthChars, double percentComplete) {
    }
}
