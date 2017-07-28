/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.policy;

import org.alfresco.api.AlfrescoPublicApi;

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
@AlfrescoPublicApi
public interface Behaviour
{
    
    /**
     * When should behaviour be notified? 
     */
    @AlfrescoPublicApi
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

