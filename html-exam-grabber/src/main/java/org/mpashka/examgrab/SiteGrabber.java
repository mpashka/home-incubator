package org.mpashka.examgrab;

import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SiteGrabber {

    final Logger log = LoggerFactory.getLogger(getClass());

    public static final Pattern tr = Pattern.compile("<tr.*?>(.*?)</tr\\s*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    public static final Pattern trStart = Pattern.compile("<tr.*?>", Pattern.CASE_INSENSITIVE);
    public static final Pattern th = Pattern.compile("<th.*?>(.*?)</th\\s*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    public static final Pattern td = Pattern.compile("<td.*?>(.*?)</td\\s*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    public static final Pattern a = Pattern.compile("<a.*?href=\"(.*?)\".*?>(.*?)</a\\s*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);


    private String name;
    Saver saver;
    Grabber grabber;

    public SiteGrabber(String name, Saver saver, Grabber grabber) {
        this.name = name;
        this.saver = saver;
        this.grabber = grabber;
    }

    public String getName() {
        return name;
    }

    public abstract HttpRequest request(String url) throws URISyntaxException;

    public abstract int grab(String data, Saver.Course course) throws Exception;

    List<String> parseRow(Pattern t, String str) {
        Matcher matcher = t.matcher(str);
        List<String> columns = new ArrayList<>();
        while (matcher.find()) {
            columns.add(matcher.group(1).trim());
        }
        return columns;
    }

    void waitIfLoaded(Grabber.PageInfo pageInfo) throws InterruptedException {
        if (pageInfo.getType() == Grabber.PageType.loaded) {
            Thread.sleep(Math.round(1000 + 3000 * Math.random()));
        }
    }

}
