package org.mpashka.totemftc.api;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.time.format.DateTimeParseException;

public class WebExceptionsMapper {
    private static final Logger log = LoggerFactory.getLogger(WebExceptionsMapper.class);

/*
    @ServerExceptionMapper
    public RestResponse<String> mapException(Throwable x) {
        log.error("Exception occured", x);
        return RestResponse.status(Response.Status.NOT_FOUND, "Exception: " + x);
    }

    @ServerExceptionMapper
    public RestResponse<String> mapException(DateTimeParseException x) {
        log.error("DateTimeParseException occured", x);
        return RestResponse.status(Response.Status.NOT_FOUND, "DateTimeParseException: " + x);
    }
*/
}
