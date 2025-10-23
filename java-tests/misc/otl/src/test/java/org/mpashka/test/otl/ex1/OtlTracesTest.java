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
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.ServiceAttributes;
import org.junit.jupiter.api.Test;
//import org.mpashka.test.log.LogHelper;

/*
 * rootSpan.getSpanContext().getTraceId()
 */
public class OtlTracesTest {

//    static {LogHelper.julToSlf4j();}
    public static final boolean STANDARD_SPANS = true;

    @Test
    public void testOtlTraces1() {
        Tracer tracer = initTracing();

        //        rootSpan(tracer, 100, 80);
//        rootSpan(tracer, 60, 40);
        rootSpan(tracer, 20, 5);

//        standardSpans(tracer);

    }

    @Test
    public void testOtlTracesIss() {
        Tracer tracer = initTracing();

/*
        Span span = tracer.spanBuilder("root-span")
                .setNoParent()
                .setSpanKind(SpanKind.CONSUMER)
                .startSpan();
        span.setStatus(StatusCode.OK);
        span.end();
*/
        Span span = tracer.spanBuilder("my-span")
                .setParent(Context.root())
                .startSpan();
        Context context = span.storeInContext(Context.root());

/*
        Span span2 = tracer.spanBuilder("span2")
                .setParent(context)
                .startSpan();
        span2.storeInContext(context);

        Thread.sleep(100L);

        span2.end();
        Thread.sleep(100L);
*/

        span.end();
    }

    private void standardSpans(Tracer tracer, Context parentContext) {
        Span span = tracer.spanBuilder("calculate LLVM")
                .setParent(parentContext)
                .startSpan();
        Context context = parentContext.with(span);

        ((ExtendedSpanBuilder) tracer.spanBuilder("call")).setParent(context).startAndCall(this::returnSmth);
        ((ExtendedSpanBuilder) tracer.spanBuilder("run")).setParent(context).startAndRun(this::runSmth);

        span.setStatus(StatusCode.OK);
        span.end();
    }

    private void rootSpan(Tracer tracer, long fromSec, long toSec) {
        Context rootContext = Context.root();
//        rootContext.wrap(() -> {
            Instant time = Instant.now().minus(fromSec, ChronoUnit.SECONDS);
            Instant to = Instant.now().minus(toSec, ChronoUnit.SECONDS);


            // is visible in main selector
            Span rootSpan = tracer.spanBuilder("test-root")
                    .setStartTimestamp(time)
                    .setParent(rootContext)
//                    .setNoParent()
                    .setAttribute("root.from", fromSec)
                    .setAttribute("root.to", toSec)
                    .startSpan();

//            Context context = rootSpan.storeInContext(rootContext);
        Context context = Context.root().with(rootSpan);

//            try (Scope scope = context.makeCurrent()) {
                int idx = 0;
                while (time.isBefore(to)) {
                    time = generateSpan(tracer, context, ++idx, 0, time);
                }
                if (STANDARD_SPANS) {
                    standardSpans(tracer, context);
                }
//            }
            rootSpan.end(time.plus(1, ChronoUnit.SECONDS));
//        });
    }

    private void runSmth() {
    }

    private int returnSmth() {
        return 3;
    }

    private Instant generateSpan(Tracer tracer, Context parent, int idx, int level, Instant start0) {
        Instant start = start0.plus(100 + Math.round(Math.random()*1000), ChronoUnit.MILLIS);
        Span span = tracer.spanBuilder("lv-" + idx + "-" + level)
                .setParent(parent)
                .setStartTimestamp(start)
                .startSpan();
        Context context = span.storeInContext(parent);
        Instant stop;
//        try (Scope scope = context.makeCurrent()) {
            if (level < 4) {
                stop = generateSpan(tracer, context, idx, level + 1, start);
            } else {
                stop = start0.plus(1000 + Math.round(Math.random() * 4000), ChronoUnit.MILLIS);
            }
            stop = stop.plus(100 + Math.round(Math.random() * 1000), ChronoUnit.MILLIS);
//        }

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

    private Tracer initTracing() {
        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint("http://localhost:25888")
                .setTimeout(10, TimeUnit.SECONDS)
                .build();

        SpanProcessor simpleSpanProcessor = SimpleSpanProcessor.create(spanExporter);
        BatchSpanProcessor batchSpanProcessor = BatchSpanProcessor.builder(spanExporter)
                .setExporterTimeout(Duration.ofSeconds(30))
                .setScheduleDelay(Duration.ofSeconds(5))
                .setMaxExportBatchSize(512)
                .setMaxQueueSize(2048)
                .build();

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
//                        .setSampler(Sampler.alwaysOn())
//                        .setResource(Resource.getDefault())
                        .setResource(Resource.getDefault().merge(Resource.create(Attributes.builder()
                                // Service name
                                .put(ServiceAttributes.SERVICE_NAME, "my-test-svc-07")
                                .put(ServiceAttributes.SERVICE_VERSION, "1.0")
                                .put("project", "test-deploy-ya-pashka-5")
//                                .put("project", "test-ya-pashka")
                                .put("cluster", "oltp-test-cluster_code")
//                                .put(AttributeKey.stringKey("service.name"), "my-test-svc6")
//                                see Resource.getDefault():
//                                .put(TelemetryAttributes.TELEMETRY_SDK_NAME, "opentelemetry")
//                                .put(TelemetryAttributes.TELEMETRY_SDK_LANGUAGE, TelemetryAttributes.TelemetrySdkLanguageValues.JAVA)
//                                .put(TelemetryAttributes.TELEMETRY_SDK_VERSION, OtelVersion.VERSION)
                                .build())))
                        .addSpanProcessor(batchSpanProcessor)
                        .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
                        .build();

        OpenTelemetrySdk sdk = OpenTelemetrySdk.builder()
                        .setTracerProvider(sdkTracerProvider)
//                        .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                        .build();

        // Виден как otel.scope.name в span-ах и event-ах
        Tracer tracer = sdk.getTracer("my-test1");

        Runtime.getRuntime().addShutdownHook(new Thread(sdk::close));

        return tracer;
    }
}
