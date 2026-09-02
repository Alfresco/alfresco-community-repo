package org.alfresco.messaging;

import org.springframework.jms.listener.DefaultMessageListenerContainer;

public class EndpointMessageListenerContainer extends DefaultMessageListenerContainer
{
    public void setEndpointUri(String endpointUri)
    {
        JmsEndpoint endpoint = JmsEndpointParser.parse(endpointUri);
        setDestinationName(endpoint.destinationName());
        setPubSubDomain(endpoint.destinationType() == JmsEndpoint.DestinationType.TOPIC);
    }
}