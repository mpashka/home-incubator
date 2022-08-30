package org.tests.jms.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.Enumeration;

public class ConsumerMessageListener implements MessageListener {
    private static final Logger log = LoggerFactory.getLogger(ConsumerMessageListener.class);

    private String consumerName;
    public ConsumerMessageListener(String consumerName) {
        this.consumerName = consumerName;
    }

    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        try {
            log.info("{} received {}", consumerName,textMessage.getText());
            Enumeration propertyNames = textMessage.getPropertyNames();
            while (propertyNames.hasMoreElements()) {
                String name = propertyNames.nextElement().toString();
                log.info("    {}={}", name, textMessage.getObjectProperty(name));
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
