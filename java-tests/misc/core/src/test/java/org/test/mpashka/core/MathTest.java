package org.test.mpashka.core;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class MathTest {

    @Test
    public void testBucketCount() {
        for (int a: new int[]{1,10,20,21,22,25,30,50, 100,1000}) {
            log.info("b({}) = {}", a, buckets(a));
        }
    }

    @Test
    public void testCast() {
        log.info("r(0.5):{}", Math.round(0.5));
        log.info("1/2:{}", 1/2);
        log.info("r(1/2):{}", Math.round((double) 1/2));
        log.info("(d)1/2:{}", (double) 1/2);
        log.info("r(0+1/2):{}", Math.round((double) 0 + 1/2));
    }

    private int buckets(long avg) {
        int HISTOGRAM_BUCKETS_COUNT = 20;
        double MIN_HISTOGRAM_BASE = 1.41;

        double base = Math.pow(avg, 2. / (HISTOGRAM_BUCKETS_COUNT - 2));
        int bucketsCount = HISTOGRAM_BUCKETS_COUNT;
        if (base < MIN_HISTOGRAM_BASE) {
            base = MIN_HISTOGRAM_BASE;
            long max = Math.min(avg * avg, avg*100);
            bucketsCount = (int) Math.floor(Math.log(max) / Math.log(base) + 3);
//            log.info("max({}): {}", avg, max);
        }
        return bucketsCount;
    }

    /*
        int bucketsCount = MonitoringConstants.HISTOGRAM_BUCKETS_COUNT;
        if (base < MonitoringConstants.MIN_HISTOGRAM_BASE) {
            base = MonitoringConstants.MIN_HISTOGRAM_BASE;
            long max = Math.min(avg * avg, avg*100);
            bucketsCount = (int) Math.floor(Math.log(max) / Math.log(base) + 3);
        }
        HistogramCollector exponential = Histograms.exponential(bucketsCount, base);
     */
}
