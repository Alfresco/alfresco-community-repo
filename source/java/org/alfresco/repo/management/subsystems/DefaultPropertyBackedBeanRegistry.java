/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
