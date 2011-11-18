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
import java.util.Collections;

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * Get the CMIS object type id property
 * 
 * @author florian.mueller
 */
public class ObjectTypeIdProperty extends AbstractProperty
{
    /**
     * Construct
     * 
     * @param serviceRegistry
     */
    public ObjectTypeIdProperty(ServiceRegistry serviceRegistry, CMISConnector connector,
            CMISDictionaryService dictionaryService)
    {
        super(serviceRegistry, connector, PropertyIds.OBJECT_TYPE_ID);
    }

    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        if(nodeInfo.getType() == null)
        {
            return (Serializable) Collections.emptyList();
        }
        
        return nodeInfo.getType().getTypeId();
    }
}
