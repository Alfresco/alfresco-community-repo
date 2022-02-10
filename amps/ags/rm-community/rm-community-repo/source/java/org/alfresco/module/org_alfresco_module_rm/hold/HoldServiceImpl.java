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

package org.alfresco.module.org_alfresco_module_rm.hold;

import static org.alfresco.model.ContentModel.ASPECT_LOCKABLE;
import static org.alfresco.model.ContentModel.ASSOC_CONTAINS;
import static org.alfresco.model.ContentModel.PROP_NAME;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldServicePolicies.BeforeAddToHoldPolicy;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldServicePolicies.BeforeCreateHoldPolicy;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldServicePolicies.BeforeDeleteHoldPolicy;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldServicePolicies.BeforeRemoveFromHoldPolicy;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldServicePolicies.OnAddToHoldPolicy;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldServicePolicies.OnCreateHoldPolicy;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldServicePolicies.OnDeleteHoldPolicy;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldServicePolicies.OnRemoveFromHoldPolicy;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

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

    /** I18N */
    private static final String MSG_ERR_ACCESS_DENIED = "permissions.err_access_denied";
    private static final String MSG_ERR_HOLD_PERMISSION_GENERIC_ERROR = "rm.hold.generic-permission-error";
    private static final String MSG_ERR_HOLD_PERMISSION_DETAILED_ERROR = "rm.hold.detailed-permission-error";

    /** Maximum number of held items to display in error message */
    private static final int MAX_HELD_ITEMS_LIST_SIZE = 5;

    /** File Plan Service */
    private FilePlanService filePlanService;

    /** Record Service */
    private RecordService recordService;

    /** Record folder service */
    private RecordFolderService recordFolderService;

    /** Permission service */
    private PermissionService permissionService;

    /** Capability service */
    private CapabilityService capabilityService;

    /** Policy component */
    private PolicyComponent policyComponent;

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
     * @param capabilityService capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }

    /**
     * Gets the policy component instance
     *
     * @return The policy component instance
     */
    protected PolicyComponent getPolicyComponent()
    {
        return this.policyComponent;
    }

    /**
     * Sets the policy component instance
     *
     * @param policyComponent The policy component instance
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * Policy delegates
     */
    private ClassPolicyDelegate<BeforeCreateHoldPolicy> beforeCreateHoldPolicyDelegate;
    private ClassPolicyDelegate<OnCreateHoldPolicy> onCreateHoldPolicyDelegate;
    private ClassPolicyDelegate<BeforeDeleteHoldPolicy> beforeDeleteHoldPolicyDelegate;
    private ClassPolicyDelegate<OnDeleteHoldPolicy> onDeleteHoldPolicyDelegate;
    private ClassPolicyDelegate<BeforeAddToHoldPolicy> beforeAddToHoldPolicyDelegate;
    private ClassPolicyDelegate<OnAddToHoldPolicy> onAddToHoldPolicyDelegate;
    private ClassPolicyDelegate<BeforeRemoveFromHoldPolicy> beforeRemoveFromHoldPolicyDelegate;
    private ClassPolicyDelegate<OnRemoveFromHoldPolicy> onRemoveFromHoldPolicyDelegate;

    /**
     * Initialise hold service
     */
    public void init()
    {
        // Register the policies
        beforeCreateHoldPolicyDelegate = getPolicyComponent().registerClassPolicy(BeforeCreateHoldPolicy.class);
        onCreateHoldPolicyDelegate = getPolicyComponent().registerClassPolicy(OnCreateHoldPolicy.class);
        beforeDeleteHoldPolicyDelegate = getPolicyComponent().registerClassPolicy(BeforeDeleteHoldPolicy.class);
        onDeleteHoldPolicyDelegate = getPolicyComponent().registerClassPolicy(OnDeleteHoldPolicy.class);
        beforeAddToHoldPolicyDelegate = getPolicyComponent().registerClassPolicy(BeforeAddToHoldPolicy.class);
        onAddToHoldPolicyDelegate = getPolicyComponent().registerClassPolicy(OnAddToHoldPolicy.class);
        beforeRemoveFromHoldPolicyDelegate = getPolicyComponent().registerClassPolicy(BeforeRemoveFromHoldPolicy.class);
        onRemoveFromHoldPolicyDelegate = getPolicyComponent().registerClassPolicy(OnRemoveFromHoldPolicy.class);

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
            checkPermissionsForDeleteHold(hold);

            RunAsWork<Void> work = new RunAsWork<Void>()
            {
                @Override
                public Void doWork()
                {
                    List<NodeRef> frozenNodes = getHeld(hold);
                    for (NodeRef frozenNode : frozenNodes)
                    {
                        //set in transaction cache in order not to trigger update policy when removing the child association
                        transactionalResourceHelper.getSet("frozen").add(frozenNode);
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
                //set in transaction cache in order not to trigger update policy when removing the aspect
                transactionalResourceHelper.getSet("frozen").add(nodeRef);
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
                            //set in transaction cache in order not to trigger update policy when removing the aspect
                            transactionalResourceHelper.getSet("frozen").add(record);
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

        List<NodeRef> holds = new ArrayList<>();

        // get the root hold container
        NodeRef holdContainer = filePlanService.getHoldContainer(filePlan);

        if (holdContainer != null)
        {
            // get the children of the root hold container
            List<ChildAssociationRef> holdsAssocs = nodeService.getChildAssocs(holdContainer, ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
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

        List<NodeRef> result = new ArrayList<>();

        // get all the immediate parent holds
        final Set<NodeRef> holdsIncludingNodeRef = getParentHolds(nodeRef);

        // check whether the record is held by virtue of it's record folder
        if (isRecord(nodeRef))
        {
            final List<NodeRef> recordFolders = recordFolderService.getRecordFolders(nodeRef);
            for (final NodeRef recordFolder : recordFolders)
            {
                holdsIncludingNodeRef.addAll(getParentHolds(recordFolder));
            }
        }

        if (!includedInHold)
        {
            final Set<NodeRef> filePlans = filePlanService.getFilePlans();
            if (!CollectionUtils.isEmpty(filePlans))
            {
                final List<NodeRef> holdsNotIncludingNodeRef = new ArrayList<>();
                filePlans.forEach(filePlan ->
                {
                    // invert list to get list of holds that do not contain this node
                    final List<NodeRef> allHolds = getHolds(filePlan);
                    holdsNotIncludingNodeRef.addAll(ListUtils.subtract(allHolds, new ArrayList<>(holdsIncludingNodeRef)));
                });
                result = holdsNotIncludingNodeRef;
            }
        }
        else
        {
            result = new ArrayList<>(holdsIncludingNodeRef);
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
        List<ChildAssociationRef> holdsAssocs = nodeService.getParentAssocs(nodeRef, ASSOC_FROZEN_CONTENT, ASSOC_FROZEN_CONTENT);
        Set<NodeRef> holds = new HashSet<>(holdsAssocs.size());
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
        NodeRef hold = nodeService.getChildByName(holdContainer, ASSOC_CONTAINS, name);
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
        List<NodeRef> children = new ArrayList<>();

        if (!isHold(hold))
        {
            throw new AlfrescoRuntimeException("Can't get the node's held, because passed node reference isn't a hold. (hold=" + hold.toString() + ")");
        }

        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(hold, ASSOC_FROZEN_CONTENT, RegexQNamePattern.MATCH_ALL);
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

        invokeBeforeCreateHold(holdContainer, name, reason);

        // create map of properties
        Map<QName, Serializable> properties = new HashMap<>(3);
        properties.put(ContentModel.PROP_NAME, name);
        properties.put(PROP_HOLD_REASON, reason);
        if (description != null && !description.isEmpty())
        {
            properties.put(ContentModel.PROP_DESCRIPTION, description);
        }

        // create assoc name
        QName assocName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name);

        // create hold
        ChildAssociationRef childAssocRef = nodeService.createNode(holdContainer, ASSOC_CONTAINS, assocName, TYPE_HOLD, properties);

        NodeRef holdNodeRef = childAssocRef.getChildRef();

        invokeOnCreateHold(holdNodeRef);

        return holdNodeRef;
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
            throw new AlfrescoRuntimeException("Can't delete hold, because passed node is not a hold. (hold=" + hold.toString() + ")");
        }

        invokeBeforeDeleteHold(hold);

        String holdName = (String) nodeService.getProperty(hold, PROP_NAME);
        Set<QName> classQNames = getTypeAndApsects(hold);

        // delete the hold node
        nodeService.deleteNode(hold);

        invokeOnDeleteHold(holdName, classQNames);
    }

    /**
     * Helper method to check if user has correct permissions to delete hold
     *
     * @param hold hold to be deleted
     */
    private void checkPermissionsForDeleteHold(NodeRef hold)
    {
        List<NodeRef> held = AuthenticationUtil.runAsSystem(() -> getHeld(hold));

        List<String> heldNames = new ArrayList<>();
        for (NodeRef nodeRef : held)
        {
            try
            {
                String permission;

                if (recordService.isRecord(nodeRef) || recordFolderService.isRecordFolder(nodeRef))
                {
                    permission = RMPermissionModel.FILING;
                }
                else
                {
                    permission =  PermissionService.READ;
                }

                if (permissionService.hasPermission(nodeRef, permission) == AccessStatus.DENIED)
                {
                    heldNames.add((String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
                }
            }
            catch (AccessDeniedException ade)
            {
                throw new AccessDeniedException(I18NUtil.getMessage(MSG_ERR_HOLD_PERMISSION_GENERIC_ERROR), ade);
            }
        }

        if (heldNames.size() > 0)
        {
            StringBuilder sb = new StringBuilder();
            Stream<String> stream1 = heldNames.stream();
            stream1.limit(MAX_HELD_ITEMS_LIST_SIZE).forEach(name -> {
                sb.append("\n ");
                sb.append("'");
                sb.append(name);
                sb.append("'");
            });
            throw new AccessDeniedException(I18NUtil.getMessage(MSG_ERR_HOLD_PERMISSION_DETAILED_ERROR) + sb.toString());
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

        List<NodeRef> holds = new ArrayList<>(1);
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

        checkNodeCanBeAddedToHold(nodeRef);

        for (final NodeRef hold : holds)
        {
            if (!isHold(hold))
            {
                final String holdName = (String) nodeService.getProperty(hold, ContentModel.PROP_NAME);
                throw new IntegrityException(I18NUtil.getMessage("rm.hold.not-hold", holdName), null);
            }

            if (!AccessStatus.ALLOWED.equals(
                            capabilityService.getCapabilityAccessState(hold, RMPermissionModel.ADD_TO_HOLD)))
            {
                throw new AccessDeniedException(I18NUtil.getMessage(MSG_ERR_ACCESS_DENIED));
            }

            // check that the node isn't already in the hold
            if (!getHeld(hold).contains(nodeRef))
            {
                // fire before add to hold policy
                invokeBeforeAddToHold(hold, nodeRef);
                // run as system to ensure we have all the appropriate permissions to perform the manipulations we require
                authenticationUtil.runAsSystem((RunAsWork<Void>) () ->
                {
                    // gather freeze properties
                    final Map<QName, Serializable> props = new HashMap<>(2);
                    props.put(PROP_FROZEN_AT, new Date());
                    props.put(PROP_FROZEN_BY, AuthenticationUtil.getFullyAuthenticatedUser());

                    addFrozenAspect(nodeRef, props);

                    // Link the record to the hold
                    //set in transaction cache in order not to trigger update policy when adding the association
                    transactionalResourceHelper.getSet("frozen").add(nodeRef);
                    nodeService.addChild(hold, nodeRef, ASSOC_FROZEN_CONTENT, ASSOC_FROZEN_CONTENT);
                    // get the documents primary parent assoc
                    ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(nodeRef);
                    nodeService.addChild(hold, nodeRef, ASSOC_CONTAINS, parentAssoc.getQName());

                    // Mark all the folders contents as frozen
                    if (isRecordFolder(nodeRef))
                    {
                        final List<NodeRef> records = recordService.getRecords(nodeRef);
                        records.forEach(record -> addFrozenAspect(record, props));
                    }

                    return null;
                });

                // fire on add to hold policy
                invokeOnAddToHold(hold, nodeRef);
            }
        }
    }

    /**
     * Check if the given node is eligible to be added into a hold
     *
     * @param nodeRef the node to be checked
     */
    private void checkNodeCanBeAddedToHold(NodeRef nodeRef)
    {
        if (!isRecordFolder(nodeRef) && !instanceOf(nodeRef, ContentModel.TYPE_CONTENT))
        {
            final String nodeName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
            throw new IntegrityException(I18NUtil.getMessage("rm.hold.add-to-hold-invalid-type", nodeName), null);
        }

        if (((isRecord(nodeRef) || isRecordFolder(nodeRef)) &&
                permissionService.hasPermission(nodeRef, RMPermissionModel.FILING) == AccessStatus.DENIED) ||
                (instanceOf(nodeRef, ContentModel.TYPE_CONTENT) &&
                        permissionService.hasPermission(nodeRef, PermissionService.WRITE) == AccessStatus.DENIED))
        {
            throw new AccessDeniedException(I18NUtil.getMessage(MSG_ERR_ACCESS_DENIED));
        }

        if (nodeService.hasAspect(nodeRef, ASPECT_ARCHIVED))
        {
            throw new IntegrityException(I18NUtil.getMessage("rm.hold.add-to-hold-archived-node"), null);
        }

        if (nodeService.hasAspect(nodeRef, ASPECT_LOCKABLE))
        {
            throw new IntegrityException(I18NUtil.getMessage("rm.hold.add-to-hold-locked-node"), null);
        }
    }

    /**
     * Add Frozen aspect only if node isn't already frozen
     *
     * @param nodeRef node on which aspect will be added
     * @param props aspect properties map
     */
    private void addFrozenAspect(NodeRef nodeRef, Map<QName, Serializable> props)
    {
        if (!nodeService.hasAspect(nodeRef, ASPECT_FROZEN))
        {
            //set in transaction cache in order not to trigger update policy when adding the aspect
            transactionalResourceHelper.getSet("frozen").add(nodeRef);
            // add freeze aspect
            nodeService.addAspect(nodeRef, ASPECT_FROZEN, props);

            if (logger.isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Frozen aspect applied to '").append(nodeRef).append("'.");
                logger.debug(msg.toString());
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

        List<NodeRef> holds = new ArrayList<>(1);
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

        if (!holds.isEmpty())
        {
            List<NodeRef> removedHolds = new ArrayList<>();
            for (final NodeRef hold : holds)
            {
                if (!isHold(hold))
                {
                    final String holdName = (String) nodeService.getProperty(hold, ContentModel.PROP_NAME);
                    throw new IntegrityException(I18NUtil.getMessage("rm.hold.not-hold", holdName), null);
                }

                if (!AccessStatus.ALLOWED.equals(
                        capabilityService.getCapabilityAccessState(hold, RMPermissionModel.REMOVE_FROM_HOLD)))
                {
                    throw new AccessDeniedException(I18NUtil.getMessage(MSG_ERR_ACCESS_DENIED));
                }

                if (getHeld(hold).contains(nodeRef))
                {
                    // fire before remove from hold policy
                    invokeBeforeRemoveFromHold(hold, nodeRef);
                    // run as system so we don't run into further permission issues
                    // we already know we have to have the correct capability to get here
                    authenticationUtil.runAsSystem((RunAsWork<Void>) () ->
                    {
                        // remove from hold
                        //set in transaction cache in order not to trigger update policy when removing the child association
                        transactionalResourceHelper.getSet("frozen").add(nodeRef);
                        nodeService.removeChild(hold, nodeRef);

                        return null;
                    });
                    removedHolds.add(hold);
                }
            }

            // run as system as we can't be sure if have remove aspect rights on node
            authenticationUtil.runAsSystem((RunAsWork<Void>) () ->
            {
                removeFreezeAspect(nodeRef, 0);
                return null;
            });
            for (NodeRef removedHold : removedHolds)
            {
                // fire on remove from hold policy
                invokeOnRemoveFromHold(removedHold, nodeRef);
            }
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

    /**
     * Invoke beforeCreateHold policy
     *
     * @param nodeRef node reference
     * @param name    hold name
     * @param reason  hold reason
     */
    protected void invokeBeforeCreateHold(NodeRef nodeRef, String name, String reason)
    {
        // execute policy for node type and aspects
        BeforeCreateHoldPolicy policy = beforeCreateHoldPolicyDelegate.get(getTypeAndApsects(nodeRef));
        policy.beforeCreateHold(name, reason);
    }

    /**
     * Invoke onCreateHold policy
     *
     * @param nodeRef node reference
     */
    protected void invokeOnCreateHold(NodeRef nodeRef)
    {
        OnCreateHoldPolicy policy = onCreateHoldPolicyDelegate.get(getTypeAndApsects(nodeRef));
        policy.onCreateHold(nodeRef);
    }

    /**
     * Invoke beforeDeleteHold policy
     *
     * @param nodeRef node reference
     */
    protected void invokeBeforeDeleteHold(NodeRef nodeRef)
    {
        BeforeDeleteHoldPolicy policy = beforeDeleteHoldPolicyDelegate.get(getTypeAndApsects(nodeRef));
        policy.beforeDeleteHold(nodeRef);
    }

    /**
     * Invoke onDeleteHold policy
     *
     * @param holdName name of the hold
     * @param classQNames hold types and aspects
     */
    protected void invokeOnDeleteHold(String holdName, Set<QName> classQNames)
    {
        // execute policy for node type and aspects
        OnDeleteHoldPolicy policy = onDeleteHoldPolicyDelegate.get(classQNames);
        policy.onDeleteHold(holdName);

    }

    /**
     * Invoke beforeAddToHold policy
     *
     * @param hold           hold node reference
     * @param contentNodeRef content node reference
     */
    protected void invokeBeforeAddToHold(NodeRef hold, NodeRef contentNodeRef)
    {
        BeforeAddToHoldPolicy policy = beforeAddToHoldPolicyDelegate.get(getTypeAndApsects(hold));
        policy.beforeAddToHold(hold, contentNodeRef);
    }

    /**
     * Invoke onAddToHold policy
     *
     * @param hold           hold node reference
     * @param contentNodeRef content node reference
     */
    protected void invokeOnAddToHold(NodeRef hold, NodeRef contentNodeRef)
    {
        OnAddToHoldPolicy policy = onAddToHoldPolicyDelegate.get(getTypeAndApsects(hold));
        policy.onAddToHold(hold, contentNodeRef);
    }

    /**
     * Invoke beforeRemoveFromHold policy
     *
     * @param hold           hold node reference
     * @param contentNodeRef content node reference
     */
    protected void invokeBeforeRemoveFromHold(NodeRef hold, NodeRef contentNodeRef)
    {
        BeforeRemoveFromHoldPolicy policy = beforeRemoveFromHoldPolicyDelegate.get(getTypeAndApsects(hold));
        policy.beforeRemoveFromHold(hold, contentNodeRef);
    }

    /**
     * Invoke onRemoveFromHold policy
     *
     * @param hold           hold node reference
     * @param contentNodeRef content node reference
     */
    protected void invokeOnRemoveFromHold(NodeRef hold, NodeRef contentNodeRef)
    {
        OnRemoveFromHoldPolicy policy = onRemoveFromHoldPolicyDelegate.get(getTypeAndApsects(hold));
        policy.onRemoveFromHold(hold, contentNodeRef);

    }
}
