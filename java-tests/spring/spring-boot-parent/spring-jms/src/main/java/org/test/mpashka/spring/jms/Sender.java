package org.test.mpashka.spring.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.context.LifecycleAutoConfiguration;
import org.springframework.jms.core.JmsTemplate;

import javax.annotation.PostConstruct;
import javax.jms.Destination;

//@Component
public class Sender extends LifecycleAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(Sender.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    @Qualifier("geps.jms.queue.in.smev3.epgu.2.0.3.request")
    private Destination destination;


//    @PostConstruct
    private void init() {
        Thread thread = new Thread(() -> {
            log.info("Start sending messages...");
            while(true) {
                try {
                    Thread.sleep(1000);
                    sendMessage("Hello");
                } catch (Exception e) {
                    log.error("Error", e);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void sendMessage(Object body) {
        /*
                            CollectionUtils.mapOfNullable(
                            new String[] {JmsConstants.SRC_MESSAGE_ID, JmsConstants.GEPS_MESSAGE_ID},
                            new String[] {readNotifyData.getSrcMessageId(), Objects.toString(readNotifyData.getMessageId(), null)}));

         */

        jmsTemplate.convertAndSend(destination, body, jmsMessage -> {
//            Message jmsMessage = session.createTextMessage(body);
//            setHeaders(jmsMessage, headers);
//            postProcessMessage(jmsMessage);
            return jmsMessage;
        });
    }
}