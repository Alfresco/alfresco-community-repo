/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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

import static org.junit.Assert.assertEquals;

import org.apache.camel.CamelContext;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.alfresco.util.testing.category.NeverRunsTests;

/**
 * Tests Camel components defined in the application's Spring context
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Category(NeverRunsTests.class)
@ContextConfiguration(locations = "/test-messaging-context.xml")
public class CamelComponentsTest
{
    @Autowired
    protected CamelContext camelContext;

    @Produce("activemq6:queue:alfresco.test")
    protected ProducerTemplate activemqTemplate;

    @Produce("amqp:queue:alfresco.test")
    protected ProducerTemplate amqpTemplate;

    @Produce("jms:queue:alfresco.test")
    protected ProducerTemplate jmsTemplate;

    @Test
    public void testActivemqComponent()
    {
        final String msg = "ping <activemq>";

        activemqTemplate.sendBody(msg);

        final Object reply = camelContext
                .createConsumerTemplate()
                .receiveBody("activemq6:queue:alfresco.test", 2000);

        assertEquals(msg, reply);
    }

    @Test
    public void testAmqpComponent()
    {
        final String msg = "ping <amqp>";

        amqpTemplate.sendBody(msg);

        final Object reply = camelContext
                .createConsumerTemplate()
                .receiveBody("amqp:queue:alfresco.test", 2000);

        assertEquals(msg, reply);
    }

    @Test
    public void testJmsComponent()
    {
        final String msg = "ping <jms>";

        jmsTemplate.sendBody(msg);

        final Object reply = camelContext
                .createConsumerTemplate()
                .receiveBody("jms:queue:alfresco.test", 2000);

        assertEquals(msg, reply);
    }
}
