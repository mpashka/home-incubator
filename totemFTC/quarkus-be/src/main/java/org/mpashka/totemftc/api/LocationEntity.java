package org.mpashka.totemftc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.StreamSupport;

public class LocationEntity {

    private static final Logger log = LoggerFactory.getLogger(LocationEntity.class);

    @JsonProperty("work_time")
    public long workTime;

    @JsonProperty("work_provider")
    public String workProvider;

    @JsonProperty("time")
    public long time;

    @JsonProperty("provider")
    public String provider;

    @JsonProperty("lat")
    public double latitude;

    @JsonProperty("long")
    public double longitude;

    @JsonProperty("accuracy")
    public double accuracy;

    @JsonProperty("battery")
    public int battery;

    @JsonProperty("mi_battery")
    public int miBattery;

    @JsonProperty("mi_steps")
    public int miSteps;

    @JsonProperty("mi_heart")
    public int miHeart;

    @JsonProperty("accel_avg")
    public double accelerometerAverage;

    @JsonProperty("accel_max")
    public double accelerometerMaximum;

    @JsonProperty("accel_count")
    public int accelerometerCount;

    @JsonProperty("activity")
    public int activity;

    public LocationEntity() {
    }

    public LocationEntity(long workTime, String workProvider, long time, String provider, double latitude,
                          double longitude, double accuracy, int battery, int miBattery, int miSteps, int miHeart,
                          double accelerometerAverage, double accelerometerMaximum, int accelerometerCount,
                          int activity) {
        this.workTime = workTime;
        this.time = time;
        this.workProvider = workProvider;
        this.provider = provider;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.battery = battery;
        this.miBattery = miBattery;
        this.miSteps = miSteps;
        this.miHeart = miHeart;
        this.accelerometerAverage = accelerometerAverage;
        this.accelerometerMaximum = accelerometerMaximum;
        this.accelerometerCount = accelerometerCount;
        this.activity = activity;
    }

    public static void init(PgPool client) {
        Uni.createFrom().item(1)
//                .flatMap(u -> client.query("DROP TABLE IF EXISTS location").execute())
                .flatMap(r -> client.query("CREATE TABLE IF NOT EXISTS location (" +
                        "work_time NUMERIC PRIMARY KEY, " +
                        "work_provider VARCHAR(10), " +
                        "time NUMERIC NOT NULL, " +
                        "provider VARCHAR(10), " +
                        "lat NUMERIC(14,11) NOT NULL, " +
                        "long NUMERIC(14,11) NOT NULL, " +
                        "accuracy NUMERIC(6,3) NOT NULL," +
                        "battery NUMERIC NOT NULL," +
                        "mi_battery NUMERIC NOT NULL," +
                        "mi_steps NUMERIC NOT NULL," +
                        "mi_heart NUMERIC NOT NULL," +
                        "accel_avg NUMERIC(6,3) NOT NULL," +
                        "accel_max NUMERIC(6,3) NOT NULL," +
                        "accel_count NUMERIC NOT NULL," +
                        "activity NUMERIC NOT NULL" +
                        ")").execute())
                .await().indefinitely();
    }

    private static LocationEntity from(Row row) {
        return new LocationEntity(
                row.getLong("work_time"),
                row.getString("work_provider"),
                row.getLong("time"),
                row.getString("provider"),
                row.getDouble("lat"),
                row.getDouble("long"),
                row.getDouble("accuracy"),
                row.getInteger("battery"),
                row.getInteger("mi_battery"),
                row.getInteger("mi_steps"),
                row.getInteger("mi_heart"),
                row.getDouble("accel_avg"),
                row.getDouble("accel_max"),
                row.getInteger("accel_count"),
                row.getInteger("activity")
                );
    }

    public static Multi<LocationEntity> findAll(PgPool client, long start, long stop) {
        return client.preparedQuery("SELECT work_time, work_provider, time, provider, lat, long, accuracy, battery, " +
                "   mi_battery, mi_steps, mi_heart, accel_avg, accel_max, accel_count, activity " +
                "FROM location " +
                "WHERE work_time >= $1 and work_time <= $2" +
                "ORDER BY time ASC")
                .execute(Tuple.of(start, stop))
                // Create a Multi from the set of rows:
                .onItem().transformToMulti(set -> Multi.createFrom().items(() -> StreamSupport.stream(set.spliterator(), false)))
                // For each row create a fruit instance
                .onItem().transform(LocationEntity::from);
    }

    public Uni<?> save(PgPool client) {
        return client.preparedQuery("INSERT INTO location (work_time, work_provider, time, provider, lat, long, accuracy, battery, " +
                "   mi_battery, mi_steps, mi_heart, accel_avg, accel_max, accel_count, activity) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15)")
                .execute(Tuple.tuple(List.of(workTime, workProvider, time, provider, latitude, longitude, accuracy, battery,
                        miBattery, miSteps, miHeart, accelerometerAverage, accelerometerMaximum, accelerometerCount, activity)))
                .onFailure().invoke(e -> log.warn("Save location error", e))
                .onFailure().recoverWithNull();
        /*
        return client.preparedQuery("INSERT INTO fruits (name) VALUES ($1) RETURNING (id)").execute(Tuple.of(name))
                .onItem().transform(pgRowSet -> pgRowSet.iterator().next().getLong("id"));
         */
    }

    @Override
    public String toString() {
        return "LocationEntity{" +
                "workTime=" + workTime +
                ", workProvider='" + workProvider + '\'' +
                ", time=" + time +
                ", provider='" + provider + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", accuracy=" + accuracy +
                ", battery=" + battery +
                ", miBattery=" + miBattery +
                ", miSteps=" + miSteps +
                ", miHeart=" + miHeart +
                ", accelerometerAverage=" + accelerometerAverage +
                ", accelerometerMaximum=" + accelerometerMaximum +
                ", accelerometerCount=" + accelerometerCount +
                ", activity=" + activity +
                '}';
    }
}
