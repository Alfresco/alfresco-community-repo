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

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.camel.ExchangePattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.event.v1.model.AbstractAssociationResource;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.Resource;
import org.alfresco.repo.rawevents.AbstractEventProducer;
import org.alfresco.util.PropertyCheck;

/**
 * An Apache Camel implementation of a message producer for repo event2.
 * <p>
 * Every message carries JMS properties that consumers can use in a message selector, so they can filter at the broker. The {@code alfresco_} prefix keeps them apart from properties set by other producers on the same topic.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class Event2MessageProducer extends AbstractEventProducer implements InitializingBean
{
    private static final Log LOGGER = LogFactory.getLog(Event2MessageProducer.class);

    /** The event {@code type}, e.g. {@code org.alfresco.event.node.Created}. */
    public static final String TYPE_PROPERTY = "alfresco_eventType";
    /** The node type of a node event, e.g. {@code cm:content}. */
    public static final String NODE_TYPE_PROPERTY = "alfresco_nodeType";
    /** The association type of a child or peer association event, e.g. {@code cm:contains}. */
    public static final String ASSOC_TYPE_PROPERTY = "alfresco_assocType";
    /** {@code true} when the configured {@code repo.event2.filter.*} rules match the event. */
    public static final String FILTERED_PROPERTY = "alfresco_filtered";

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
        send(event, false);
    }

    /**
     * @param event
     *            a {@link RepoEvent} or its JSON representation
     * @param filtered
     *            whether the configured {@code repo.event2.filter.*} rules match the event
     */
    public void send(Object event, boolean filtered)
    {
        final Map<String, Object> properties;
        try
        {
            properties = messageProperties(event);
        }
        catch (JsonProcessingException e)
        {
            throw new AlfrescoRuntimeException(ERROR_SENDING, e);
        }
        properties.put(FILTERED_PROPERTY, filtered);
        send(this.endpoint, null, event, properties);
    }

    Map<String, Object> messageProperties(Object event) throws JsonProcessingException
    {
        final Map<String, Object> properties = new HashMap<>();

        if (event instanceof RepoEvent<?> repoEvent)
        {
            putIfNotNull(properties, TYPE_PROPERTY, repoEvent.getType());
            final Resource resource = repoEvent.getData() == null ? null : repoEvent.getData().getResource();
            if (resource instanceof NodeResource nodeResource)
            {
                putIfNotNull(properties, NODE_TYPE_PROPERTY, nodeResource.getNodeType());
            }
            else if (resource instanceof AbstractAssociationResource associationResource)
            {
                putIfNotNull(properties, ASSOC_TYPE_PROPERTY, associationResource.getAssocType());
            }
        }
        else if (event instanceof String json)
        {
            // The Enterprise outbox stores the event as JSON and sends that string.
            final JsonNode root = this.objectMapper.readTree(json);
            putIfNotNull(properties, TYPE_PROPERTY, root.path("type").textValue());
            final JsonNode resource = root.path("data").path("resource");
            putIfNotNull(properties, NODE_TYPE_PROPERTY, resource.path("nodeType").textValue());
            putIfNotNull(properties, ASSOC_TYPE_PROPERTY, resource.path("assocType").textValue());
        }
        return properties;
    }

    private static void putIfNotNull(Map<String, Object> properties, String name, String value)
    {
        if (value != null)
        {
            properties.put(name, value);
        }
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

            final Map<String, Object> messageHeaders = this.addHeaders(headers);
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Sending event2 message to " + endpointUri + " with headers " + messageHeaders);
            }
            this.producer.sendBodyAndHeaders(endpointUri, exchangePattern, event, messageHeaders);
        }
        catch (Exception e)
        {
            throw new AlfrescoRuntimeException(ERROR_SENDING, e);
        }
    }
}
