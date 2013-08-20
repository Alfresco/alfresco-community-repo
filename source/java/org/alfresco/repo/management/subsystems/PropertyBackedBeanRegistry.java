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
import java.util.Map;

import org.springframework.context.ApplicationListener;

/**
 * An object that tracks the initialization and destruction of {@link PropertyBackedBean} instances. A
 * <code>PropertyBackedBean</code> should call {@link #register(PropertyBackedBean)} after initialization and
 * {@link #deregister(PropertyBackedBean, boolean)} when discarded. Other classes may register for notification of these
 * events by calling {@link #addListener(ApplicationListener)}.
 * 
 * @author dward
 */
public interface PropertyBackedBeanRegistry
{
    /**
     * Registers a listener object that will be notified of register and deregister calls via a
     * {@link PropertyBackedBeanEvent}.
     * 
     * @param listener
     *            the listener
     */
    public void addListener(ApplicationListener listener);

    /**
     * Signals that a {@link PropertyBackedBean} has been initialized.
     * 
     * @param bean
     *            the bean
     */
    public void register(PropertyBackedBean bean);

    /**
     * Signals that {@link PropertyBackedBean#destroy(boolean)} has been called on a bean.
     * 
     * @param bean
     *            the bean
     * @param isPermanent
     *            is the component being destroyed forever, i.e. should persisted values be removed? On server shutdown,
     *            this value would be <code>false</code>, whereas on the removal of a dynamically created instance, this
     *            value would be <code>true</code>.
     */
    public void deregister(PropertyBackedBean bean, boolean isPermanent);

    /**
     * Signals that a {@link PropertyBackedBean} has been started.
     * 
     * @param bean
     *            the bean
     */
    public void broadcastStart(PropertyBackedBean bean);


    /**
     * Signals that a {@link PropertyBackedBean} has been stopped.
     * 
     * @param bean
     *            the bean
     */
    public void broadcastStop(PropertyBackedBean bean);
    
    /**
     * Signals that a {@link PropertyBackedBean} has been asked to
     * update a property.
     * 
     * @param bean
     *            the bean
     * @param name
     *            the name
     * @param value
     *            the value
     */
    public void broadcastSetProperty(PropertyBackedBean bean, String name, String value);
    
    /**
     * Signals that a {@link PropertyBackedBean} has been asked to
     * update properties.
     * 
     * @param bean
     *            the bean
     */
    public void broadcastSetProperties(PropertyBackedBean bean, Map<String, String> properties);

    /**
     * Signals that a {@link PropertyBackedBean} has been asked to
     * remove properties.
     * 
     * @param bean
     *            the bean
     */
    public void broadcastRemoveProperties(PropertyBackedBean bean, Collection<String> properties);
}
