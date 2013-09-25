/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.dictionary.DictionaryRepositoryBootstrappedEvent;
import org.alfresco.repo.domain.schema.SchemaAvailableEvent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListener;
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
public class DefaultPropertyBackedBeanRegistry implements PropertyBackedBeanRegistry, ApplicationListener, TransactionListener
{
    /** Is the database schema available yet? */
    private boolean isSchemaAvailable;

    private boolean wasDictionaryBootstrapped = false;

    /** Events deferred until the database schema is available. */
    private List<PropertyBackedBeanEvent> deferredEvents = new LinkedList<PropertyBackedBeanEvent>();

    /** Events to be broadcasted after the transaction finished. */
    private List<PropertyBackedBeanEvent> afterTransactionEvents = new LinkedList<PropertyBackedBeanEvent>();

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

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.management.subsystems.PropertyBackedBeanRegistry#broadcastSetProperty(org.alfresco.repo.management
     * .subsystems.PropertyBackedBean, String, String)
     */
    @Override
    public void broadcastSetProperty(PropertyBackedBean bean, String name, String value)
    {
        broadcastEvent(new PropertyBackedBeanSetPropertyEvent(bean, name, value));
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.management.subsystems.PropertyBackedBeanRegistry#broadcastSetProperties(org.alfresco.repo.management
     * .subsystems.PropertyBackedBean, Map<String, String>)
     */
    @Override
    public void broadcastSetProperties(PropertyBackedBean bean, Map<String, String> properties)
    {
        broadcastEvent(new PropertyBackedBeanSetPropertiesEvent(bean, properties));
    }

    @Override
    public void broadcastRemoveProperties(PropertyBackedBean bean, Collection<String> properties)
    {
        broadcastEvent(new PropertyBackedBeanRemovePropertiesEvent(bean, properties));
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
        if (this.isSchemaAvailable && this.wasDictionaryBootstrapped)
        {
            // If we have a transaction, the changed properties in it should be updated earlier,
            // then the bean restart message will be sent to other node
            // see ALF-20066
            if (AlfrescoTransactionSupport.getTransactionId() != null &&
                    (event instanceof PropertyBackedBeanStartedEvent ||
                    event instanceof PropertyBackedBeanStoppedEvent))
            {
                this.afterTransactionEvents.add(event);
                AlfrescoTransactionSupport.bindListener(this);
            }
            else
            {
                for (ApplicationListener listener : this.listeners)
                {
                    listener.onApplicationEvent(event);
                }
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

            if (wasDictionaryBootstrapped && isSchemaAvailable)
            {
            // Broadcast all the events we had been deferring until this event
            for (PropertyBackedBeanEvent event1 : this.deferredEvents)
            {
                broadcastEvent(event1);
            }
            this.deferredEvents.clear();
        }

           
        }
        if (event instanceof DictionaryRepositoryBootstrappedEvent)
        {
            this.wasDictionaryBootstrapped = true;
            
            if (wasDictionaryBootstrapped && isSchemaAvailable)
            {
                // Broadcast all the events we had been deferring until this event
                for (PropertyBackedBeanEvent event1 : this.deferredEvents)
                {
                    broadcastEvent(event1);
                }
                this.deferredEvents.clear();
            }
        }
    }

    @Override
    public void beforeCommit(boolean readOnly)
    {
        // No-op
    }

    @Override
    public void afterCommit()
    {
        for (ApplicationEvent event : this.afterTransactionEvents)
        {
            for (ApplicationListener listener : this.listeners)
            {
                listener.onApplicationEvent(event);
            }
        }
        this.afterTransactionEvents.clear();
    }

    @Override
    public void beforeCompletion()
    {
        // No-op
    }

    @Override
    public void afterRollback()
    {
        // No-op
    }

    @Override
    public void flush()
    {
        // No-op
    }
}
