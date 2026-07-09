package dev.homeincubator.lngedu.reading;

import dev.homeincubator.lngedu.book.BookText;
import dev.homeincubator.lngedu.book.BookTextRepository;
import dev.homeincubator.lngedu.reading.ReadingCommands.ChunkResultView;
import dev.homeincubator.lngedu.reading.ReadingCommands.Comprehension;
import dev.homeincubator.lngedu.reading.ReadingCommands.RecordChunkResultCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** record_chunk_result persistence + idempotency (@tag:request-id). Pure Mockito, no Spring/DB. */
@ExtendWith(MockitoExtension.class)
class ReadingServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-08T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private dev.homeincubator.lngedu.book.BookRepository bookRepository;
    @Mock
    private BookTextRepository bookTextRepository;
    @Mock
    private dev.homeincubator.lngedu.user.UserLanguageSkillRepository skillRepository;
    @Mock
    private ReadingProgressRepository readingProgressRepository;
    @Mock
    private ReadingEventRepository readingEventRepository;

    private ReadingService service() {
        return new ReadingService(bookRepository, bookTextRepository, skillRepository,
                readingProgressRepository, readingEventRepository, new ReadingBlockAssembler(), clock);
    }

    private static BookText bookText(int length) {
        // BookText has a protected constructor; an anonymous subclass can call it.
        BookText t = new BookText() {
        };
        t.setContent("x".repeat(length));
        t.setLengthChars(length);
        return t;
    }

    @Test
    void recordChunkResult_persistsOneEventAndAdvancesProgress() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        ReadingService service = service();

        when(bookTextRepository.findById(bookId)).thenReturn(Optional.of(bookText(1000)));
        // No progress yet -> starts at 0.
        when(readingProgressRepository.findByUserIdAndBookId(userId, bookId)).thenReturn(Optional.empty());
        when(readingEventRepository.findByRequestId("r1")).thenReturn(Optional.empty());

        RecordChunkResultCommand cmd = new RecordChunkResultCommand(
                userId, bookId, sessionId, 120, Comprehension.PARTIAL, "r1");
        ChunkResultView view = service.recordChunkResult(cmd);

        assertThat(view.positionChar()).isEqualTo(120);
        assertThat(view.result()).isEqualTo(Comprehension.PARTIAL);

        ArgumentCaptor<ReadingProgress> prog = ArgumentCaptor.forClass(ReadingProgress.class);
        verify(readingProgressRepository).save(prog.capture());
        assertThat(prog.getValue().getPositionChar()).isEqualTo(120);

        ArgumentCaptor<ReadingEvent> event = ArgumentCaptor.forClass(ReadingEvent.class);
        verify(readingEventRepository, times(1)).save(event.capture());
        ReadingEvent saved = event.getValue();
        assertThat(saved.getStartOffset()).isEqualTo(0);
        assertThat(saved.getEndOffset()).isEqualTo(120);
        assertThat(saved.getCharsRead()).isEqualTo(120);
        assertThat(saved.getComprehension()).isEqualTo("partial");
        assertThat(saved.getSessionId()).isEqualTo(sessionId);
        assertThat(saved.getRequestId()).isEqualTo("r1");
        assertThat(saved.getOccurredAt()).isEqualTo(Instant.now(clock));
    }

    @Test
    void recordChunkResult_replayWithSameRequestId_insertsNoSecondEventAndDoesNotAdvance() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        ReadingService service = service();

        ReadingEvent existing = new ReadingEvent();
        existing.setEndOffset(120);
        existing.setComprehension("partial");

        ReadingProgress progress = new ReadingProgress();
        progress.setUserId(userId);
        progress.setBookId(bookId);
        progress.setPositionChar(120); // already advanced by the first call

        when(readingEventRepository.findByRequestId("r1")).thenReturn(Optional.of(existing));
        lenient().when(readingProgressRepository.findByUserIdAndBookId(userId, bookId))
                .thenReturn(Optional.of(progress));

        RecordChunkResultCommand cmd = new RecordChunkResultCommand(
                userId, bookId, null, 120, Comprehension.PARTIAL, "r1");
        ChunkResultView view = service.recordChunkResult(cmd);

        assertThat(view.positionChar()).isEqualTo(120);
        assertThat(view.result()).isEqualTo(Comprehension.PARTIAL);
        // No new event, no progress write on replay.
        verify(readingEventRepository, never()).save(any());
        verify(readingProgressRepository, never()).save(any());
        // Book text is never even loaded on the idempotent short-circuit.
        verify(bookTextRepository, never()).findById(any());
    }

    @Test
    void recordChunkResult_advanceIsMonotonic() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        ReadingService service = service();

        ReadingProgress progress = new ReadingProgress();
        progress.setUserId(userId);
        progress.setBookId(bookId);
        progress.setPositionChar(300);

        when(bookTextRepository.findById(bookId)).thenReturn(Optional.of(bookText(1000)));
        when(readingProgressRepository.findByUserIdAndBookId(userId, bookId))
                .thenReturn(Optional.of(progress));
        when(readingEventRepository.findByRequestId("r2")).thenReturn(Optional.empty());

        // A stale/earlier endOffset must not move progress backwards.
        RecordChunkResultCommand cmd = new RecordChunkResultCommand(
                userId, bookId, null, 120, Comprehension.UNDERSTOOD, "r2");
        ChunkResultView view = service.recordChunkResult(cmd);

        assertThat(view.positionChar()).isEqualTo(300);
        ArgumentCaptor<ReadingEvent> event = ArgumentCaptor.forClass(ReadingEvent.class);
        verify(readingEventRepository).save(event.capture());
        // charsRead never negative.
        assertThat(event.getValue().getCharsRead()).isEqualTo(0);
    }
}
