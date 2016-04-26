
package org.alfresco.repo.bulkimport.importfilters;

import org.alfresco.repo.bulkimport.ImportFilter;
import org.alfresco.repo.bulkimport.ImportableItem;


/**
 * This class provides an <code>ImportFilter</code> that returns the opposite of the configured <code>SourceFilter</code>.
 *
 * @since 4.0
 */
public class NotImportFilter
    implements ImportFilter
{
    private ImportFilter original;
    
    public NotImportFilter(final ImportFilter original)
    {
        // PRECONDITIONS
        assert original  != null : "original must not be null.";
        
        // Body
        this.original = original;
    }
    

    /**
     * @see org.alfresco.repo.bulkimport.ImportFilter#shouldFilter(org.alfresco.repo.bulkimport.ImportableItem)
     */
    public boolean shouldFilter(final ImportableItem importableItem)
    {
        return(!original.shouldFilter(importableItem));
    }

}
