/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.content.transform;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * Transformation options for the runtime executable transformer.
 * <p>
 * Values set here are mapped to ${valueName} style strings in the tranformer
 * execution string. 
 * 
 * @author Roy Wetherall
 */
public class RuntimeExecutableContentTransformerOptions extends TransformationOptions
{
    /** Map of property values */
    private Map<String, String> propertyValues = new HashMap<String, String>(11);
    
    /**
     * Sets the map of property values that are used when executing the transformer
     * 
     * @param propertyValues    property value
     */
    public void setPropertyValues(Map<String, String> propertyValues)
    {
        this.propertyValues = propertyValues;
    }
    
    /**
     * Overrides the base class implementation to add all values set in {@link #setPropertyValues(Map)}
     */
    @Override
    public Map<String, Object> toMap()
    {
        Map<String, Object> baseProps = super.toMap();
        Map<String, Object> props = new HashMap<String, Object>(baseProps);
        props.putAll(propertyValues);
        return props;
    }
}
