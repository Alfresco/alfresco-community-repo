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

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;

/**
 * Get the CMIS object id property.
 */
public class NodeRefProperty extends AbstractProperty
{
    public static final String NodeRefPropertyId = "alfcmis:nodeRef";

    /**
     * Construct
     * 
     * @param serviceRegistry
     */
    public NodeRefProperty(ServiceRegistry serviceRegistry, CMISConnector connector)
    {
        super(serviceRegistry, connector, NodeRefPropertyId);
    }

    @Override
    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        if (nodeInfo.getNodeRef() != null)
        {
            if (nodeInfo.isCurrentVersion())
            {
                return nodeInfo.getCurrentNodeNodeRef().toString();
            } else
            {
                return nodeInfo.getNodeRef().toString();
            }
        } else if (nodeInfo.getAssociationRef() != null)
        {
            return nodeInfo.getAssociationRef().toString();
        }

        return null;
    }
}
