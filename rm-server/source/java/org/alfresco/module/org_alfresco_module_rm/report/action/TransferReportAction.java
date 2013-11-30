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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
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
        NodeRef[] transferNodes = getTransferNodes(nodeRef);

        // Get the disposition authority
        String dispositionAuthority = getDispositionAuthority(transferNodes);

        // Save to the properties map
        Map<String, Serializable> properties = new HashMap<String, Serializable>(2);
        properties.put("transferNodes", transferNodes);
        properties.put("dispositionAuthority", dispositionAuthority);

        return properties;
    }

    /**
     * Returns an array of NodeRefs representing the items to be transferred.
     *
     * @param transferNode The transfer object
     * @return Array of NodeRefs
     */
    private NodeRef[] getTransferNodes(NodeRef transferNode)
    {
        List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(transferNode,
                    RecordsManagementModel.ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
        NodeRef[] itemsToTransfer = new NodeRef[assocs.size()];
        for (int idx = 0; idx < assocs.size(); idx++)
        {
            itemsToTransfer[idx] = assocs.get(idx).getChildRef();
        }
        return itemsToTransfer;
    }

    /**
     * Gets the disposition authority from the array of the transfer objects
     *
     * @param itemsToTransfer   The transfer objects
     * @return  Disposition authority
     */
    private String getDispositionAuthority(NodeRef[] itemsToTransfer)
    {
        // use RMService to get disposition authority
        String dispositionAuthority = null;
        if (itemsToTransfer.length > 0)
        {
            // use the first transfer item to get to disposition schedule
            DispositionSchedule ds = dispositionService.getDispositionSchedule(itemsToTransfer[0]);
            if (ds != null)
            {
                dispositionAuthority = ds.getDispositionAuthority();
            }
        }
        return dispositionAuthority == null ? StringUtils.EMPTY : dispositionAuthority;
    }
}
