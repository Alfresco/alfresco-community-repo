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
