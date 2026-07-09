// @tag:reading-block @tag:request-id
package dev.homeincubator.lngedu.reading;

import dev.homeincubator.lngedu.book.Book;
import dev.homeincubator.lngedu.book.BookRepository;
import dev.homeincubator.lngedu.book.BookText;
import dev.homeincubator.lngedu.book.BookTextRepository;
import dev.homeincubator.lngedu.common.NotFoundException;
import dev.homeincubator.lngedu.common.ValidationException;
import dev.homeincubator.lngedu.reading.ReadingCommands.ChunkResultView;
import dev.homeincubator.lngedu.reading.ReadingCommands.Comprehension;
import dev.homeincubator.lngedu.reading.ReadingCommands.GetNextChunkCommand;
import dev.homeincubator.lngedu.reading.ReadingCommands.NextChunkView;
import dev.homeincubator.lngedu.reading.ReadingCommands.RecordChunkResultCommand;
import dev.homeincubator.lngedu.user.UserLanguageSkill;
import dev.homeincubator.lngedu.user.UserLanguageSkillRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

/**
 * Reading use case.
 *
 * <p>{@code get_next_chunk} builds the reading block dynamically (@tag:reading-block) from
 * {@code book_texts} starting at {@code reading_progress.position_char}, honouring the learner's
 * {@code user_language_skills} block_min_words/block_max_words. It is a pure query and does NOT
 * advance progress.
 *
 * <p>{@code record_chunk_result} persists a {@code reading_events} row AND advances
 * {@code reading_progress.position_char} to the end of the delivered block, atomically in one
 * transaction. Idempotency (@tag:request-id): when a {@code request_id} is supplied it is looked
 * up in {@code reading_events} first and the stored result is returned unchanged on a repeat (the
 * DB UNIQUE constraint is the concurrency backstop), so a replay neither double-inserts an event
 * nor double-advances progress. Progress is also set to the absolute block end and only ever moves
 * forward ({@code max(current, endOffset)}).
 */
@Service
public class ReadingService {

    private final BookRepository bookRepository;
    private final BookTextRepository bookTextRepository;
    private final UserLanguageSkillRepository skillRepository;
    private final ReadingProgressRepository readingProgressRepository;
    private final ReadingEventRepository readingEventRepository;
    private final ReadingBlockAssembler assembler;
    private final Clock clock;

    public ReadingService(BookRepository bookRepository,
                          BookTextRepository bookTextRepository,
                          UserLanguageSkillRepository skillRepository,
                          ReadingProgressRepository readingProgressRepository,
                          ReadingEventRepository readingEventRepository,
                          ReadingBlockAssembler assembler,
                          Clock clock) {
        this.bookRepository = bookRepository;
        this.bookTextRepository = bookTextRepository;
        this.skillRepository = skillRepository;
        this.readingProgressRepository = readingProgressRepository;
        this.readingEventRepository = readingEventRepository;
        this.assembler = assembler;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public NextChunkView getNextChunk(GetNextChunkCommand cmd) {
        Book book = bookRepository.findById(cmd.bookId())
                .orElseThrow(() -> NotFoundException.of("book", cmd.bookId()));
        BookText text = bookTextRepository.findById(cmd.bookId())
                .orElseThrow(() -> NotFoundException.of("book text", cmd.bookId()));
        UserLanguageSkill skill = skillRepository
                .findByUserIdAndLanguage(cmd.userId(), book.getLanguage())
                .orElseThrow(() -> new NotFoundException(
                        "language skill not found for user " + cmd.userId()
                                + " and language " + book.getLanguage()));

        int start = readingProgressRepository.findByUserIdAndBookId(cmd.userId(), cmd.bookId())
                .map(ReadingProgress::getPositionChar)
                .orElse(0);

        ReadingBlock block = assembler.assemble(
                text.getContent(), start, skill.getBlockMinWords(), skill.getBlockMaxWords());

        boolean endOfBook = block.endOffset() >= text.getContent().length();
        return new NextChunkView(
                book.getId(), book.getLanguage(),
                Math.min(start, text.getContent().length()),
                block.endOffset(), block.text(), endOfBook);
    }

    @Transactional
    public ChunkResultView recordChunkResult(RecordChunkResultCommand cmd) {
        if (cmd.result() == null) {
            throw new ValidationException("comprehension result is required");
        }

        // Idempotent repeat: the event for this request_id already exists -> return it, no effect.
        boolean hasRequestId = cmd.requestId() != null && !cmd.requestId().isBlank();
        if (hasRequestId) {
            var existing = readingEventRepository.findByRequestId(cmd.requestId());
            if (existing.isPresent()) {
                ReadingEvent e = existing.get();
                int current = readingProgressRepository
                        .findByUserIdAndBookId(cmd.userId(), cmd.bookId())
                        .map(ReadingProgress::getPositionChar)
                        .orElse(e.getEndOffset());
                return new ChunkResultView(cmd.bookId(), current,
                        Comprehension.fromWire(e.getComprehension()));
            }
        }

        BookText text = bookTextRepository.findById(cmd.bookId())
                .orElseThrow(() -> NotFoundException.of("book text", cmd.bookId()));

        int target = clamp(cmd.endOffset(), 0, text.getLengthChars());

        ReadingProgress progress = readingProgressRepository
                .findByUserIdAndBookId(cmd.userId(), cmd.bookId())
                .orElseGet(() -> {
                    ReadingProgress p = new ReadingProgress();
                    p.setUserId(cmd.userId());
                    p.setBookId(cmd.bookId());
                    p.setPositionChar(0);
                    return p;
                });

        int startOffset = progress.getPositionChar();
        // Monotonic, absolute advance -> a replayed result cannot double-advance.
        int newPosition = Math.max(startOffset, target);
        int charsRead = Math.max(0, target - startOffset);
        Instant now = Instant.now(clock);

        progress.setPositionChar(newPosition);
        progress.setUpdatedAt(now);
        readingProgressRepository.save(progress);

        ReadingEvent event = new ReadingEvent();
        event.setUserId(cmd.userId());
        event.setBookId(cmd.bookId());
        event.setSessionId(cmd.sessionId());
        event.setStartOffset(startOffset);
        event.setEndOffset(target);
        event.setCharsRead(charsRead);
        event.setComprehension(cmd.result().wireCode());
        event.setRequestId(hasRequestId ? cmd.requestId() : null);
        event.setOccurredAt(now);
        try {
            readingEventRepository.save(event);
        } catch (DataIntegrityViolationException race) {
            // Lost a concurrent race on the UNIQUE request_id: the other writer already applied it.
            readingEventRepository.findByRequestId(cmd.requestId()).orElseThrow(() -> race);
        }

        return new ChunkResultView(cmd.bookId(), newPosition, cmd.result());
    }

    private static int clamp(int value, int lo, int hi) {
        return Math.max(lo, Math.min(hi, value));
    }
}
