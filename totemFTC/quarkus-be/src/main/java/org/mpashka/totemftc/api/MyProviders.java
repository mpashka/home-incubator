package org.mpashka.totemftc.api;

import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

public class MyProviders {
    @Produces
    @Singleton
    public WebClient webClient(Vertx vertx) {
        return WebClient.create(vertx);
    }

}
