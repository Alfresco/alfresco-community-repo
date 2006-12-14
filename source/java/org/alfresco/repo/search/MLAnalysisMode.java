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
    LOCALE_ONLY
    {
        public boolean includesAll()
        {
            return false;
        }
        public boolean includesContained()
        {
            return false;
        }
        public boolean includesContaining()
        {
            return false;
        }
        public boolean includesExact()
        {
            return true;
        }
        
    },
    
    /**
     * Only the exact locale and no local === all lnaguages 
     */
    LOCALE_AND_ALL
    {
        public boolean includesAll()
        {
            return true;
        }
        public boolean includesContained()
        {
            return false;
        }
        public boolean includesContaining()
        {
            return false;
        }
        public boolean includesExact()
        {
            return true;
        }
    },
    
    /**
     * Expand the locale to include all the locales that contain it.
     * en_GB would be en_GB, en, but not all languages
     */
    LOCALE_AND_ALL_CONTAINING_LOCALES
    {
        public boolean includesAll()
        {
            return false;
        }
        public boolean includesContained()
        {
            return false;
        }
        public boolean includesContaining()
        {
            return true;
        }
        public boolean includesExact()
        {
            return true;
        }
    },
    
    /**
     * Expand the locale to include all the locales that contain it.
     * en_GB would be en_GB, en, and all.
     */
    LOCALE_AND_ALL_CONTAINING_LOCALES_AND_ALL
    {
        public boolean includesAll()
        {
            return true;
        }
        public boolean includesContained()
        {
            return false;
        }
        public boolean includesContaining()
        {
            return true;
        }
        public boolean includesExact()
        {
            return true;
        }
    },
    
    /**
     * Expand to all the locales that are contained by this.
     * en would expand to en, en_GB, en_US, ....
     */
    LOCALE_AND_ALL_CONTAINED_LOCALES
    {
        public boolean includesAll()
        {
            return false;
        }
        public boolean includesContained()
        {
            return true;
        }
        public boolean includesContaining()
        {
            return false;
        }
        public boolean includesExact()
        {
            return true;
        }
    },
    
    /**
     * No prefix only
     */
    ALL_ONLY
    {
        public boolean includesAll()
        {
            return true;
        }
        public boolean includesContained()
        {
            return false;
        }
        public boolean includesContaining()
        {
            return false;
        }
        public boolean includesExact()
        {
            return false;
        }
    };
    
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
    
    public abstract boolean includesAll();
    
    public abstract boolean includesContained();
    
    public abstract boolean includesContaining();
    
    public abstract boolean includesExact();
    
}
