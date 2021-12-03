package org.mpashka.totemftc.api;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public Uni<String> schedulePropagate() {
        return dbScheduler.schedulePropagate()
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
}
