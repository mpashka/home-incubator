package org.mpashka.totemftc.api;

import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.pgclient.PgPool;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class DBInit {

    private static final Logger log = LoggerFactory.getLogger(DBInit.class);

    private final PgPool client;
    private final boolean schemaCreate;

    public DBInit(PgPool client, @ConfigProperty(name = "myapp.schema.create", defaultValue = "true") boolean schemaCreate) {
        log.debug("DBInit.new");
        this.client = client;
        this.schemaCreate = schemaCreate;
    }

    void onStart(@Observes StartupEvent ev) {
        if (schemaCreate) {
            initdb();
        }
    }

    private void initdb() {
        log.debug("Init database...");
        try {
            LocationEntity.init(client);
        } catch (Exception e) {
            log.error("Db init error", e);
        }
    }

    private Uni<?> saveLocations(List<LocationEntity> locationEntities) {
        if (locationEntities == null || locationEntities.isEmpty()) {
            log.debug("No locations");
            return Uni.createFrom().voidItem();
        }
        return Multi.createFrom().iterable(locationEntities).onItem()
                .transformToUni(l -> l.save(client))
                .merge().collect().asList()
                .invoke(l -> log.debug("Locations saved"));
    }

}
