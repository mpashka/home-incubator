package org.mpashka.tests.misc.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParentContext2 extends AbstractModule {
    @Provides
    @Singleton
    @Named("Str1")
    public String dependantBean1(MyBean myBean) {
        log.info("Ctx: {}, Bean1: {}", this.hashCode(), myBean.hashCode());
        return "Bean1:" + myBean.hashCode();
    }

    @Provides
    @Singleton
    @Named("Str2")
    public String dependantBean2(MyBean myBean) {
        log.info("Ctx: {}, Bean2: {}", this.hashCode(), myBean.hashCode());
        return "Bean2:" + myBean.hashCode();
    }

    @Provides
    @Singleton
    @Named("Str3")
    public String dependantBean3(MyBean myBean) {
        log.info("Ctx: {}, Bean3: {}", this.hashCode(), myBean.hashCode());
        return "Bean3:" + myBean.hashCode();
    }

    @Override
    protected void configure() {
    }
}
