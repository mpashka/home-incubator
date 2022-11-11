package org.tests.jms.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.Enumeration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumerMessageListener implements MessageListener {
    public static final String RETRY = "Important Task retry";
    private int retry;

    private String consumerName;
    public ConsumerMessageListener(String consumerName) {
        this.consumerName = consumerName;
    }

    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        try {
            String text = textMessage.getText();
            log.info("{} received {}", consumerName, text);
            Enumeration<?> propertyNames = textMessage.getPropertyNames();
            while (propertyNames.hasMoreElements()) {
                String name = propertyNames.nextElement().toString();
                log.info("    {}={}", name, textMessage.getObjectProperty(name));
            }
            if (text.startsWith(RETRY)) {
                int msgRetry = Integer.parseInt(text.substring(RETRY.length()).trim());
                log.info("Retry {} -> {}", retry, msgRetry);
                if (retry < msgRetry) {
                    throw new RuntimeException("Retry " + retry++);
                }
            }
        } catch (JMSException e) {
            log.error("Unexpected error", e);
        }
    }
}
