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

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.QName;

/**
 * A simple 1-1 property mapping from a CMIS property name to an alfresco property
 * 
 * @author florian.mueller
 */
public class DirectProperty extends AbstractProperty
{
    private QName alfrescoName;

    /**
     * Construct
     */
    public DirectProperty(ServiceRegistry serviceRegistry, CMISConnector connector, String propertyName,
            QName alfrescoName)
    {
        super(serviceRegistry, connector, propertyName);
        this.alfrescoName = alfrescoName;
    }

    public QName getMappedProperty()
    {
        return alfrescoName;
    }

    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        if (nodeInfo.getType() == null)
        {
            // Invalid node
            return null;
        }
        
        if (nodeInfo.getNodeRef() != null)
        {
            return getServiceRegistry().getNodeService().getProperty(nodeInfo.getNodeRef(), alfrescoName);
        }
        else if (nodeInfo.getAssociationRef() != null)
        {
            return getServiceRegistry().getNodeService().getProperty(
                    nodeInfo.getAssociationRef().getSourceRef(),
                    alfrescoName);
        }

        return null;
    }
}
