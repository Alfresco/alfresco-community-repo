/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.util;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Helper class for for use when checking properties.  This class uses
 * I18N for its messages.
 * 
 * @author Derek Hulley
 */
public class PropertyCheck
{
    public static final String ERR_PROPERTY_NOT_SET = "system.err.property_not_set";
    
    /**
     * Checks that the property with the given name is not null.
     * 
     * @param target the object on which the property must have been set
     * @param propertyName the name of the property
     * @param value of the property value
     */
    public static void mandatory(Object target, String propertyName, Object value)
    {
        if (value == null)
        {
            throw new AlfrescoRuntimeException(
                    ERR_PROPERTY_NOT_SET,
                    new Object[] {propertyName, target, target.getClass()});
        }
    }

    /**
     * Checks that the given string is not:
     * <ul>
     *   <li>null</li>
     *   <li>empty</li>
     *   <li>a placeholder of form '${...}'</li>
     * </ul>
     *
     * @param value         the value to check
     * @return              <tt>true</tt> if the checks all pass
     */
    public static boolean isValidPropertyString(String value)
    {
        if (value == null || value.length() == 0)
        {
            return false;
        }
        if (value.startsWith("${") && value.endsWith("}"))
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    
    /**
     * Dig out the property name from a placeholder-style property of form
     * <b>${prop.name}</b>, which will yield <b>prop.name</b>.  If the placeholders
     * are not there, the value is returned directly.  <tt>null</tt> values are
     * not allowed, but empty strings are.
     * 
     * @param value     The property with or without property placeholders
     * @return          Returns the core property without the property placeholders
     *                  <b>${</b> and <b>}</b>.
     * @throws IllegalArgumentException         if the value is <tt>null</tt>
     */
    public static String getPropertyName(String value)
    {
        if (value == null)
        {
            throw new IllegalArgumentException("'value' is a required argument.");
        }
        if (!value.startsWith("${"))
        {
            return value;
        }
        if (!value.endsWith("}"))
        {
            return value;
        }
        int strLen = value.length();
        return value.substring(2, strLen - 1);
    }
}