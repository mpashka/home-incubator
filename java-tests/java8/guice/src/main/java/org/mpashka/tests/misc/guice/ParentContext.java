package org.mpashka.tests.misc.guice;

import java.lang.annotation.Annotation;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParentContext extends AbstractModule {

    @Provides
    @Singleton
//    @Named("TestBean")
    public MyBeanImpl dependantBean(@Named("prop") String prop) {
        log.info("Ctx: {}, Prop: {}", this.hashCode(), prop);
        return new MyBeanImpl("Bean:" + prop);
    }

//    @Provides
//    @Singleton
//    public MyBean

    @Provides
    @Singleton
    @Named("prop")
    public String providerBean() {
        log.info("Create plain");
        return "original";
    }

    @Override
    protected void configure() {
        log.info("Before configure");
        bind(MyBeanEager.class).asEagerSingleton();

        binder().bind(MyBean.class).to(MyBeanImpl.class);
        log.info("After configure");
    }
}
