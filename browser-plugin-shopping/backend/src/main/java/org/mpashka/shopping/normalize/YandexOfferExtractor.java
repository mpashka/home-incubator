package org.mpashka.shopping.normalize;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Yandex AI Studio provider (default). Uses the OpenAI-compatible Chat Completions endpoint
 * with `response_format: json_object` — per prior testing, JSON-schema mode is unstable on
 * Yandex, so we ask for a JSON object and parse it ourselves. Models are addressed as
 * `gpt://<folder_id>/<model>`.
 */
@ApplicationScoped
public class YandexOfferExtractor implements OfferExtractor {

    private static final Logger LOG = Logger.getLogger(YandexOfferExtractor.class);

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @Inject
    ObjectMapper mapper;

    @ConfigProperty(name = "shopping.llm.yandex.base-url", defaultValue = "https://ai.api.cloud.yandex.net/v1")
    String baseUrl;

    @ConfigProperty(name = "shopping.llm.yandex.model", defaultValue = "qwen3.6-35b-a3b/latest")
    String model;

    @ConfigProperty(name = "shopping.llm.yandex.folder-id", defaultValue = "")
    String folderId;

    @ConfigProperty(name = "shopping.llm.yandex.token", defaultValue = "")
    String token;

    @ConfigProperty(name = "shopping.llm.yandex.max-tokens", defaultValue = "16000")
    int maxTokens;

    @ConfigProperty(name = "shopping.llm.yandex.temperature", defaultValue = "0.1")
    double temperature;

    @Override
    public String provider() {
        return "yandex";
    }

    /** Turn a short model name into a full gpt://<folder>/<model> URI when a folder is set. */
    private String modelUri() {
        if (model.startsWith("gpt://") || folderId.isBlank()) return model;
        return "gpt://" + folderId + "/" + model;
    }

    @Override
    public List<NormalizedOffer> extract(NormalizeInput input) {
        ObjectNode body = mapper.createObjectNode();
        body.put("model", modelUri());
        body.put("temperature", temperature);
        body.put("max_tokens", maxTokens);
        body.putObject("response_format").put("type", "json_object");
        ArrayNode messages = body.putArray("messages");
        messages.addObject().put("role", "system").put("content", Prompts.SYSTEM + "\n\n" + Prompts.JSON_SHAPE);
        messages.addObject().put("role", "user").put("content", Prompts.userMessage(input));

        String content = call(body);
        NormalizedCapture parsed = parse(content);
        List<NormalizedOffer> offers = parsed.offers() == null ? List.of() : parsed.offers();
        LOG.infof("[yandex] capture %s → %d offers", input.id(), offers.size());
        return offers;
    }

    private String call(ObjectNode body) {
        if (token.isBlank()) {
            throw new IllegalStateException("YANDEX_AI_TOKEN / shopping.llm.yandex.token is not set");
        }
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + "/chat/completions"))
                    .timeout(Duration.ofMinutes(3))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();
            HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() / 100 != 2) {
                throw new RuntimeException("Yandex API HTTP " + res.statusCode() + ": " + res.body());
            }
            JsonNode root = mapper.readTree(res.body());
            return root.path("choices").path(0).path("message").path("content").asText("");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Yandex API call failed", e);
        }
    }

    /** Parse the model's JSON content, tolerating stray markdown fences. */
    private NormalizedCapture parse(String content) {
        String json = content.strip();
        if (json.startsWith("```")) {
            int nl = json.indexOf('\n');
            json = nl >= 0 ? json.substring(nl + 1) : json;
            if (json.endsWith("```")) json = json.substring(0, json.length() - 3);
            json = json.strip();
        }
        try {
            return mapper.readValue(json, NormalizedCapture.class);
        } catch (Exception e) {
            LOG.warnf("could not parse Yandex JSON (%s): %s", e.getMessage(),
                    json.substring(0, Math.min(json.length(), 200)));
            return new NormalizedCapture(List.of());
        }
    }
}
