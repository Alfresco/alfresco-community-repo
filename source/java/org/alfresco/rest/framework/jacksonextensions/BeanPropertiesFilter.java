package org.alfresco.rest.framework.jacksonextensions;

import java.util.Set;

/**
 * Basic bean filtering.
 * 
 * It's based on Jackson's BeanPropertyFilter but uses slightly different method signatures
 *
 * @author Gethin James
 */
public class BeanPropertiesFilter
{
    private final Set<String> filteredProperties;
    public static final BeanPropertiesFilter ALLOW_ALL = new AllProperties();
    
    public BeanPropertiesFilter(Set<String> properties) {
        filteredProperties = properties;
 //       properties.add("id"); //always need id
    }
    
    /**
     * Indicates if the given property name is permitted to be used ie. is not filtered out
     * @param propertyName - bean property name
     * @return true - if the property is allowed to be used.
     */
    public boolean isAllowed(String propertyName)
    {
      return filteredProperties.contains(propertyName);  
    }
    
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("BeanPropertiesFilter [filteredProperties=").append(this.filteredProperties).append("]");
        return builder.toString();
    }
    
    /**
     * Default All properties filter
     *
     * @author Gethin James
     */
    public static class AllProperties extends BeanPropertiesFilter
    {

        public AllProperties()
        {
            super(null);
        }

        /*
         * @see org.alfresco.rest.framework.jacksonextensions.BeanPropertiesFilter#isAllowed(java.lang.String)
         */
        @Override
        public boolean isAllowed(String propertyName)
        {
            return true;
        }
        
        
    }
                
}
