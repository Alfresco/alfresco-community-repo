/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
     * @see org.alfresco.extension.bulkfilesystemimport.ImportFilter#shouldFilter(org.alfresco.extension.bulkfilesystemimport.ImportableItem)
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
