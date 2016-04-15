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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * 
 * @author steveglover
 *
 */
public class SecondaryTypesProperty extends AbstractProperty
{
    private CMISMapping cmisMapping;

    /**
     * Construct
     */
    public SecondaryTypesProperty(ServiceRegistry serviceRegistry, CMISConnector connector, CMISMapping cmisMapping)
    {
        super(serviceRegistry, connector, PropertyIds.SECONDARY_OBJECT_TYPE_IDS);
        this.cmisMapping = cmisMapping;
    }

    @Override
    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        NodeRef nodeRef = nodeInfo.getNodeRef();

        if(nodeRef == null || nodeInfo.getType() == null)
        {
        	// If the nodeRef or type is null, we can't handle it so return an empty list	
        	return (Serializable) Collections.emptyList();
        }

        Set<QName> aspects = nodeInfo.getNodeAspects();
        ArrayList<String> results = new ArrayList<String>(aspects.size());
        for (QName aspect : aspects)
        {
        	String typeId = cmisMapping.getCmisTypeId(aspect);
        	if (typeId != null)
        	{
        		results.add(typeId);
        	}
        }
        return results;
    }
}
