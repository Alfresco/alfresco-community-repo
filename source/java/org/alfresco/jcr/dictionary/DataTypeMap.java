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
package org.alfresco.jcr.dictionary;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;


/**
 * Responsible for mapping Alfresco Data Types to JCR Property Types and vice versa.
 * 
 * @author David Caruana
 */
public class DataTypeMap
{

    /** Map of Alfresco Data Type to JCR Property Type */
    private static Map<QName, Integer> dataTypeToPropertyType = new HashMap<QName, Integer>();
    static
    {
        dataTypeToPropertyType.put(DataTypeDefinition.TEXT, PropertyType.STRING);
        dataTypeToPropertyType.put(DataTypeDefinition.MLTEXT, PropertyType.STRING);
        dataTypeToPropertyType.put(DataTypeDefinition.CONTENT, PropertyType.BINARY);
        dataTypeToPropertyType.put(DataTypeDefinition.INT, PropertyType.LONG);
        dataTypeToPropertyType.put(DataTypeDefinition.LONG, PropertyType.LONG);
        dataTypeToPropertyType.put(DataTypeDefinition.FLOAT, PropertyType.DOUBLE);
        dataTypeToPropertyType.put(DataTypeDefinition.DOUBLE, PropertyType.DOUBLE);
        dataTypeToPropertyType.put(DataTypeDefinition.DATE, PropertyType.DATE);
        dataTypeToPropertyType.put(DataTypeDefinition.DATETIME, PropertyType.DATE);
        dataTypeToPropertyType.put(DataTypeDefinition.BOOLEAN, PropertyType.BOOLEAN);
        dataTypeToPropertyType.put(DataTypeDefinition.QNAME, PropertyType.NAME);
        dataTypeToPropertyType.put(DataTypeDefinition.CATEGORY, PropertyType.STRING);  // TODO: Check this mapping
        dataTypeToPropertyType.put(DataTypeDefinition.NODE_REF, PropertyType.REFERENCE);
        dataTypeToPropertyType.put(DataTypeDefinition.PATH, PropertyType.PATH);
        dataTypeToPropertyType.put(DataTypeDefinition.ANY, PropertyType.UNDEFINED);
        dataTypeToPropertyType.put(DataTypeDefinition.ENCRYPTED, PropertyType.UNDEFINED);
        dataTypeToPropertyType.put(DataTypeDefinition.LOCALE, PropertyType.STRING);
    }
    
    /** Map of JCR Property Type to Alfresco Data Type */
    private static Map<Integer, QName> propertyTypeToDataType = new HashMap<Integer, QName>();
    static
    {
        propertyTypeToDataType.put(PropertyType.STRING, DataTypeDefinition.TEXT);
        propertyTypeToDataType.put(PropertyType.BINARY, DataTypeDefinition.CONTENT);
        propertyTypeToDataType.put(PropertyType.LONG, DataTypeDefinition.LONG);
        propertyTypeToDataType.put(PropertyType.DOUBLE, DataTypeDefinition.DOUBLE);
        propertyTypeToDataType.put(PropertyType.DATE, DataTypeDefinition.DATETIME);
        propertyTypeToDataType.put(PropertyType.BOOLEAN, DataTypeDefinition.BOOLEAN);
        propertyTypeToDataType.put(PropertyType.NAME, DataTypeDefinition.QNAME);
        propertyTypeToDataType.put(PropertyType.REFERENCE, DataTypeDefinition.NODE_REF);
        propertyTypeToDataType.put(PropertyType.PATH, DataTypeDefinition.PATH);
        propertyTypeToDataType.put(PropertyType.UNDEFINED, DataTypeDefinition.ANY);
    }
    
    /**
     * Convert an Alfresco Data Type to a JCR Property Type
     * 
     * @param datatype  alfresco data type
     * @return  JCR property type
     * @throws RepositoryException
     */
    public static int convertDataTypeToPropertyType(QName datatype)
    {
        Integer propertyType = dataTypeToPropertyType.get(datatype);
        if (propertyType == null)
        {
            throw new AlfrescoRuntimeException("Cannot map Alfresco data type " + datatype + " to JCR property type.");
        }
        return propertyType;
    }

    /**
     * Convert a JCR Property Type to an Alfresco Data Type
     * 
     * @param  propertyType  JCR property type
     * @return  alfresco data type
     * @throws RepositoryException
     */
    public static QName convertPropertyTypeToDataType(int propertyType)
    {
        QName type = propertyTypeToDataType.get(propertyType);
        if (type == null)
        {
            throw new AlfrescoRuntimeException("Cannot map JCR property type " + propertyType + " to Alfresco data type.");
        }
        return type;
    }

}
