// @tag:vertical-slice
package dev.homeincubator.lngedu.stats;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Thin REST adapter for daily stats. Delegates to {@link StatsService} (computed in the learner's tz). */
@RestController
@RequestMapping("/api/stats")
@Tag(name = "Stats", description = "Daily learning statistics computed in the learner's timezone")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/daily")
    @Operation(summary = "Get today's learning stats for a learner (in the learner's timezone)")
    public DailyStatsView daily(@RequestParam UUID userId) {
        return statsService.getDailyStats(userId);
    }
}
