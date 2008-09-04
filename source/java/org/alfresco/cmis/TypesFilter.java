package org.alfresco.cmis;

/**
 * Types Filter
 *  
 * @author davidc
 */
public enum TypesFilter
{
    Documents,
    Folders,
    Policies,
    Any;
    
    /**
     * Gets the default Types filter
     * 
     * @return  default types filter
     */
    public static TypesFilter getDefault()
    {
        return Any;
    }

    /**
     * Is specified Types filter valid?
     * 
     * @param typesFilter  types filter
     * @return  true => valid
     */
    public static boolean isValid(String typesFilter)
    {
        try
        {
            TypesFilter.valueOf(typesFilter);
            return true;
        }
        catch(IllegalArgumentException e)
        {
            return false;
        }
        catch(NullPointerException e)
        {
            return false;
        }
    }
    
    /**
     * Resolve to a Types Filter
     * 
     * NOTE: If specified types filter is not specified or invalid, the default types
     *       filter is returned
     *       
     * @param typesFilter  types filter
     * @return  resolved types filter
     */
    public static TypesFilter toTypesFilter(String typesFilter)
    {
        if (isValid(typesFilter))
        {
            return TypesFilter.valueOf(typesFilter);
        }
        else
        {
            return getDefault();
        }
    }
    
}