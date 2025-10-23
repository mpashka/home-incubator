package org.mpashka.tests.misc.guice;

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
//        Provider<String> myInit2 = getProvider(Key.get(String.class, Names.named("my_init2")));
//        log.info("My init2: {}", myInit2);
//        bind(MyInit2.class).asEagerSingleton();
        bind(MyBeanEager.class).asEagerSingleton();

        binder().bind(MyBean.class).to(MyBeanImpl.class);
        log.info("After configure");
    }
}
