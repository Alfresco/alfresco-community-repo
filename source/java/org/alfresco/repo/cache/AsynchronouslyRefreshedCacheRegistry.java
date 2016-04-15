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
package org.alfresco.repo.cache;

/**
 * A registry of all AsynchronouslyRefreshedCaches to be used for notification.
 * 
 * @author Andy
 *
 */
public interface AsynchronouslyRefreshedCacheRegistry
{
    /**
     * Register a listener
     * @param listener RefreshableCacheListener
     */
    public void register(RefreshableCacheListener listener);
    
    /**
     * Fire an even 
     * @param event RefreshableCacheEvent
     * @param toAll - true goes to all listeners, false only to listeners that have a matching cacheId 
     */
    public void broadcastEvent(RefreshableCacheEvent event, boolean toAll);
}
