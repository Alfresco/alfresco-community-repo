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
import java.util.ArrayList;
import java.util.Collections;

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * Get the CMIS allowedChildObjectTypeIds property.
 * 
 * @author florian.mueller
 */
public class AllowedChildObjectTypeIdsProperty extends AbstractProperty
{
    private CMISMapping cmisMapping;

    /**
     * Construct
     * 
     * @param serviceRegistry
     */
    public AllowedChildObjectTypeIdsProperty(ServiceRegistry serviceRegistry, CMISConnector connector,
            CMISMapping cmisMapping)
    {
        super(serviceRegistry, connector, PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS);
        this.cmisMapping = cmisMapping;
    }

    @Override
    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        TypeDefinition type = getServiceRegistry().getDictionaryService()
                .getType(nodeInfo.getType().getAlfrescoClass());
        if ((type != null) && (type.getChildAssociations() != null) && (!type.getChildAssociations().isEmpty()))
        {
            ArrayList<String> result = new ArrayList<String>();

            for (ChildAssociationDefinition cad : type.getChildAssociations().values())
            {
                String typeId = cmisMapping.getCmisTypeId(cad.getTargetClass().getName());
                if (typeId != null)
                {
                    result.add(typeId);
                }
            }

            return result;
        }

        return (Serializable) Collections.emptyList();
    }
}
