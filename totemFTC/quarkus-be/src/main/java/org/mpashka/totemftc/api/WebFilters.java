package org.mpashka.totemftc.api;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.runtime.configuration.ProfileManager;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerResponseFilter;

import javax.annotation.PostConstruct;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class WebFilters {

    private static final List<String> TYPE_JSON = Collections.singletonList(MediaType.APPLICATION_JSON);
    private static final String TYPE_JSON_UTF8 = MediaType.APPLICATION_JSON_TYPE.withCharset(StandardCharsets.UTF_8.name()).toString();

/*

todo make this configurable

    private String indexHtml;

    @PostConstruct
    void init() throws IOException {
        InputStream in = WebFilters.class.getResourceAsStream("/META-INF/resources/index.html");
        if (in == null) {
            throw new RuntimeException("Index.html not found");
        }
        StringWriter out = new StringWriter(1000);
        new InputStreamReader(in).transferTo(out);
        indexHtml = out.toString();
    }

    @ServerResponseFilter
    public void getFilter(ContainerResponseContext responseContext) {
        if (responseContext.getStatus() == HttpResponseStatus.NOT_FOUND.code() && ProfileManager.getActiveProfile().equals("prod")) {
            responseContext.setStatusInfo(RestResponse.Status.OK);
            responseContext.setEntity(indexHtml, null, MediaType.TEXT_HTML_TYPE);
        }
    }
*/

    @ServerResponseFilter
    public void charsetFilter(ContainerResponseContext responseContext) {
        if (responseContext.getStatus() == HttpResponseStatus.OK.code() && TYPE_JSON.equals(responseContext.getHeaders().get(HttpHeaders.CONTENT_TYPE))) {
            responseContext.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, TYPE_JSON_UTF8);
        }
    }


}
