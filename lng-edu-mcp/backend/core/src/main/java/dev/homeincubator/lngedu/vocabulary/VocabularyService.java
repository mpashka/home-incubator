// @tag:request-id
package dev.homeincubator.lngedu.vocabulary;

import dev.homeincubator.lngedu.common.NotFoundException;
import dev.homeincubator.lngedu.common.ValidationException;
import dev.homeincubator.lngedu.user.UserRepository;
import dev.homeincubator.lngedu.vocabulary.VocabularyCommands.RecordUnknownWordCommand;
import dev.homeincubator.lngedu.vocabulary.VocabularyCommands.VocabularyItemView;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Vocabulary use case: record an unknown word and list a learner's vocabulary.
 *
 * <p>{@code record_unknown_word} upserts {@code vocabulary_items} (keyed by user+language+lemma)
 * and inserts a {@code word_events} row. Idempotency (@tag:request-id): the {@code request_id} is
 * looked up in {@code word_events} first and its item is returned unchanged on a repeat; the DB
 * UNIQUE constraint on {@code word_events.request_id} is the backstop for concurrent inserts, so
 * a replay yields exactly one word_event.
 */
@Service
public class VocabularyService {

    private static final String EVENT_UNKNOWN = "unknown";
    private static final String STATUS_NEW = "new";
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("sr", "en");

    private final VocabularyItemRepository itemRepository;
    private final WordEventRepository eventRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public VocabularyService(VocabularyItemRepository itemRepository,
                             WordEventRepository eventRepository,
                             UserRepository userRepository,
                             Clock clock) {
        this.itemRepository = itemRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional
    public VocabularyItemView recordUnknownWord(RecordUnknownWordCommand cmd) {
        if (cmd.requestId() == null || cmd.requestId().isBlank()) {
            throw new ValidationException("requestId is required");
        }
        String language = requireLanguage(cmd.language());
        String lemma = normalizeLemma(cmd.lemma());

        // Idempotent repeat: the event for this request_id already exists -> return its item.
        var existingEvent = eventRepository.findByRequestId(cmd.requestId());
        if (existingEvent.isPresent()) {
            VocabularyItem item = itemRepository.findById(existingEvent.get().getVocabularyItemId())
                    .orElseThrow(() -> NotFoundException.of(
                            "vocabulary item", existingEvent.get().getVocabularyItemId()));
            return VocabularyItemView.of(item);
        }

        if (!userRepository.existsById(cmd.userId())) {
            throw NotFoundException.of("user", cmd.userId());
        }

        Instant now = Instant.now(clock);
        VocabularyItem item = upsertItem(cmd.userId(), language, lemma, cmd.context(), now);

        WordEvent event = new WordEvent();
        event.setVocabularyItemId(item.getId());
        event.setSessionId(cmd.sessionId());
        event.setEventType(EVENT_UNKNOWN);
        event.setContext(cmd.context());
        event.setOccurredAt(now);
        event.setRequestId(cmd.requestId());
        try {
            eventRepository.save(event);
        } catch (DataIntegrityViolationException race) {
            // Lost a concurrent race on the UNIQUE request_id: return the item, no second event.
            eventRepository.findByRequestId(cmd.requestId()).orElseThrow(() -> race);
        }

        return VocabularyItemView.of(item);
    }

    @Transactional(readOnly = true)
    public List<VocabularyItemView> listVocabulary(java.util.UUID userId, String language) {
        String lang = requireLanguage(language);
        return itemRepository.findByUserIdAndLanguage(userId, lang).stream()
                .map(VocabularyItemView::of)
                .toList();
    }

    private VocabularyItem upsertItem(java.util.UUID userId, String language, String lemma,
                                      String context, Instant now) {
        return itemRepository.findByUserIdAndLanguageAndLemma(userId, language, lemma)
                .map(existing -> {
                    existing.setLastContext(context);
                    existing.setLastSeenAt(now);
                    return itemRepository.save(existing);
                })
                .orElseGet(() -> {
                    VocabularyItem created = new VocabularyItem();
                    created.setUserId(userId);
                    created.setLanguage(language);
                    created.setLemma(lemma);
                    created.setStatus(STATUS_NEW);
                    created.setLastContext(context);
                    created.setLastSeenAt(now);
                    created.setCreatedAt(now);
                    return itemRepository.save(created);
                });
    }

    private static String requireLanguage(String language) {
        if (language == null || !SUPPORTED_LANGUAGES.contains(language)) {
            throw new ValidationException("unsupported language: " + language);
        }
        return language;
    }

    private static String normalizeLemma(String lemma) {
        if (lemma == null || lemma.isBlank()) {
            throw new ValidationException("lemma is required");
        }
        return lemma.trim().toLowerCase(Locale.ROOT);
    }
}
