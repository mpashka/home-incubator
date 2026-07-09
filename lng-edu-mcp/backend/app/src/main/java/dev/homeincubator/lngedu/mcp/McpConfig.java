// @tag:mcp-tools
package dev.homeincubator.lngedu.mcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wiring for the MCP transport (@tag:mcp-tools): registers the {@code @Tool}-annotated methods of
 * {@link LngEduMcpTools} with the Spring AI MCP server via a {@link MethodToolCallbackProvider}.
 */
@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider lngEduToolCallbackProvider(LngEduMcpTools tools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(tools)
                .build();
    }
}
