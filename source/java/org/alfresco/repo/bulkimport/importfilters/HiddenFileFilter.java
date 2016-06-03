
package org.alfresco.repo.bulkimport.importfilters;

import org.alfresco.repo.bulkimport.ImportFilter;
import org.alfresco.repo.bulkimport.ImportableItem;


/**
 * This class is an <code>ImportFilter</code> that filters out hidden files.
 * 
 * The exact definition of "hidden" is OS dependent - see http://download.oracle.com/javase/6/docs/api/java/io/File.html#isHidden() for details.
 *
 * @since 4.0
 *
 */
public class HiddenFileFilter implements ImportFilter
{
    /**
     * @see org.alfresco.repo.bulkimport.ImportFilter#shouldFilter(org.alfresco.repo.bulkimport.ImportableItem)
     */
    public boolean shouldFilter(final ImportableItem importableItem)
    {
        boolean result = false;
        
        if (importableItem.getHeadRevision().contentFileExists())
        {
            result = importableItem.getHeadRevision().getContentFile().isHidden();
        }

        return(result);
    }

}
