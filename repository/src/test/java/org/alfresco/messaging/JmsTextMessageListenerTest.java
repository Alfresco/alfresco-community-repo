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