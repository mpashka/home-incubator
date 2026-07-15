// @tag:auth
package dev.homeincubator.lngedu.auth;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * 401 entry point that advertises the Protected Resource Metadata location (RFC 9728,
 * {@code @tag:auth}). Delegates to {@link BearerTokenAuthenticationEntryPoint} (which sets the 401
 * status and the standard {@code WWW-Authenticate: Bearer ...} challenge, including error details on
 * an invalid/expired token) and then appends a {@code resource_metadata} parameter pointing at
 * {@code <resource>/.well-known/oauth-protected-resource} so an MCP client (e.g. ChatGPT) can
 * bootstrap its OAuth flow from a bare 401.
 */
public class ResourceMetadataAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final AuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();
    private final String resourceMetadataUrl;

    public ResourceMetadataAuthenticationEntryPoint(String resource) {
        this.resourceMetadataUrl = resource + "/.well-known/oauth-protected-resource";
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        delegate.commence(request, response, authException);
        String existing = response.getHeader(HttpHeaders.WWW_AUTHENTICATE);
        String base = (existing == null || existing.isBlank()) ? "Bearer" : existing;
        // A bare "Bearer" challenge takes a space before the first auth-param; a challenge that
        // already carries params (error="invalid_token", ...) takes a comma separator.
        String separator = base.contains("=") ? ", " : " ";
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE,
                base + separator + "resource_metadata=\"" + resourceMetadataUrl + "\"");
    }
}
