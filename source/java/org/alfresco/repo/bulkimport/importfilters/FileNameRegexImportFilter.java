
package org.alfresco.repo.bulkimport.importfilters;


import java.util.regex.Pattern;

import org.alfresco.repo.bulkimport.ImportFilter;
import org.alfresco.repo.bulkimport.ImportableItem;


/**
 * This class is an <code>ImportFilter</code> that filters out files and/or folders whose name, excluding
 * path, matches the configured regular expression. 
 *
 * @since 4.0
 */
public class FileNameRegexImportFilter implements ImportFilter
{
    private final Pattern pattern;
    
    /**
     * Simple constructor for a FileNameRegexSourceFilter
     * 
     * @param filenameRegex The regex to use to match against file and folder names <i>(must not be null)</i>.
     */
    public FileNameRegexImportFilter(final String filenameRegex)
    {
        this.pattern = Pattern.compile(filenameRegex);
    }
    
    /**
     * @see org.alfresco.repo.bulkimport.ImportFilter#shouldFilter(org.alfresco.repo.bulkimport.ImportableItem)
     */
    public boolean shouldFilter(final ImportableItem importableItem)
    {
        return(pattern.matcher(importableItem.getHeadRevision().getContentFile().getName()).matches());
    }

}
