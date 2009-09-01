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
