package org.tests.jms.simple;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQDestination;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.net.URISyntaxException;
import java.util.Scanner;

public class PubSubApp {
    private static final Logger log = LoggerFactory.getLogger(PubSubApp.class);

    public static void main(String[] args) throws URISyntaxException, Exception {
        log.info("Pub sub app");
        String queue = "jms.queue.geps.out.read.notify";

        Connection connection = null;
        try {
            // Producer
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616", "local", "local");
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            ActiveMQQueue pubSub = ActiveMQDestination.createQueue(queue);
            MessageConsumer topic = session.createConsumer(pubSub, "GepsInstanceCode = 'geps'");

            // Consumer1 subscribes to customerTopic
//            MessageConsumer consumer1 = session.createConsumer(topic);
            topic.setMessageListener(new ConsumerMessageListener("Consumer1"));

            // Consumer2 subscribes to customerTopic
//            MessageConsumer consumer2 = session.createConsumer(topic);
//            consumer2.setMessageListener(new ConsumerMessageListener("Consumer2"));

            connection.start();

            boolean a = true;

            Scanner scanner = new Scanner(System.in);
            while (a) {
                log.info("Press enter to send message or 'stop' to quit");
                if ("stop".equals(scanner.nextLine())) {
                    break;
                }

                // Publish
                String payload = "Important Task";
                Message msg = session.createTextMessage(payload);
                msg.setStringProperty("GepsInstanceCode", "geps");
                MessageProducer producer = session.createProducer(pubSub);
                log.info("Sending text '{}'", payload);
                producer.send(msg);
            }

            session.close();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}
