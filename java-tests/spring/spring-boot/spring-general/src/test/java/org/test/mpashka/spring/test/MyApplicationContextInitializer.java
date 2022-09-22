package org.test.mpashka.spring.test;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.event.PrepareTestInstanceEvent;
import org.springframework.test.context.event.TestContextEvent;

import lombok.extern.slf4j.Slf4j;

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
