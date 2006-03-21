/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
