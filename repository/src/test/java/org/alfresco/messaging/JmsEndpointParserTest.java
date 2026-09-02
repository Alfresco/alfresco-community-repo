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
import static org.junit.Assert.assertThrows;

import static org.alfresco.messaging.JmsEndpoint.DestinationType.QUEUE;
import static org.alfresco.messaging.JmsEndpoint.DestinationType.TOPIC;

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
