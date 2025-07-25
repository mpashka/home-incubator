package org.mpashka.test.otl.ex1;

import java.time.Duration;
import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OtlLogsSimpleTest {
    public static final String DEPLOY_PROJECT = "project";

    static {
        addOtelLogger();
    }

    private static void addOtelLogger() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        var otelAppender = new OpenTelemetryAppender();
        otelAppender.setCaptureMdcAttributes("project,pod_id,aaa,aaa1,workload_id,deploy.pod_id");
        otelAppender.setContext(lc);
        otelAppender.start();
        var rootLogger = lc.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(otelAppender);
        rootLogger.setLevel(Level.ALL);
    }

    private void init() {
        ResourceBuilder otelResourceBuilder = Resource.builder()
                .put("project", "test-ya-pashka")
                .put("cluster", "test_cluster2")
                .put("service.name", "my_test2")
                ;

        LogRecordExporter logRecordExporter = OtlpHttpLogRecordExporter.builder()
                .setEndpoint("http://localhost:25889/write")
                .setTimeout(Duration.ofMillis(100))
                .build();

        OpenTelemetrySdk otelSdk = OpenTelemetrySdk.builder()
                .setLoggerProvider(
                        SdkLoggerProvider.builder()
                                .setResource(otelResourceBuilder.build())
                                .addLogRecordProcessor(
//                                        BatchLogRecordProcessor.builder(logRecordExporter)
//                                                .build()
                                        SimpleLogRecordProcessor.create(logRecordExporter)
                                )
                                .build())
                .buildAndRegisterGlobal();

        OpenTelemetryAppender.install(otelSdk);
    }

    /**
     * <a href="https://docs.yandex-team.ru/logs/concepts/data-model">data-model</a>
     *
     */
    @Test
    public void testProduceLogs() throws InterruptedException {
        init();

        Logger log = LoggerFactory.getLogger("org.test.my-logger");
        while (true) {
            log.info("Info message, no MDC");

            MDC.pushByKey("aaa", "bbb");
            MDC.put("aaa1", "bbb");
            assertThat(MDC.getMDCAdapter().getCopyOfContextMap(), is(Map.of("aaa1", "bbb")));
//            assertThat(MDC.get("aaa"), is("bbb"));
            log.warn("Warn message, MDC{aaa:bbb}");
            assertThat(MDC.popByKey("aaa"), is("bbb"));
            MDC.remove("aaa1");

            MDC.pushByKey(DEPLOY_PROJECT, "ya-pashka-test1");
            MDC.put(DEPLOY_PROJECT, "ya-pashka-test1");
            log.info("Info message to ya-pashka-test2, MDC{project:ya-pashka-test1}");
            assertThat(MDC.popByKey(DEPLOY_PROJECT), is("ya-pashka-test1"));
            MDC.remove(DEPLOY_PROJECT);

            MDC.pushByKey(DEPLOY_PROJECT, "alextrushkin_test");
            MDC.put(DEPLOY_PROJECT, "alextrushkin_test");
            log.info("Info message to alextrushkin_test, MDC{project:alextrushkin_test}");
            assertThat(MDC.popByKey(DEPLOY_PROJECT), is("alextrushkin_test"));
            MDC.remove(DEPLOY_PROJECT);

            MDC.pushByKey(DEPLOY_PROJECT, "ya-pashka-test2");
            MDC.put(DEPLOY_PROJECT, "ya-pashka-test2");
            MDC.pushByKey("deploy.pod_id", "my_pod");
            MDC.pushByKey("deploy.box_id", "my_box");
            log.info("Info message to ya-pashka-test2, MDC{project:ya-pashka-test2, pod_id:my_pod}");
            assertThat(MDC.popByKey(DEPLOY_PROJECT), is("ya-pashka-test2"));
            assertThat(MDC.popByKey("deploy.pod_id"), is("my_pod"));
            assertThat(MDC.popByKey("deploy.box_id"), is("my_box"));
            MDC.remove(DEPLOY_PROJECT);

            Thread.sleep(3_000);
        }
    }
}
