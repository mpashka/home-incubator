package dev.homeincubator.lngedu.vocabulary;

import dev.homeincubator.lngedu.user.UserRepository;
import dev.homeincubator.lngedu.vocabulary.VocabularyCommands.RecordUnknownWordCommand;
import dev.homeincubator.lngedu.vocabulary.VocabularyCommands.VocabularyItemView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Idempotency of record_unknown_word (@tag:request-id). Pure Mockito, no Spring/DB. */
@ExtendWith(MockitoExtension.class)
class VocabularyServiceIdempotencyTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-08T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private VocabularyItemRepository itemRepository;
    @Mock
    private WordEventRepository eventRepository;
    @Mock
    private UserRepository userRepository;

    @Test
    void recordingSameWordTwiceWithSameRequestId_insertsOneWordEvent() {
        VocabularyService service = new VocabularyService(itemRepository, eventRepository, userRepository, clock);
        UUID userId = UUID.randomUUID();
        String requestId = "req-word-1";
        RecordUnknownWordCommand cmd = new RecordUnknownWordCommand(
                userId, "sr", "Kuća", "Ovo je kuća.", null, requestId);

        lenient().when(userRepository.existsById(userId)).thenReturn(true);

        UUID itemId = UUID.randomUUID();
        VocabularyItem item = new VocabularyItem();
        item.setId(itemId);
        item.setUserId(userId);
        item.setLanguage("sr");
        item.setLemma("kuća"); // normalized (trimmed + lower-cased)
        item.setStatus("new");
        item.setLastSeenAt(Instant.now(clock));
        item.setCreatedAt(Instant.now(clock));

        // New item on the first call (no existing lemma yet).
        when(itemRepository.findByUserIdAndLanguageAndLemma(userId, "sr", "kuća"))
                .thenReturn(Optional.empty());
        when(itemRepository.save(any(VocabularyItem.class))).thenReturn(item);
        lenient().when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        WordEvent event = new WordEvent();
        event.setId(UUID.randomUUID());
        event.setVocabularyItemId(itemId);
        event.setRequestId(requestId);

        // First call: no event for this request_id; second call: the event created by the first.
        when(eventRepository.findByRequestId(requestId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(event));
        when(eventRepository.save(any(WordEvent.class))).thenReturn(event);

        VocabularyItemView first = service.recordUnknownWord(cmd);
        VocabularyItemView second = service.recordUnknownWord(cmd);

        assertThat(first.id()).isEqualTo(itemId);
        assertThat(second.id()).isEqualTo(itemId);
        assertThat(second.lemma()).isEqualTo("kuća");
        // Exactly one word_event is inserted despite two calls.
        verify(eventRepository, times(1)).save(any(WordEvent.class));
        // The second call short-circuits before touching the user check.
        verify(userRepository, times(1)).existsById(eq(userId));
    }

    @Test
    void unsupportedLanguage_isRejected() {
        VocabularyService service = new VocabularyService(itemRepository, eventRepository, userRepository, clock);
        RecordUnknownWordCommand cmd = new RecordUnknownWordCommand(
                UUID.randomUUID(), "de", "Haus", null, null, "req-x");
        assertThat(org.junit.jupiter.api.Assertions.assertThrows(
                dev.homeincubator.lngedu.common.ValidationException.class,
                () -> service.recordUnknownWord(cmd))).isNotNull();
        verify(eventRepository, never()).save(any());
    }
}
