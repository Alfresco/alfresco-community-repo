package org.alfresco.repo.search;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Enum to specify how multi-lingual properties should be treate for indexing and search.
 * 
 * @author andyh
 *
 */
public enum MLAnalysisMode
{
    /**
     * Only exact locale is used.
     */
    LOCALE_ONLY, 
    
    /**
     * Only the exact locale and no local === all lnaguages 
     */
    LOCALE_AND_ALL, 
    
    /**
     * Expand the locale to include all the locales that contain it.
     * en_GB would be en_GB, en, but not all languages
     */
    LOCALE_AND_ALL_CONTAINING_LOCALES, 
    
    /**
     * Expand the locale to include all the locales that contain it.
     * en_GB would be en_GB, en, and all.
     */
    LOCALE_AND_ALL_CONTAINING_LOCALES_AND_ALL, 
    
    /**
     * Expand to all the locales that are contained by this.
     * en would expand to en, en_GB, en_US, ....
     */
    LOCAL_AND_ALL_CONTAINED_LOCALES;
    
    public static MLAnalysisMode getMLAnalysisMode(String mode)
    {
        for(MLAnalysisMode test : MLAnalysisMode.values())
        {
            if(test.toString().equalsIgnoreCase(mode))
            {
                return test;
            }
        }
        throw new AlfrescoRuntimeException("Unknown ML Analysis mode "+mode);
    }
}
