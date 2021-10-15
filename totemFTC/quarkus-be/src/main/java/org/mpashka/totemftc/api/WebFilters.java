package org.mpashka.totemftc.api;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.arc.profile.IfBuildProfile;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerResponseFilter;

import javax.annotation.PostConstruct;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

public class WebFilters {

    private String indexHtml;

    @PostConstruct
    @IfBuildProfile("prod")
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
        if (responseContext.getStatus() == HttpResponseStatus.NOT_FOUND.code()) {
            responseContext.setStatusInfo(RestResponse.Status.OK);
            responseContext.setEntity(indexHtml, null, MediaType.TEXT_HTML_TYPE);
        }
    }
}
