/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

import java.util.Optional;
import java.util.concurrent.Executor;
import jakarta.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.env.PropertyResolver;

import org.alfresco.util.PropertyCheck;

public class EventSenderFactoryBean extends AbstractFactoryBean<EventSender>
{
    static final String LEGACY_SKIP_QUEUE_PROPERTY = "repo.event2.queue.skip";
    static final String EVENT_SEND_STRATEGY_PROPERTY = "repo.event2.send.strategy";
    private static final String DIRECT_EVENT_SENDER_NAME = "direct";
    private static final String ASYNC_EVENT_SENDER_NAME = "async";

    private final PropertyResolver propertyResolver;
    private final Event2MessageProducer event2MessageProducer;
    private final Executor enqueueThreadPoolExecutor;
    private final Executor dequeueThreadPoolExecutor;

    private String configuredSenderName;
    private boolean legacySkipQueueConfig;

    public EventSenderFactoryBean(@Autowired PropertyResolver propertyResolver, Event2MessageProducer event2MessageProducer,
            Executor enqueueThreadPoolExecutor, Executor dequeueThreadPoolExecutor)
    {
        super();
        PropertyCheck.mandatory(this, "propertyResolver", propertyResolver);
        PropertyCheck.mandatory(this, "event2MessageProducer", event2MessageProducer);
        PropertyCheck.mandatory(this, "enqueueThreadPoolExecutor", enqueueThreadPoolExecutor);
        PropertyCheck.mandatory(this, "dequeueThreadPoolExecutor", dequeueThreadPoolExecutor);
        this.propertyResolver = propertyResolver;
        this.event2MessageProducer = event2MessageProducer;
        this.enqueueThreadPoolExecutor = enqueueThreadPoolExecutor;
        this.dequeueThreadPoolExecutor = dequeueThreadPoolExecutor;
    }

    @Value("${" + LEGACY_SKIP_QUEUE_PROPERTY + "}")
    public void setLegacySkipQueueConfig(boolean legacySkipQueueConfig)
    {
        this.legacySkipQueueConfig = legacySkipQueueConfig;
    }

    @Value("${" + EVENT_SEND_STRATEGY_PROPERTY + "}")
    public void setConfiguredSenderName(String configuredSenderName)
    {
        this.configuredSenderName = configuredSenderName;
    }

    @Override
    public Class<?> getObjectType()
    {
        return EventSender.class;
    }

    @Override
    @Nonnull
    protected EventSender createInstance() throws Exception
    {
        EventSender sender = instantiateConfiguredSender();

        sender.initialize();

        return sender;
    }

    private EventSender instantiateConfiguredSender()
    {
        if (isSenderNameConfigured())
        {
            return instantiateSender(getConfiguredSenderName());
        }
        return isLegacySkipQueueConfigured() ? instantiateDirectSender() : instantiateAsyncSender();
    }

    protected EventSender instantiateSender(String senderName)
    {
        if (DIRECT_EVENT_SENDER_NAME.equalsIgnoreCase(senderName))
        {
            return instantiateDirectSender();
        }

        if (ASYNC_EVENT_SENDER_NAME.equalsIgnoreCase(senderName))
        {
            return instantiateAsyncSender();
        }

        throw new IllegalStateException("Failed to instantiate sender: " + senderName);
    }

    private DirectEventSender instantiateDirectSender()
    {
        return new DirectEventSender(getEvent2MessageProducer());
    }

    private EnqueuingEventSender instantiateAsyncSender()
    {
        return new EnqueuingEventSender(getEvent2MessageProducer(), enqueueThreadPoolExecutor, dequeueThreadPoolExecutor);
    }

    private boolean isSenderNameConfigured()
    {
        return !Optional.ofNullable(getConfiguredSenderName())
                .map(String::isBlank)
                .orElse(true);
    }

    private boolean isLegacySkipQueueConfigured()
    {
        return Optional.ofNullable(resolveProperty(LEGACY_SKIP_QUEUE_PROPERTY, Boolean.class))
                .orElse(legacySkipQueueConfig);
    }

    private String getConfiguredSenderName()
    {
        return Optional.ofNullable(resolveProperty(EVENT_SEND_STRATEGY_PROPERTY, String.class))
                .orElse(configuredSenderName);
    }

    protected <T> T resolveProperty(String key, Class<T> targetType)
    {
        return propertyResolver.getProperty(key, targetType);
    }

    protected Event2MessageProducer getEvent2MessageProducer()
    {
        return event2MessageProducer;
    }
}
