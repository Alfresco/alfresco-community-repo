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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EventGeneratorDisabledTest extends AbstractContextAwareRepoEvent
{
    private static final String EVENT2_TOPIC_NAME = "alfresco.repo.event2";
    private static final String BROKER_URL = "tcp://localhost:61616";
   
    @Autowired @Qualifier("event2ObjectMapper")
    private ObjectMapper objectMapper;
    
    @Autowired
    protected ObjectMapper event2ObjectMapper;
    
    //private EventGenerator eventGenerator;

    private ActiveMQConnection connection;
    protected List<RepoEvent<?>> receivedEvents;
    

    @Before
    public void setup() throws Exception
    {        
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        connection = (ActiveMQConnection) connectionFactory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createTopic(EVENT2_TOPIC_NAME);
        MessageConsumer consumer = session.createConsumer(destination);

        receivedEvents = Collections.synchronizedList(new LinkedList<>());
        consumer.setMessageListener(new MessageListener()
        {
            @Override
            public void onMessage(Message message)
            {
                String text = getText(message);
                RepoEvent<?> event = toRepoEvent(text);
                receivedEvents.add(event);
            }

            private RepoEvent<?> toRepoEvent(String json)
            {
                try
                {
                    return objectMapper.readValue(json, RepoEvent.class);
                } catch (Exception e)
                {
                    e.printStackTrace();
                    return null;
                }
            }
        });
    }

    @After
    public void shutdownTopicListener() throws Exception
    {
        connection.close();
        connection = null;
    }

    @Test
    public void shouldNotReceiveEvent2EventsOnNodeCreation() throws Exception
    {
        if (eventGenerator.isEnabled())
        {
            eventGenerator.disable();
        }
        
        createNode(ContentModel.TYPE_CONTENT);

        Awaitility.await().pollDelay(6, TimeUnit.SECONDS).until(() -> receivedEvents.size() == 0);
        
        assertTrue(EVENT_CONTAINER.getEvents().size() == 0);
        assertTrue(receivedEvents.size() == 0);

        eventGenerator.enable();
        
    }
    
    @Test
    public void shouldReceiveEvent2EventsOnNodeCreation() throws Exception
    {
        if (!eventGenerator.isEnabled())
        {
            eventGenerator.enable();
        }
        
        createNode(ContentModel.TYPE_CONTENT);
        
        Awaitility.await().atMost(6, TimeUnit.SECONDS).until(() -> receivedEvents.size() == 1);
        
        assertTrue(EVENT_CONTAINER.getEvents().size() == 1);
        assertTrue(receivedEvents.size() == 1);

        RepoEvent<?> sent = getRepoEvent(1);
        RepoEvent<?> received = receivedEvents.get(0);
        assertEventsEquals("Events are different!", sent, received);
    }
    
    private void assertEventsEquals(String message, RepoEvent<?> expected, RepoEvent<?> current)
    {
        assertEquals(message, expected, current);
    }

    private static String getText(Message message)
    {
        try
        {
            ActiveMQTextMessage am = (ActiveMQTextMessage) message;
            return am.getText();
        } catch (JMSException e)
        {
            return null;
        }
    }

}
