/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.freeze;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.hold.HoldService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Freeze Service Implementation
 *
 * @author Roy Wetherall
 * @author Tuna Aksoy
 * @since 2.1
 */
public class FreezeServiceImpl extends    ServiceBaseImpl
                               implements FreezeService,
                                          RecordsManagementModel
{
    /** Logger */
    private static Log logger = LogFactory.getLog(FreezeServiceImpl.class);

    /** I18N */
    private static final String MSG_FREEZE_ONLY_RECORDS_FOLDERS = "rm.action.freeze-only-records-folders";
    private static final String MSG_HOLD_NAME = "rm.hold.name";

    /** Hold node reference key */
    private static final String KEY_HOLD_NODEREF = "holdNodeRef";

    /** Record service */
    protected RecordService recordService;

    /** File Plan Service */
    protected FilePlanService filePlanService;

    /** Record folder service */
    protected RecordFolderService recordFolderService;

    /** Hold service */
    protected HoldService holdService;

    /**
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @param recordFolderService record folder service
     */
    public void setRecordFolderService(RecordFolderService recordFolderService)
    {
        this.recordFolderService = recordFolderService;
    }

    /**
     * @param holdService hold service
     */
    public void setHoldService(HoldService holdService)
    {
        this.holdService = holdService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#isHold(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean isHold(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        QName type = nodeService.getType(nodeRef);
        if (nodeService.exists(nodeRef) && (TYPE_HOLD.equals(type)) || dictionaryService.isSubClass(type, TYPE_HOLD))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#isFrozen(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean isFrozen(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        return nodeService.hasAspect(nodeRef, ASPECT_FROZEN);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#getFrozen(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public Set<NodeRef> getFrozen(NodeRef hold)
    {
        ParameterCheck.mandatory("hold", hold);

        Set<NodeRef> frozenNodes = new HashSet<NodeRef>();
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(hold, ASSOC_FROZEN_RECORDS,
                RegexQNamePattern.MATCH_ALL);
        if (childAssocs != null && !childAssocs.isEmpty())
        {
            for (ChildAssociationRef childAssociationRef : childAssocs)
            {
                frozenNodes.add(childAssociationRef.getChildRef());
            }
        }
        return frozenNodes;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#freeze(java.lang.String,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public NodeRef freeze(String reason, NodeRef nodeRef)
    {
        ParameterCheck.mandatoryString("reason", reason);
        ParameterCheck.mandatory("nodeRef", nodeRef);

        // FIXME: Should we consider only records and record folders or 'any'
        // node references
        // Check if the actionedUponNodeRef is a valid file plan component
        boolean isRecord = recordService.isRecord(nodeRef);
        boolean isFolder = recordFolderService.isRecordFolder(nodeRef);

        if (!(isRecord || isFolder))
        {
        	throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_FREEZE_ONLY_RECORDS_FOLDERS));
        }

        // Log a message about freezing the node with the reason
        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Freezing node '").append(nodeRef).append("'");
            if (isFolder)
            {
                msg.append(" (folder)");
            }
            msg.append(" with reason '").append(reason).append("'.");
            logger.debug(msg.toString());
        }

        // Create the hold object
        NodeRef holdNodeRef = createHold(nodeRef, reason);

        // Freeze the node and add it to the hold
        freeze(holdNodeRef, nodeRef);

        return holdNodeRef;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#freeze(org.alfresco.service.cmr.repository.NodeRef,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void freeze(NodeRef hold, NodeRef nodeRef)
    {
        ParameterCheck.mandatory("hold", hold);
        ParameterCheck.mandatory("nodeRef", nodeRef);

        holdService.addToHoldContainer(hold, nodeRef);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#freeze(java.lang.String,
     *      java.util.Set)
     */
    @Override
    public NodeRef freeze(String reason, Set<NodeRef> nodeRefs)
    {
        ParameterCheck.mandatoryString("reason", reason);
        ParameterCheck.mandatoryCollection("nodeRefs", nodeRefs);

        // FIXME: Can we assume that the nodeRefs are in the same filePlan???
        NodeRef nodeRef = nodeRefs.iterator().next();
        NodeRef hold = createHold(nodeRef, reason);

        freeze(hold, nodeRefs);

        return hold;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#freeze(org.alfresco.service.cmr.repository.NodeRef,
     *      java.util.Set)
     */
    @Override
    public void freeze(NodeRef hold, Set<NodeRef> nodeRefs)
    {
        ParameterCheck.mandatory("hold", hold);
        ParameterCheck.mandatoryCollection("nodeRefs", nodeRefs);

        for (NodeRef nodeRef : nodeRefs)
        {
            freeze(hold, nodeRef);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#unFreeze(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void unFreeze(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        if (nodeService.hasAspect(nodeRef, ASPECT_FROZEN))
        {
            boolean isRecordFolder = recordFolderService.isRecordFolder(nodeRef);

            if (logger.isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Unfreezing node '").append(nodeRef).append("'");
                if (isRecordFolder)
                {
                    msg.append(" (folder)");
                }
                msg.append(".");
                logger.debug(msg.toString());
            }

            // Remove freeze from node
            removeFreeze(nodeRef);

            // Remove freeze from records if a record folder
            if (isRecordFolder)
            {
                List<NodeRef> records = recordService.getRecords(nodeRef);
                for (NodeRef record : records)
                {
                    removeFreeze(record);
                }
            }
        }
        else
        {
            StringBuilder msg = new StringBuilder();
            msg.append("The node '").append(nodeRef).append("' was not frozen. So it cannot be unfrozen!");
            logger.info(msg.toString());
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#unFreeze(java.util.Set)
     */
    @Override
    public void unFreeze(Set<NodeRef> nodeRefs)
    {
        ParameterCheck.mandatoryCollection("nodeRefs", nodeRefs);

        for (NodeRef nodeRef : nodeRefs)
        {
            unFreeze(nodeRef);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#relinquish(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void relinquish(NodeRef hold)
    {
        ParameterCheck.mandatory("hold", hold);

        List<ChildAssociationRef> frozenNodeAssocs = nodeService.getChildAssocs(hold, ASSOC_FROZEN_RECORDS,
                RegexQNamePattern.MATCH_ALL);

        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Relinquishing hold '").append(hold).append("' which has '").append(frozenNodeAssocs.size())
                    .append("' frozen node(s).");
            logger.debug(msg.toString());
        }

        for (ChildAssociationRef assoc : frozenNodeAssocs)
        {
            // Remove the freeze if this is the only hold that references the
            // node
            removeFreeze(assoc.getChildRef(), hold);
        }

        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Deleting hold object '").append(hold).append("' with name '").append(
                    nodeService.getProperty(hold, ContentModel.PROP_NAME)).append("'.");
            logger.debug(msg.toString());
        }

        // Delete the hold node
        nodeService.deleteNode(hold);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#getReason(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public String getReason(NodeRef hold)
    {
        ParameterCheck.mandatory("hold", hold);

        return (String) nodeService.getProperty(hold, PROP_HOLD_REASON);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#updateReason(org.alfresco.service.cmr.repository.NodeRef,
     *      java.lang.String)
     */
    @Override
    public void updateReason(NodeRef hold, String reason)
    {
        ParameterCheck.mandatory("hold", hold);
        ParameterCheck.mandatoryString("reason", reason);

        nodeService.setProperty(hold, PROP_HOLD_REASON, reason);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#getHold(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public Set<NodeRef> getHolds(NodeRef filePlan)
    {
        ParameterCheck.mandatory("filePlan", filePlan);

        return new HashSet<NodeRef>(holdService.getHoldsInFilePlan(filePlan));
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#hasFrozenChildren(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean hasFrozenChildren(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(nodeRef, ContentModel.ASSOC_CONTAINS,
                RegexQNamePattern.MATCH_ALL);
        if (childAssocs != null && !childAssocs.isEmpty())
        {
            for (ChildAssociationRef childAssociationRef : childAssocs)
            {
                if (isFrozen(childAssociationRef.getChildRef())) { return true; }
            }
        }

        return false;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#getFreezeDate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public Date getFreezeDate(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        if (isFrozen(nodeRef))
        {
            Serializable property = nodeService.getProperty(nodeRef, PROP_FROZEN_AT);
            if (property != null) { return (Date) property; }
        }

        return null;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#getFreezeInitiator(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public String getFreezeInitiator(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        if (isFrozen(nodeRef))
        {
            Serializable property = nodeService.getProperty(nodeRef, PROP_FROZEN_BY);
            if (property != null) { return (String) property; }
        }

        return null;
    }

    /**
     * Helper Methods
     */

    /**
     * Creates a hold using the given nodeRef and reason
     *
     * @param nodeRef the nodeRef which will be frozen
     * @param reason the reason why the record will be frozen
     * @return NodeRef of the created hold
     */
    private NodeRef createHold(NodeRef nodeRef, String reason)
    {
        // get the hold container
        final NodeRef root = filePlanService.getFilePlan(nodeRef);
        NodeRef holdContainer = filePlanService.getHoldContainer(root);

        // calculate the hold name
        int nextCount = getNextCount(holdContainer);
        String holdName = I18NUtil.getMessage(MSG_HOLD_NAME) + " " + StringUtils.leftPad(Integer.toString(nextCount), 10, "0");

        // Create the properties for the hold object
        Map<QName, Serializable> holdProps = new HashMap<QName, Serializable>(2);
        holdProps.put(ContentModel.PROP_NAME, holdName);
        holdProps.put(PROP_HOLD_REASON, reason);

        // create the hold object
        QName holdQName = QName.createQName(RM_URI, holdName);
        final NodeRef holdNodeRef = nodeService.createNode(holdContainer, ContentModel.ASSOC_CONTAINS, holdQName, TYPE_HOLD, holdProps).getChildRef();

        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Created hold object '").append(holdNodeRef).append("' with name '").append(holdQName).append("'.");
            logger.debug(msg.toString());
        }

        // Bind the hold node reference to the transaction
        AlfrescoTransactionSupport.bindResource(KEY_HOLD_NODEREF, holdNodeRef);

        return holdNodeRef;
    }

    /**
     * Removes a freeze from a node. The unfrozen node is automatically removed
     * from the hold(s) it is in. If the hold is subsequently empty, the hold is
     * automatically deleted.
     *
     * @param nodeRef node reference
     */
    private void removeFreeze(NodeRef nodeRef)
    {
        // Get all the holds and remove this node from them
        List<ChildAssociationRef> assocs = nodeService.getParentAssocs(nodeRef, ASSOC_FROZEN_RECORDS,
                RegexQNamePattern.MATCH_ALL);

        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Removing freeze from node '").append(nodeRef).append("' which has '").append(assocs.size())
                    .append("' holds.");
            logger.debug(msg.toString());
        }

        for (ChildAssociationRef assoc : assocs)
        {
            // Remove the frozen node as a child
            NodeRef holdNodeRef = assoc.getParentRef();
            nodeService.removeChild(holdNodeRef, nodeRef);

            if (logger.isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Removed frozen node '").append(nodeRef).append("' from hold '").append(holdNodeRef).append(
                        "'.");
                logger.debug(msg.toString());
            }

            // Check to see if we should delete the hold
            List<ChildAssociationRef> holdAssocs = nodeService.getChildAssocs(holdNodeRef, ASSOC_FROZEN_RECORDS,
                    RegexQNamePattern.MATCH_ALL);
            if (holdAssocs != null && holdAssocs.isEmpty())
            {
                if (logger.isDebugEnabled())
                {
                    StringBuilder msg = new StringBuilder();
                    msg.append("Hold node '").append(holdNodeRef).append("' with name '").append(
                            nodeService.getProperty(holdNodeRef, ContentModel.PROP_NAME)).append(
                            "' has no frozen nodes. Hence deleting it.");
                    logger.debug(msg.toString());
                }

                // Delete the hold object
                nodeService.deleteNode(holdNodeRef);
            }
        }

        // Remove the aspect
        nodeService.removeAspect(nodeRef, ASPECT_FROZEN);

        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Removed frozen aspect from '").append(nodeRef).append("'.");
            logger.debug(msg.toString());
        }
    }

    /**
     * Removes a freeze from a node from the given hold
     *
     * @param nodeRef node reference
     * @param hold hold
     */
    private void removeFreeze(NodeRef nodeRef, NodeRef hold)
    {
        // We should only remove the frozen aspect if there are no other 'holds'
        // in effect for this node.
        // One complication to consider is that holds can be placed on records
        // or on folders.
        // Therefore if the nodeRef here is a record, we need to go up the
        // containment hierarchy looking
        // for holds at each level.

        // Get all the holds and remove this node from them.
        List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(nodeRef, ASSOC_FROZEN_RECORDS,
                RegexQNamePattern.MATCH_ALL);
        // If the nodeRef is a record, there could also be applicable holds as
        // parents of the folder(s).
        if (recordService.isRecord(nodeRef))
        {
            List<NodeRef> parentFolders = recordFolderService.getRecordFolders(nodeRef);
            for (NodeRef folder : parentFolders)
            {
                List<ChildAssociationRef> moreAssocs = nodeService.getParentAssocs(folder, ASSOC_FROZEN_RECORDS,
                        RegexQNamePattern.MATCH_ALL);
                parentAssocs.addAll(moreAssocs);
            }
        }

        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Removing freeze from ").append(nodeRef).append(" which has ").append(parentAssocs.size())
                    .append(" holds");
            logger.debug(msg.toString());
        }

        boolean otherHoldsAreInEffect = false;
        for (ChildAssociationRef chAssRef : parentAssocs)
        {
            if (!chAssRef.getParentRef().equals(hold))
            {
                otherHoldsAreInEffect = true;
                break;
            }
        }

        if (!otherHoldsAreInEffect)
        {
            if (logger.isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Removing frozen aspect from ").append(nodeRef);
                logger.debug(msg.toString());
            }

            // Remove the aspect
            nodeService.removeAspect(nodeRef, ASPECT_FROZEN);
        }

        // Remove the freezes on the child records as long as there is no other
        // hold referencing them
        if (recordFolderService.isRecordFolder(nodeRef) == true)
        {
            if (logger.isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append(nodeRef).append(" is a record folder");
                logger.debug(msg.toString());
            }
            for (NodeRef record : recordService.getRecords(nodeRef))
            {
                removeFreeze(record, hold);
            }
        }
    }
}