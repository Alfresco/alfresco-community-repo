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
    public static final String CONTENT_PROPERTY = "::content";

    private ServiceRegistry serviceRegistry;
    protected CMISConnector connector;
    private String propertyName;

    /**
     * Construct
     * 
     * @param serviceRegistry ServiceRegistry
     * @param connector CMISConnector
     * @param propertyName String
     */
    protected AbstractProperty(ServiceRegistry serviceRegistry, CMISConnector connector, String propertyName)
    {
        this.serviceRegistry = serviceRegistry;
        this.connector = connector;
        this.propertyName = propertyName;
    }

    protected String getGuid(String nodeId)
    {
    	int idx = nodeId.lastIndexOf("/");
    	if(idx != -1)
    	{
    		return nodeId.substring(idx+1);
    	}
    	else
    	{
    		return nodeId;
    	}
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
            Serializable value = nodeInfo.getPropertyValue(propertyName);
            return value;
        } else
        {
            Serializable value = getValueInternal(nodeInfo);
            nodeInfo.putPropertyValue(propertyName, value);
            return value;
        }
    }

    protected abstract Serializable getValueInternal(CMISNodeInfo nodeInfo);

    public CMISNodeInfo createNodeInfo(NodeRef nodeRef)
    {
        return connector.createNodeInfo(nodeRef);
    }

    public CMISNodeInfo createNodeInfo(AssociationRef assocRef)
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

            Serializable value = nodeInfo.getNodeProps().get(ContentModel.PROP_CONTENT);

            if (value != null)
            {
                contentData = DefaultTypeConverter.INSTANCE.convert(ContentData.class, value);
            }

            nodeInfo.putPropertyValue(CONTENT_PROPERTY, contentData);
            return contentData;
        }
    }
}
