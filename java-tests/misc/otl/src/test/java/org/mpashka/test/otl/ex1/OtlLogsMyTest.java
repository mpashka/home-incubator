package org.mpashka.test.otl.ex1;

import java.io.Closeable;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.LoggerBuilder;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.AttributesMap;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.ReadWriteLogRecord;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OtlLogsMyTest {
    private static final LogLimits LOG_LIMITS = LogLimits.getDefault();

    private static final ThreadLocal<MyContext> context = ThreadLocal.withInitial(MyContext::new);

    public static final String ATTRIBUTE_DEPLOY_PROJECT = "project";
    public static final String PROJECT = "test-ya-pashka";
    public static final String PROJECT1 = "test-ya-pashka1";
    public static final String PROJECT2 = "test-ya-pashka2";
    public static final String PROJECT3 = "test-deploy-ya-pashka-5";
    public static final String RESOURCE_KEY_PROJECT = "project";
    public static final String RESOURCE_KEY_CLUSTER = "cluster";
    public static final String RESOURCE_KEY_SERVICE = "service.name";
    public static final String CLUSTER = "my_test_cluster";
    public static final String SERVICE = "my_test_service";

    // "alextrushkin_test"

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

    private void initMy() {
        LogRecordExporter logRecordExporter = OtlpHttpLogRecordExporter.builder()
                .setEndpoint("http://localhost:25889/write")
                .setTimeout(Duration.ofMillis(100))
                .build();

        LogRecordProcessor logRecordProcessor =
                BatchLogRecordProcessor.builder(logRecordExporter)
                .build()
//                SimpleLogRecordProcessor.create(logRecordExporter)
                ;
        OpenTelemetry otelSdk = new MyOpenTelemetry(logRecordProcessor);

        OpenTelemetryAppender.install(otelSdk);
    }

    /**
     * <a href="https://docs.yandex-team.ru/logs/concepts/data-model">data-model</a>
     *
     */
    @Test
    public void testProduceLogs() throws InterruptedException {
        initMy();

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

            myContext.setMonitoringProject(PROJECT);
            MDC.pushByKey(ATTRIBUTE_DEPLOY_PROJECT, PROJECT1);
            MDC.put(ATTRIBUTE_DEPLOY_PROJECT, PROJECT1);
            log.trace("Trace message1, MDC{project:{}}", PROJECT1);
            log.debug("Debug message1, MDC{project:{}}", PROJECT1);
            log.info("Info message1, MDC{project:{}}", PROJECT1);
            log.warn("Warn message1, MDC{project:{}}", PROJECT1);
            log.error("Error message1, MDC{project:{}}", PROJECT1);
            assertThat(MDC.popByKey(ATTRIBUTE_DEPLOY_PROJECT), is(PROJECT1));
            MDC.remove(ATTRIBUTE_DEPLOY_PROJECT);
            myContext.setMonitoringProject(null);

            myContext.setMonitoringProject(PROJECT);
            MDC.pushByKey(ATTRIBUTE_DEPLOY_PROJECT, PROJECT2);
            MDC.put(ATTRIBUTE_DEPLOY_PROJECT, PROJECT2);
            log.debug("Info message2, MDC{project:{}}", PROJECT2);
            assertThat(MDC.popByKey(ATTRIBUTE_DEPLOY_PROJECT), is(PROJECT2));
            MDC.remove(ATTRIBUTE_DEPLOY_PROJECT);
            myContext.setMonitoringProject(null);

            myContext.setMonitoringProject(PROJECT);
            MDC.pushByKey(ATTRIBUTE_DEPLOY_PROJECT, PROJECT3);
            MDC.put(ATTRIBUTE_DEPLOY_PROJECT, PROJECT3);
            MDC.pushByKey("deploy.pod_id", "my_pod");
            MDC.pushByKey("deploy.box_id", "my_box");
            log.trace("Info message3, MDC{project:{}, pod_id:my_pod}", PROJECT3);
            assertThat(MDC.popByKey(ATTRIBUTE_DEPLOY_PROJECT), is(PROJECT3));
            assertThat(MDC.popByKey("deploy.pod_id"), is("my_pod"));
            assertThat(MDC.popByKey("deploy.box_id"), is("my_box"));
            MDC.remove(ATTRIBUTE_DEPLOY_PROJECT);
            myContext.setMonitoringProject(null);

            myContext.setMonitoringProject(PROJECT);
            MDC.pushByKey(ATTRIBUTE_DEPLOY_PROJECT, PROJECT3);
            MDC.put(ATTRIBUTE_DEPLOY_PROJECT, PROJECT3);
            MDC.pushByKey("deploy.pod_id", "my_pod");
            MDC.pushByKey("deploy.box_id", "my_box");
            log.error("Error message3, MDC{project:{}, pod_id:my_pod}", PROJECT3);
            assertThat(MDC.popByKey(ATTRIBUTE_DEPLOY_PROJECT), is(PROJECT3));
            assertThat(MDC.popByKey("deploy.pod_id"), is("my_pod"));
            assertThat(MDC.popByKey("deploy.box_id"), is("my_box"));
            MDC.remove(ATTRIBUTE_DEPLOY_PROJECT);
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

    private static class MyOpenTelemetry implements OpenTelemetry, LoggerProvider, Closeable {
        private final LogRecordProcessor logRecordProcessor;

        public MyOpenTelemetry(LogRecordProcessor logRecordProcessor) {
            this.logRecordProcessor = logRecordProcessor;
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
            return this;
        }

        @Override
        public LoggerBuilder loggerBuilder(String instrumentationScopeName) {
            return new MyLoggerBuilder(logRecordProcessor, instrumentationScopeName);
        }

        @Override
        public void close() {
            logRecordProcessor.close();
        }
    }

    private static class MyLoggerBuilder implements LoggerBuilder {
        private final LogRecordProcessor logRecordProcessor;
        private final String instrumentationScopeName;
        @Nullable private String instrumentationScopeVersion;
        @Nullable private String schemaUrl;

        public MyLoggerBuilder(LogRecordProcessor logRecordProcessor, String instrumentationScopeName) {
            this.logRecordProcessor = logRecordProcessor;
            this.instrumentationScopeName = instrumentationScopeName;
        }

        @Override
        public LoggerBuilder setSchemaUrl(String schemaUrl) {
            this.schemaUrl = schemaUrl;
            return this;
        }

        @Override
        public LoggerBuilder setInstrumentationVersion(String instrumentationScopeVersion) {
            this.instrumentationScopeVersion = instrumentationScopeVersion;
            return this;
        }

        @Override
        public io.opentelemetry.api.logs.Logger build() {
            InstrumentationScopeInfo instrumentationScopeInfo = InstrumentationScopeInfo.builder(instrumentationScopeName)
                    .setVersion(instrumentationScopeVersion)
                    .setSchemaUrl(schemaUrl)
                    .setAttributes(Attributes.empty())
                    .build();

            return new MyLogger(logRecordProcessor, instrumentationScopeInfo);
        }
    }

    private static class MyLogger implements io.opentelemetry.api.logs.Logger, LogRecordBuilder {
        private final LogRecordProcessor logRecordProcessor;
        private final InstrumentationScopeInfo instrumentationScopeInfo;
        private Resource resource;

        private long timestampEpochNanos;
        private long observedTimestampEpochNanos;
        private Context context;
        private Severity severity = Severity.UNDEFINED_SEVERITY_NUMBER;
        private String severityText;
        private Value<?> body;
        private AttributesMap attributes;

        public MyLogger(LogRecordProcessor logRecordProcessor, InstrumentationScopeInfo instrumentationScopeInfo) {
            this.logRecordProcessor = logRecordProcessor;
            this.instrumentationScopeInfo = instrumentationScopeInfo;
        }

        @Override
        public LogRecordBuilder logRecordBuilder() {
            MyContext myContext = OtlLogsMyTest.context.get();
            System.out.println("My context: " + myContext);

            resource = Resource.builder()
                    .put(RESOURCE_KEY_PROJECT, myContext.getMonitoringProject() != null ? myContext.getMonitoringProject() : PROJECT)
                    .put(RESOURCE_KEY_CLUSTER, CLUSTER)
                    .put(RESOURCE_KEY_SERVICE, SERVICE)
//                    .put("custom1", "my_custom1")
//                .put("cluster", "my_cluster")
//                .put("service.name", "my_service")
                    .build()
                    ;

            setAttribute("aaa", myContext.getAaa());
            return this;
        }

        @Override
        public LogRecordBuilder setTimestamp(long timestamp, TimeUnit unit) {
            this.timestampEpochNanos = unit.toNanos(timestamp);
            return this;
        }

        @Override
        public LogRecordBuilder setTimestamp(Instant instant) {
            this.timestampEpochNanos =
                    TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano();
            return this;
        }

        @Override
        public LogRecordBuilder setObservedTimestamp(long timestamp, TimeUnit unit) {
            this.observedTimestampEpochNanos = unit.toNanos(timestamp);
            return this;
        }

        @Override
        public LogRecordBuilder setObservedTimestamp(Instant instant) {
            this.observedTimestampEpochNanos = TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano();
            return this;
        }

        @Override
        public LogRecordBuilder setContext(Context context) {
            this.context = context;
            return this;
        }

        @Override
        public LogRecordBuilder setSeverity(Severity severity) {
            this.severity = severity;
            return this;
        }

        @Override
        public LogRecordBuilder setSeverityText(String severityText) {
            this.severityText = severityText;
            return this;
        }

        @Override
        public LogRecordBuilder setBody(String body) {
            return setBody(Value.of(body));
        }

        @Override
        public LogRecordBuilder setBody(Value<?> value) {
            this.body = value;
            return this;
        }

        @Override
        public <T> LogRecordBuilder setAttribute(AttributeKey<T> key, T value) {
            if (key == null || key.getKey().isEmpty() || value == null) {
                return this;
            }
            if (this.attributes == null) {
                this.attributes = AttributesMap.create(LOG_LIMITS.getMaxNumberOfAttributes(), LOG_LIMITS.getMaxAttributeValueLength());
            }
            this.attributes.put(key, value);
            return this;
        }

        @Override
        public void emit() {
            Context context = this.context == null ? Context.current() : this.context;
            long observedTimestampEpochNanos =
                    this.observedTimestampEpochNanos == 0
                            ? System.nanoTime()
                            : this.observedTimestampEpochNanos;

            logRecordProcessor
                    .onEmit(
                            context,
                            new MyReadWriteLogRecord(
                                    attributes,
                                    body,
                                    severityText,
                                    severity,
                                    Span.fromContext(context).getSpanContext(),
                                    observedTimestampEpochNanos,
                                    timestampEpochNanos,
                                    instrumentationScopeInfo,
                                    resource));
        }
    }

    public static class MyReadWriteLogRecord implements ReadWriteLogRecord {
        protected final Resource resource;
        protected final InstrumentationScopeInfo instrumentationScopeInfo;
        protected final long timestampEpochNanos;
        protected final long observedTimestampEpochNanos;
        protected final SpanContext spanContext;
        protected final Severity severity;
        @Nullable protected final String severityText;
        @Nullable protected final Value<?> body;
        private final Object lock = new Object();

        @GuardedBy("lock")
        @Nullable
        private AttributesMap attributes;

        public MyReadWriteLogRecord(AttributesMap attributes, Value<?> body, String severityText, Severity severity, SpanContext spanContext, long observedTimestampEpochNanos, long timestampEpochNanos, InstrumentationScopeInfo instrumentationScopeInfo, Resource resource) {
            this.attributes = attributes;
            this.body = body;
            this.severityText = severityText;
            this.severity = severity;
            this.spanContext = spanContext;
            this.observedTimestampEpochNanos = observedTimestampEpochNanos;
            this.timestampEpochNanos = timestampEpochNanos;
            this.instrumentationScopeInfo = instrumentationScopeInfo;
            this.resource = resource;
        }

        @Override
        public <T> ReadWriteLogRecord setAttribute(AttributeKey<T> key, T value) {
            if (key == null || key.getKey().isEmpty() || value == null) {
                return this;
            }
            synchronized (lock) {
                if (attributes == null) {
                    attributes = AttributesMap.create(LOG_LIMITS.getMaxNumberOfAttributes(), LOG_LIMITS.getMaxAttributeValueLength());
                }
                attributes.put(key, value);
            }
            return this;
        }

        private Attributes getImmutableAttributes() {
            synchronized (lock) {
                if (attributes == null || attributes.isEmpty()) {
                    return Attributes.empty();
                }
                return attributes.immutableCopy();
            }
        }

        @Override
        public LogRecordData toLogRecordData() {
            synchronized (lock) {
                return new MyLogRecordData(
                        resource,
                        instrumentationScopeInfo,
                        timestampEpochNanos,
                        observedTimestampEpochNanos,
                        spanContext,
                        severity,
                        severityText,
                        body,
                        getImmutableAttributes());
            }
        }

        @Override
        public InstrumentationScopeInfo getInstrumentationScopeInfo() {
            return instrumentationScopeInfo;
        }

        @Override
        public long getTimestampEpochNanos() {
            return timestampEpochNanos;
        }

        @Override
        public long getObservedTimestampEpochNanos() {
            return observedTimestampEpochNanos;
        }

        @Override
        public SpanContext getSpanContext() {
            return spanContext;
        }

        @Override
        public Severity getSeverity() {
            return severity;
        }

        @Nullable
        @Override
        public String getSeverityText() {
            return severityText;
        }

        @Nullable
        @Override
        public Value<?> getBodyValue() {
            return body;
        }

        @Override
        public Attributes getAttributes() {
            return getImmutableAttributes();
        }

        @Override
        public <T> T getAttribute(AttributeKey<T> key) {
            synchronized (lock) {
                if (attributes == null || attributes.isEmpty()) {
                    return null;
                }
                return attributes.get(key);
            }
        }
    }

    record MyLogRecordData(
            Resource getResource,
            InstrumentationScopeInfo getInstrumentationScopeInfo,
            long getTimestampEpochNanos,
            long getObservedTimestampEpochNanos,
            SpanContext getSpanContext,
            Severity getSeverity,
            String getSeverityText,
            Value<?> getBodyValue,
            Attributes getAttributes
    ) implements LogRecordData {

        public Body getBody() {
            Value<?> valueBody = getBodyValue();
            return valueBody == null
                    ? Body.empty()
                    : Body.string(valueBody.asString());
        }

        @Override
        public int getTotalAttributeCount() {
            return getAttributes() != null ? getAttributes().size() : 0;
        }
    }
}
