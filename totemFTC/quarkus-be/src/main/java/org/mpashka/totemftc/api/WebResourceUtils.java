package org.mpashka.totemftc.api;

import io.quarkus.runtime.configuration.ProfileManager;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.jar.Manifest;

@Path("/api/utils")
public class WebResourceUtils {

    @Inject
    DbScheduler dbScheduler;

    @Inject
    Instance<AuthProviderOidc> authProviders;

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
    public ClientConfig clientConfig(@RestQuery WebResourceLogin.ClientId clientId) throws IOException {
        String serverId = ConfigProvider.getConfig().getConfigValue("server-id").getValue();
        String profile = ProfileManager.getActiveProfile();
        String build = null;
        Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
        while (resources.hasMoreElements()) {
            Manifest manifest = new Manifest(resources.nextElement().openStream());
            String b = manifest.getMainAttributes().getValue("Implementation-Build");
            if (b != null) {
                build = b;
            }
        }

        if (build == null) {
            try (InputStream propsStream = getClass().getClassLoader().getResourceAsStream("build.properties")) {
                if (propsStream != null) {
                    Properties props = new Properties();
                    props.load(propsStream);
                    build = props.getProperty("version") + "-" + props.getProperty("revision") + "-" + props.getProperty("build.timestamp");
                }
            }
        }

        if (build == null) {
            build = "unknown";
        }

        Map<String, String> oidcClientIds = new HashMap<>();
        authProviders.stream().forEach(a -> oidcClientIds.put(a.getName(), a.getClientId(clientId)));
        return new ClientConfig(serverId, profile, build, oidcClientIds);
    }

    public record ClientConfig(String serverId, String serverRunProfile, String serverBuild, Map<String, String> oidcClientIds) {}
}
