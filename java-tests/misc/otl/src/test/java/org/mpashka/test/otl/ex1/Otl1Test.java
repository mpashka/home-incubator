package org.mpashka.test.otl.ex1;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.trace.ExtendedSpanBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.Test;
import org.mpashka.test.log.LogHelper;

public class Otl1Test {

    static {LogHelper.julToSlf4j();}
    public static final boolean STANDARD_SPANS = false;

    @Test
    public void testOtl1() {
        SdkTracerProvider sdkTracerProvider =
                SdkTracerProvider.builder()
//                        .setResource(Resource.getDefault())
                        .setResource(Resource.create(Attributes.builder()
                                // Service name
//                                .put(ResourceAttributes.SERVICE_NAME, "my-test-svc2")
//                                .put(AttributeKey.stringKey("service.name"), "my-test-svc2")
//                                .put(AttributeKey.stringKey("telemetry.sdk.name"), "opentelemetry")
//                                .put(AttributeKey.stringKey("telemetry.sdk.language"), "java")
//                                .put(AttributeKey.stringKey("telemetry.sdk.version"), OtelVersion.VERSION)
                                .build()))
                        .addSpanProcessor(SimpleSpanProcessor.create(OtlpGrpcSpanExporter.builder()
                                .setEndpoint("http://localhost:4318")
                                .setTimeout(10, TimeUnit.SECONDS)
                                .build()))
                        .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
                        .build();

        OpenTelemetrySdk sdk =
                OpenTelemetrySdk.builder()
                        .setTracerProvider(sdkTracerProvider)
//                        .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                        .build();

        // Виден как otel.scope.name в span-ах и event-ах
        Tracer tracer = sdk.getTracer("my-test1");
        rootSpan(tracer, 100, 80);
        rootSpan(tracer, 60, 40);
        rootSpan(tracer, 20, 5);

//        standardSpans(tracer);

        Runtime.getRuntime().addShutdownHook(new Thread(sdk::close));
    }

    private void standardSpans(Tracer tracer, Context context) {
        Span span = tracer.spanBuilder("calculate LLVM")
                .setParent(context)
                .startSpan();
        span.setStatus(StatusCode.OK);
        span.end();

        ((ExtendedSpanBuilder) tracer.spanBuilder("call")).startAndCall(this::returnSmth);
        ((ExtendedSpanBuilder) tracer.spanBuilder("run")).startAndRun(this::runSmth);
    }

    private void rootSpan(Tracer tracer, long fromSec, long toSec) {
        Instant time = Instant.now().minus(fromSec, ChronoUnit.SECONDS);
        Instant to = Instant.now().minus(toSec, ChronoUnit.SECONDS);

        // is visible in main selector
        Span rootSpan = tracer.spanBuilder("test-root")
                .setStartTimestamp(time)
                .setNoParent()
                .setAttribute("root.from", fromSec)
                .setAttribute("root.to", toSec)
                .startSpan();
        Context context = Context.current().with(rootSpan);
        try (Scope scope = context.makeCurrent()) {
            int idx = 0;
            while (time.isBefore(to)) {
                time = generateSpan(tracer, context, ++idx, 0, time);
            }
            if (STANDARD_SPANS) {
                standardSpans(tracer, context);
            }
        }
        rootSpan.end(time.plus(1, ChronoUnit.SECONDS));
    }

    private void runSmth() {
    }

    private int returnSmth() {
        return 3;
    }

    private Instant generateSpan(Tracer tracer, Context parent, int idx, int level, Instant start0) {
        Instant start = start0.plus(100 + Math.round(Math.random()*1000), ChronoUnit.MILLIS);
        Span span = tracer.spanBuilder("lv-" + idx + "-" + level)
                .setStartTimestamp(start)
                .startSpan();
        Context context = span.storeInContext(parent);
        Instant stop;
        try (Scope scope = context.makeCurrent()) {
            if (level < 4) {
                stop = generateSpan(tracer, context, idx, level + 1, start);
            } else {
                stop = start0.plus(1000 + Math.round(Math.random() * 4000), ChronoUnit.MILLIS);
            }
            stop = stop.plus(100 + Math.round(Math.random() * 1000), ChronoUnit.MILLIS);
        }

        Duration duration = Duration.between(start, stop);
        for (int i = 0; i < 3; i++) {
            span.addEvent("ev-" + i, Attributes.of(
                            AttributeKey.stringKey("idx"), String.valueOf(idx),
                            AttributeKey.stringKey("lv"), String.valueOf(level),
                            AttributeKey.stringKey("ev"), String.valueOf(i)
                    ),
                    start.plus(Math.round(Math.random() * duration.toMillis()), ChronoUnit.MILLIS));
        }
        span.setAttribute("idx", idx);
        span.setAttribute("level", level);
        for (int i = 0; i < 3; i++) {
            span.setAttribute("attr-" + i, level * 10000 + idx * 100 + i);
        }
        span.setStatus(StatusCode.OK);
        span.end(stop);
        return stop.plus(100 + Math.round(Math.random()*1000), ChronoUnit.MILLIS);
    }
}
