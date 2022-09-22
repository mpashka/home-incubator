package org.test.mpashka.spring.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.jms.support.converter.MessageType;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;

@SpringBootApplication
@ImportResource({"classpath:context-jms.xml"})
@EnableJms
public class JmsApp {
    private static final Logger log = LoggerFactory.getLogger(JmsApp.class);

/*
    @Bean
    public JmsListenerContainerFactory<?> myFactory(ConnectionFactory connectionFactory,
                                                    DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        // This provides all boot's default to this factory, including the message converter
        configurer.configure(factory, connectionFactory);
        // You could still override some of Boot's default if necessary.
        return factory;
    }
*/

    @Bean // Serialize message content to json using TextMessage
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }


    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(JmsApp.class, args);
        JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);

        // Send a message with a POJO - the template reuse the message converter
        log.info("Sending an email message.");
        jmsTemplate.convertAndSend("jms.queue.geps.out.read.notify", "Hello", message -> {
            message.setStringProperty("GepsInstanceCode", "geps");
            return message;
        });

        log.info("Sending an smev message.");
        jmsTemplate.convertAndSend("jms.queue.geps.in.smev3.epgu.2.0.3.request", "Hello from SMEV3!", message -> {
//            message.setStringProperty("GepsInstanceCode", "geps");
            return message;
        });
    }

//    @Bean
//    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
//        return args -> {
//
//            log.info("Let's inspect the beans provided by Spring Boot:");
//
//            String[] beanNames = ctx.getBeanDefinitionNames();
//            Arrays.sort(beanNames);
//            for (String beanName : beanNames) {
//                log.info("    {}", beanName);
//            }
//        };
//    }

    @Bean
    public CommandLineRunner commandLineRunner(JmsTemplate jmsTemplate, @Qualifier("geps.jms.queue.in.smev3.epgu.2.0.3.request") Destination destination) {
        return args -> {
            log.info("Send to {}", destination);

            jmsTemplate.convertAndSend(destination, "Hello SMEV3", jmsMessage -> {
//            Message jmsMessage = session.createTextMessage(body);
//            setHeaders(jmsMessage, headers);
//            postProcessMessage(jmsMessage);
                return jmsMessage;
            });

        };
    }

}
