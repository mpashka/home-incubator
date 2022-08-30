package org.test.mpashka.spring.jms;

import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

import javax.jms.ConnectionFactory;

@Configuration(proxyBeanMethods = false)
public class MyJmsConfiguration {

//    /**
//     * id="geps.jms.queue.out.read.notify"
//     */
//    @Bean(name = "geps.jms.queue.out.read.notify")
//    public ActiveMQQueue destination() {
////                geps.jms.queue.out.read.notify.name
//        return new ActiveMQQueue("jms.queue.geps.out.read.notify");
//    }

//    @Bean
//    public DefaultJmsListenerContainerFactory myFactory(DefaultJmsListenerContainerFactoryConfigurer configurer) {
//        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
////        ConnectionFactory connectionFactory = getCustomConnectionFactory();
//        ConnectionFactory connectionFactory = getCustomConnectionFactory();
//        configurer.configure(factory, connectionFactory);
//        factory.setMessageConverter(new MyMessageConverter());
//        return factory;
//    }

//    private ConnectionFactory getCustomConnectionFactory() {
//        return ...
//    }

}