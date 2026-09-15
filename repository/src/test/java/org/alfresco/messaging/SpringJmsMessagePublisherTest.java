/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.messaging;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import jakarta.jms.Destination;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

@RunWith(MockitoJUnitRunner.class)
public class SpringJmsMessagePublisherTest
{
    @Mock
    private JmsTemplate queueTemplate;
    @Mock
    private JmsTemplate topicTemplate;
    @Mock
    private Session session;
    @Mock
    private TextMessage textMessage;
    @Mock
    private Destination replyTo;
    @Captor
    private ArgumentCaptor<MessageCreator> messageCreatorCaptor;

    private SpringJmsMessagePublisher publisher;

    @Before
    public void setUp()
    {
        publisher = new SpringJmsMessagePublisher(queueTemplate, topicTemplate);
    }

    @Test
    public void publishesQueueEndpointAsTextMessage() throws Exception
    {
        when(session.createTextMessage("request")).thenReturn(textMessage);

        publisher.send("jms:transform-requests?jmsMessageType=Text", "request", null);

        verify(queueTemplate).send(eq("transform-requests"), messageCreatorCaptor.capture());
        verify(topicTemplate, never()).send(eq("transform-requests"), messageCreatorCaptor.capture());
        messageCreatorCaptor.getValue().createMessage(session);
        verify(session).createTextMessage("request");
    }

    @Test
    public void publishesEvent2EndpointToTopic()
    {
        publisher.send("amqp:topic:alfresco.repo.event2", "event", null);

        verify(topicTemplate).send(eq("alfresco.repo.event2"), messageCreatorCaptor.capture());
        verify(queueTemplate, never()).send(eq("alfresco.repo.event2"), messageCreatorCaptor.capture());
    }

    @Test
    public void mapsJmsHeadersAndApplicationProperties() throws Exception
    {
        when(session.createTextMessage("reply")).thenReturn(textMessage);
        Map<String, Object> headers = Map.of(
                SpringJmsMessagePublisher.JMS_REPLY_TO, replyTo,
                SpringJmsMessagePublisher.JMS_CORRELATION_ID, "request-id",
                "JMS_AMQP_MESSAGE_FORMAT", (short) 0);

        publisher.send("jms:transform-replies", "reply", headers);
        verify(queueTemplate).send(eq("transform-replies"), messageCreatorCaptor.capture());
        messageCreatorCaptor.getValue().createMessage(session);

        verify(textMessage).setJMSReplyTo(replyTo);
        verify(textMessage).setJMSCorrelationID("request-id");
        verify(textMessage).setObjectProperty("JMS_AMQP_MESSAGE_FORMAT", (short) 0);
    }
}
