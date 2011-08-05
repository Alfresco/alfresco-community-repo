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
package org.alfresco.opencmis.mapping;

import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.opencmis.dictionary.CMISPropertyAccessor;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;

/**
 * Base class for all property accessors
 * 
 * @author andyh
 * 
 */
public abstract class AbstractProperty implements CMISPropertyAccessor
{
    private static final String CONTENT_PROPERTY = "::content";

    private ServiceRegistry serviceRegistry;
    private CMISConnector connector;
    private String propertyName;

    /**
     * Construct
     * 
     * @param serviceRegistry
     * @param propertyName
     */
    protected AbstractProperty(ServiceRegistry serviceRegistry, CMISConnector connector, String propertyName)
    {
        this.serviceRegistry = serviceRegistry;
        this.connector = connector;
        this.propertyName = propertyName;
    }

    /**
     * @return service registry
     */
    protected ServiceRegistry getServiceRegistry()
    {
        return serviceRegistry;
    }

    public String getName()
    {
        return propertyName;
    }

    public QName getMappedProperty()
    {
        return null;
    }

    @Override
    public void setValue(NodeRef nodeRef, Serializable value)
    {
        throw new UnsupportedOperationException();
    }

    public Serializable getValue(NodeRef nodeRef)
    {
        return getValue(createNodeInfo(nodeRef));
    }

    public Serializable getValue(AssociationRef assocRef)
    {
        return getValue(createNodeInfo(assocRef));
    }

    public Serializable getValue(CMISNodeInfo nodeInfo)
    {
        if (nodeInfo.containsPropertyValue(propertyName))
        {
            return nodeInfo.getPropertyValue(propertyName);
        } else
        {
            Serializable value = getValueInternal(nodeInfo);
            nodeInfo.putPropertyValue(propertyName, value);
            return value;
        }
    }

    protected abstract Serializable getValueInternal(CMISNodeInfo nodeInfo);

    protected CMISNodeInfo createNodeInfo(NodeRef nodeRef)
    {
        return connector.createNodeInfo(nodeRef);
    }

    protected CMISNodeInfo createNodeInfo(AssociationRef assocRef)
    {
        return connector.createNodeInfo(assocRef);
    }

    protected ContentData getContentData(CMISNodeInfo nodeInfo)
    {
        if (!nodeInfo.isDocument())
        {
            return null;
        }

        if (nodeInfo.containsPropertyValue(CONTENT_PROPERTY))
        {
            return (ContentData) nodeInfo.getPropertyValue(CONTENT_PROPERTY);
        } else
        {
            ContentData contentData = null;

            Serializable value = getServiceRegistry().getNodeService().getProperty(nodeInfo.getNodeRef(),
                    ContentModel.PROP_CONTENT);
            if (value != null)
            {
                contentData = DefaultTypeConverter.INSTANCE.convert(ContentData.class, value);
            }

            nodeInfo.putPropertyValue(CONTENT_PROPERTY, contentData);
            return contentData;
        }
    }
}
