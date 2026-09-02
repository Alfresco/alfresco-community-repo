package org.alfresco.messaging;

import static org.alfresco.messaging.JmsEndpoint.DestinationType.QUEUE;
import static org.alfresco.messaging.JmsEndpoint.DestinationType.TOPIC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class JmsEndpointParserTest
{
    @Test
    public void parsesCommunityRenditionQueue()
    {
        JmsEndpoint endpoint = JmsEndpointParser.parse("jms:acs-repo-rendition-events?jmsMessageType=Text");

        assertEquals("acs-repo-rendition-events", endpoint.destinationName());
        assertEquals(QUEUE, endpoint.destinationType());
    }

    @Test
    public void parsesCommunityEvent2Topic()
    {
        JmsEndpoint endpoint = JmsEndpointParser.parse("amqp:topic:alfresco.repo.event2");

        assertEquals("alfresco.repo.event2", endpoint.destinationName());
        assertEquals(TOPIC, endpoint.destinationType());
    }

    @Test
    public void parsesEnterpriseTransformRequestWithCamelQosOption()
    {
        JmsEndpoint endpoint = JmsEndpointParser.parse(
                "jms:org.alfresco.transform.t-request.acs?jmsMessageType=Text&preserveMessageQos=true");

        assertEquals("org.alfresco.transform.t-request.acs", endpoint.destinationName());
        assertEquals(QUEUE, endpoint.destinationType());
    }

    @Test
    public void parsesExplicitQueueFormsUsedByCamelComponents()
    {
        assertEquals(new JmsEndpoint("alfresco.test", QUEUE), JmsEndpointParser.parse("jms:queue:alfresco.test"));
        assertEquals(new JmsEndpoint("alfresco.test", QUEUE), JmsEndpointParser.parse("amqp:queue:alfresco.test"));
    }

    @Test
    public void rejectsUnsupportedCamelComponent()
    {
        assertThrows(IllegalArgumentException.class, () -> JmsEndpointParser.parse("direct:alfresco.events"));
    }

    @Test
    public void rejectsUnsupportedCamelOption()
    {
        assertThrows(IllegalArgumentException.class,
                () -> JmsEndpointParser.parse("jms:events?concurrentConsumers=4"));
    }

    @Test
    public void rejectsMissingDestination()
    {
        assertThrows(IllegalArgumentException.class, () -> JmsEndpointParser.parse("jms:?jmsMessageType=Text"));
    }
}