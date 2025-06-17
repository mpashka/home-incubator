package org.mpashka.tests.misc.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OverrideContext extends AbstractModule {
    @Provides
    @Singleton
    @Named("prop")
    public String providerBean() {
        log.info("Create overridden");
        return "overridden";
    }

    @Override
    protected void configure() {
    }
}
