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
package org.alfresco.cmis.search;

import org.alfresco.cmis.CMISDataTypeEnum;
import org.alfresco.cmis.CMISPropertyDefinition;
import org.alfresco.cmis.CMISResultSetColumn;
import org.alfresco.service.namespace.QName;

/**
 * @author andyh
 *
 */
public class CMISResultSetColumnImpl implements CMISResultSetColumn
{

    private String name;
    
    private CMISPropertyDefinition propertyDefinition;
    
    private CMISDataTypeEnum dataType;
    
    private QName alfrescoPropertyQName;
    
    private QName alfrescoDataTypeQName;
    
    CMISResultSetColumnImpl(String name, CMISPropertyDefinition propertyDefinition, CMISDataTypeEnum dataType, QName alfrescoPropertyQName, QName alfrescoDataTypeQName)
    {
        this.name = name;
        this.propertyDefinition = propertyDefinition;
        this.dataType = dataType;
        this.alfrescoPropertyQName = alfrescoPropertyQName;
        this.alfrescoDataTypeQName = alfrescoDataTypeQName;
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.cmis.search.CMISResultSetColumn#getName()
     */
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.search.CMISResultSetColumn#getPropertyDefinition()
     */
    public CMISPropertyDefinition getCMISPropertyDefinition()
    {
        return propertyDefinition;
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.search.CMISResultSetColumn#getPropertyType()
     */
    public CMISDataTypeEnum getCMISDataType()
    {
       return dataType;
    }


    public QName getDataType()
    {
        return alfrescoDataTypeQName;
    }


    public QName getPropertyType()
    {
        return alfrescoPropertyQName;
    }

}
