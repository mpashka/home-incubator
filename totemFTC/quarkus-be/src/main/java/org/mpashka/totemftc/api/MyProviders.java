package org.mpashka.totemftc.api;

import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

public class MyProviders {
    @Produces
    @Singleton
    public WebClient webClient(Vertx vertx) {
        WebClientOptions options = new WebClientOptions()
                //.setUserAgent("My-App/1.2.3")
                //.setKeepAlive(false)
                .setConnectTimeout(2000)
                ;
        return WebClient.create(vertx, options);
    }

}
