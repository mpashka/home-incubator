package dev.homeincubator.lngedu.stats;

import dev.homeincubator.lngedu.common.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web slice test: a domain NotFoundException maps to a Problem Details 404.
 * Security filters disabled ({@code addFilters = false}); Phase H keeps this surface open.
 */
@WebMvcTest(StatsController.class)
@AutoConfigureMockMvc(addFilters = false)
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatsService statsService;

    @Test
    void unknownUserReturnsProblemDetails404() throws Exception {
        UUID userId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(statsService.getDailyStats(any())).thenThrow(NotFoundException.of("user", userId));

        mockMvc.perform(get("/api/stats/daily").param("userId", userId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.type").value("urn:problem-type:not-found"))
                .andExpect(jsonPath("$.detail").value("user not found: " + userId))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
