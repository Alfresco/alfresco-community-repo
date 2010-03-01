/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.management.subsystems;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.repo.domain.schema.SchemaAvailableEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * A default implementation of {@link PropertyBackedBeanRegistry}. An instance of this class will defer broadcasting
 * {@link PropertyBackedBeanEvent}s until it is notified that the database schema is available via a
 * {@link SchemaAvailableEvent}. This allows listeners to potentially reconfigure the beans using persisted database
 * information.
 * 
 * @author dward
 */
public class DefaultPropertyBackedBeanRegistry implements PropertyBackedBeanRegistry, ApplicationListener
{
    /** Is the database schema available yet? */
    private boolean isSchemaAvailable;

    /** Events deferred until the database schema is available. */
    private List<PropertyBackedBeanEvent> deferredEvents = new LinkedList<PropertyBackedBeanEvent>();

    /** Registered listeners. */
    private List<ApplicationListener> listeners = new LinkedList<ApplicationListener>();

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.management.PropertyBackedBeanRegistry#addListener(org.springframework.context.ApplicationListener
     * )
     */
    public void addListener(ApplicationListener listener)
    {
        this.listeners.add(listener);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.management.PropertyBackedBeanRegistry#register(org.alfresco.repo.management.PropertyBackedBean)
     */
    public void register(PropertyBackedBean bean)
    {
        broadcastEvent(new PropertyBackedBeanRegisteredEvent(bean));
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.management.subsystems.PropertyBackedBeanRegistry#deregister(org.alfresco.repo.management.subsystems
     * .PropertyBackedBean, boolean)
     */
    public void deregister(PropertyBackedBean bean, boolean isPermanent)
    {
        broadcastEvent(new PropertyBackedBeanUnregisteredEvent(bean, isPermanent));
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.management.subsystems.PropertyBackedBeanRegistry#broadcastStart(org.alfresco.repo.management
     * .subsystems.PropertyBackedBean)
     */
    public void broadcastStart(PropertyBackedBean bean)
    {
        broadcastEvent(new PropertyBackedBeanStartedEvent(bean));
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.management.subsystems.PropertyBackedBeanRegistry#broadcastStop(org.alfresco.repo.management
     * .subsystems.PropertyBackedBean)
     */
    public void broadcastStop(PropertyBackedBean bean)
    {
        broadcastEvent(new PropertyBackedBeanStoppedEvent(bean));
    }

    /**
     * Broadcast event.
     * 
     * @param event
     *            the event
     */
    private void broadcastEvent(PropertyBackedBeanEvent event)
    {
        // If the system is up and running, broadcast the event immediately
        if (this.isSchemaAvailable)
        {
            for (ApplicationListener listener : this.listeners)
            {
                listener.onApplicationEvent(event);
            }
        }
        // Otherwise, defer broadcasting until the schema available event is handled
        else
        {
            this.deferredEvents.add(event);
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    public void onApplicationEvent(ApplicationEvent event)
    {
        if (event instanceof SchemaAvailableEvent)
        {
            this.isSchemaAvailable = true;

            // Broadcast all the events we had been deferring until this event
            for (PropertyBackedBeanEvent event1 : this.deferredEvents)
            {
                broadcastEvent(event1);
            }
            this.deferredEvents.clear();
        }
    }
}
