package dev.homeincubator.lngedu.user;

import java.util.List;
import java.util.UUID;

/** A learner profile with its per-language reading skills. Transport-agnostic DTO. */
public record LearnerSummary(
        UUID id,
        String displayName,
        String timezone,
        List<LanguageSkillSummary> skills) {

    public record LanguageSkillSummary(
            String language,
            String level,
            int blockMinWords,
            int blockMaxWords) {
    }
}
