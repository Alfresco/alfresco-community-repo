/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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

import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.rawevents.AbstractEventProducer;
import org.alfresco.util.PropertyCheck;
import org.apache.camel.ExchangePattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;

/**
 * An Apache Camel implementation of a message producer for repo event2.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class Event2MessageProducer extends AbstractEventProducer implements InitializingBean
{

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "producer", this.producer);
        PropertyCheck.mandatory(this, "endpoint", this.endpoint);
        PropertyCheck.mandatory(this, "objectMapper", this.objectMapper);

        if (StringUtils.isEmpty(this.endpoint))
        {
            throw new IllegalArgumentException("Property 'endpoint' cannot be an empty string.");
        }
    }

    public void send(Object event)
    {
        send(this.endpoint, null, event, null);
    }

    @Override
    public void send(String endpointUri, ExchangePattern exchangePattern, Object event, Map<String, Object> headers)
    {
        try
        {
            if (!(event instanceof String))
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
