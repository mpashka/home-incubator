// @tag:vertical-slice @tag:auth
package dev.homeincubator.lngedu.stats;

import dev.homeincubator.lngedu.account.AccountService;
import dev.homeincubator.lngedu.security.CurrentAccount;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Thin REST adapter for daily stats. Delegates to {@link StatsService} (computed in the learner's tz);
 * verifies that the authenticated account owns the learner before delegating (@tag:auth).
 */
@RestController
@RequestMapping("/api/stats")
@Tag(name = "Stats", description = "Daily learning statistics computed in the learner's timezone")
public class StatsController {

    private final StatsService statsService;
    private final AccountService accountService;
    private final CurrentAccount currentAccount;

    public StatsController(StatsService statsService, AccountService accountService,
                           CurrentAccount currentAccount) {
        this.statsService = statsService;
        this.accountService = accountService;
        this.currentAccount = currentAccount;
    }

    @GetMapping("/daily")
    @Operation(summary = "Get today's learning stats for a learner (in the learner's timezone)")
    public DailyStatsView daily(@RequestParam UUID userId) {
        accountService.assertOwnsLearner(currentAccount.accountId(), userId);
        return statsService.getDailyStats(userId);
    }
}
