/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.alfresco.module.org_alfresco_module_rm.audit.event.AuditEvent;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.rma.type.HoldType;
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
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
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

    /** Audit event keys */
    private static final String AUDIT_ADD_TO_HOLD = "addToHold";
    private static final String AUDIT_REMOVE_FROM_HOLD = "removeFromHold";

    /** File Plan Service */
    private FilePlanService filePlanService;

    /** Record Service */
    private RecordService recordService;

    /** Record folder service */
    private RecordFolderService recordFolderService;

    /** Permission service */
    private PermissionService permissionService;

    /** records management audit service */
    private RecordsManagementAuditService recordsManagementAuditService;

    private HoldType holdType;

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
     * Set the permission service
     *
     * @param permissionService the permission services
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * @param recordsManagementAuditService records management audit service
     */
    public void setRecordsManagementAuditService(RecordsManagementAuditService recordsManagementAuditService)
    {
        this.recordsManagementAuditService = recordsManagementAuditService;
    }

    public void setHoldType(HoldType holdType)
    {
        this.holdType = holdType;
    }

    /**
     * Initialise hold service
     */
    public void init()
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                recordsManagementAuditService.registerAuditEvent(new AuditEvent(AUDIT_ADD_TO_HOLD, "capability.AddToHold.title"));
                recordsManagementAuditService.registerAuditEvent(new AuditEvent(AUDIT_REMOVE_FROM_HOLD, "capability.RemoveFromHold.title"));
                return null;
            }
        });
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
                        removeFreezeAspect(frozenNode, 1);
                    }

                    return null;
                }
            };

            // run as system user
            authenticationUtil.runAsSystem(work);
        }
    }

    /**
     * Helper method removes the freeze aspect from the record and record folder if it is no longer
     * in a hold.
     *
     * @param nodeRef
     */
    private void removeFreezeAspect(NodeRef nodeRef, int index)
    {
        List<NodeRef> otherHolds = heldBy(nodeRef, true);
        if (otherHolds.size() == index)
        {
            if (nodeService.hasAspect(nodeRef, ASPECT_FROZEN))
            {
                // remove the freeze aspect from the node
                nodeService.removeAspect(nodeRef, ASPECT_FROZEN);
            }

            if (isRecordFolder(nodeRef))
            {
                List<NodeRef> records = recordService.getRecords(nodeRef);
                for (NodeRef record : records)
                {
                    if (nodeService.hasAspect(record, ASPECT_FROZEN))
                    {
                        List<NodeRef> recordsOtherHolds = heldBy(record, true);
                        if (recordsOtherHolds.size() == index)
                        {
                            // remove the freeze aspect from the node
                            nodeService.removeAspect(record, ASPECT_FROZEN);
                        }
                    }
                }
            }
        }

    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.hold.HoldService#getHolds(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public List<NodeRef> getHolds(NodeRef filePlan)
    {
        ParameterCheck.mandatory("filePlan", filePlan);

        List<NodeRef> holds = new ArrayList<NodeRef>();

        // get the root hold container
        NodeRef holdContainer = filePlanService.getHoldContainer(filePlan);

        if (holdContainer != null)
        {
            // get the children of the root hold container
            List<ChildAssociationRef> holdsAssocs = nodeService.getChildAssocs(holdContainer, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef holdAssoc : holdsAssocs)
            {
                NodeRef hold = holdAssoc.getChildRef();
                if (isHold(hold))
                {
                    // add to list of holds
                    holds.add(hold);
                }
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

        if (!isHold(hold))
        {
            throw new AlfrescoRuntimeException("Can't get the node's held, because passed node reference isn't a hold. (hold=" + hold.toString() + ")");
        }

        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(hold, ASSOC_FROZEN_RECORDS, RegexQNamePattern.MATCH_ALL);
        if (childAssocs != null && !childAssocs.isEmpty())
        {
            for (ChildAssociationRef childAssociationRef : childAssocs)
            {
                children.add(childAssociationRef.getChildRef());
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
    public void deleteHold(final NodeRef hold)
    {
        ParameterCheck.mandatory("hold", hold);

        if (!isHold(hold))
        {
            throw new AlfrescoRuntimeException("Can't delete hold, becuase passed node is not a hold. (hold=" + hold.toString() + ")");
        }

        List<NodeRef> held = AuthenticationUtil.runAsSystem(new RunAsWork<List<NodeRef>>()
        {
            @Override
            public List<NodeRef> doWork()
            {
                return getHeld(hold);
            }
        });

        List<String> heldNames = new ArrayList<String>();
        for (NodeRef nodeRef : held)
        {
            try
            {
                if (permissionService.hasPermission(nodeRef, RMPermissionModel.FILING) == AccessStatus.DENIED)
                {
                    heldNames.add((String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
                }
            }
            catch (AccessDeniedException ade)
            {
                throw new AlfrescoRuntimeException("Can't delete hold, because you don't have filling permissions on all the items held within the hold.", ade);
            }
        }

        if (heldNames.size() > 0)
        {
            StringBuilder sb = new StringBuilder();
            for (String name : heldNames)
            {
                sb.append("\n ");
                sb.append("'");
                sb.append(name);
                sb.append("'");
            }
            throw new AlfrescoRuntimeException("Can't delete hold, because filing permissions for the following items are needed: " + sb.toString());
        }

        // delete the hold node
        nodeService.deleteNode(hold);
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
    public void addToHolds(final List<NodeRef> holds, final NodeRef nodeRef)
    {
        ParameterCheck.mandatoryCollection("holds", holds);
        ParameterCheck.mandatory("nodeRef", nodeRef);

        if (!isRecord(nodeRef) && !isRecordFolder(nodeRef))
        {
            String nodeName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
            throw new AlfrescoRuntimeException("'" + nodeName + "' is neither a record nor a record folder. Only records or record folders can be added to a hold.");
        }

        if (permissionService.hasPermission(nodeRef, RMPermissionModel.FILING) == AccessStatus.DENIED)
        {
            String nodeName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
            throw new AlfrescoRuntimeException("Filing permission on '" + nodeName + "' is needed.");
        }

        for (final NodeRef hold : holds)
        {
            if (!isHold(hold))
            {
                String holdName = (String) nodeService.getProperty(hold, ContentModel.PROP_NAME);
                throw new AlfrescoRuntimeException("'" + holdName + "' is not a hold so record folders/records cannot be added.");
            }

            if (permissionService.hasPermission(hold, RMPermissionModel.FILING) == AccessStatus.DENIED)
            {
                String nodeName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                String holdName = (String) nodeService.getProperty(hold, ContentModel.PROP_NAME);
                throw new AlfrescoRuntimeException("'" + nodeName + "' can't be added to the hold container as filing permission for '" + holdName + "' is needed.");
            }

            // check that the node isn't already in the hold
            if (!getHeld(hold).contains(nodeRef))
            {
                // run as system to ensure we have all the appropriate permissions to perform the manipulations we require
                authenticationUtil.runAsSystem(new RunAsWork<Void>()
                {
                    @Override
                    public Void doWork()
                    {
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

                        // Link the record to the hold
                        holdType.disable();
                        try
                        {
                            nodeService.addChild(hold, nodeRef, ASSOC_FROZEN_RECORDS, ASSOC_FROZEN_RECORDS);
                        }
                        finally
                        {
                            holdType.enable();
                        }

                        // audit item being added to the hold
                        recordsManagementAuditService.auditEvent(nodeRef, AUDIT_ADD_TO_HOLD);

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

                        return null;
                    }
                });
            }
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.hold.HoldService#addToHolds(java.util.List, java.util.List)
     */
    @Override
    public void addToHolds(List<NodeRef> holds, List<NodeRef> nodeRefs)
    {
        ParameterCheck.mandatoryCollection("holds", holds);
        ParameterCheck.mandatoryCollection("nodeRefs", nodeRefs);

        for (NodeRef nodeRef : nodeRefs)
        {
            addToHolds(holds, nodeRef);
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

        if (holds != null && !holds.isEmpty())
        {
            for (final NodeRef hold : holds)
            {
                if (!instanceOf(hold, TYPE_HOLD))
                {
                    throw new AlfrescoRuntimeException("Can't remove from hold, because it isn't a hold. (hold=" + hold + ")");
                }

                if (getHeld(hold).contains(nodeRef))
                {
                    // run as system so we don't run into further permission issues
                    // we already know we have to have the correct capability to get here
                    authenticationUtil.runAsSystem(new RunAsWork<Void>()
                    {
                        @Override
                        public Void doWork()
                        {
                            // remove from hold
                            nodeService.removeChild(hold, nodeRef);

                            // audit that the node has been remove from the hold
                            // TODO add details of the hold that the node was removed from
                            recordsManagementAuditService.auditEvent(nodeRef, AUDIT_REMOVE_FROM_HOLD);

                            return null;
                        }
                     });
                }
            }

            // run as system as we can't be sure if have remove aspect rights on node
            authenticationUtil.runAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork()
                {
                    removeFreezeAspect(nodeRef, 0);
                    return null;
                }
            });
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.hold.HoldService#removeFromHolds(java.util.List, java.util.List)
     */
    @Override
    public void removeFromHolds(List<NodeRef> holds, List<NodeRef> nodeRefs)
    {
        ParameterCheck.mandatoryCollection("holds", holds);
        ParameterCheck.mandatoryCollection("nodeRefs", nodeRefs);

        for (NodeRef nodeRef : nodeRefs)
        {
            removeFromHolds(holds, nodeRef);
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
