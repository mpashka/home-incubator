package org.mpashka.bw.txt;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UriTest {
    private static final Logger log = LoggerFactory.getLogger(UriTest.class);

    @Test
    public void testUri() throws Exception {
        URI uri = URI.create("http://user:passwd@local:123/dfsd?fff=dd#ggg");
        log.info("Host: {}, Authority: {}, User: {}, Port: {}, Path:{}, Query: {}, Fragment: {}",
                uri.getHost(), uri.getAuthority(), uri.getUserInfo(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
        log.info("SSP: {}", uri.getSchemeSpecificPart());

        uri = URI.create("http://local/dfsd?fff=dd#ggg");
        log.info("Host: {}, Authority: {}, User: {}, RawUser: {}, Port: {}",
                uri.getHost(), uri.getAuthority(), uri.getUserInfo(), uri.getRawUserInfo(), uri.getPort());


    }
}
