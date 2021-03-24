package org.alfresco.repo.event2;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;

public class EventGeneratorTest extends AbstractContextAwareRepoEvent implements ExceptionListener
{

    @Test
    public void shouldReceiveEvent2Events() throws Exception
    {
        NodeRef nodeRef = createNode(ContentModel.TYPE_CONTENT);
        System.out.println(nodeRef);
        
        try
        {
            listen();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    public void listen() throws Exception
    {
        // Create a ConnectionFactory
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");

        // Create a Connection
        Connection connection = connectionFactory.createConnection();
        connection.start();

        connection.setExceptionListener(this);

        // Create a Session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create the destination (Topic or Queue)
        Destination destination = session.createQueue("alfresco.repo.event2");

        // Create a MessageConsumer from the Session to the Topic or Queue
        MessageConsumer consumer = session.createConsumer(destination);

        // Wait for a message
        Message message = consumer.receive(60000);

        System.out.println("Received: " + message);

        consumer.close();
        session.close();
        connection.close();
    }

    public synchronized void onException(JMSException ex)
    {
        System.out.println("JMS Exception occured.  Shutting down client.");
        ex.printStackTrace();
    }
}
