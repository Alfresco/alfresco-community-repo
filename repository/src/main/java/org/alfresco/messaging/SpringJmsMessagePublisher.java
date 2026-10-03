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

import java.util.Collections;
import java.util.Map;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;

import org.springframework.jms.core.JmsTemplate;

public class SpringJmsMessagePublisher implements MessagePublisher
{
    static final String JMS_CORRELATION_ID = "JMSCorrelationID";
    static final String JMS_REPLY_TO = "JMSReplyTo";

    private final JmsTemplate queueTemplate;
    private final JmsTemplate topicTemplate;

    public SpringJmsMessagePublisher(JmsTemplate queueTemplate, JmsTemplate topicTemplate)
    {
        this.queueTemplate = queueTemplate;
        this.topicTemplate = topicTemplate;
    }

    @Override
    public void send(String endpointUri, String body, Map<String, Object> headers)
    {
        JmsEndpoint endpoint = JmsEndpointParser.parse(endpointUri);
        JmsTemplate template = endpoint.destinationType() == JmsEndpoint.DestinationType.TOPIC ? topicTemplate : queueTemplate;

        template.send(endpoint.destinationName(), session -> {
            TextMessage message = session.createTextMessage(body);
            applyHeaders(message, headers == null ? Collections.emptyMap() : headers);
            return message;
        });
    }

    private void applyHeaders(TextMessage message, Map<String, Object> headers) throws JMSException
    {
        for (Map.Entry<String, Object> header : headers.entrySet())
        {
            if (JMS_CORRELATION_ID.equals(header.getKey()))
            {
                message.setJMSCorrelationID((String) header.getValue());
            }
            else if (JMS_REPLY_TO.equals(header.getKey()))
            {
                message.setJMSReplyTo((Destination) header.getValue());
            }
            else
            {
                message.setObjectProperty(header.getKey(), header.getValue());
            }
        }
    }
}
