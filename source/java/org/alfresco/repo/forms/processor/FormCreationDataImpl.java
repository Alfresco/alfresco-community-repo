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

package org.alfresco.repo.forms.processor;

import java.util.List;
import java.util.Map;

/**
 * Simple DTO containing various objects needed to generate Forms.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public class FormCreationDataImpl implements FormCreationData
{
    private final Object itemData;
    private final List<String> forcedFields;
    private final Map<String, Object> context;

    public FormCreationDataImpl(Object itemData, List<String> forcedFields, Map<String, Object> context)
    {
        this.itemData = itemData;
        this.forcedFields = forcedFields;
        this.context = context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.forms.processor.FormCreationData#getItemData()
     */
    public Object getItemData()
    {
        return itemData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.repo.forms.processor.FormCreationData#isForcedField(java
     * .lang.String)
     */
    public boolean isForcedField(String fieldName)
    {
        if (forcedFields == null)
        {
            return false;
        }
        
        return forcedFields.contains(fieldName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.forms.processor.FormCreationData#getContext()
     */
    public Map<String, Object> getContext()
    {
        return context;
    }
}
