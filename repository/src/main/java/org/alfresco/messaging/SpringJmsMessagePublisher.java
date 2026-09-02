package org.alfresco.messaging;

import java.util.Collections;
import java.util.Map;

import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;
import org.springframework.jms.core.JmsTemplate;

public class SpringJmsMessagePublisher implements MessagePublisher
{
    static final String JMS_CORRELATION_ID = "JMSCorrelationID";
    static final String JMS_REPLY_TO = "JMSReplyTo";

    private final JmsTemplate queueTemplate;
    private final JmsTemplate topicTemplate;

    public SpringJmsMessagePublisher(JmsTemplate queueTemplate, JmsTemplate topicTemplate)
    {
        this.queueTemplate = queueTemplate;
        this.topicTemplate = topicTemplate;
    }

    @Override
    public void send(String endpointUri, String body, Map<String, Object> headers)
    {
        JmsEndpoint endpoint = JmsEndpointParser.parse(endpointUri);
        JmsTemplate template = endpoint.destinationType() == JmsEndpoint.DestinationType.TOPIC ? topicTemplate : queueTemplate;

        template.send(endpoint.destinationName(), session -> {
            TextMessage message = session.createTextMessage(body);
            applyHeaders(message, headers == null ? Collections.emptyMap() : headers);
            return message;
        });
    }

    private void applyHeaders(TextMessage message, Map<String, Object> headers) throws JMSException
    {
        for (Map.Entry<String, Object> header : headers.entrySet())
        {
            if (JMS_CORRELATION_ID.equals(header.getKey()))
            {
                message.setJMSCorrelationID((String) header.getValue());
            }
            else if (JMS_REPLY_TO.equals(header.getKey()))
            {
                message.setJMSReplyTo((Destination) header.getValue());
            }
            else
            {
                message.setObjectProperty(header.getKey(), header.getValue());
            }
        }
    }
}