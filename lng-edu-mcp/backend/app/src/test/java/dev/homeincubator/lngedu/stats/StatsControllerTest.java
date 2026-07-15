package dev.homeincubator.lngedu.stats;

import dev.homeincubator.lngedu.account.AccountService;
import dev.homeincubator.lngedu.common.NotFoundException;
import dev.homeincubator.lngedu.security.CurrentAccount;
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
 * Web slice test: a domain NotFoundException maps to a Problem Details 404. The ownership guard is
 * stubbed to pass (mocked {@link CurrentAccount} / {@link AccountService}) so the request reaches the
 * service. Security filters disabled ({@code addFilters = false}); full auth is covered elsewhere.
 */
@WebMvcTest(StatsController.class)
@AutoConfigureMockMvc(addFilters = false)
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatsService statsService;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private CurrentAccount currentAccount;

    @Test
    void unknownUserReturnsProblemDetails404() throws Exception {
        UUID userId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(currentAccount.accountId()).thenReturn(UUID.randomUUID());
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
