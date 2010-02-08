/*
 * Copyright (C) 2009-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.repo.action.constraint;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.extensions.surf.util.I18NUtil;

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
    
    /** Map of allowable values */
    private Map<String, String> allowableValues;
    
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
    public Map<String, String> getAllowableValues()
    {
        if (this.allowableValues == null)
        {            
            // Get the enum class
            Class<?> enumClass = getEnumClass();
                                               
            Object[] enumValues = enumClass.getEnumConstants();
            this.allowableValues = new HashMap<String, String>(enumValues.length);
            
            for (Object enumValue : enumValues)
            {
                // Look up the I18N value
                String displayLabel = getI18NLabel(enumClass.getName(), enumValue);
                
                // Add to the map of allowed values
                this.allowableValues.put(enumValue.toString(), displayLabel);                    
            } 
        }
        
        return this.allowableValues;
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
    
    /**
     * Get the I18N display label for a particular enum value
     * 
     * @param enumClassName
     * @param enumValue
     * @return
     */
    private String getI18NLabel(String enumClassName, Object enumValue)
    {
        String result = enumValue.toString();
        StringBuffer key = new StringBuffer(name).
                                    append(".").
                                    append(enumValue.toString().toLowerCase());
        String i18n = I18NUtil.getMessage(key.toString());
        if (i18n != null)
        {
            result = i18n;
        }
        return result;
    }
    
    /**
     * @see org.alfresco.service.cmr.action.ParameterConstraint#getValueDisplayLabel(java.io.Serializable)
     */
    public String getValueDisplayLabel(String value)
    {
        return getAllowableValues().get(value);        
    }

    /**
     * @see org.alfresco.service.cmr.action.ParameterConstraint#isValidValue(java.io.Serializable)
     */
    public boolean isValidValue(String value)
    {
        return getAllowableValues().containsKey(value);
    }
}
