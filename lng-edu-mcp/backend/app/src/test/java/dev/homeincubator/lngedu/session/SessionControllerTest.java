package dev.homeincubator.lngedu.session;

import dev.homeincubator.lngedu.account.AccountService;
import dev.homeincubator.lngedu.security.CurrentAccount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web slice test: a validation failure is rendered as an RFC 7807 Problem Details 400. Body
 * validation runs before the controller body (and thus before the ownership guard), so the mocked
 * account collaborators are unused here. Security filters disabled ({@code addFilters = false}).
 */
@WebMvcTest(SessionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SessionService sessionService;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private CurrentAccount currentAccount;

    @Test
    void missingBodyFieldsReturnProblemDetails400() throws Exception {
        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.type").value("urn:problem-type:validation-error"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
