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
}
