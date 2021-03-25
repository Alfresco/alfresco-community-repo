/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.event2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.event.databind.ObjectMapperFactory;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.DestinationSource;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.activemq.command.ActiveMQTopic;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EventGeneratorTest extends AbstractContextAwareRepoEvent
{
    private static final boolean DEBUG = true;

    private static final String EVENT2_TOPIC_NAME = "alfresco.repo.event2";

    private ActiveMQConnection connection;
    private ObjectMapper objectMapper;
    private List<RepoEvent<?>> receivedEvents;

    @Before
    public void startupTopicListener() throws Exception
    {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        connection = (ActiveMQConnection) connectionFactory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createTopic(EVENT2_TOPIC_NAME);
        MessageConsumer consumer = session.createConsumer(destination);

        objectMapper = ObjectMapperFactory.createInstance();
        receivedEvents = Collections.synchronizedList(new ArrayList<>());
        consumer.setMessageListener(new MessageListener()
        {
            @Override
            public void onMessage(Message message)
            {
                String text = getText(message);
                RepoEvent<?> event = toRepoEvent(text);

                if (DEBUG)
                {
                    System.err.println("Received message " + message + "\n" + text + "\n" + event);
                }

                receivedEvents.add(event);
            }

            private RepoEvent<?> toRepoEvent(String json)
            {
                try
                {
                    return objectMapper.readValue(json, RepoEvent.class);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return null;
                }
            }
        });

        if (DEBUG) System.err.println("Now actively listening on topic " + EVENT2_TOPIC_NAME);
    }

    @After
    public void shutdownTopicListener() throws Exception
    {
        connection.close();
        connection = null;
    }

    @Test
    public void shouldReceiveEvent2EventsOnNodeCreation() throws Exception
    {
        createNode(ContentModel.TYPE_CONTENT);

        int i = 100;
        while (--i > 0)
        {
            Thread.sleep(55l);
            if (receivedEvents.size() == 1)
                break;
        }

        assertFalse("No messages were received!", receivedEvents.isEmpty());

        RepoEvent<?> sent = getRepoEvent(1);
        RepoEvent<?> received = receivedEvents.get(0);

        assertEquals("Events are different!", sent, received);
    }

    private static String getText(Message message)
    {
        try
        {
            ActiveMQTextMessage am = (ActiveMQTextMessage) message;
            return am.getText();
        }
        catch (JMSException e)
        {
            return null;
        }
    }

    // a simple main to investigate he contents of the local broker
    public static void main(String[] args) throws Exception
    {
        dumpBroker("tcp://localhost:61616");
        System.exit(0);
    }

    private static void dumpBroker(String url) throws Exception
    {
        System.out.println("Broker at url: '" + url + "'");

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        ActiveMQConnection connection = (ActiveMQConnection) connectionFactory.createConnection();
        try
        {
            connection.start();

            DestinationSource ds = connection.getDestinationSource();

            Set<ActiveMQQueue> queues = ds.getQueues();
            System.out.println("\nFound " + queues.size() + " queues:");
            for (ActiveMQQueue queue : queues)
            {
                try
                {
                    System.out.println("- " + queue.getQueueName());
                }
                catch (JMSException e)
                {
                    e.printStackTrace();
                }
            }

            Set<ActiveMQTopic> topics = ds.getTopics();
            System.out.println("\nFound " + topics.size() + " topics:");
            for (ActiveMQTopic topic : topics)
            {
                try
                {
                    System.out.println("- " + topic.getTopicName());
                }
                catch (JMSException e)
                {
                    e.printStackTrace();
                }
            }
            
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(EVENT2_TOPIC_NAME);
            MessageConsumer consumer = session.createConsumer(destination);

            System.out.println("\nListening to topic "+EVENT2_TOPIC_NAME+"...");
            consumer.setMessageListener(new MessageListener()
            {
                @Override
                public void onMessage(Message message)
                {
                    String text = getText(message);
                    System.out.println("Received message " + message + "\n" + text+"\n");
                }
            });
            
            Thread.sleep(50000000l);
        }
        finally
        {
            connection.close();
        }
    }
}
