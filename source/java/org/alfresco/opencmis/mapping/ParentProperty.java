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
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * Get the CMIS parent property
 * 
 * @author florian.mueller
 * 
 */
public class ParentProperty extends AbstractProperty
{
    /**
     * Construct
     * 
     * @param serviceRegistry
     */
    public ParentProperty(ServiceRegistry serviceRegistry, CMISConnector connector)
    {
        super(serviceRegistry, connector, PropertyIds.PARENT_ID);
    }

    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        if (nodeInfo.isRootFolder())
        {
            return null;
        }

        ChildAssociationRef car = getServiceRegistry().getNodeService().getPrimaryParent(nodeInfo.getNodeRef());
        if ((car != null) && (car.getParentRef() != null))
        {
            return car.getParentRef().toString();
        }

        return null;
    }
}
