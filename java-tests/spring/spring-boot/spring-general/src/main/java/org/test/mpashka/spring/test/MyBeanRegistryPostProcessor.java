package org.test.mpashka.spring.test;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MyBeanRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    public MyBeanRegistryPostProcessor() {
//        log.info("MyBeanRegistryPostProcessor", new Throwable("MyBeanRegistryPostProcessor"));
    }

    @PostConstruct
    public void init() {
//        log.info("MyBeanRegistryPostProcessor.init", new Throwable("MyBeanRegistryPostProcessor.init"));
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
//        log.info("MyBeanRegistryPostProcessor.postProcessBeanDefinitionRegistry: {}", registry, new Throwable("MyBeanRegistryPostProcessor.postProcessBeanDefinitionRegistry"));
        registry.registerBeanDefinition("myCustomBean", new AnnotatedGenericBeanDefinition(MyCustomBean.class));
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
//        log.info("MyBeanRegistryPostProcessor.postProcessBeanFactory: {}", beanFactory, new Throwable("MyBeanRegistryPostProcessor.postProcessBeanFactory"));
    }
}
