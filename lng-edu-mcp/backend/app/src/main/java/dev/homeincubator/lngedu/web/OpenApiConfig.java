package dev.homeincubator.lngedu.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Minimal OpenAPI metadata for the REST adapter. springdoc serves the spec at
 * {@code /v3/api-docs} and Swagger UI at {@code /swagger-ui.html} out of the box; this only
 * supplies title/version/description.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lngEduOpenApi() {
        return new OpenAPI().info(new Info()
                .title("lng-edu-mcp REST API")
                .version("0.1.0")
                .description("REST adapter for the language-learning MVP vertical slice: "
                        + "profiles, books, learning sessions, reading, vocabulary and daily stats. "
                        + "A thin transport over the shared application layer (also exposed via MCP). "
                        + "Errors use RFC 7807 Problem Details; timestamps are UTC ISO-8601."));
    }
}
