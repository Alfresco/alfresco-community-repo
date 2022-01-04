/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.email;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

/**
 * Extended RFC822 Metadata Extractor that is sensitive to whether we are in a RM
 * site or not.
 *
 * @author Roy Wetherall
 */
public class RFC822MetadataExtracter extends org.alfresco.repo.content.metadata.RFC822MetadataExtracter
{
    /** Reference to default properties */
    private static final String PROPERTIES_URL = "alfresco/metadata/RFC822MetadataExtracter.properties";

    /** Node service */
    private NodeService nodeService;

    /**
     * Sets the node service
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @see org.alfresco.repo.content.metadata.AbstractMappingMetadataExtracter#filterSystemProperties(java.util.Map, java.util.Map)
     */
    @Override
    protected void filterSystemProperties(Map<QName, Serializable> systemProperties, Map<QName, Serializable> targetProperties)
    {
        NodeRef nodeRef = getNodeRef(targetProperties);
        if(nodeRef == null)
        {
            return;
        }

        // Remove record properties from non-record nodes
        if (!nodeService.hasAspect(nodeRef, RecordsManagementModel.ASPECT_RECORD))
        {
            // Remove all rm namespace properties from the system map
            Map<QName, Serializable> clone = new HashMap<>(systemProperties);
            for (QName propName : clone.keySet())
            {
                if (RecordsManagementModel.RM_URI.equals(propName.getNamespaceURI()))
                {
                    systemProperties.remove(propName);
                }
            }
        }

        // Remove dod5015 properties from non-dod5015 nodes
        if (!nodeService.hasAspect(nodeRef, DOD5015Model.ASPECT_DOD_5015_RECORD))
        {
            // Remove all dod5015 namespace properties from the system map
            Map<QName, Serializable> clone = new HashMap<>(systemProperties);
            for (QName propName : clone.keySet())
            {
                if (DOD5015Model.DOD_URI.equals(propName.getNamespaceURI()))
                {
                    systemProperties.remove(propName);
                }
            }
        }
    }

    /**
     * @see org.alfresco.repo.content.metadata.AbstractMappingMetadataExtracter#getDefaultMapping()
     */
    protected Map<String, Set<QName>> getDefaultMapping()
    {
        // Attempt to load the properties
        return readMappingProperties(PROPERTIES_URL);
    }

    /**
     * Given a set of properties, try and retrieve the node reference
     * @param properties    node properties
     * @return NodeRef      null if none, otherwise valid node reference
     */
    private NodeRef getNodeRef(Map<QName, Serializable> properties)
    {
        NodeRef result = null;

        // Get the elements of the node reference
        String storeProto = (String)properties.get(ContentModel.PROP_STORE_PROTOCOL);
        String storeId = (String)properties.get(ContentModel.PROP_STORE_IDENTIFIER);
        String nodeId = (String)properties.get(ContentModel.PROP_NODE_UUID);

        if (storeProto != null && storeProto.length() != 0 &&
            storeId != null && storeId.length() != 0 &&
            nodeId != null && nodeId.length() != 0)

        {
            // Create the node reference
            result = new NodeRef(new StoreRef(storeProto, storeId), nodeId);
        }

        return result;
    }
}
