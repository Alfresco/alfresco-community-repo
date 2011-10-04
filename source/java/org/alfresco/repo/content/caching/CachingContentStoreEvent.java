/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.content.caching;

import org.springframework.context.ApplicationEvent;

/**
 * Abstract base class for CachingContentStore related application events.
 * 
 * @author Matt Ward
 */
public abstract class CachingContentStoreEvent extends ApplicationEvent
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructor that captures the source of the event.
     * 
     * @param source
     */
    public CachingContentStoreEvent(Object source)
    {
        super(source);
    }
    
    /**
     * Is the event an instance of the specified type (or subclass)?
     * 
     * @param type
     * @return
     */
    public boolean isType(Class<?> type)
    {
        return type.isAssignableFrom(getClass());
    }
}
