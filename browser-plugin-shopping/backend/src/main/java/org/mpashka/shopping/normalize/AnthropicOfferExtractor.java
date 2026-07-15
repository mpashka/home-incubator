package org.mpashka.shopping.normalize;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Anthropic provider — Claude with schema-enforced structured output. Optional: only used
 * when `shopping.llm.provider=anthropic`. The client is created lazily so the app boots
 * without ANTHROPIC_API_KEY when Yandex is the default.
 */
@ApplicationScoped
public class AnthropicOfferExtractor implements OfferExtractor {

    private static final Logger LOG = Logger.getLogger(AnthropicOfferExtractor.class);

    @ConfigProperty(name = "shopping.llm.anthropic.model", defaultValue = "claude-opus-4-8")
    String model;

    @ConfigProperty(name = "shopping.llm.anthropic.max-tokens", defaultValue = "16000")
    long maxTokens;

    private volatile AnthropicClient client;

    @Override
    public String provider() {
        return "anthropic";
    }

    private AnthropicClient client() {
        AnthropicClient c = client;
        if (c == null) {
            synchronized (this) {
                if (client == null) client = AnthropicOkHttpClient.fromEnv();
                c = client;
            }
        }
        return c;
    }

    @Override
    public List<NormalizedOffer> extract(NormalizeInput input) {
        StructuredMessageCreateParams<NormalizedCapture> params = MessageCreateParams.builder()
                .model(model)
                .maxTokens(maxTokens)
                .system(Prompts.SYSTEM)
                .addUserMessage(Prompts.userMessage(input))
                .outputConfig(NormalizedCapture.class)
                .build();

        NormalizedCapture result = client().messages().create(params).content().stream()
                .flatMap(cb -> cb.text().stream())
                .map(t -> t.text())
                .findFirst()
                .orElse(new NormalizedCapture(List.of()));

        List<NormalizedOffer> offers = result.offers() == null ? List.of() : result.offers();
        LOG.infof("[anthropic] capture %s → %d offers", input.id(), offers.size());
        return offers;
    }
}
