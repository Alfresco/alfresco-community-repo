
package org.alfresco.repo.bulkimport.importfilters;

import org.alfresco.repo.bulkimport.ImportFilter;
import org.alfresco.repo.bulkimport.ImportableItem;


/**
 * This class is an <code>ImportFilter</code> that filters out importable items whose content file doesn't exist. 
 *
 * @since 4.0
 */
public class NonExistentContentFileImportFilter
    implements ImportFilter
{
    /**
     * @see org.alfresco.repo.bulkimport.ImportFilter#shouldFilter(org.alfresco.repo.bulkimport.ImportableItem)
     */
    public boolean shouldFilter(final ImportableItem importableItem)
    {
        return(!importableItem.getHeadRevision().contentFileExists());
    }

}
