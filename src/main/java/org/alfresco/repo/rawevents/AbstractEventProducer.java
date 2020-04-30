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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract helper to send events to an endpoint. The
 * <code>AbstractEventProducer</code> acts as a wrapper that provides
 * marshalling for a Camel <code>ProducerTemplate</code>. <br/>
 * <p/>
 * A client has the option of creating an event producer without supplying an
 * endpoint. In this case, a endpoint must be provided with every send
 * operation. <br/>
 * <p/>
 * A client also has the option to provide an <code>ObjectMapper</code> that
 * will be used to marshal basic POJOs (Plain Old Java Objects) to JSON before
 * sending the event.
 * <p/>
 */
public abstract class AbstractEventProducer
{
    protected static final String ERROR_SENDING = "Could not send event";
    public static final String JMS_AMQP_PREFIX = "JMS_AMQP_";

    public static final String MESSAGE_FORMAT = "MESSAGE_FORMAT";
    public static final String JMS_AMQP_MESSAGE_FORMAT = JMS_AMQP_PREFIX + MESSAGE_FORMAT;
    public static final short AMQP_UNKNOWN = 0;

    protected ProducerTemplate producer;
    protected String endpoint;
    protected ObjectMapper objectMapper;

    public void setProducer(ProducerTemplate producer)
    {
        this.producer = producer;
    }

    public void setEndpoint(String endpoint)
    {
        this.endpoint = endpoint;
    }

    public void setObjectMapper(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    protected Map<String, Object> addHeaders(Map<String, Object> origHeaders)
    {
        if (origHeaders == null)
        {
            origHeaders = new HashMap<>();
        }

        origHeaders.put(JMS_AMQP_MESSAGE_FORMAT, AMQP_UNKNOWN);
        return origHeaders;
    }

    public void send(String endpointUri, Object event)
    {
        send(endpointUri, event, null);
    }

    public void send(String endpointUri, Object event, Map<String, Object> headers)
    {
        send(endpointUri, null, event, headers);
    }

    public void send(String endpointUri, ExchangePattern exchangePattern, Object event, Map<String, Object> headers)
    {
        try
        {
            if (StringUtils.isEmpty(endpointUri))
            {
                endpointUri = this.endpoint;
            }

            if (this.objectMapper != null && !(event instanceof String))
            {
                event = this.objectMapper.writeValueAsString(event);
            }

            if (exchangePattern == null)
            {
                exchangePattern = ExchangePattern.InOnly;
            }

            this.producer.sendBodyAndHeaders(endpointUri, exchangePattern, event, this.addHeaders(headers));
        }
        catch (Exception e)
        {
            throw new AlfrescoRuntimeException(ERROR_SENDING, e);
        }
    }
}
