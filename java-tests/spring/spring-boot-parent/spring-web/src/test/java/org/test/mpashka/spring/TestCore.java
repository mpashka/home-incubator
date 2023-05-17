package org.test.mpashka.spring;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplateHandler;

public class TestCore {
    private static final Logger logger = LoggerFactory.getLogger(TestCore.class);


    @Test
    public void testQueryBuilder() throws UnsupportedEncodingException {
//        logger.info("+ ecode: {}", UriUtils.encode("+", StandardCharsets.UTF_8));

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://aaa.my.com");
        builder.queryParam("p1", "a1");
        builder.queryParam("p1", "a1+");
        builder.queryParam("p1", "+a1");
        builder.queryParam("p1", "+");
        builder.queryParam("p1.1", "+");
//        builder.queryParam("p2", "=");
//        builder.queryParam("p3", "&");

        logger.info("No encode - No encode {}", builder.build(false));
        logger.info("No encode - Encode {}", builder.build(true));

        builder.encode();
        logger.info("Encode - No encode {}", builder.build(false));
        logger.info("Encode - Encode {}", builder.build(true));


//        logger.info("Encode + {}", URLEncoder.encode("+", StandardCharsets.UTF_8.name()));


        DefaultUriBuilderFactory uriFactory = new DefaultUriBuilderFactory();
        UriTemplateHandler uriTemplateHandler = uriFactory;

        for (DefaultUriBuilderFactory.EncodingMode value : DefaultUriBuilderFactory.EncodingMode.values()) {
            uriFactory.setEncodingMode(value);  // for backwards compatibility..
            logger.info("Expanded(mode:{}) {}", value, uriTemplateHandler.expand("http://aaa.m+y.com?p1={p1}&p1=a1+&p1=+a1&p1=+", Map.of("p1", "+a+=&1+")));
        }
    }
}
