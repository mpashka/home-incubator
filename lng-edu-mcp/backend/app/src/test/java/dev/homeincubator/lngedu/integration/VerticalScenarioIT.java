// @tag:vertical-slice @tag:request-id
package dev.homeincubator.lngedu.integration;

import dev.homeincubator.lngedu.book.BookService;
import dev.homeincubator.lngedu.book.BookSummary;
import dev.homeincubator.lngedu.reading.ReadingCommands.ChunkResultView;
import dev.homeincubator.lngedu.reading.ReadingCommands.Comprehension;
import dev.homeincubator.lngedu.reading.ReadingCommands.GetNextChunkCommand;
import dev.homeincubator.lngedu.reading.ReadingCommands.NextChunkView;
import dev.homeincubator.lngedu.reading.ReadingCommands.RecordChunkResultCommand;
import dev.homeincubator.lngedu.reading.ReadingEventRepository;
import dev.homeincubator.lngedu.reading.ReadingService;
import dev.homeincubator.lngedu.session.LearningSessionRepository;
import dev.homeincubator.lngedu.session.SessionCommands.FinishSessionCommand;
import dev.homeincubator.lngedu.session.SessionCommands.SessionView;
import dev.homeincubator.lngedu.session.SessionCommands.StartSessionCommand;
import dev.homeincubator.lngedu.session.SessionService;
import dev.homeincubator.lngedu.stats.DailyStatsView;
import dev.homeincubator.lngedu.stats.StatsService;
import dev.homeincubator.lngedu.support.EnabledIfDockerAvailable;
import dev.homeincubator.lngedu.user.LearnerSummary;
import dev.homeincubator.lngedu.user.ProfileService;
import dev.homeincubator.lngedu.user.UserRepository;
import dev.homeincubator.lngedu.vocabulary.VocabularyCommands.RecordUnknownWordCommand;
import dev.homeincubator.lngedu.vocabulary.VocabularyCommands.VocabularyItemView;
import dev.homeincubator.lngedu.vocabulary.VocabularyService;
import dev.homeincubator.lngedu.vocabulary.WordEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end vertical scenario (@tag:vertical-slice) against a real Spring context on a fresh
 * PostgreSQL (Testcontainers, postgres:16). Booting under the {@code dev} profile runs the Flyway
 * migrations on a clean database and loads {@link dev.homeincubator.lngedu.common.DevDataSeeder},
 * so this test validates migrations + seed as well as the services.
 *
 * <p>It drives the whole scenario through the shared application/service layer:
 * start_learning_session → get_next_chunk → record_chunk_result → record_unknown_word →
 * finish_learning_session → get_daily_stats, then asserts idempotency (@tag:request-id): replaying
 * each mutating call with the SAME {@code request_id} does not change the result or create duplicate
 * rows.
 *
 * <p>Docker gating: the class is annotated {@link EnabledIfDockerAvailable}, so when no Docker
 * daemon is reachable (as in the build environment here) it is SKIPPED before any container starts,
 * and {@code :backend:check} stays green. The container is managed manually (not via
 * {@code @Testcontainers}) and started from {@link #datasourceProperties}, which only runs once the
 * class has passed the Docker condition.
 */
@SpringBootTest
@ActiveProfiles("dev")
@EnabledIfDockerAvailable
class VerticalScenarioIT {

    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        // Reached only after the Docker condition enabled the class -> safe to touch Docker here.
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private ProfileService profileService;
    @Autowired
    private BookService bookService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private ReadingService readingService;
    @Autowired
    private VocabularyService vocabularyService;
    @Autowired
    private StatsService statsService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LearningSessionRepository sessionRepository;
    @Autowired
    private ReadingEventRepository readingEventRepository;
    @Autowired
    private WordEventRepository wordEventRepository;

    @Test
    void runsVerticalScenarioAndIsIdempotentOnReplay() {
        // --- migrations + seed loaded on the fresh DB ---
        assertThat(userRepository.count()).isEqualTo(2);

        List<LearnerSummary> learners = profileService.listLearners();
        assertThat(learners).hasSize(2);
        LearnerSummary learner = learners.stream()
                .filter(l -> l.skills().stream().anyMatch(s -> s.language().equals("en")))
                .findFirst()
                .orElseThrow();
        UUID userId = learner.id();

        List<BookSummary> enBooks = bookService.listBooks("en", userId);
        assertThat(enBooks).isNotEmpty();
        BookSummary book = enBooks.get(0);
        UUID bookId = book.id();
        // Progress starts at zero on a clean DB.
        assertThat(book.progress().positionChar()).isZero();

        // --- start_learning_session (idempotent by request_id) ---
        String startReq = "it-start-" + UUID.randomUUID();
        SessionView session = sessionService.startLearningSession(
                new StartSessionCommand(userId, bookId, startReq));
        assertThat(session.active()).isTrue();
        UUID sessionId = session.id();

        SessionView startReplay = sessionService.startLearningSession(
                new StartSessionCommand(userId, bookId, startReq));
        assertThat(startReplay.id()).isEqualTo(sessionId);
        assertThat(sessionRepository.count()).isEqualTo(1);

        // --- get_next_chunk: read-only, does not advance progress ---
        NextChunkView chunk = readingService.getNextChunk(new GetNextChunkCommand(userId, bookId));
        assertThat(chunk.startOffset()).isZero();
        assertThat(chunk.endOffset()).isPositive();
        assertThat(chunk.text()).isNotBlank();
        assertThat(chunk.endOfBook()).isFalse();
        int endOffset = chunk.endOffset();

        // getNextChunk must not have moved progress.
        NextChunkView chunkAgain = readingService.getNextChunk(new GetNextChunkCommand(userId, bookId));
        assertThat(chunkAgain.startOffset()).isZero();

        // --- record_chunk_result: advances progress to endOffset ---
        String chunkReq = "it-chunk-" + UUID.randomUUID();
        ChunkResultView result = readingService.recordChunkResult(new RecordChunkResultCommand(
                userId, bookId, sessionId, endOffset, Comprehension.UNDERSTOOD, chunkReq));
        assertThat(result.positionChar()).isEqualTo(endOffset);
        assertThat(readingEventRepository.count()).isEqualTo(1);

        // Replay with the SAME request_id: no double-advance, no extra reading_event.
        ChunkResultView resultReplay = readingService.recordChunkResult(new RecordChunkResultCommand(
                userId, bookId, sessionId, endOffset, Comprehension.UNDERSTOOD, chunkReq));
        assertThat(resultReplay.positionChar()).isEqualTo(endOffset);
        assertThat(readingEventRepository.count()).isEqualTo(1);
        assertThat(readingEventRepository.findByRequestId(chunkReq)).isPresent();

        // Progress now visible on the book listing.
        BookSummary afterRead = bookService.listBooks("en", userId).get(0);
        assertThat(afterRead.progress().positionChar()).isEqualTo(endOffset);

        // --- record_unknown_word (idempotent by request_id) ---
        String wordReq = "it-word-" + UUID.randomUUID();
        VocabularyItemView word = vocabularyService.recordUnknownWord(new RecordUnknownWordCommand(
                userId, "en", "Fortune", "in possession of a good fortune", sessionId, wordReq));
        assertThat(word.lemma()).isEqualTo("fortune"); // normalized lower-case
        assertThat(wordEventRepository.count()).isEqualTo(1);

        VocabularyItemView wordReplay = vocabularyService.recordUnknownWord(new RecordUnknownWordCommand(
                userId, "en", "Fortune", "in possession of a good fortune", sessionId, wordReq));
        assertThat(wordReplay.id()).isEqualTo(word.id());
        assertThat(wordEventRepository.count()).isEqualTo(1);

        // --- finish_learning_session (idempotent: stamps finishedAt once) ---
        SessionView finished = sessionService.finishLearningSession(new FinishSessionCommand(sessionId));
        assertThat(finished.active()).isFalse();
        assertThat(finished.finishedAt()).isNotNull();

        SessionView finishReplay = sessionService.finishLearningSession(new FinishSessionCommand(sessionId));
        assertThat(finishReplay.finishedAt()).isEqualTo(finished.finishedAt());

        // --- get_daily_stats reflects the scenario ---
        DailyStatsView stats = statsService.getDailyStats(userId);
        assertThat(stats.sessions()).isEqualTo(1);
        assertThat(stats.newWords()).isEqualTo(1);
        assertThat(stats.blocksRead()).isEqualTo(1);
        assertThat(stats.charsRead()).isEqualTo(endOffset);
    }
}
