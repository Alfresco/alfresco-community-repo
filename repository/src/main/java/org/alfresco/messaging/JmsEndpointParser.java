package org.alfresco.messaging;

import static org.alfresco.messaging.JmsEndpoint.DestinationType.QUEUE;
import static org.alfresco.messaging.JmsEndpoint.DestinationType.TOPIC;

import java.util.HashSet;
import java.util.Set;

public final class JmsEndpointParser
{
    private static final String JMS_PREFIX = "jms:";
    private static final String AMQP_QUEUE_PREFIX = "amqp:queue:";
    private static final String AMQP_TOPIC_PREFIX = "amqp:topic:";

    private static final String JMS_MESSAGE_TYPE = "jmsMessageType";
    private static final String PRESERVE_MESSAGE_QOS = "preserveMessageQos";

    private JmsEndpointParser()
    {}

    public static JmsEndpoint parse(String endpointUri)
    {
        if (endpointUri == null || endpointUri.isBlank())
        {
            throw new IllegalArgumentException("JMS endpoint must not be blank");
        }

        String[] endpointParts = endpointUri.split("\\?", 2);
        String destination = endpointParts[0];
        if (endpointParts.length == 2)
        {
            validateOptions(endpointParts[1], endpointUri);
        }

        if (destination.startsWith(AMQP_TOPIC_PREFIX))
        {
            return endpoint(destination.substring(AMQP_TOPIC_PREFIX.length()), TOPIC, endpointUri);
        }
        if (destination.startsWith(AMQP_QUEUE_PREFIX))
        {
            return endpoint(destination.substring(AMQP_QUEUE_PREFIX.length()), QUEUE, endpointUri);
        }
        if (destination.startsWith(JMS_PREFIX))
        {
            String destinationName = destination.substring(JMS_PREFIX.length());
            if (destinationName.startsWith("queue:"))
            {
                destinationName = destinationName.substring("queue:".length());
            }
            return endpoint(destinationName, QUEUE, endpointUri);
        }

        throw new IllegalArgumentException("Unsupported JMS endpoint syntax: " + endpointUri);
    }

    private static JmsEndpoint endpoint(String destinationName, JmsEndpoint.DestinationType destinationType, String endpointUri)
    {
        if (destinationName.isBlank())
        {
            throw new IllegalArgumentException("JMS endpoint has no destination name: " + endpointUri);
        }
        return new JmsEndpoint(destinationName, destinationType);
    }

    private static void validateOptions(String query, String endpointUri)
    {
        if (query.isBlank())
        {
            throw new IllegalArgumentException("JMS endpoint has an empty option list: " + endpointUri);
        }

        Set<String> optionNames = new HashSet<>();
        for (String option : query.split("&"))
        {
            String[] optionParts = option.split("=", 2);
            if (optionParts.length != 2 || !optionNames.add(optionParts[0]))
            {
                throw new IllegalArgumentException("Invalid JMS endpoint option: " + option);
            }

            boolean supportedMessageType = JMS_MESSAGE_TYPE.equals(optionParts[0]) && "Text".equals(optionParts[1]);
            boolean supportedQosFlag = PRESERVE_MESSAGE_QOS.equals(optionParts[0]) && "true".equals(optionParts[1]);
            if (!supportedMessageType && !supportedQosFlag)
            {
                throw new IllegalArgumentException("Unsupported JMS endpoint option: " + option);
            }
        }
    }
}