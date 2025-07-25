package org.mpashka.test.otl.ex1;

import java.io.Closeable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.LoggerBuilder;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.AttributesMap;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OtlLogsTest {

    private static final ThreadLocal<MyContext> context = ThreadLocal.withInitial(MyContext::new);

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
                .put("project", "test-ya-pashka1")
                .put("custom1", "my_custom1")
//                .put("cluster", "my_cluster")
//                .put("service.name", "my_service")
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
                                        BatchLogRecordProcessor.builder(logRecordExporter)
                                                .build())
                                .build())
                .buildAndRegisterGlobal();

        OpenTelemetryAppender.install(otelSdk);
    }

    private void initCustom() {
        ResourceBuilder otelResourceBuilder = Resource.builder()
                .put("project", "test-ya-pashka1")
                .put("custom1", "my_custom1")
//                .put("cluster", "my_cluster")
//                .put("service.name", "my_service")
                ;

        LogRecordExporter logRecordExporter = OtlpHttpLogRecordExporter.builder()
                .setEndpoint("http://localhost:25889/write")
                .setTimeout(Duration.ofMillis(100))
                .build();

        LogRecordExporter exporterWrapper = new LogRecordExporterWrapper(logRecordExporter);

        OpenTelemetrySdk otelSdk = OpenTelemetrySdk.builder()
                .setLoggerProvider(
                        SdkLoggerProvider.builder()
                                .setResource(otelResourceBuilder.build())
                                .addLogRecordProcessor(
//                                        BatchLogRecordProcessor.builder(logRecordExporter)

                                        BatchLogRecordProcessor.builder(exporterWrapper)
                                                .build()

//                                        SimpleLogRecordProcessor.create(exporterWrapper)
                                )
                                .build())
                .buildAndRegisterGlobal();

        OpenTelemetryAppender.install(otelSdk);
    }

    private void initCustom2() {
        ResourceBuilder otelResourceBuilder = Resource.builder()
                .put("project", "test-ya-pashka1")
                .put("custom1", "my_custom1")
//                .put("cluster", "my_cluster")
//                .put("service.name", "my_service")
                ;

        LogRecordExporter logRecordExporter = OtlpHttpLogRecordExporter.builder()
                .setEndpoint("http://localhost:25889/write")
                .setTimeout(Duration.ofMillis(100))
                .build();

        OpenTelemetry otelSdk = new MyOpenTelemetry(
                new MyLoggerProvider(
                        SdkLoggerProvider.builder()
                                .setResource(otelResourceBuilder.build())
                                .addLogRecordProcessor(
                                        BatchLogRecordProcessor.builder(logRecordExporter)
                                                .build()
                                )
                                .build())
        );

        OpenTelemetryAppender.install(otelSdk);
    }

    /**
     * <a href="https://docs.yandex-team.ru/logs/concepts/data-model">data-model</a>
     *
     */
    @Test
    public void testProduceLogs() throws InterruptedException {
        initCustom2();

        Logger log = LoggerFactory.getLogger("org.test.my-logger");
        while (true) {
            log.info("Info message, no MDC");

            MyContext myContext = context.get();

            myContext.setAaa("bbb");
            MDC.pushByKey("aaa", "bbb");
            MDC.put("aaa1", "bbb");
            assertThat(MDC.getMDCAdapter().getCopyOfContextMap(), is(Map.of("aaa1", "bbb")));
//            assertThat(MDC.get("aaa"), is("bbb"));
            log.warn("Warn message, MDC{aaa:bbb}");
            assertThat(MDC.popByKey("aaa"), is("bbb"));
            MDC.remove("aaa1");
            myContext.setAaa(null);

            myContext.setMonitoringProject("ya-pashka-test1");
            MDC.pushByKey(DEPLOY_PROJECT, "ya-pashka-test1");
            MDC.put(DEPLOY_PROJECT, "ya-pashka-test1");
            log.info("Info message to ya-pashka-test2, MDC{project:ya-pashka-test1}");
            assertThat(MDC.popByKey(DEPLOY_PROJECT), is("ya-pashka-test1"));
            MDC.remove(DEPLOY_PROJECT);
            myContext.setMonitoringProject(null);

            myContext.setMonitoringProject("alextrushkin_test");
            MDC.pushByKey(DEPLOY_PROJECT, "alextrushkin_test");
            MDC.put(DEPLOY_PROJECT, "alextrushkin_test");
            log.info("Info message to alextrushkin_test, MDC{project:alextrushkin_test}");
            assertThat(MDC.popByKey(DEPLOY_PROJECT), is("alextrushkin_test"));
            MDC.remove(DEPLOY_PROJECT);
            myContext.setMonitoringProject(null);

            myContext.setMonitoringProject("ya-pashka-test2");
            MDC.pushByKey(DEPLOY_PROJECT, "ya-pashka-test2");
            MDC.put(DEPLOY_PROJECT, "ya-pashka-test2");
            MDC.pushByKey("deploy.pod_id", "my_pod");
            MDC.pushByKey("deploy.box_id", "my_box");
            log.info("Info message to ya-pashka-test2, MDC{project:ya-pashka-test2, pod_id:my_pod}");
            assertThat(MDC.popByKey(DEPLOY_PROJECT), is("ya-pashka-test2"));
            assertThat(MDC.popByKey("deploy.pod_id"), is("my_pod"));
            assertThat(MDC.popByKey("deploy.box_id"), is("my_box"));
            MDC.remove(DEPLOY_PROJECT);
            myContext.setMonitoringProject(null);

            Thread.sleep(3_000);
        }
    }


    private static class MyContext {
        private String aaa;
        private String monitoringProject;

        public String getAaa() {
            return aaa;
        }

        public void setAaa(String aaa) {
            this.aaa = aaa;
        }

        public String getMonitoringProject() {
            return monitoringProject;
        }

        public void setMonitoringProject(String monitoringProject) {
            this.monitoringProject = monitoringProject;
        }

        @Override
        public String toString() {
            return "MyContext{" +
                    "aaa=" + aaa +
                    ", monitoringProject='" + monitoringProject + '\'' +
                    '}';
        }
    }

    private static class LogRecordExporterWrapper implements LogRecordExporter {
        public static final AttributeKey<String> PROJECT = AttributeKey.stringKey("project");
        private final LogRecordExporter exporter;

        public LogRecordExporterWrapper(LogRecordExporter exporter) {
            this.exporter = exporter;
        }

        @Override
        public CompletableResultCode export(Collection<LogRecordData> logs) {
            List<LogRecordData> projectLogs = new ArrayList<>(logs.size());
            for (LogRecordData log : logs) {
                String project = log.getAttributes().get(PROJECT);
                if (project != null) {
                    Attributes attributes = log.getAttributes().toBuilder().remove(PROJECT).build();
                    Resource resource = log.getResource().toBuilder().put("project", project).build();
                    LogRecordData projectLog = new LogRecordDataImpl(log, resource, attributes);
                    System.out.println("Replace resource for project " + project);
                    projectLogs.add(projectLog);
                } else {
                    projectLogs.add(log);
                }
            }

            return exporter.export(projectLogs);
        }

        @Override
        public CompletableResultCode flush() {
            return exporter.flush();
        }

        @Override
        public CompletableResultCode shutdown() {
            return exporter.shutdown();
        }

        @Override
        public void close() {
            exporter.close();
        }
    }

    record LogRecordDataImpl(
            Resource getResource,
            InstrumentationScopeInfo getInstrumentationScopeInfo,
            long getTimestampEpochNanos,
            long getObservedTimestampEpochNanos,
            SpanContext getSpanContext,
            Severity getSeverity,
            String getSeverityText,
            Body getBody,
            Attributes getAttributes,
            int getTotalAttributeCount
    ) implements LogRecordData {

        public LogRecordDataImpl(LogRecordData log, Resource resource, Attributes attributes) {
            this(
                    resource,
                    log.getInstrumentationScopeInfo(),
                    log.getTimestampEpochNanos(),
                    log.getObservedTimestampEpochNanos(),
                    log.getSpanContext(),
                    log.getSeverity(),
                    log.getSeverityText(),
                    log.getBody(),
                    attributes,
                    log.getTotalAttributeCount()
            );
        }
    }

    public static class MyOpenTelemetry implements OpenTelemetry {
        private final LoggerProvider loggerProvider;

        public MyOpenTelemetry(LoggerProvider loggerProvider) {
            this.loggerProvider = loggerProvider;
        }

        @Override
        public TracerProvider getTracerProvider() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ContextPropagators getPropagators() {
            throw new UnsupportedOperationException();
        }

        @Override
        public LoggerProvider getLogsBridge() {
            return loggerProvider;
        }
    }

    public static class MyLoggerProvider implements LoggerProvider, Closeable {
        private final SdkLoggerProvider loggerProvider;

        public MyLoggerProvider(SdkLoggerProvider loggerProvider) {
            this.loggerProvider = loggerProvider;
        }

        @Override
        public LoggerBuilder loggerBuilder(String instrumentationScopeName) {
            return new MyLoggerBuilder(loggerProvider.loggerBuilder(instrumentationScopeName));
        }

        @Override
        public void close() {
            loggerProvider.close();
        }
    }

    public static class MyLoggerBuilder implements LoggerBuilder {
        private final LoggerBuilder loggerBuilder;

        public MyLoggerBuilder(LoggerBuilder loggerBuilder) {
            this.loggerBuilder = loggerBuilder;
        }

        @Override
        public LoggerBuilder setSchemaUrl(String schemaUrl) {
            return loggerBuilder.setSchemaUrl(schemaUrl);
        }

        @Override
        public LoggerBuilder setInstrumentationVersion(String instrumentationScopeVersion) {
            return loggerBuilder.setInstrumentationVersion(instrumentationScopeVersion);
        }

        @Override
        public io.opentelemetry.api.logs.Logger build() {
            return new MyLogger(loggerBuilder.build());
        }
    }

    public static class MyLogger implements io.opentelemetry.api.logs.Logger {
        private final io.opentelemetry.api.logs.Logger logger;

        public MyLogger(io.opentelemetry.api.logs.Logger logger) {
            this.logger = logger;
        }

        @Override
        public LogRecordBuilder logRecordBuilder() {
            LogRecordBuilder logRecordBuilder = logger.logRecordBuilder();
            MyContext myContext = context.get();
            System.out.println("My context: " + myContext);
            logRecordBuilder.setAttribute("aaa", myContext.getAaa());
            logRecordBuilder.setAttribute("project", myContext.getMonitoringProject());

//            LogRecordData projectLog = new LogRecordDataImpl(log, resource, attributes);
//
//            if (myContext.getAaa())

            return logRecordBuilder;
        }

//        private void set
    }

    public static class MyLogRecordBuilder implements LogRecordBuilder {
        protected final LoggerSharedState loggerSharedState;
        protected final LogLimits logLimits;

        protected final InstrumentationScopeInfo instrumentationScopeInfo;
        protected long timestampEpochNanos;
        protected long observedTimestampEpochNanos;
        protected Context context;
        protected Severity severity = Severity.UNDEFINED_SEVERITY_NUMBER;
        protected String severityText;
        protected Value<?> body;
        protected String eventName;
        private AttributesMap attributes;

        SdkLogRecordBuilder(
                LoggerSharedState loggerSharedState, InstrumentationScopeInfo instrumentationScopeInfo) {

        }
    }
}
