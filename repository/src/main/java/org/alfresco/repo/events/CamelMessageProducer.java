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
package org.alfresco.repo.events;

import java.util.Map;
import java.util.Objects;

import org.apache.camel.ProducerTemplate;

class CamelMessageProducer implements MessageProducer
{
    private static final Map<String, Object> AMQP_HEADERS = Map.of("JMS_AMQP_MESSAGE_FORMAT", 0L);
    private final ProducerTemplate producer;
    private final String endpoint;

    CamelMessageProducer(ProducerTemplate producer, String endpoint)
    {
        this.producer = Objects.requireNonNull(producer);
        this.endpoint = Objects.requireNonNull(endpoint);
    }

    @Override
    public void send(Object message)
    {
        try
        {
            producer.sendBodyAndHeaders(endpoint, message, AMQP_HEADERS);
        }
        catch (Exception e)
        {
            throw new MessagingException("Could not send message", e);
        }
    }
}
