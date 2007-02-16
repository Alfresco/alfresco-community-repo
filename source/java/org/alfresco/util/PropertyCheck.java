/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
                    new Object[] {propertyName, target});
        }
    }
}
