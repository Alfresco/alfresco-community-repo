/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.framework.resource.parameters;

import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Represents paging of collections of resources. Set by the client request.<br/>
 * skipCount - How many entries exist in the entire collection before those included in the list<br/>
 * maxItems - The maximum number of items the client requires. Defaults to 100.
 * 
 * @author Gethin James, Martin Muller (mmuller)
 */
public class Paging
{
    
    public static final int DEFAULT_SKIP_COUNT = 0;
    public static final int DEFAULT_MAX_ITEMS = 100;
    public static final Paging DEFAULT = Paging.valueOf(DEFAULT_SKIP_COUNT, DEFAULT_MAX_ITEMS);
        
    private final int skipCount;
    private final int maxItems;

    private Paging(int skipCount, int maxItems)
    {
        super();
        if(skipCount < 0)
        {
        	throw new InvalidArgumentException("Negative values not supported for skipCount.");
        }
        if(maxItems < 1)
        {
        	throw new InvalidArgumentException("Only positive values supported for maxItems.");
        }
        this.skipCount = skipCount;
        this.maxItems = maxItems;
    }

    /**
     * How many entries exist in the entire collection before those included in the list
     * @return Integer
     */
    public int getSkipCount()
    {
        return this.skipCount;
    }

    /**
     * The maximum number of items the client requires. Defaults to 100.
     * @return Integer
     */
    public int getMaxItems()
    {
        return this.maxItems;
    }

    @JsonCreator
    public static Paging valueOf(@JsonProperty("skipCount") int skipCount, @JsonProperty("maxItems") int maxItems)
    {
        return new Paging(skipCount,maxItems);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Paging [skipCount=");
        builder.append(this.skipCount);
        builder.append(", maxItems=");
        builder.append(this.maxItems);
        builder.append("]");
        return builder.toString();
    }

}
