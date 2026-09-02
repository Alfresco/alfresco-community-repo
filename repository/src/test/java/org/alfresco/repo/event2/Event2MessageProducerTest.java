package org.alfresco.repo.event2;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.alfresco.messaging.MessagePublisher;
import org.alfresco.repo.event.v1.model.RepoEvent;

@RunWith(MockitoJUnitRunner.class)
public class Event2MessageProducerTest
{
    private static final String ENDPOINT = "amqp:topic:alfresco.repo.event2";

    @Mock
    private MessagePublisher publisher;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private RepoEvent<?> event;

    private Event2MessageProducer producer;

    @Before
    public void setUp() throws Exception
    {
        producer = new Event2MessageProducer();
        producer.setPublisher(publisher);
        producer.setEndpoint(ENDPOINT);
        producer.setObjectMapper(objectMapper);
        producer.afterPropertiesSet();
    }

    @Test
    public void serializesAndPublishesEvent() throws Exception
    {
        when(objectMapper.writeValueAsString(event)).thenReturn("{\"type\":\"NODE_CREATED\"}");

        producer.send(event);

        verify(publisher).send(eq(ENDPOINT), eq("{\"type\":\"NODE_CREATED\"}"),
                argThat(headers -> headers.get("JMS_AMQP_MESSAGE_FORMAT").equals((short) 0)));
    }
}