package org.mpashka.totemftc.api;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.ConfigValue;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class UtilsTest {
    private static final Logger log = LoggerFactory.getLogger(UtilsTest.class);

    @Test
    void testKeysIterate() {
        String clientId = "hello-world-prod";
        String[] clientIdParts = clientId.split("-");
        int mask = 1 << clientIdParts.length;
        int maxKeyLength = -1;
        String value = null;
        while (--mask >= 0) {
            List<String> key = new ArrayList<>();
            for (int i = 0; i < clientIdParts.length; i++) {
                boolean isSet = (mask & (1 << i)) != 0;
                if (isSet) {
                    key.add(clientIdParts[i]);
                }
            }
            if (maxKeyLength >= key.size()) {
                continue;
            }
//            log.debug("Key: {}", key);
            System.out.println("Key: " + key);

/*
            ConfigValue redirectUriConfig = ConfigProvider.getConfig().getConfigValue(keyFunction.apply(key));
            if (redirectUriConfig.getValue() != null) {
                value = redirectUriConfig.getValue();
                maxKeyLength = key.size();
            }
*/
        }
    }
}
