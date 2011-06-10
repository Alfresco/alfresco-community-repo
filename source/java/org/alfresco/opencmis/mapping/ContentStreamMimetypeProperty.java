/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.opencmis.mapping;

import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQuery;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * Accessor for CMIS content stream mimetype property
 * 
 * @author andyh
 */
public class ContentStreamMimetypeProperty extends AbstractProperty
{
    /**
     * Construct
     * 
     * @param serviceRegistry
     */
    public ContentStreamMimetypeProperty(ServiceRegistry serviceRegistry)
    {
        super(serviceRegistry, PropertyIds.CONTENT_STREAM_MIME_TYPE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.cmis.property.PropertyAccessor#getValue(org.alfresco.service
     * .cmr.repository.NodeRef)
     */
    public Serializable getValue(NodeRef nodeRef)
    {
        Serializable value = getServiceRegistry().getNodeService().getProperty(nodeRef, ContentModel.PROP_CONTENT);
        if (value != null)
        {
            ContentData contentData = DefaultTypeConverter.INSTANCE.convert(ContentData.class, value);
            return contentData.getMimetype();
        } else
        {
            return "";
        }
    }

    public String getLuceneFieldName()
    {
        StringBuilder field = new StringBuilder(128);
        field.append("@");
        field.append(ContentModel.PROP_CONTENT);
        field.append(".mimetype");
        return field.toString();
    }
    
    public QName getMappedProperty()
    {
        // spoof
        return GetChildrenCannedQuery.SORT_QNAME_CONTENT_MIMETYPE;
    }

    protected String getValueAsString(Serializable value)
    {
        Object converted = DefaultTypeConverter.INSTANCE.convert(getServiceRegistry().getDictionaryService()
                .getDataType(DataTypeDefinition.TEXT), value);
        String asString = DefaultTypeConverter.INSTANCE.convert(String.class, converted);
        return asString;
    }

    protected QName getQNameForExists()
    {
        return ContentModel.PROP_CONTENT;
    }

    protected DataTypeDefinition getInDataType()
    {
        return getServiceRegistry().getDictionaryService().getDataType(DataTypeDefinition.TEXT);
    }
}
