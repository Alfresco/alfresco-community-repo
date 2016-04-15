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

package org.alfresco.repo.bulkimport.importfilters;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.bulkimport.ImportFilter;
import org.alfresco.repo.bulkimport.ImportableItem;

/**
 * This class provides an <code>ImportFilter</code> that only returns true if all of the configured <code>ImportFilter</code>s return true.
 *
 * @since 4.0
 */
public class AndImportFilter
    implements ImportFilter
{
    private final List<ImportFilter> filters;
    
    public AndImportFilter(final ImportFilter left, final ImportFilter right)
    {
        // PRECONDITIONS
        assert left  != null : "left must not be null.";
        assert right != null : "right must not be null.";
        
        // Body
        this.filters = new ArrayList<ImportFilter>(2);
        
        filters.add(left);
        filters.add(right);
    }
    
    public AndImportFilter(final List<ImportFilter> filters)
    {
        // PRECONDITIONS
        assert filters        != null : "filters must not be null.";
        assert filters.size() >= 2    : "filters must contain at least 2 items.";
        
        // Body
        this.filters = filters;
    }
    

    /**
     * @see org.alfresco.repo.bulkimport.ImportFilter#shouldFilter(org.alfresco.repo.bulkimport.ImportableItem)
     */
    public boolean shouldFilter(final ImportableItem importableItem)
    {
        boolean result = true;
        
        for (final ImportFilter sourceFilter : filters)
        {
            if (!sourceFilter.shouldFilter(importableItem))
            {
                result = false;
                break;
            }
        }

        return(result);
    }

}
