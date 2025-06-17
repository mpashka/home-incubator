package org.mpashka.tests.misc.guice;

import com.google.inject.Module;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestGuice {

    private void test() {
        testContext("plain", new ParentContext());
        testContext("override-plain", Modules.override(new OverrideContext())
                .with(new ParentContext()));
        testContext("plain-override", Modules.override(new ParentContext())
                .with(new OverrideContext()));


    }

    private void testHierarchy() {
        Module override = Modules
                .override(new OverrideContext())
                .with(new Override2Context());

        Module middle = Modules.override(new ParentContext(), new ParentContext2())
                .with(override);

        Injector injector = Guice.createInjector(middle);
        MyBean testBean = injector.getInstance(Key.get(MyBean.class));
        MyBeanImpl testBeanImpl = injector.getInstance(Key.get(MyBeanImpl.class));

        log.info("Bean: {}, Impl: {}", testBean, testBeanImpl);
    }

    private void testContext(String name, Module context) {
        Injector injector = Guice.createInjector(context);
        String testBean = injector.getInstance(Key.get(String.class, Names.named("TestBean")));
        String prop = injector.getInstance(Key.get(String.class, Names.named("prop")));

        log.info("Test [{}]. Bean: {}, prop: {}", name, testBean, prop);
    }

    public static void main(String[] args) {
        new TestGuice().testHierarchy();
    }
}
