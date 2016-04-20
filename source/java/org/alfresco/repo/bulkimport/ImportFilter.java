
package org.alfresco.repo.bulkimport;

/**
 * Definition of a source filter - a class that filters out importable items idenfitied from the source
 * directory from the import.
 * 
 * Note that source filters can be "chained", in which case each source filter effectively has
 * "veto" power - if any single filter requests that a given importable item be filtered, it
 * <strong>will</strong> be filtered.
 *
 * @since 4.0
 */
public interface ImportFilter
{
    
    /**
     * Method that checks whether the given file or folder should be filtered.
     * 
     * @param importableItem The source importable item to check for filtering <i>(will not be null)</i>.
     * @return True if the given importable item should be filtered, false otherwise. 
     */
    boolean shouldFilter(final ImportableItem importableItem);
    
}
