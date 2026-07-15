// @tag:auth
package dev.homeincubator.lngedu.web;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Protected Resource Metadata endpoint (RFC 9728, Phase I, {@code @tag:auth}). Served openly at
 * {@code GET /.well-known/oauth-protected-resource} so an MCP client (e.g. ChatGPT) can discover the
 * resource identifier and its Authorization Server and bootstrap the OAuth flow. The 401 challenge
 * from the protected chain points here via {@code WWW-Authenticate: Bearer resource_metadata="..."}.
 */
@RestController
@Tag(name = "OAuth", description = "OAuth2 Protected Resource Metadata (RFC 9728)")
public class ProtectedResourceMetadataController {

    private final Map<String, Object> metadata;

    public ProtectedResourceMetadataController(
            @Value("${MCP_RESOURCE_URI:http://localhost:8080}") String resource,
            @Value("${AUTH_ISSUER_URI:http://localhost:8080}") String issuer) {
        this.metadata = Map.of(
                "resource", resource,
                "authorization_servers", List.of(issuer),
                "scopes_supported", List.of("openid", "email", "mcp"),
                "bearer_methods_supported", List.of("header"));
    }

    @GetMapping(value = "/.well-known/oauth-protected-resource", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Protected Resource Metadata: resource id and its Authorization Server(s)")
    public Map<String, Object> protectedResourceMetadata() {
        return metadata;
    }
}
