/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.opencmis.search;

import org.alfresco.opencmis.dictionary.PropertyDefinitionWrapper;
import org.alfresco.service.cmr.search.ResultSetColumn;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;

/**
 * @author andyh
 * 
 */
public class CMISResultSetColumn implements ResultSetColumn
{

    private String name;

    private PropertyDefinitionWrapper propertyDefinition;

    private PropertyType dataType;

    private QName alfrescoPropertyQName;

    private QName alfrescoDataTypeQName;

    CMISResultSetColumn(String name, PropertyDefinitionWrapper propertyDefinition, PropertyType dataType,
            QName alfrescoPropertyQName, QName alfrescoDataTypeQName)
    {
        this.name = name;
        this.propertyDefinition = propertyDefinition;
        this.dataType = dataType;
        this.alfrescoPropertyQName = alfrescoPropertyQName;
        this.alfrescoDataTypeQName = alfrescoDataTypeQName;
    }

    public String getName()
    {
        return name;
    }

    public PropertyDefinitionWrapper getCMISPropertyDefinition()
    {
        return propertyDefinition;
    }

    public PropertyType getCMISDataType()
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
