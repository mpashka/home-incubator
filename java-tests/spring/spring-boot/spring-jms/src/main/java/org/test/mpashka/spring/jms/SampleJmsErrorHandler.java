package org.test.mpashka.spring.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ErrorHandler;

public class SampleJmsErrorHandler implements ErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(SampleJmsErrorHandler.class);

    @Override
    public void handleError(Throwable t) {
        log.warn("In default jms error handler...");
        log.error("Error Message : {}", t.getMessage());
    }}
