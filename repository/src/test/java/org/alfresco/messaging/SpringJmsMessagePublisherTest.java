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