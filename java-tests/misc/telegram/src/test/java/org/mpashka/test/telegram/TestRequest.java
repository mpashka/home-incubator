package org.mpashka.test.telegram;

import java.io.InputStream;
import java.io.Serializable;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.GetUpdates;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestRequest {
    private HttpClient httpClient = HttpClient.newBuilder()
//            .proxy(ProxySelector.getDefault())
//            .followRedirects(HttpClient.Redirect.ALWAYS)
            .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
            .build();
    private ObjectMapper objectMapper = new ObjectMapper();
    private String token = token();

    @Test
    public void testUpdates() throws Exception {
        request(GetUpdates.builder()
                .offset(0)
                .timeout(100)
                .build());

//        request()
    }

    private <T extends Serializable> T request(BotApiMethod<T> botApiMethod) throws Exception {
        botApiMethod.validate();

        String requestBody = objectMapper.writeValueAsString(botApiMethod);
        log.info("Sending body: {}", requestBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.telegram.org/bot" + token + "/" + botApiMethod.getMethod()))
                .header("content-type", "application/json")
//                .timeout(Duration.of(10, SECONDS))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

//        ((CookieManager) httpClient.cookieHandler().get()).getCookieStore()


        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        response.headers().map().forEach((k, v) -> log.info("    Header '{}':{}", k, v));
        String responseBody = response.body();
        T result = botApiMethod.deserializeResponse(responseBody);

        log.info("Response: {}", responseBody);
        log.info("Response Object: {}", result);
        return result;
    }

    private String token() {
        Properties properties = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/telegram.properties")) {
            properties.load(in);
            return Objects.requireNonNull(properties.getProperty("geps-bot.token"), "'geps-bot.token' is not null");
        } catch (Exception e) {
            throw new RuntimeException("Internal error get token. Make sure telegram.properties has geps-bot.token", e);
        }
    }
}
