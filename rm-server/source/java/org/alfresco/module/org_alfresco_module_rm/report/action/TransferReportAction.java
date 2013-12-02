/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.report.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.lang.StringUtils;

/**
 * Transfer report action
 *
 * @author Tuna Aksoy
 * @since 2.2
 */
public class TransferReportAction extends BaseReportAction
{
    @Override
    protected Map<String, Serializable> addProperties(NodeRef nodeRef)
    {
        // Get all 'transferred' nodes
        List<TransferNode> transferNodes = getTransferNodes(nodeRef);

        // Get the disposition authority
        String dispositionAuthority = getDispositionAuthority(transferNodes);

        // Save to the properties map
        Map<String, Serializable> properties = new HashMap<String, Serializable>(2);
        properties.put("transferNodes", (ArrayList<TransferNode>) transferNodes);
        properties.put("dispositionAuthority", dispositionAuthority);

        return properties;
    }

    /**
     * Returns a list of transfer nodes
     *
     * @param nodeRef The transfer object
     * @return Transfer node list
     */
    private List<TransferNode> getTransferNodes(NodeRef nodeRef)
    {
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, RecordsManagementModel.ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
        List<TransferNode> transferNodes = new ArrayList<TransferNode>(assocs.size());
        for (ChildAssociationRef assoc : assocs)
        {
            NodeRef childRef = assoc.getChildRef();
            boolean isFolder = dictionaryService.isSubClass(nodeService.getType(childRef), ContentModel.TYPE_FOLDER);
            Map<String, Serializable> properties = getTransferNodeProperties(childRef, isFolder);
            transferNodes.add(new TransferNode(childRef, isFolder, properties));
        }
        return transferNodes;
    }

    /**
     * Helper method to get the properties of a transfer node
     *
     * @param childRef  Node reference
     * @param isFolder  Type of the transfer node
     * @return Transfer node properties
     */
    private Map<String, Serializable> getTransferNodeProperties(NodeRef childRef, boolean isFolder)
    {
        Map<String, Serializable> transferNodeProperties = new HashMap<String, Serializable>(2);
        if (isFolder)
        {
            Map<QName, Serializable> properties = nodeService.getProperties(childRef);
            transferNodeProperties.put("name", properties.get(ContentModel.PROP_NAME));
            transferNodeProperties.put("identifier", properties.get(RecordsManagementModel.PROP_IDENTIFIER));
        }
        else
        {
            // FIXME: Record
        }
        return transferNodeProperties;
    }

    /**
     * Gets the disposition authority from the list of the transfer nodes
     *
     * @param transferNodes   The transfer nodes
     * @return Disposition authority
     */
    private String getDispositionAuthority(List<TransferNode> transferNodes)
    {
        // use RMService to get disposition authority
        String dispositionAuthority = null;
        if (transferNodes.size() > 0)
        {
            // use the first transfer item to get to disposition schedule
            NodeRef nodeRef = transferNodes.iterator().next().getNodeRef();
            DispositionSchedule ds = dispositionService.getDispositionSchedule(nodeRef);
            if (ds != null)
            {
                dispositionAuthority = ds.getDispositionAuthority();
            }
        }
        return dispositionAuthority == null ? StringUtils.EMPTY : dispositionAuthority;
    }
}
