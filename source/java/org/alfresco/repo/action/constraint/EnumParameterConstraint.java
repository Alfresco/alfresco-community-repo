/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.action.constraint;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Enumerated type parameter constraint
 * 
 * @author Roy Wetherall
 */
public class EnumParameterConstraint extends BaseParameterConstraint
{
    /** Enum class name */
    private String enumClassName;

    /** Enum clss */
    private Class<?> enumClass;
    
    /**
     * Set the enum class name
     * 
     * @param enumClassName enum class name
     */
    public void setEnumClassName(String enumClassName)
    {
        this.enumClassName = enumClassName;
    }
    
    /**
     * @see org.alfresco.service.cmr.action.ParameterConstraint#getAllowableValues()
     */
    protected Map<String, String> getAllowableValuesImpl()
    {                  
        // Get the enum class
        Class<?> enumClass = getEnumClass();
                                           
        Object[] enumValues = enumClass.getEnumConstants();
        Map<String, String> allowableValues = new HashMap<String, String>(enumValues.length);
        
        for (Object enumValue : enumValues)
        {
            // Look up the I18N value
            String displayLabel = getI18NLabel(enumValue.toString());
            
            // Add to the map of allowed values
            allowableValues.put(enumValue.toString(), displayLabel);                    
        } 
        
        return allowableValues;
    }    
    
    /**
     * Get the enum class
     * 
     * @return
     */
    private Class<?> getEnumClass()
    {
        if (this.enumClass == null)
        {        
            try
            {   
                // Check that a enum class name has specified
                if (enumClassName == null || enumClassName.length() == 0)
                {
                    throw new AlfrescoRuntimeException("No enum class has been defined");
                }
                
                // Get the enum class
                Class<?> enumClass = Class.forName(enumClassName);
                
                // Check that the class is an enum class
                if (enumClass.isEnum() == true)
                {
                    this.enumClass = enumClass;
                }
            }
            catch (ClassNotFoundException e)
            {
                throw new AlfrescoRuntimeException("Unable to find enum class " + this.enumClassName, e);
            }
        }
        return this.enumClass;
    }
}
