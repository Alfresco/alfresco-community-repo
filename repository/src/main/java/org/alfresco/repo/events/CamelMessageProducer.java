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
    private static final String HEADER_JMS_AMQP_MESSAGE_FORMAT = "JMS_AMQP_MESSAGE_FORMAT";
    private static final Long HEADER_JMS_AMQP_MESSAGE_FORMAT_VALUE = 0L;
    private static final String ERROR_SENDING = "Could not send message";

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
            producer.sendBodyAndHeaders(endpoint, message, getHeaders());
        }
        catch (Exception e)
        {
            throw new MessagingException(ERROR_SENDING, e);
        }
    }

    private Map<String, Object> getHeaders()
    {
        return Map.of(HEADER_JMS_AMQP_MESSAGE_FORMAT, HEADER_JMS_AMQP_MESSAGE_FORMAT_VALUE);
    }

}
