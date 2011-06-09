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

package org.alfresco.repo.publishing;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.service.cmr.publishing.PublishingEventFilter;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class PublishingEventFilterImpl implements PublishingEventFilter
{
    private Set<String> ids = new HashSet<String>();
    
    /**
    * {@inheritDoc}
    */
    public PublishingEventFilter setIds(String... ids)
    {
        if(ids != null && ids.length>0)
        {
            this.ids.addAll(Arrays.asList(ids));
        }
        return this;
    }
    
    /**
    * {@inheritDoc}
    */
    public Set<String> getIds()
    {
        return Collections.unmodifiableSet(ids);
    }
    
}
