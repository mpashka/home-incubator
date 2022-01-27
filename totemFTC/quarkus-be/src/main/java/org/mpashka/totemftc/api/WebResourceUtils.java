package org.mpashka.totemftc.api;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.resteasy.reactive.RestQuery;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Path("/api/utils")
public class WebResourceUtils {

    @Inject
    DbScheduler dbScheduler;

    /**
     * Used to propagate/apply schedule for dev env (while scheduler doesn't work) and show some results
     */
    @GET
    @Path("schedulePropagate")
    @RolesAllowed(MySecurityProvider.ROLE_ADMIN)
    public Uni<String> schedulePropagate(@RestQuery LocalDate weekStart) {
        return dbScheduler.schedulePropagate(weekStart != null ? weekStart : LocalDate.now())
                .onItem().transform(res -> {
                    if (res == null) {
                        return "Nothing updated\n";
                    }
                    int total = 0;
                    SortedMap<String, Integer> byType = new TreeMap<>();
                    SortedMap<LocalDate, Integer> byDate = new TreeMap<>();
                    do {
                        for (Row row: res) {
                            LocalDateTime trainingTime = row.getLocalDateTime("training_time");
                            LocalDate trainingDate = trainingTime.toLocalDate();
                            String trainingType = row.getString("training_type");
                            byDate.put(trainingDate, byDate.getOrDefault(trainingDate, 0) + 1);
                            byType.put(trainingType, byType.getOrDefault(trainingType, 0) + 1);
                        }
                        total += res.rowCount();
                    } while ((res = res.next()) != null);

                    return "Total " + total + "\n" +
                            "By Date: " + byDate + "\n" +
                            "By Type: " + byType + "\n";
                });
    }

    @GET
    @Path("clientConfig")
    public ClientConfig clientConfig() {
        Map<String, String> oidcClientIds = new HashMap<>();
        Config config = ConfigProvider.getConfig();
        for (String propertyName : config.getPropertyNames()) {
            if (propertyName.startsWith("oidc.provider.") && propertyName.endsWith(".clientId")) {
                String clientId = config.getConfigValue(propertyName).getValue();
                String providerName = propertyName.substring(14, propertyName.length() - 9);
                if (!providerName.contains("-") && !providerName.contains(".")) {
                    oidcClientIds.put(providerName, clientId);
                }
            }
        }
        return new ClientConfig(oidcClientIds);
    }

    public static class ClientConfig {
        private Map<String, String> oidcClientIds;

        public ClientConfig(Map<String, String> oidcClientIds) {
            this.oidcClientIds = oidcClientIds;
        }
    }
}
