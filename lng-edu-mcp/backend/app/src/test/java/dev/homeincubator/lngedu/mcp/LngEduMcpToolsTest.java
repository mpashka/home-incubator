package dev.homeincubator.lngedu.mcp;

import dev.homeincubator.lngedu.account.AccountService;
import dev.homeincubator.lngedu.book.BookService;
import dev.homeincubator.lngedu.reading.ReadingService;
import dev.homeincubator.lngedu.security.CurrentAccount;
import dev.homeincubator.lngedu.session.SessionCommands.SessionView;
import dev.homeincubator.lngedu.session.SessionCommands.StartSessionCommand;
import dev.homeincubator.lngedu.session.SessionService;
import dev.homeincubator.lngedu.stats.StatsService;
import dev.homeincubator.lngedu.user.LearnerSummary;
import dev.homeincubator.lngedu.user.ProfileService;
import dev.homeincubator.lngedu.vocabulary.VocabularyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * MCP adapter (@tag:mcp-tools) unit test: instantiates the tool class with mocked services (no
 * Spring/MCP context, no DB) and asserts a couple of tools delegate to the shared service layer.
 */
@ExtendWith(MockitoExtension.class)
class LngEduMcpToolsTest {

    @Mock private ProfileService profileService;
    @Mock private BookService bookService;
    @Mock private SessionService sessionService;
    @Mock private ReadingService readingService;
    @Mock private VocabularyService vocabularyService;
    @Mock private StatsService statsService;
    @Mock private AccountService accountService;
    @Mock private CurrentAccount currentAccount;

    private LngEduMcpTools tools() {
        return new LngEduMcpTools(profileService, bookService, sessionService,
                readingService, vocabularyService, statsService, accountService, currentAccount);
    }

    @Test
    void listLearners_delegatesToAccountScopedListing() {
        UUID accountId = UUID.randomUUID();
        UUID learnerId = UUID.randomUUID();
        LearnerSummary learner = new LearnerSummary(
                learnerId, "Ana", "Europe/Belgrade",
                List.of(new LearnerSummary.LanguageSkillSummary("sr", "A2", 40, 80)));
        when(currentAccount.accountId()).thenReturn(accountId);
        when(profileService.listLearnersOwnedBy(accountId)).thenReturn(List.of(learner));

        List<LearnerSummary> result = tools().listLearners();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(learnerId);
        assertThat(result.get(0).displayName()).isEqualTo("Ana");
        assertThat(result.get(0).skills()).singleElement()
                .satisfies(s -> assertThat(s.language()).isEqualTo("sr"));
    }

    @Test
    void startLearningSession_forwardsRequestIdToService() {
        UUID learnerId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String requestId = "req-abc";

        SessionView view = new SessionView(sessionId, learnerId, bookId,
                Instant.parse("2026-07-08T10:00:00Z"), null, true);
        when(sessionService.startLearningSession(org.mockito.ArgumentMatchers.any()))
                .thenReturn(view);

        SessionView result = tools().startLearningSession(learnerId, bookId, requestId);

        ArgumentCaptor<StartSessionCommand> captor = ArgumentCaptor.forClass(StartSessionCommand.class);
        org.mockito.Mockito.verify(sessionService).startLearningSession(captor.capture());
        assertThat(captor.getValue().requestId()).isEqualTo(requestId);
        assertThat(captor.getValue().userId()).isEqualTo(learnerId);
        assertThat(captor.getValue().bookId()).isEqualTo(bookId);
        assertThat(result.id()).isEqualTo(sessionId);
    }
}
