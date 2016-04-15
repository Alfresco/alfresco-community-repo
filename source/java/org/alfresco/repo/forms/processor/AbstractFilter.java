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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract base class for all Filter implementations.
 *
 * @author Gavin Cornwell
 */
public abstract class AbstractFilter<ItemType, PersistType> implements Filter<ItemType, PersistType>
{
    private static final Log logger = LogFactory.getLog(AbstractFilter.class);
    
    protected FilterRegistry filterRegistry;
    protected boolean active = true;

    /**
     * Sets the filter registry
     * 
     * @param filterRegistry The FilterRegistry instance
     */
    public void setFilterRegistry(FilterRegistry filterRegistry)
    {
        this.filterRegistry = filterRegistry;
    }
    
    /**
     * Sets whether this filter is active
     * 
     * @param active true if the filter should be active
     */
    public void setActive(boolean active)
    {
        this.active = active;
    }
    
    /**
     * Registers this filter with the filter registry
     */
    public void register()
    {
        if (filterRegistry == null)
        {
            if (logger.isWarnEnabled())
                logger.warn("Property 'filterRegistry' has not been set. Ignoring auto-registration of filter: " + this);
            
            return;
        }

        // register this instance
        filterRegistry.addFilter(this);
    }

    /*
     * @see org.alfresco.repo.forms.processor.Filter#isActive()
     */
    public boolean isActive()
    {
        return this.active;
    }
    
    /*
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder buffer = new StringBuilder(super.toString());
        buffer.append(" (");
        buffer.append("active=").append(this.isActive());
        buffer.append(")");
        return buffer.toString();
    }
}
