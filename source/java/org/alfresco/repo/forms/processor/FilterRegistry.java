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

package org.alfresco.repo.forms.processor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Holds a list of filters for a type of form processor.
 * <p>
 * Each filter is called before and after the processor generates and persists
 * the form, thus allowing the form and the effected objects to be manipulated
 * prior to generation or persistence or after the fact.
 * </p>
 * <p>
 * Each filter is responsible for determing whether it applies to the item being
 * processed.
 * </p>
 * 
 * @see org.alfresco.repo.forms.processor.Filter
 * @author Gavin Cornwell
 */
public class FilterRegistry<ItemType, PersistType>
{
    private static final Log logger = LogFactory.getLog(FilterRegistry.class);

    protected List<Filter<ItemType, PersistType>> filters;

    /**
     * Constructs the registry
     */
    public FilterRegistry()
    {
        this.filters = new ArrayList<Filter<ItemType, PersistType>>(4);
    }

    /**
     * Registers a filter
     * 
     * @param filter The Filter to regsiter
     */
    public void addFilter(Filter<ItemType, PersistType> filter)
    {
        if (filter.isActive())
        {
            this.filters.add(filter);

            if (logger.isDebugEnabled()) logger.debug("Registered filter: " + filter + " in registry: " + this);
        }
        else if (logger.isWarnEnabled())
        {
            logger.warn("Ignored registration of filter " + filter + " as it was marked as inactive");
        }
    }

    /**
     * Returns a list of active filters
     * 
     * @return List of active Filter objects
     */
    public List<Filter<ItemType, PersistType>> getFilters()
    {
        List<Filter<ItemType, PersistType>> activeFilters = new ArrayList<Filter<ItemType, PersistType>>(4);

        // iterate round the filters and add each active filter to the list
        for (Filter<ItemType, PersistType> filter : this.filters)
        {
            if (filter.isActive())
            {
                activeFilters.add(filter);
            }
        }

        if (logger.isDebugEnabled()) logger.debug("Returning active filters: " + activeFilters);

        return activeFilters;
    }
}
