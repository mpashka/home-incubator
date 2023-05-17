package org.test.mpashka.spring.test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.event.PrepareTestInstanceEvent;
import org.springframework.test.context.event.TestContextEvent;
import org.springframework.test.context.support.TestPropertySourceUtils;

import lombok.extern.slf4j.Slf4j;

import static org.springframework.test.context.support.TestPropertySourceUtils.INLINED_PROPERTIES_PROPERTY_SOURCE_NAME;

@Slf4j
public class MyApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public MyApplicationContextInitializer() {
//        log.info("MyApplicationContextInitializer", new Throwable("MyApplicationContextInitializer"));
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
//        log.info("MyApplicationContextInitializer.initialize: {}", applicationContext, new Throwable("MyApplicationContextInitializer.initialize"));

        applicationContext.addApplicationListener(e -> {
//            log.info("Event received: {}", e, new Throwable("Event received: " + e));
            if (e instanceof TestContextEvent t) {
                TestContext testContext = t.getTestContext();
//                log.info("    Test context: {}", testContext.getApplicationContext());

                if (t instanceof PrepareTestInstanceEvent tp) {
//                    applicationContext.getBeanFactory().registerSingleton("MyTestBean", new MyTestBean());
                    MyFactoryBean bean = applicationContext.getBeanFactory().getBean("&myBean", MyFactoryBean.class);
                    TestContext tc = tp.getTestContext();
                    bean.setMyProducedBean(new MyFactoryBeanData(tc.getTestClass().getSimpleName()));
                }
            }
        });

        AnnotatedGenericBeanDefinition beanDefinition = new AnnotatedGenericBeanDefinition(MyFactoryBean.class);
        beanDefinition.setScope(ConfigurableBeanFactory.SCOPE_PROTOTYPE);
        ((BeanDefinitionRegistry) applicationContext.getBeanFactory()).registerBeanDefinition("myBean", beanDefinition);


        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext, "my.test-props.array-of-objects[0].int-prop=1");
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext, "my.test-props.single-object.str-prop=replaced_first");
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext, "my.test-props.int-prop=1");

//        extracted(applicationContext, "application.yml");
    }

    private static void extracted(ConfigurableApplicationContext applicationContext, String prefix) {
        MutablePropertySources propertySources = applicationContext.getEnvironment().getPropertySources();
/*
        propertySources.stream().filter(p -> p.getName().contains("'class path resource [" + name + "]'"))
                .map(PropertySource::getSource)
                .filter(s -> s instanceof Map)
                .map(s -> (Map) s)
                .forEach();
        PropertySource<?> propertySource = propertySources.get("Config resource 'class path resource [application.yml]' via location 'optional:classpath:/'");
*/
        MapPropertySource ps0 = (MapPropertySource) applicationContext.getEnvironment().getPropertySources().get(INLINED_PROPERTIES_PROPERTY_SOURCE_NAME);
        if (ps0 == null) {
            ps0 = new MapPropertySource(INLINED_PROPERTIES_PROPERTY_SOURCE_NAME, new LinkedHashMap<>());
            applicationContext.getEnvironment().getPropertySources().addFirst(ps0);
        }
        MapPropertySource ps = ps0;
        propertySources.stream().filter(p -> p instanceof EnumerablePropertySource)
                .map(p -> (EnumerablePropertySource<?>) p)
                .flatMap(p ->
                    Arrays.stream(p.getPropertyNames())
                            .filter(n -> n.startsWith(prefix))
                            .map(n -> Map.entry(n, p.getProperty(n)))
                ).forEach(e -> ps.getSource().put(e.getKey(), e.getValue()));
//        PropertySource<?> propertySource = propertySources.get("Config resource 'class path resource [application.yml]' via location 'optional:classpath:/'");
//        propertySource.getSource()
    }

    public static class MyFactoryBeanData {
        private String name;

        public MyFactoryBeanData(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "MyFactoryBeanData{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }


    //@Component
    //@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Slf4j
    public static class MyFactoryBean implements FactoryBean<MyFactoryBeanData> {

        private MyFactoryBeanData myProducedBean = new MyFactoryBeanData("none");

        public void setMyProducedBean(MyFactoryBeanData myProducedBean) {
            this.myProducedBean = myProducedBean;
        }

        @Override
        public MyFactoryBeanData getObject() throws Exception {
            log.info("Produce bean: {}", myProducedBean);
            return myProducedBean;
        }

        @Override
        public Class<?> getObjectType() {
            return MyFactoryBeanData.class;
        }

    }
}
