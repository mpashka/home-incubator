// @tag:auth
package dev.homeincubator.lngedu.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web slice test for the RFC 9728 Protected Resource Metadata document ({@code @tag:auth}). It is
 * served openly and exposes the resource id and its Authorization Server(s).
 */
@WebMvcTest(ProtectedResourceMetadataController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "MCP_RESOURCE_URI=http://localhost:8097",
        "AUTH_ISSUER_URI=http://localhost:8097"
})
class ProtectedResourceMetadataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void servesProtectedResourceMetadata() throws Exception {
        mockMvc.perform(get("/.well-known/oauth-protected-resource"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resource").value("http://localhost:8097"))
                .andExpect(jsonPath("$.authorization_servers[0]").value("http://localhost:8097"))
                .andExpect(jsonPath("$.scopes_supported").isArray())
                .andExpect(jsonPath("$.bearer_methods_supported[0]").value("header"));
    }
}
