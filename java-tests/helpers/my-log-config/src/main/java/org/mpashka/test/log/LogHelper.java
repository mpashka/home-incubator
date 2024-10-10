package org.mpashka.test.log;

import org.slf4j.bridge.SLF4JBridgeHandler;

public class LogHelper {
    /**
     * This installs redirect jul to slf4j
     * Another option is to specify `-Djava.util.logging.config.file`
     * ```
     * handlers = org.slf4j.bridge.SLF4JBridgeHandler
     * ```
     */
    public static boolean julToSlf4j() {
        // Optionally remove existing handlers attached to j.u.l root logger
        SLF4JBridgeHandler.removeHandlersForRootLogger();

        // add SLF4JBridgeHandler to j.u.l's root logger, should be done once during
        // the initialization phase of your application
        SLF4JBridgeHandler.install();
        return true;
    }
}
