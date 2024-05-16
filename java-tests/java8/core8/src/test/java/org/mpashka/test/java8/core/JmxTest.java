package org.mpashka.test.java8.core;

import java.lang.management.ManagementFactory;

import com.sun.management.UnixOperatingSystemMXBean;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class JmxTest {

    private static final UnixOperatingSystemMXBean OS_MX_BEAN = (UnixOperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    @Test
    public void testFreeMemory() {
/* Returns the maximum amount of memory available to
   the Java Virtual Machine set by the '-mx' or '-Xmx' flags. */
        long xmx = Runtime.getRuntime().maxMemory();

/* Returns the total memory allocated from the system
   (which can at most reach the maximum memory value
   returned by the previous function). */
        long total = Runtime.getRuntime().totalMemory();

/* Returns the free memory *within* the total memory
   returned by the previous function. */
        long free = Runtime.getRuntime().freeMemory();

        log.info("max:{}, total:{}, free:{}", xmx, total, free);
    }

    @Test
    public void osInfoTest() {
        log.info("load: {}, time: {}, files: {}", OS_MX_BEAN.getProcessCpuLoad(), OS_MX_BEAN.getProcessCpuTime(),
                OS_MX_BEAN.getOpenFileDescriptorCount());
    }

}
