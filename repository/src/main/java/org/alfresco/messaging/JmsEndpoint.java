package org.alfresco.messaging;

public record JmsEndpoint(String destinationName, DestinationType destinationType)
{
    public enum DestinationType
    {
        QUEUE,
        TOPIC
    }
}