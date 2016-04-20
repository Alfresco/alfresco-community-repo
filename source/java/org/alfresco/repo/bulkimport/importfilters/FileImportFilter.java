
package org.alfresco.repo.bulkimport.importfilters;

import org.alfresco.repo.bulkimport.ImportFilter;
import org.alfresco.repo.bulkimport.ImportableItem;


/**
 * This class is an <code>ImportFilter</code> that filters out files.
 *
 * @since 4.0
 */
public class FileImportFilter
    implements ImportFilter
{
    /**
     * @see org.alfresco.repo.bulkimport.ImportFilter#shouldFilter(org.alfresco.repo.bulkimport.ImportableItem)
     */
    public boolean shouldFilter(final ImportableItem importableItem)
    {
        boolean result = false;
        
        if (importableItem.getHeadRevision().contentFileExists())
        {
            result = ImportableItem.FileType.FILE.equals(importableItem.getHeadRevision().getContentFileType());
        }

        return(result);
    }

}
