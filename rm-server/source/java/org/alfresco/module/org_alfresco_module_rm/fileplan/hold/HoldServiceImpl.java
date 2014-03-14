/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.fileplan.hold;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Hold service implementation
 *
 * @author Tuna Aksoy
 * @since 2.2
 */
public class HoldServiceImpl implements HoldService, RecordsManagementModel
{
    /** Logger */
    private static Log logger = LogFactory.getLog(HoldServiceImpl.class);

    /** File Plan Service */
    private FilePlanService filePlanService;

    /** Node Service */
    private NodeService nodeService;

    /** Record Service */
    private RecordService recordService;

    /** Record Folder Service */
    private RecordFolderService recordFolderService;

    /**
     * Set the file plan service
     *
     * @param filePlanService the file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * Set the node service
     *
     * @param nodeService the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set the record service
     *
     * @param recordService the record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    /**
     * Set the record folder service
     *
     * @param recordFolderService the record folder service
     */
    public void setRecordFolderService(RecordFolderService recordFolderService)
    {
        this.recordFolderService = recordFolderService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.hold.HoldService#getHolds(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public List<NodeRef> getHolds(NodeRef filePlan)
    {
        ParameterCheck.mandatory("filePlan", filePlan);

        NodeRef holdContainer = filePlanService.getHoldContainer(filePlan);
        List<ChildAssociationRef> holdsAssocs = nodeService.getChildAssocs(holdContainer, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
        List<NodeRef> holds = new ArrayList<NodeRef>(holdsAssocs.size());
        for (ChildAssociationRef holdAssoc : holdsAssocs)
        {
            holds.add(holdAssoc.getChildRef());
        }

        return holds;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.hold.HoldService#getHolds(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    @Override
    public List<NodeRef> getHolds(NodeRef nodeRef, boolean includedInHold)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        List<NodeRef> result = new ArrayList<NodeRef>();

        List<ChildAssociationRef> holdsAssocs = nodeService.getParentAssocs(nodeRef, ASSOC_FROZEN_RECORDS, ASSOC_FROZEN_RECORDS);
        List<NodeRef> holdsNotIncludingNodeRef = new ArrayList<NodeRef>(holdsAssocs.size());
        for (ChildAssociationRef holdAssoc : holdsAssocs)
        {
            holdsNotIncludingNodeRef.add(holdAssoc.getParentRef());
        }
        result.addAll(holdsNotIncludingNodeRef);

        if (!includedInHold)
        {
            NodeRef filePlan = filePlanService.getFilePlan(nodeRef);
            List<NodeRef> allHolds = getHolds(filePlan);
            @SuppressWarnings("unchecked")
            List<NodeRef> holdsIncludingNodeRef = ListUtils.subtract(allHolds, holdsNotIncludingNodeRef);
            result.clear();
            result.addAll(holdsIncludingNodeRef);
        }

        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.hold.HoldService#addToHoldContainer(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void addToHoldContainer(NodeRef hold, NodeRef nodeRef)
    {
        ParameterCheck.mandatory("hold", hold);
        ParameterCheck.mandatory("nodeRef", nodeRef);

        List<NodeRef> holds = new ArrayList<NodeRef>(1);
        holds.add(hold);
        addToHoldContainers(Collections.unmodifiableList(holds), nodeRef);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.hold.HoldService#addToHoldContainers(java.util.List, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void addToHoldContainers(List<NodeRef> holds, NodeRef nodeRef)
    {
        ParameterCheck.mandatoryCollection("holds", holds);
        ParameterCheck.mandatory("nodeRef", nodeRef);

        for (NodeRef hold : holds)
        {
            // Link the record to the hold
            nodeService.addChild(hold, nodeRef, ASSOC_FROZEN_RECORDS, ASSOC_FROZEN_RECORDS);

            Map<QName, Serializable> props = new HashMap<QName, Serializable>(2);
            // Apply the freeze aspect
            props.put(PROP_FROZEN_AT, new Date());
            props.put(PROP_FROZEN_BY, AuthenticationUtil.getFullyAuthenticatedUser());
            boolean hasFrozenAspect = nodeService.hasAspect(nodeRef, ASPECT_FROZEN);


            if (!hasFrozenAspect)
            {
                nodeService.addAspect(nodeRef, ASPECT_FROZEN, props);

                if (logger.isDebugEnabled())
                {
                    StringBuilder msg = new StringBuilder();
                    msg.append("Frozen aspect applied to '").append(nodeRef).append("'.");
                    logger.debug(msg.toString());
                }
            }

            // Mark all the folders contents as frozen
            if (recordFolderService.isRecordFolder(nodeRef))
            {
                List<NodeRef> records = recordService.getRecords(nodeRef);
                for (NodeRef record : records)
                {
                    // no need to freeze if already frozen!
                    if (!hasFrozenAspect)
                    {
                        nodeService.addAspect(record, ASPECT_FROZEN, props);

                        if (logger.isDebugEnabled())
                        {
                            StringBuilder msg = new StringBuilder();
                            msg.append("Frozen aspect applied to '").append(record).append("'.");
                            logger.debug(msg.toString());
                        }
                    }
                }
            }
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.hold.HoldService#removeFromHoldContainer(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void removeFromHoldContainer(NodeRef hold, NodeRef nodeRef)
    {
        ParameterCheck.mandatory("hold", hold);
        ParameterCheck.mandatory("nodeRef", nodeRef);

        List<NodeRef> holds = new ArrayList<NodeRef>(1);
        holds.add(hold);
        removeFromHoldContainers(Collections.unmodifiableList(holds), nodeRef);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.hold.HoldService#removeFromHoldContainers(java.util.List, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void removeFromHoldContainers(List<NodeRef> holds, NodeRef nodeRef)
    {
        ParameterCheck.mandatory("holds", holds);
        ParameterCheck.mandatory("nodeRef", nodeRef);

        for (NodeRef hold : holds)
        {
            nodeService.removeChild(hold, nodeRef);
        }

        List<NodeRef> holdList = getHolds(nodeRef, true);
        if (holdList.size() == 0)
        {
            nodeService.removeAspect(nodeRef, ASPECT_FROZEN);
        }
    }
}
