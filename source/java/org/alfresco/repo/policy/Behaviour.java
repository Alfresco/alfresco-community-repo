/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.policy;


/**
 * A Behaviour represents an encapsulated piece of logic (system or business)
 * that may be bound to a Policy.  The logic may be expressed in any
 * language (java, script etc).  
 *
 * Once bound to a Policy, the behaviour must be able to provide the interface
 * declared by that policy.
 * 
 * @author David Caruana
 */
public interface Behaviour
{
    
    /**
     * When should behaviour be notified? 
     */
    public enum NotificationFrequency
    {
        EVERY_EVENT,
        FIRST_EVENT,
        TRANSACTION_COMMIT
    }
    
    /**
     * Gets the requested policy interface onto the behaviour 
     * 
     * @param policy  the policy interface class
     * @return  the policy interface
     */
    public <T> T getInterface(Class<T> policy);
    
    /**
     * Disable the behaviour (for this thread only)
     */
    public void disable();

    /**
     * Enable the behaviour (for this thread only)
     */
    public void enable();

    /**
     * @return  is the behaviour enabled (for this thread only)
     */
    public boolean isEnabled();

    /**
     * @return  the notification
     */
    public NotificationFrequency getNotificationFrequency();

}

