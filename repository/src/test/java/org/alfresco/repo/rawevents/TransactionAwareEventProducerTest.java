/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.repo.rawevents;

import org.alfresco.repo.rawevents.types.EventType;
import org.alfresco.repo.rawevents.types.OnContentUpdatePolicyEvent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.apache.camel.CamelContext;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Provides a base set of tests for {@link TransactionAwareEventProducer}
 *
 * @author Cristian Turlica
 */
public class TransactionAwareEventProducerTest extends BaseSpringTest
{
    @Autowired
    private RetryingTransactionHelper retryingTransactionHelper;
    @Autowired
    private CamelContext camelContext;
    @Autowired
    private TransactionAwareEventProducer eventProducer;
    @Autowired
    @Qualifier("alfrescoEventObjectMapper")
    private ObjectMapper messagingObjectMapper;

    @Test
    public void send() throws Exception
    {
        String endpointUri = getMockEndpointUri();

        MockEndpoint mockEndpoint = camelContext.getEndpoint(endpointUri, MockEndpoint.class);
        mockEndpoint.setAssertPeriod(500);

        String stringMessage = "stringMessage";
        OnContentUpdatePolicyEvent objectMessage = new OnContentUpdatePolicyEvent();
        objectMessage.setId(GUID.generate());
        objectMessage.setType(EventType.CONTENT_UPDATED.toString());
        objectMessage.setTimestamp(System.currentTimeMillis());

        retryingTransactionHelper.doInTransaction(() -> {
            eventProducer.send(endpointUri, stringMessage);

            // Assert that the endpoint didn't receive any message
            // Event is sent only on transaction commit.
            mockEndpoint.setExpectedCount(0);
            mockEndpoint.assertIsSatisfied();

            eventProducer.send(endpointUri, objectMessage);

            // Assert that the endpoint didn't receive any message
            // Event is sent only on transaction commit.
            mockEndpoint.setExpectedCount(0);
            mockEndpoint.assertIsSatisfied();

            return null;
        });

        // Assert that the endpoint received 2 messages
        mockEndpoint.setExpectedCount(2);
        mockEndpoint.assertIsSatisfied();

        // Get the sent string message
        String stringMessageSent = (String) mockEndpoint.getExchanges().get(0).getIn().getBody();

        assertNotNull(stringMessageSent);
        assertEquals(stringMessage, stringMessageSent);

        // Get the sent json marshaled object message
        String jsonMessageSent = (String) mockEndpoint.getExchanges().get(1).getIn().getBody();
        assertNotNull(jsonMessageSent);

        OnContentUpdatePolicyEvent objectMessageSent = messagingObjectMapper.readValue(jsonMessageSent, OnContentUpdatePolicyEvent.class);

        assertNotNull(objectMessageSent);
        assertEquals(objectMessage.getId(), objectMessageSent.getId());
        assertEquals(objectMessage.getType(), objectMessageSent.getType());
        assertEquals(objectMessage.getTimestamp(), objectMessageSent.getTimestamp());
    }

    private String getMockEndpointUri()
    {
        return "mock:" + this.getClass().getSimpleName() + "_" + GUID.generate();
    }
}