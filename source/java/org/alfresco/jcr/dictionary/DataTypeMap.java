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
