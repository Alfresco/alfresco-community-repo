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
