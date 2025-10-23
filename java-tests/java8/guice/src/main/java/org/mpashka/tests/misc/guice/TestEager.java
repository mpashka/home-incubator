package org.mpashka.tests.misc.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestEager {
    public static class TestEagerContext extends AbstractModule {
/*
        @Provides
        @Singleton
//    @Named("my_init2")
        public MyInit2 myInitBean2() {
            log.info("My init bean 2");
            return new MyInit2();
        }
*/

        @Override
        protected void configure() {
            bind(MyInit2.class).asEagerSingleton();
        }
    }

    public static class MyInit2 {
        public MyInit2() {
            log.info("My init 2...");
        }
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new TestEagerContext());
        log.info("Done");
    }
}
