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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.forms.processor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Holds a list of filters for a type of form processor.
 * <p>
 * Each filter is called before and after the processor generates and
 * persists the form, thus allowing the form and the effected objects
 * to be manipulated prior to generation or persistence or after the 
 * fact.
 * </p>
 * <p>
 * Each filter is responsible for determing whether it applies to the item
 * being processed.
 * </p>
 * 
 * @see org.alfresco.repo.forms.processor.Filter
 * @author Gavin Cornwell
 */
public class FilterRegistry
{
    private static final Log logger = LogFactory.getLog(FilterRegistry.class);
    
    protected List<Filter> filters;
    
    /**
     * Constructs the registry
     */
    public FilterRegistry()
    {
        this.filters = new ArrayList<Filter>(4);
    }
    
    /**
     * Registers a filter
     * 
     * @param filter The Filter to regsiter
     */
    public void addFilter(Filter filter)
    {
        if (filter.isActive())
        {
            this.filters.add(filter);
            
            if (logger.isDebugEnabled())
                logger.debug("Registered filter: " + filter + " in registry: " + this);
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
    public List<Filter> getFilters()
    {
        List<Filter> activeFilters = new ArrayList<Filter>(4);
        
        // iterate round the filters and add each active filter to the list
        for (Filter filter: this.filters)
        {
            if (filter.isActive())
            {
                activeFilters.add(filter);
            }
        }
        
        if (logger.isDebugEnabled())
            logger.debug("Returning active filters: " + activeFilters);
        
        return activeFilters;
    }
}
