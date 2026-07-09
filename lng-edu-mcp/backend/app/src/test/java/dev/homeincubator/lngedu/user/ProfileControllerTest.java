package dev.homeincubator.lngedu.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web slice test: profiles list returns 200 and the expected JSON shape. No DB / Docker.
 * Security filters are disabled ({@code addFilters = false}) — Phase H keeps this surface open and
 * this slice only exercises controller mapping/serialization.
 */
@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    @Test
    void listsProfilesAsJson() throws Exception {
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(profileService.listLearners()).thenReturn(List.of(new LearnerSummary(
                id, "Ana (dev)", "Europe/Belgrade",
                List.of(new LearnerSummary.LanguageSkillSummary("sr", "A2", 30, 60)))));

        mockMvc.perform(get("/api/profiles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].displayName").value("Ana (dev)"))
                .andExpect(jsonPath("$[0].timezone").value("Europe/Belgrade"))
                .andExpect(jsonPath("$[0].skills[0].language").value("sr"))
                .andExpect(jsonPath("$[0].skills[0].level").value("A2"))
                .andExpect(jsonPath("$[0].skills[0].blockMinWords").value(30))
                .andExpect(jsonPath("$[0].skills[0].blockMaxWords").value(60));
    }
}
