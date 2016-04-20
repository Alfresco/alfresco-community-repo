
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
