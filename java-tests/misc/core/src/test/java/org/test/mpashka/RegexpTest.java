package org.test.mpashka;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegexpTest {

    private static final Logger log = LoggerFactory.getLogger(RegexpTest.class);

    @Test
    public void testRegexp() {
        Pattern NON_ASCII = Pattern.compile("[^\\p{Alnum}.\\-_]+");

        log.info("{}", NON_ASCII.matcher("my-msg-name  . ,(:)['] ").replaceAll("_"));
        log.info("{}", NON_ASCII.matcher("my-msg-name ,(:)['] ").replaceAll("_"));
        log.info("{}", Pattern.compile("(?<podId>[^/]+)").matcher("my-ms/g-name ,(:)['] ").replaceAll("_podId:${podId}_"));
        log.info("{}", Pattern.compile(
                "db/iss3/volumes/pod_(?<podId>[^/]+)_place/" +
                        "db/iss3/volumes/PAD-IT-(?<id2>[^\\u002F])+/pod_agent/public_volume/" +
                        "(?<file>eventlog|human_readable_current_spec.json|logs_transmitter_job_worker_eventlog|logs_transnmitter_job_worker_eventlog|tree_trace_eventlog)"
        ).matcher("db/iss3/volumes/pod_g-name ,(:)['] /_place/db/iss3/volumes/PAD-IT-fqwefqwe/pod_agent/public_volume/eventlog").replaceAll("_podId:${podId}_"));
    }
}
