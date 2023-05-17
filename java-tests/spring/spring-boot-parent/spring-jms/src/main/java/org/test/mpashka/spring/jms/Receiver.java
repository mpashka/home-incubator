package org.test.mpashka.spring.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.lang.NonNull;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Component
public class Receiver {

    private static final Logger log = LoggerFactory.getLogger(Receiver.class);

/*
    @JmsListener(id="geps.jms.listener.out.read.notify",
            destination = "geps.jms.queue.out.read.notify",
            selector = "${geps.jms.instance.code.selector}",
            containerFactory = "geps.core.jms.feedContainerFactory",
            concurrency = "${geps.jms.queue.out.read.notify.listener.concurrency:1-10}")

    ReadNotificationHandler
    public void notifySenderCallback(Message jmsMessage, @Payload final ReadNotifyData notifyData) {

    @Bean(name = "geps.smvev3.jmsMessageConverter")
    public TypedMappingJackson2MessageConverter typedMappingJackson2MessageConverter() {
        return new TypedMappingJackson2MessageConverter(ru.gosuslugi.geps.smev3.dto.JmsEpguRequestResponse.class, StandardCharsets.UTF_8.name(), MessageType.TEXT);
    }



    <bean id="jmsMessageConverter" class="org.springframework.jms.support.converter.MappingJackson2MessageConverter"
          p:objectMapper-ref="geps.common.beans.objectMapper"
          p:typeIdPropertyName="${geps.common.queue.typeHeader:javaType}"
          p:encoding="UTF-8"
          p:targetType="TEXT" />

    <bean abstract="true" id="abstract.jmsListenerContainerFactory" class="org.springframework.jms.config.DefaultJmsListenerContainerFactory"
        p:connectionFactory-ref="jmsConnectionFactory"
        p:destinationResolver-ref="destinationResolver"
        p:messageConverter-ref="jmsMessageConverter"
        p:errorHandler-ref="geps.core.jms.errorHandler"
        p:sessionTransacted="true" />

    <bean id="geps.core.jms.feedContainerFactory" class="org.springframework.jms.config.DefaultJmsListenerContainerFactory"
        parent="abstract.jmsListenerContainerFactory"
        p:concurrency="${geps.jms.queue.feed.executor.concurrency:20}" />


    <bean id="geps.core.jms.errorHandler" class="ru.gosuslugi.geps.jms.ErrorLogErrorHandler" />

*/

    @JmsListener(
            id="geps.jms.listener.out.read.notify",
//            destination = "mailbox"
            destination = "geps.jms.queue.out.read.notify",
//            selector = "${geps.jms.instance.code.selector:'GepsInstanceCode = geps'}",
            selector = "GepsInstanceCode = 'geps'",
            containerFactory = "myContainerFactory",
            concurrency = "1-10"
    )
    public void receiveMessage(Message jmsMessage, @Payload String body) throws JMSException {
        Map<String, Object> properties = new HashMap<>();
        Enumeration propertyNames = jmsMessage.getPropertyNames();
        while (propertyNames.hasMoreElements()) {
            String name = propertyNames.nextElement().toString();
            Object objectProperty = jmsMessage.getObjectProperty(name);
            properties.put(name + "[" + name.length() + "]", objectProperty.getClass().getSimpleName() + ":'" + objectProperty + "'");
        }
        log.info("Received <{}>. Properties: {}", body, properties);
    }

    @JmsListener(
            id = "geps.jms.queue.in.smev3.registryHandler.2.0.3_test",
            destination = "geps.jms.queue.in.smev3.epgu.2.0.3.request",
            containerFactory = "myContainerFactory",
//            containerFactory = "geps.smev3.jmsListenerContainerFactory",
            concurrency = "1-5"
    )
    public void processSmev3RequestMessage(@NonNull TextMessage textMessage) throws JMSException, IOException {
        log.info("EPGU SMEV3 203 in request received: {} / [{}]", textMessage.getText(), textMessage);
    }
}
