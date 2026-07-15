// @tag:mcp-tools @tag:vertical-slice @tag:request-id @tag:auth
package dev.homeincubator.lngedu.mcp;

import dev.homeincubator.lngedu.account.AccountService;
import dev.homeincubator.lngedu.book.BookService;
import dev.homeincubator.lngedu.book.BookSummary;
import dev.homeincubator.lngedu.common.ValidationException;
import dev.homeincubator.lngedu.security.CurrentAccount;
import dev.homeincubator.lngedu.reading.ReadingCommands.ChunkResultView;
import dev.homeincubator.lngedu.reading.ReadingCommands.Comprehension;
import dev.homeincubator.lngedu.reading.ReadingCommands.GetNextChunkCommand;
import dev.homeincubator.lngedu.reading.ReadingCommands.NextChunkView;
import dev.homeincubator.lngedu.reading.ReadingCommands.RecordChunkResultCommand;
import dev.homeincubator.lngedu.reading.ReadingService;
import dev.homeincubator.lngedu.session.SessionCommands.FinishSessionCommand;
import dev.homeincubator.lngedu.session.SessionCommands.SessionView;
import dev.homeincubator.lngedu.session.SessionCommands.StartSessionCommand;
import dev.homeincubator.lngedu.session.SessionService;
import dev.homeincubator.lngedu.stats.DailyStatsView;
import dev.homeincubator.lngedu.stats.StatsService;
import dev.homeincubator.lngedu.user.LearnerSummary;
import dev.homeincubator.lngedu.user.ProfileService;
import dev.homeincubator.lngedu.vocabulary.VocabularyCommands.RecordUnknownWordCommand;
import dev.homeincubator.lngedu.vocabulary.VocabularyCommands.VocabularyItemView;
import dev.homeincubator.lngedu.vocabulary.VocabularyService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * MCP adapter (@tag:mcp-tools): the thin transport layer that exposes the vertical learning
 * scenario (@tag:vertical-slice) as small, typed, LLM-friendly MCP tools. It is a second adapter
 * over the SAME application/service layer as REST — it holds no business logic and never leaks JPA
 * entities; every method delegates to a shared service and returns its transport-agnostic result
 * record.
 *
 * <p>Mutating tools ({@code start_learning_session}, {@code record_chunk_result},
 * {@code record_unknown_word}, {@code finish_learning_session}) take a {@code request_id} for safe
 * retry and are idempotent purely via the existing service idempotency (@tag:request-id) — no
 * second mechanism is added here. {@code finish_learning_session} is idempotent by nature (it stamps
 * {@code finished_at} only once), so its {@code request_id} exists for interface consistency and safe
 * replay by the LLM client.
 *
 * <p>Ownership (@tag:auth): the account is taken from the validated bearer token via
 * {@link CurrentAccount}, never from a tool parameter. {@code list_learners} returns only that
 * account's learners, and every tool that names a learner asserts ownership
 * ({@link AccountService#assertOwnsLearner}) before delegating (closes AGENTS rule 5).
 */
@Component
public class LngEduMcpTools {

    private final ProfileService profileService;
    private final BookService bookService;
    private final SessionService sessionService;
    private final ReadingService readingService;
    private final VocabularyService vocabularyService;
    private final StatsService statsService;
    private final AccountService accountService;
    private final CurrentAccount currentAccount;

    public LngEduMcpTools(ProfileService profileService,
                          BookService bookService,
                          SessionService sessionService,
                          ReadingService readingService,
                          VocabularyService vocabularyService,
                          StatsService statsService,
                          AccountService accountService,
                          CurrentAccount currentAccount) {
        this.profileService = profileService;
        this.bookService = bookService;
        this.sessionService = sessionService;
        this.readingService = readingService;
        this.vocabularyService = vocabularyService;
        this.statsService = statsService;
        this.accountService = accountService;
        this.currentAccount = currentAccount;
    }

    @Tool(name = "list_learners",
            description = "List your learner profiles with their per-language reading skills. Use "
                    + "this first to pick a learner id; do not invent ids. Only profiles owned by "
                    + "your account are returned.")
    public List<LearnerSummary> listLearners() {
        return profileService.listLearnersOwnedBy(currentAccount.accountId());
    }

    @Tool(name = "list_books",
            description = "List books, optionally filtered by learning language (BCP 47: 'sr' or "
                    + "'en'). When a learner id is supplied, each book also carries that learner's "
                    + "reading progress.")
    public List<BookSummary> listBooks(
            @ToolParam(required = false,
                    description = "Learning language filter, BCP 47 ('sr' or 'en'); omit to list all")
            String language,
            @ToolParam(required = false,
                    description = "Learner id (UUID); when set, each book includes this learner's progress")
            UUID learnerId) {
        if (learnerId != null) {
            accountService.assertOwnsLearner(currentAccount.accountId(), learnerId);
        }
        return bookService.listBooks(language, learnerId);
    }

    @Tool(name = "start_learning_session",
            description = "Start a learning session for a learner and book, returning the session "
                    + "id. Idempotent by request_id: repeating the same request_id returns the same "
                    + "session without creating a duplicate.")
    public SessionView startLearningSession(
            @ToolParam(description = "Learner id (UUID)") UUID learnerId,
            @ToolParam(description = "Book id (UUID)") UUID bookId,
            @ToolParam(description = "Client-generated idempotency key; reuse it to safely retry")
            String requestId) {
        accountService.assertOwnsLearner(currentAccount.accountId(), learnerId);
        return sessionService.startLearningSession(new StartSessionCommand(learnerId, bookId, requestId));
    }

    @Tool(name = "get_next_chunk",
            description = "Get the next reading block for a learner and book, assembled dynamically "
                    + "from the current reading position. This is a read-only query and does NOT "
                    + "advance progress; call record_chunk_result to advance.")
    public NextChunkView getNextChunk(
            @ToolParam(description = "Learner id (UUID)") UUID learnerId,
            @ToolParam(description = "Book id (UUID)") UUID bookId) {
        accountService.assertOwnsLearner(currentAccount.accountId(), learnerId);
        return readingService.getNextChunk(new GetNextChunkCommand(learnerId, bookId));
    }

    @Tool(name = "record_chunk_result",
            description = "Record the learner's comprehension of the delivered reading block and "
                    + "advance reading progress to endOffset. comprehension must be one of "
                    + "'understood', 'partial', 'unclear'. Idempotent by request_id: a replay does "
                    + "not double-advance progress.")
    public ChunkResultView recordChunkResult(
            @ToolParam(description = "Learner id (UUID)") UUID learnerId,
            @ToolParam(description = "Book id (UUID)") UUID bookId,
            @ToolParam(description = "Active session id (UUID)") UUID sessionId,
            @ToolParam(description = "Absolute character offset of the end of the delivered block "
                    + "(use endOffset from get_next_chunk)") int endOffset,
            @ToolParam(description = "Comprehension outcome: 'understood', 'partial' or 'unclear'")
            String comprehension,
            @ToolParam(description = "Client-generated idempotency key; reuse it to safely retry")
            String requestId) {
        accountService.assertOwnsLearner(currentAccount.accountId(), learnerId);
        Comprehension result = parseComprehension(comprehension);
        return readingService.recordChunkResult(new RecordChunkResultCommand(
                learnerId, bookId, sessionId, endOffset, result, requestId));
    }

    @Tool(name = "record_unknown_word",
            description = "Record a word the learner did not know, with its source context, into "
                    + "their vocabulary for the given language (BCP 47: 'sr' or 'en'). Idempotent "
                    + "by request_id: a replay yields exactly one word event.")
    public VocabularyItemView recordUnknownWord(
            @ToolParam(description = "Learner id (UUID)") UUID learnerId,
            @ToolParam(description = "Word language, BCP 47 ('sr' or 'en')") String language,
            @ToolParam(description = "The word (lemma / dictionary form)") String lemma,
            @ToolParam(required = false,
                    description = "Sentence or phrase the word appeared in") String context,
            @ToolParam(required = false,
                    description = "Session id (UUID) this happened in, if any") UUID sessionId,
            @ToolParam(description = "Client-generated idempotency key; reuse it to safely retry")
            String requestId) {
        accountService.assertOwnsLearner(currentAccount.accountId(), learnerId);
        return vocabularyService.recordUnknownWord(new RecordUnknownWordCommand(
                learnerId, language, lemma, context, sessionId, requestId));
    }

    @Tool(name = "finish_learning_session",
            description = "Finish an active learning session, stamping its finish time. Idempotent: "
                    + "finishing an already-finished session returns it unchanged. request_id is "
                    + "accepted for safe retry.")
    public SessionView finishLearningSession(
            @ToolParam(description = "Session id (UUID) to finish") UUID sessionId,
            @ToolParam(required = false,
                    description = "Client-generated idempotency key; reuse it to safely retry")
            String requestId) {
        // Resolve the session's learner (404 if unknown) and assert ownership before finishing, so a
        // caller can only finish sessions of learners its account owns (@tag:auth).
        UUID learnerId = sessionService.getSessionLearner(sessionId);
        accountService.assertOwnsLearner(currentAccount.accountId(), learnerId);
        // finish is idempotent by nature (stamps finished_at once); request_id needs no extra
        // mechanism, so it is not forwarded to the service.
        return sessionService.finishLearningSession(new FinishSessionCommand(sessionId));
    }

    @Tool(name = "get_daily_stats",
            description = "Get the learner's stats for 'today' in their own timezone: minutes "
                    + "studied, session count, new words, characters and blocks read.")
    public DailyStatsView getDailyStats(
            @ToolParam(description = "Learner id (UUID)") UUID learnerId) {
        accountService.assertOwnsLearner(currentAccount.accountId(), learnerId);
        return statsService.getDailyStats(learnerId);
    }

    private static Comprehension parseComprehension(String wire) {
        try {
            return Comprehension.fromWire(wire);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException(
                    "comprehension must be one of understood/partial/unclear, got: " + wire);
        }
    }
}
