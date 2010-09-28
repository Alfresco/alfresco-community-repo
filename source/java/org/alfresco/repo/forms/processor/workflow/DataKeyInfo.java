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

package org.alfresco.repo.forms.processor.workflow;

import org.alfresco.service.namespace.QName;

/**
 * Data transfer object that represents a data key.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public class DataKeyInfo
{
    private final String fieldName;
    private final QName qName;
    private final FieldType fieldType;
    private final boolean isAdd;
    
    private DataKeyInfo(String dataKey, QName qName, FieldType fieldType, boolean isAdd)
    {
        this.fieldName = dataKey;
        this.qName = qName;
        this.fieldType = fieldType;
        this.isAdd = isAdd;
    }
    
    public static DataKeyInfo makeAssociationDataKeyInfo(String dataKey, QName qName, boolean isAdd)
    {
        return new DataKeyInfo(dataKey, qName, FieldType.ASSOCIATION, isAdd);
    }
    
    public static DataKeyInfo makePropertyDataKeyInfo(String dataKey, QName qName)
    {
        return new DataKeyInfo(dataKey, qName, FieldType.PROPERTY, true);
    }
    
    public static DataKeyInfo makeTransientPropertyDataKeyInfo(String dataKey)
    {
        return new DataKeyInfo(dataKey, null, FieldType.TRANSIENT_PROPERTY, true);
    }
    
    public static DataKeyInfo makeTransientAssociationDataKeyInfo(String dataKey, boolean isAdd)
    {
        return new DataKeyInfo(dataKey, null, FieldType.TRANSIENT_ASSOCIATION, isAdd);
    }
    
    /**
     * @return the fieldName
     */
    public String getFieldName()
    {
        return fieldName;
    }
    
    /**
     * @return the qName
     */
    public QName getQName()
    {
        return qName;
    }
    

    /**
     * @return the fieldType
     */
    public FieldType getFieldType()
    {
        return fieldType;
    }
    
    /**
     * @return the isAdd
     */
    public boolean isAdd()
    {
        return isAdd;
    }

}
