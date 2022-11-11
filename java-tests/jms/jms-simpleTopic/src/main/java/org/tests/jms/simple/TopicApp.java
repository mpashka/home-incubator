package org.tests.jms.simple;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;
import java.net.URISyntaxException;
import java.util.Scanner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TopicApp {
    public static void main(String[] args) throws URISyntaxException, Exception {
        Connection connection = null;
        try {
            // Producer
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616", "local", "local");
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
//            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic("customerTopic");

            // Consumer1 subscribes to customerTopic
            MessageConsumer consumer1 = session.createConsumer(topic);
            consumer1.setMessageListener(new ConsumerMessageListener("Consumer1"));

            // Consumer2 subscribes to customerTopic
            MessageConsumer consumer2 = session.createConsumer(topic);
            consumer2.setMessageListener(new ConsumerMessageListener("Consumer2"));

            connection.start();

            boolean a = true;

            log.info("Enter message to send. Enter 'done' for the end.");
            Scanner scanner = new Scanner(System.in);
            while (a) {
                String s = scanner.nextLine();
                if ("done".equals(s)) {
                    a = false;
                }

                // Publish
                String payload = "Important Task " + s;
                Message msg = session.createTextMessage(payload);
                MessageProducer producer = session.createProducer(topic);
                log.info("Sending text '{}'", payload);
                producer.send(msg);
                session.commit();
            }

            Thread.sleep(3000);
            session.close();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }}
