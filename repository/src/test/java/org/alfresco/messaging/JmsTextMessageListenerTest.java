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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;

import org.junit.Test;

public class JmsTextMessageListenerTest
{
    @Test
    public void dispatchesTextMessageBody() throws Exception
    {
        TextMessageHandler handler = mock(TextMessageHandler.class);
        TextMessage message = mock(TextMessage.class);
        when(message.getText()).thenReturn("payload");

        new JmsTextMessageListener(handler).onMessage(message);

        verify(handler).process("payload");
    }

    @Test
    public void rejectsNonTextMessage()
    {
        JmsTextMessageListener listener = new JmsTextMessageListener(mock(TextMessageHandler.class));

        assertThrows(IllegalArgumentException.class, () -> listener.onMessage(mock(Message.class)));
    }

    @Test
    public void wrapsTextReadFailure() throws Exception
    {
        TextMessage message = mock(TextMessage.class);
        when(message.getText()).thenThrow(new JMSException("read failed"));

        JmsTextMessageListener listener = new JmsTextMessageListener(mock(TextMessageHandler.class));

        assertThrows(IllegalStateException.class, () -> listener.onMessage(message));
    }

    @Test
    public void configuresQueueEndpoint()
    {
        EndpointMessageListenerContainer container = new EndpointMessageListenerContainer();

        container.setEndpointUri("jms:acs-repo-rendition-events?jmsMessageType=Text");

        assertEquals("acs-repo-rendition-events", container.getDestinationName());
        assertFalse(container.isPubSubDomain());
    }

    @Test
    public void configuresTopicEndpoint()
    {
        EndpointMessageListenerContainer container = new EndpointMessageListenerContainer();

        container.setEndpointUri("amqp:topic:alfresco.repo.event2");

        assertEquals("alfresco.repo.event2", container.getDestinationName());
        assertTrue(container.isPubSubDomain());
    }
}
