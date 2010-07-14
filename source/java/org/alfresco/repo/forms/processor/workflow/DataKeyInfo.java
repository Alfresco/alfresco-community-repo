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
 * @author Nick Smith
 *
 */
public class DataKeyInfo
{
    private final String dataKey;
    private final QName qName;
    private final FieldType fieldType;
    private final boolean isAdd;
    
    private DataKeyInfo(String dataKey, QName qName, FieldType fieldType, boolean isAdd)
    {
        this.dataKey = dataKey;
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
    
    public static DataKeyInfo makeTransientDataKeyInfo(String dataKey)
    {
        return new DataKeyInfo(dataKey, null, FieldType.TRANSIENT, true);
    }
    
    /**
     * @return the dataKey
     */
    public String getDataKey()
    {
        return dataKey;
    }
    
    /**
     * @return the qName
     */
    public QName getqName()
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
