package org.test.mpashka.telegram;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TelegramTest {
    private HttpClient httpClient = HttpClient.newBuilder()
//            .proxy(ProxySelector.getDefault())
//            .followRedirects(HttpClient.Redirect.ALWAYS)
            .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
            .build();

    @Test
    public void test() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/CustomerServicePort"))
//                .header("key1", "value1")
//                .timeout(Duration.of(10, SECONDS))
                .POST(HttpRequest.BodyPublishers.ofString("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><ns2:getCustomersByName xmlns:ns2=\"http://customerservice.example.com/\"><name>Smith</name></ns2:getCustomersByName></soap:Body></soap:Envelope>"))
                .build();

//        ((CookieManager) httpClient.cookieHandler().get()).getCookieStore()

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        log.info("Response: {}", body);
    }
}
