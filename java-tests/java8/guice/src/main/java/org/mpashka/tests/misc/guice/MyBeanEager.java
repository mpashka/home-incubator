package org.mpashka.tests.misc.guice;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyBeanEager {

    @Inject
    public MyBeanEager(
            MyBeanImpl myBeanImpl,
            MyBean myBean,
            @Named("Str1") String str1,
            @Named("Str2") String str2,
            @Named("Str3") String str3
    ) {

        log.info("Created eager: {} {} {} {} {}", myBeanImpl, myBean, str1, str2, str3);
    }
}
