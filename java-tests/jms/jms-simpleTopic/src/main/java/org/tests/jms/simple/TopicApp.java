package org.tests.jms.simple;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;
import java.net.URISyntaxException;
import java.util.Scanner;

public class TopicApp {
    public static void main(String[] args) throws URISyntaxException, Exception {
        Connection connection = null;
        try {
            // Producer
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616", "local", "local");
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic("customerTopic");

            // Consumer1 subscribes to customerTopic
            MessageConsumer consumer1 = session.createConsumer(topic);
            consumer1.setMessageListener(new ConsumerMessageListener("Consumer1"));

            // Consumer2 subscribes to customerTopic
            MessageConsumer consumer2 = session.createConsumer(topic);
            consumer2.setMessageListener(new ConsumerMessageListener("Consumer2"));

            connection.start();

            boolean a = true;

            Scanner scanner = new Scanner(System.in);
            while (a) {
                // Publish
                String payload = "Important Task";
                Message msg = session.createTextMessage(payload);
                MessageProducer producer = session.createProducer(topic);
                System.out.println("Sending text '" + payload + "'");
                producer.send(msg);

                scanner.nextLine();
            }

            Thread.sleep(3000);
            session.close();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }}
