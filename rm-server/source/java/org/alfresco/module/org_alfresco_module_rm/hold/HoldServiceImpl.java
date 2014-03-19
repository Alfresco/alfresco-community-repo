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
package org.alfresco.module.org_alfresco_module_rm.hold;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
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
@BehaviourBean
public class HoldServiceImpl extends ServiceBaseImpl
                             implements HoldService, 
                                        NodeServicePolicies.BeforeDeleteNodePolicy,
                                        RecordsManagementModel
{
    /** Logger */
    private static Log logger = LogFactory.getLog(HoldServiceImpl.class);

    /** File Plan Service */
    private FilePlanService filePlanService;

    /** Record Service */
    private RecordService recordService;
    
    /** Record folder service */
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
     * @param recordFolderService   the record folder service
     */
    public void setRecordFolderService(RecordFolderService recordFolderService)
    {
        this.recordFolderService = recordFolderService;
    }
    
    /**
     * Behaviour unfreezes node's that will no longer he held after delete.
     * 
     * @see org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy#beforeDeleteNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Behaviour(kind=BehaviourKind.CLASS, type="rma:hold", notificationFrequency=NotificationFrequency.EVERY_EVENT)
    @Override
    public void beforeDeleteNode(final NodeRef hold)
    {
        if (nodeService.exists(hold) && isHold(hold))
        {
            RunAsWork<Void> work = new RunAsWork<Void>()
            {
                @Override
                public Void doWork()
                {
                    List<NodeRef> frozenNodes = getHeld(hold);
                    for (NodeRef frozenNode : frozenNodes)
                    {
                        List<NodeRef> otherHolds = heldBy(frozenNode, true);
                        if (otherHolds.size() == 1)
                        {   
                            // remove the freeze aspect from the node
                            nodeService.removeAspect(frozenNode, ASPECT_FROZEN);
                            
                            if (isRecordFolder(frozenNode))
                            {
                                List<NodeRef> records = recordService.getRecords(frozenNode);
                                for (NodeRef record : records)
                                {
                                    if (nodeService.hasAspect(record, ASPECT_FROZEN))
                                    {
                                        List<NodeRef> recordsOtherHolds = heldBy(record, true);
                                        if (recordsOtherHolds.size() == 1)
                                        {
                                            // remove the freeze aspect from the node
                                            nodeService.removeAspect(record, ASPECT_FROZEN);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    return null;
                }
            };

            // run as system user
            runAsSystem(work);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.hold.HoldService#getHolds(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public List<NodeRef> getHolds(NodeRef filePlan)
    {
        ParameterCheck.mandatory("filePlan", filePlan);

        // get the root hold container
        NodeRef holdContainer = filePlanService.getHoldContainer(filePlan);
        
        // get the children of the root hold container
        List<ChildAssociationRef> holdsAssocs = nodeService.getChildAssocs(holdContainer, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
        List<NodeRef> holds = new ArrayList<NodeRef>(holdsAssocs.size());
        for (ChildAssociationRef holdAssoc : holdsAssocs)
        {
            NodeRef hold = holdAssoc.getChildRef();
            if (isHold(hold))
            {
                // add to list of holds
                holds.add(hold);
            }
        }

        return holds;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.hold.HoldService#heldBy(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<NodeRef> heldBy(NodeRef nodeRef, boolean includedInHold)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        
        List<NodeRef> result = null;

        // get all the immediate parent holds
        Set<NodeRef> holdsNotIncludingNodeRef = getParentHolds(nodeRef);
        
        // check whether the record is held by vitue of it's record folder
        if (isRecord(nodeRef))
        {
            List<NodeRef> recordFolders = recordFolderService.getRecordFolders(nodeRef);
            for (NodeRef recordFolder : recordFolders)
            {
                holdsNotIncludingNodeRef.addAll(getParentHolds(recordFolder));
            }
        }

        if (!includedInHold)
        {
            // invert list to get list of holds that do not contain this node
            NodeRef filePlan = filePlanService.getFilePlan(nodeRef);
            List<NodeRef> allHolds = getHolds(filePlan);
            result = ListUtils.subtract(allHolds, new ArrayList<NodeRef>(holdsNotIncludingNodeRef));
        }
        else
        {
            result = new ArrayList<NodeRef>(holdsNotIncludingNodeRef);
        }

        return result;
    }
    
    /**
     * Helper method to get holds that are direct parents of the given node.
     * 
     * @param nodeRef   node reference
     * @return Set<{@link NodeRef}> set of parent holds
     */
    private Set<NodeRef> getParentHolds(NodeRef nodeRef)
    {
        List<ChildAssociationRef> holdsAssocs = nodeService.getParentAssocs(nodeRef, ASSOC_FROZEN_RECORDS, ASSOC_FROZEN_RECORDS);
        Set<NodeRef> holds = new HashSet<NodeRef>(holdsAssocs.size());
        for (ChildAssociationRef holdAssoc : holdsAssocs)
        {
            holds.add(holdAssoc.getParentRef());
        }
        
        return holds;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.hold.HoldService#getHold(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Override
    public NodeRef getHold(NodeRef filePlan, String name)
    {
        ParameterCheck.mandatory("filePlan", filePlan);
        ParameterCheck.mandatory("name", name);
        
        // get the root hold container
        NodeRef holdContainer = filePlanService.getHoldContainer(filePlan);
        
        // get the hold by name
        NodeRef hold = nodeService.getChildByName(holdContainer, ContentModel.ASSOC_CONTAINS, name);
        if (hold != null && !isHold(hold))
        {
            throw new AlfrescoRuntimeException("Can not get hold, because the named node reference isn't a hold.");
        }
        
        return hold;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.hold.HoldService#getHeld(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public List<NodeRef> getHeld(NodeRef hold)
    {
        ParameterCheck.mandatory("hold", hold);
        List<NodeRef> children = new ArrayList<NodeRef>();
        
        if (nodeService.exists(hold) && isHold(hold))
        {            
            List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(hold, ASSOC_FROZEN_RECORDS, RegexQNamePattern.MATCH_ALL);
            if (childAssocs != null && !childAssocs.isEmpty())
            {
                for (ChildAssociationRef childAssociationRef : childAssocs)
                {
                    children.add(childAssociationRef.getChildRef());
                }
            }
        }
        
        return children;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.hold.HoldService#createHold(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public NodeRef createHold(NodeRef filePlan, String name, String reason, String description)
    {
        ParameterCheck.mandatory("filePlan", filePlan);
        ParameterCheck.mandatory("name", name);
        ParameterCheck.mandatory("reason", reason);
        
        // get the root hold container
        NodeRef holdContainer = filePlanService.getHoldContainer(filePlan);

        // create map of properties       
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(3);
        properties.put(ContentModel.PROP_NAME, name);
        properties.put(PROP_HOLD_REASON, reason);
        if (description != null && !description.isEmpty())
        {
            properties.put(ContentModel.PROP_DESCRIPTION, description);
        }
        
        // create assoc name
        QName assocName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name);
        
        // create hold
        ChildAssociationRef childAssocRef = nodeService.createNode(holdContainer, ContentModel.ASSOC_CONTAINS, assocName, TYPE_HOLD, properties);
        
        return childAssocRef.getChildRef();
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.hold.HoldService#getHoldReason(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public String getHoldReason(NodeRef hold)
    {
        ParameterCheck.mandatory("hold", hold);
        
        String reason = null;
        
        if (nodeService.exists(hold) && isHold(hold))
        {
            // get the reason 
            reason = (String)nodeService.getProperty(hold, PROP_HOLD_REASON);
        }
        
        return reason;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.hold.HoldService#setHoldReason(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Override
    public void setHoldReason(NodeRef hold, String reason)
    {
        ParameterCheck.mandatory("hold", hold);
        ParameterCheck.mandatory("reason", reason);
        
        if (nodeService.exists(hold) && isHold(hold))
        {
            nodeService.setProperty(hold, PROP_HOLD_REASON, reason);
        }
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.hold.HoldService#deleteHold(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void deleteHold(NodeRef hold)
    {
        ParameterCheck.mandatory("hold", hold);
        
        if (nodeService.exists(hold) && isHold(hold))
        {
            // delete the hold node
            nodeService.deleteNode(hold);
        }        
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.hold.HoldService#addToHold(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void addToHold(NodeRef hold, NodeRef nodeRef)
    {
        ParameterCheck.mandatory("hold", hold);
        ParameterCheck.mandatory("nodeRef", nodeRef);

        List<NodeRef> holds = new ArrayList<NodeRef>(1);
        holds.add(hold);
        addToHolds(Collections.unmodifiableList(holds), nodeRef);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.hold.HoldService#addToHold(org.alfresco.service.cmr.repository.NodeRef, java.util.List)
     */
    @Override
    public void addToHold(NodeRef hold, List<NodeRef> nodeRefs)
    {
        ParameterCheck.mandatory("hold", hold);
        ParameterCheck.mandatory("nodeRefs", nodeRefs);
        
        for (NodeRef nodeRef : nodeRefs)
        {
            addToHold(hold, nodeRef);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.hold.HoldService#addToHolds(java.util.List, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void addToHolds(List<NodeRef> holds, NodeRef nodeRef)
    {
        ParameterCheck.mandatoryCollection("holds", holds);
        ParameterCheck.mandatory("nodeRef", nodeRef);

        for (NodeRef hold : holds)
        {
            // Link the record to the hold
            nodeService.addChild(hold, nodeRef, ASSOC_FROZEN_RECORDS, ASSOC_FROZEN_RECORDS);
            
            // gather freeze properties
            Map<QName, Serializable> props = new HashMap<QName, Serializable>(2);
            props.put(PROP_FROZEN_AT, new Date());
            props.put(PROP_FROZEN_BY, AuthenticationUtil.getFullyAuthenticatedUser());

            if (!nodeService.hasAspect(nodeRef, ASPECT_FROZEN))
            {
                // add freeze aspect
                nodeService.addAspect(nodeRef, ASPECT_FROZEN, props);

                if (logger.isDebugEnabled())
                {
                    StringBuilder msg = new StringBuilder();
                    msg.append("Frozen aspect applied to '").append(nodeRef).append("'.");
                    logger.debug(msg.toString());
                }
            }

            // Mark all the folders contents as frozen
            if (isRecordFolder(nodeRef))
            {
                List<NodeRef> records = recordService.getRecords(nodeRef);
                for (NodeRef record : records)
                {
                    // no need to freeze if already frozen!
                    if (!nodeService.hasAspect(record, ASPECT_FROZEN))
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
     * @see org.alfresco.module.org_alfresco_module_rm.hold.HoldService#removeFromHold(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void removeFromHold(NodeRef hold, NodeRef nodeRef)
    {
        ParameterCheck.mandatory("hold", hold);
        ParameterCheck.mandatory("nodeRef", nodeRef);

        List<NodeRef> holds = new ArrayList<NodeRef>(1);
        holds.add(hold);
        removeFromHolds(Collections.unmodifiableList(holds), nodeRef);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.hold.HoldService#removeFromHold(org.alfresco.service.cmr.repository.NodeRef, java.util.List)
     */
    @Override
    public void removeFromHold(NodeRef hold, List<NodeRef> nodeRefs)
    {
        ParameterCheck.mandatory("hold", hold);
        ParameterCheck.mandatory("nodeRefs", nodeRefs);

        for (NodeRef nodeRef : nodeRefs)
        {
            removeFromHold(hold, nodeRef);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.hold.HoldService#removeFromHolds(java.util.List, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void removeFromHolds(List<NodeRef> holds, final NodeRef nodeRef)
    {
        ParameterCheck.mandatory("holds", holds);
        ParameterCheck.mandatory("nodeRef", nodeRef);

        for (NodeRef hold : holds)
        {
            nodeService.removeChild(hold, nodeRef);
        }

        // check to see if this node can be unfrozen
        List<NodeRef> holdList = heldBy(nodeRef, true);
        if (holdList.size() == 0)
        {
            // run as system as we can't be sure if have remove aspect rights on node
            runAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork()
                {
                    // remove frozen aspect
                    nodeService.removeAspect(nodeRef, ASPECT_FROZEN);
                    return null;
                }
             });            
        }
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.hold.HoldService#removeFromAllHolds(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void removeFromAllHolds(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        
        // remove the node from all the holds it's held by
        List<NodeRef> holds = heldBy(nodeRef, true);
        for (NodeRef hold : holds)
        {
            // remove node from hold
            removeFromHold(hold, nodeRef);
        }
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.hold.HoldService#removeFromAllHolds(java.util.List)
     */
    @Override
    public void removeFromAllHolds(List<NodeRef> nodeRefs)
    {
        ParameterCheck.mandatory("nodeRefs", nodeRefs);
        
        for (NodeRef nodeRef : nodeRefs)
        {
            removeFromAllHolds(nodeRef);
        }        
    } 
}
