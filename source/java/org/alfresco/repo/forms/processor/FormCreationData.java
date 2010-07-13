/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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

package org.alfresco.repo.forms.processor;

import java.util.List;
import java.util.Map;

/**
 * Simple DTO containing various objects needed to generate Forms.
 * @author Nick Smith
 */
public class FormCreationData
{
    private final Object              itemData;
    private final List<String>        forcedFields;
    private final Map<String, Object> context;
    
    public FormCreationData(Object itemData,
            List<String> forcedFields,
            Map<String, Object> context)
    {
        this.itemData = itemData;
        this.forcedFields = forcedFields;
        this.context = context;
    }

    
    /**
     * @return the itemData
     */
    public Object getItemData()
    {
        return itemData;
    }
    
    /**
     * @return If the <code>fieldName</code> given is a forced field then
     *         returns <code>true</code>, otherwise returns <code>false</code>.
     */
    public boolean isForcedField(String fieldName)
    {
        if (forcedFields == null)
            return false;
        return forcedFields.contains(fieldName);
    }
    
    /**
     * @return the context
     */
    public Map<String, Object> getContext()
    {
        return context;
    }
}
