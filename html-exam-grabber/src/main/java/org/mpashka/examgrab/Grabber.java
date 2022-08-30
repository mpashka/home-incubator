package org.mpashka.examgrab;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.CookieManager;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Grabber {

    private static final Logger log = LoggerFactory.getLogger(Grabber.class);

    private static final String cacheDirName = ".cache";
    private static final String SITE_URL = "site_url";

    private File cacheDir;
    private File cacheListFile;

    private AtomicInteger cacheNumber;
    private Map<String, PageInfo> pages = new ConcurrentHashMap<>();
    private HttpClient client;
    private List<CompletableFuture<PageInfo>> fetchTasks = new CopyOnWriteArrayList<>();
    private Saver saver;

    public Grabber(Saver saver) throws Exception {
        this.saver = saver;
        cacheDir = new File(cacheDirName);
        cacheDir.mkdirs();
        loadCache();

        client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .cookieHandler(new CookieManager())
//                .proxy(ProxySelector.of(new InetSocketAddress("proxy.example.com", 80)))
//                .authenticator(Authenticator.getDefault())
                .build();

    }

    public HttpClient getClient() {
        return client;
    }

    private void loadCache() throws Exception {
        cacheListFile = new File(cacheDir, "list.properties");
        cacheNumber = new AtomicInteger(10_000);
        if (cacheListFile.exists()) {
            Properties properties = new Properties();
            try (FileReader reader = new FileReader(cacheListFile)) {
                properties.load(reader);
            }
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String name = entry.getKey().toString();
                if (name.startsWith(SITE_URL)) {
                    new PageInfo(entry.getValue().toString()).loadFromProperty(name, properties);
                }
            }
        }
    }

    private static final int MAX_UPDATE = 20;
    private static final Duration MAX_UPDATE_PERIOD = Duration.ofMinutes(1);
    private int iterations = 0;
    private Instant lastUpdate = Instant.now();
    public synchronized void saveCacheIfNeeded() {
        Instant now = Instant.now();
        if (iterations++ > MAX_UPDATE || now.minus(MAX_UPDATE_PERIOD).isAfter(lastUpdate)) {
            try {
                iterations = 0;
                lastUpdate = now;
                saveCache();
            } catch (Exception e) {
                log.error("Error saving cache", e);
            }
        }
    }

    public void saveCache() throws Exception {
        log.info("Saving file cache...");
        Properties properties = new Properties();
        pages.forEach((k, v) -> v.saveToProperties(properties));
        try (FileWriter fileWriter = new FileWriter(cacheListFile)) {
            properties.store(fileWriter, "sites");
        }
    }

    public void waitTasks() throws Exception {
        for (var fetchTask : fetchTasks) {
            try {
                fetchTask.get(60, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("Task error", e);
            }
        }
    }

    public CompletableFuture<PageInfo> loadPage(String url, SiteGrabber grabber, boolean allowError, PageConsumer pageConsumer) throws Exception {
        PageInfo pageInfo = pages.get(url);
        if (pageInfo != null && (allowError || pageInfo.getResponseCode() == 200)) {
            log.info("Page {}/{}. Parse by {}: {}", pageInfo.getType(), pageInfo.getNumber(), grabber, url);
            pageConsumer.page(pageInfo);
            return CompletableFuture.completedFuture(pageInfo);
        } else {
            return loadPageAsync(url, grabber, allowError, pageConsumer);
        }
    }

    private CompletableFuture<PageInfo> loadPageAsync(String url, SiteGrabber grabber, boolean allowError, PageConsumer pageConsumer) throws Exception {
        HttpRequest request = grabber.request(url);

        CompletableFuture<PageInfo> siteGrab = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    String s = response.body();
                    PageInfo pageInfo = new PageInfo(url, s, response.statusCode());
                    log.info("Page loaded {}:{}. Parse by {}: {}", pageInfo.getNumber(), pageInfo.getResponseCode(), grabber, url);
                    if (response.statusCode() == 200 || allowError) {
                        try {
                            pageConsumer.page(pageInfo);
                        } catch (Exception e) {
                            log.error("Error processing page: {}", pageInfo.getNumber(), e);
                            log.error("Body: {}", s);
                            throw new RuntimeException(e);
                        }
                    }
                    return pageInfo;
                });
        fetchTasks.add(siteGrab);
        return siteGrab;
    }

    public CompletableFuture<PageInfo> grabCourse(String url, SiteGrabber grabber, String courseName) throws Exception {
        log.info("Grab course {}: {}: {}", grabber, courseName, url);
        Saver.Course course = saver.addCourse(grabber, courseName);
        return loadPage(url, grabber, false, s -> grabCourse(url, grabber, course, s));
    }

    private void grabCourse(String url, SiteGrabber grabber, Saver.Course course, PageInfo pageInfo) {
        log.info("Parse course page/{}. Url {}. Grab by [{}]: {}", pageInfo.getNumber(), pageInfo.getType(), grabber, url);
        if (pageInfo.isProcessed()) {
            return;
        }
        try {
            int count = grabber.grab(pageInfo.getBody(), course);
            if (count <= 0) {
                log.error("Parse error. Count: {}. Cached file: {}", count, pageInfo.getNumber());
            } else {
                log.info("Parsed {}", count);
            }
            pageInfo.setProcessed(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public class PageInfo {
        private String url;
        private int number;
        private Instant updateTime;
        private String body;
        private PageType type;
        private boolean processed;
        private int responseCode;

        public PageInfo(String url) {
            this.url = url;
            this.type = PageType.cached;
        }

        public PageInfo(String url, String body, int responseCode) {
            this.url = url;
            this.responseCode = responseCode;
            this.number = cacheNumber.getAndIncrement();
            this.updateTime = Instant.now();
            this.body = body;
            this.type = PageType.loaded;
            if (body != null) {
                try {
                    Files.writeString(getSiteFile(), body);
                } catch (IOException e1) {
                    throw new RuntimeException(e1);
                }
            }
            pages.put(url, this);
            saveCacheIfNeeded();
        }

        public void saveToProperties(Properties properties) {
            properties.put(SITE_URL + "|" + number + "|" + updateTime.toString() + "|" + processed + "|" + responseCode, url);
        }

        public boolean loadFromProperty(String name, Properties properties) {
            String[] nameParts = name.split("\\|");

            this.number = Integer.parseInt(nameParts[1]);
            if (nameParts.length == 4) {
                this.updateTime = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(nameParts[2]));
                this.processed = Boolean.parseBoolean(nameParts[3]);
                this.responseCode = 200;
            } else if (nameParts.length == 5) {
                this.updateTime = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(nameParts[2]));
                this.processed = Boolean.parseBoolean(nameParts[3]);
                this.responseCode = Integer.parseInt(nameParts[4]);
            } else {
                log.error("Unknown property {}", name);
                throw new RuntimeException("Unknown property " + name);
            }

            cacheNumber.set(Math.max(cacheNumber.get(), number+1));
            pages.put(url, this);
            if (!Files.exists(getSiteFile()) && this.responseCode == 200) {
                this.responseCode = 404;
            }
            return responseCode == 200;
        }

        public PageType getType() {
            return type;
        }

        public int getNumber() {
            return number;
        }

        public int getResponseCode() {
            return responseCode;
        }

        public Instant getUpdateTime() {
            return updateTime;
        }

        private Path getSiteFile() {
            return new File(cacheDir, "site." + number + ".txt").toPath();
        }

        public String getBody() {
            if (body == null) {
                try {
                    body = Files.readString(getSiteFile());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return body;
        }

        public boolean isProcessed() {
            return processed;
        }

        public void setProcessed(boolean processed) {
            this.processed = processed;
        }
    }

    enum PageType {
        cached, loaded
    }

    @FunctionalInterface
    public interface PageConsumer {
        void page(PageInfo pageInfo) throws Exception;
    }
}
