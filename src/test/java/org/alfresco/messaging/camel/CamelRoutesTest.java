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
package org.alfresco.messaging.camel;

import static org.junit.Assert.*;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests routes defined via Spring with package scan
 *
 * @author Ray Gauss II
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test-messaging-context.xml")
public class CamelRoutesTest
{
    @EndpointInject(uri = "mock:result1")
    protected MockEndpoint resultEndpoint1;
    
    @EndpointInject(uri = "mock:result2")
    protected MockEndpoint resultEndpoint2;
    
    @EndpointInject(uri = "mock:dlq")
    protected MockEndpoint dlqEndpoint;
    
    @Autowired
    protected CamelContext camelContext;
    
    @Produce(uri = "direct-vm:alfresco.test.1")
    protected ProducerTemplate template1;
    
    @Produce(uri = "direct-vm:alfresco.test.2")
    protected ProducerTemplate template2;
    
    @Produce(uri = "direct-vm:alfresco.default")
    protected ProducerTemplate template3;
    
    @Produce(uri = "direct-vm:alfresco.test.transacted")
    protected ProducerTemplate template4;
    
    @Autowired
    protected MockExceptionProcessor messagingExceptionProcessor;
    
    @Autowired
    protected MockConsumer mockConsumer;
    
    @Autowired
    protected MockExceptionThrowingConsumer mockExceptionThrowingConsumer;

    @Test
    public void testMessageRouteXmlDefined() throws Exception {
        String expectedBody = "<matched.>";
 
        resultEndpoint1.expectedBodiesReceived(expectedBody);
 
        template1.sendBody(expectedBody);
 
        resultEndpoint1.assertIsSatisfied();
    }
    
    @Test
    public void testMessageRoutePackageDefined() throws Exception {
        String expectedBody = "<matched.>";
 
        resultEndpoint2.expectedBodiesReceived(expectedBody);
 
        template2.sendBody(expectedBody);
 
        resultEndpoint2.assertIsSatisfied();
    }
    
    @Test
    public void testMessageRouteXmlOverride() throws Exception {
        String expectedBody = "<matched.>";
 
        dlqEndpoint.expectedBodiesReceived(expectedBody);
 
        template3.sendBody(expectedBody);
 
        dlqEndpoint.assertIsSatisfied();
    }
    
    @Test
    public void testTransactedRoute() throws Exception {
        String expectedBody = "<matched.>";
        
        template4.sendBody(expectedBody);
        
        // Wait for Camel and ActiveMQ to process
        Thread.sleep(2000);
        
        // Test that our exception processor received the error
        assertNotNull(messagingExceptionProcessor.getLastError());
        assertTrue(messagingExceptionProcessor.getLastError().getClass().equals(
                IllegalArgumentException.class));
        
        // Check that an error was thrown the first time
        assertTrue(mockExceptionThrowingConsumer.isErrorThrown());
        assertNull(mockExceptionThrowingConsumer.getLastMessage());
        
        // Check that the message was re-delivered to a second consumer
        assertEquals(expectedBody, mockConsumer.getLastMessage());
    }
}
