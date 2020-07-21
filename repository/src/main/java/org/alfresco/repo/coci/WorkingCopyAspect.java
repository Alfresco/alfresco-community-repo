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

package org.alfresco.repo.coci;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.lock.mem.Lifetime;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;

public class WorkingCopyAspect implements CopyServicePolicies.OnCopyNodePolicy, NodeServicePolicies.OnRemoveAspectPolicy, NodeServicePolicies.BeforeArchiveNodePolicy, NodeServicePolicies.OnRestoreNodePolicy
{    
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private NodeDAO nodeDAO;
    private LockService lockService;
    private CheckOutCheckInService checkOutCheckInService;
    private BehaviourFilter policyBehaviourFilter;
    
    
    /**
     * The working copy aspect copy behaviour callback.
     */
    private WorkingCopyAspectCopyBehaviourCallback workingCopyAspectCopyBehaviourCallback = new WorkingCopyAspectCopyBehaviourCallback();
    
    /**
     * Sets the policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent) 
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * Set the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set the node dao
     */
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    /**
     * Set the lock service
     */
    public void setLockService(LockService lockService)
    {
        this.lockService = lockService;
    }

    /**
     * @param checkOutCheckInService            the service dealing with working copies
     */
    public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService)
    {
        this.checkOutCheckInService = checkOutCheckInService;
    }

    /**
     * @param policyBehaviourFilter BehaviourFilter
     */
    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter)
    {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }
    
    /**
     * Initialise method
     */
    public void init()
    {
        // Register copy behaviour for the working copy aspect
        this.policyComponent.bindClassBehaviour(
                CopyServicePolicies.OnCopyNodePolicy.QNAME,
                ContentModel.TYPE_CMOBJECT,
                new JavaBehaviour(this, "getCopyCallback"));
        this.policyComponent.bindClassBehaviour(
                CopyServicePolicies.OnCopyNodePolicy.QNAME,
                ContentModel.ASPECT_WORKING_COPY,
                new JavaBehaviour(this, "getCopyCallback"));
        this.policyComponent.bindClassBehaviour(
                CopyServicePolicies.OnCopyNodePolicy.QNAME,
                ContentModel.ASPECT_CHECKED_OUT,
                new JavaBehaviour(this, "getCopyCallback"));
        
        // register beforeArchiveNode class behaviour for the working copy aspect
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.BeforeArchiveNodePolicy.QNAME,
                ContentModel.ASPECT_WORKING_COPY,
                new JavaBehaviour(this, "beforeArchiveNode"));

        // register onBeforeDelete class behaviour for the working copy aspect
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.BeforeDeleteNodePolicy.QNAME,
                ContentModel.ASPECT_WORKING_COPY,
                new JavaBehaviour(this, "beforeDeleteWorkingCopy"));

        // register onRestoreNode class behaviour for archived Lockable aspect
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnRestoreNodePolicy.QNAME,
                ContentModel.ASPECT_ARCHIVE_LOCKABLE,
                new JavaBehaviour(this, "onRestoreNode"));

        // Watch for removal of the aspect and ensure that the cm:workingcopylink assoc is removed
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnRemoveAspectPolicy.QNAME,
                ContentModel.ASPECT_WORKING_COPY,
                new JavaBehaviour(this, "onRemoveAspect"));

        this.policyComponent.bindAssociationBehaviour(
                    NodeServicePolicies.OnDeleteAssociationPolicy.QNAME,
                    ContentModel.ASPECT_CMIS_CREATED_CHECKEDOUT,
                    ContentModel.ASSOC_WORKING_COPY_LINK,
                    new JavaBehaviour(this, "onDeleteCmisCreatedCheckoutWorkingCopyAssociation"));
    }
    
    /**
     * beforeDeleteNode policy behaviour
     * 
     * @param nodeRef   the node reference about to be deleted
     */
    public void beforeDeleteWorkingCopy(NodeRef nodeRef)
    {
        NodeRef checkedOutNodeRef = checkOutCheckInService.getCheckedOut(nodeRef);
        if (checkedOutNodeRef != null)
        {
            policyBehaviourFilter.disableBehaviour(checkedOutNodeRef, ContentModel.ASPECT_AUDITABLE);
            try
            {
                lockService.unlock(checkedOutNodeRef, false, true);
                nodeService.removeAspect(checkedOutNodeRef, ContentModel.ASPECT_CHECKED_OUT);

            }
            finally
            {
                policyBehaviourFilter.enableBehaviour(checkedOutNodeRef, ContentModel.ASPECT_AUDITABLE);
            }
        }
    }

    /**
     * onDeleteAssociation policy behaviour If the node has the aspect ASPECT_CMIS_CREATED_CHECKEDOUT and ASSOC_WORKING_COPY_LINK association is deleted, delete the node. Fix for MNT-14850.
     * 
     * @param nodeAssocRef ASSOC_WORKING_COPY_LINK association where the source is the checkedOut node and the target is the workingCopy
     */
    public void onDeleteCmisCreatedCheckoutWorkingCopyAssociation(AssociationRef nodeAssocRef)
    {
        NodeRef checkedOutNodeRef = nodeAssocRef.getSourceRef();
        policyBehaviourFilter.disableBehaviour(checkedOutNodeRef, ContentModel.ASPECT_AUDITABLE);
        try
        {

            nodeService.deleteNode(checkedOutNodeRef);
        }
        finally
        {
            policyBehaviourFilter.enableBehaviour(checkedOutNodeRef, ContentModel.ASPECT_AUDITABLE);
        }

    }
    
    /**
     * beforeArchiveNode policy behaviour
     * 
     * @param nodeRef
     *            the node reference about to be archived
     */
    @Override
    public void beforeArchiveNode(NodeRef workingCopyNodeRef)
    {
        NodeRef checkedOutNodeRef = checkOutCheckInService.getCheckedOut(workingCopyNodeRef);

        if (checkedOutNodeRef != null)
        {
            try
            {
                policyBehaviourFilter.disableBehaviour(workingCopyNodeRef, ContentModel.ASPECT_AUDITABLE);

                if (nodeService.hasAspect(checkedOutNodeRef, ContentModel.ASPECT_LOCKABLE))
                {

                    Map<QName, Serializable> checkedOutNodeProperties = nodeService.getProperties(checkedOutNodeRef);
                    Map<QName, Serializable> workingCopyProperties = nodeService.getProperties(workingCopyNodeRef);

                    Long nodeId = nodeDAO.getNodePair(workingCopyNodeRef).getFirst();

                    //get lock properties from checked out node and set them on working copy node in order to be available for restore
                    String lockOwner = (String) checkedOutNodeProperties.get(ContentModel.PROP_LOCK_OWNER);
                    Date expiryDate = (Date) checkedOutNodeProperties.get(ContentModel.PROP_EXPIRY_DATE);
                    String lockTypeStr = (String) checkedOutNodeProperties.get(ContentModel.PROP_LOCK_TYPE);
                    LockType lockType = lockTypeStr != null ? LockType.valueOf(lockTypeStr) : null;
                    String lifetimeStr = (String) checkedOutNodeProperties.get(ContentModel.PROP_LOCK_LIFETIME);
                    Lifetime lifetime = lifetimeStr != null ? Lifetime.valueOf(lifetimeStr) : null;
                    String additionalInfo = (String) checkedOutNodeProperties.get(ContentModel.PROP_LOCK_ADDITIONAL_INFO);

                    nodeService.addAspect(workingCopyNodeRef, ContentModel.ASPECT_ARCHIVE_LOCKABLE, null);

                    workingCopyProperties.put(ContentModel.PROP_ARCHIVED_LOCK_OWNER, lockOwner);
                    workingCopyProperties.put(ContentModel.PROP_ARCHIVED_LOCK_TYPE, lockType);
                    workingCopyProperties.put(ContentModel.PROP_ARCHIVED_LOCK_LIFETIME, lifetime);
                    workingCopyProperties.put(ContentModel.PROP_ARCHIVED_EXPIRY_DATE, expiryDate);
                    workingCopyProperties.put(ContentModel.PROP_ARCHIVED_LOCK_ADDITIONAL_INFO, additionalInfo);

                    // Target associations
                    Collection<Pair<Long, AssociationRef>> targetAssocs = nodeDAO.getTargetNodeAssocs(nodeId, null);
                    for (Pair<Long, AssociationRef> targetAssocPair : targetAssocs)
                    {
                        if (ContentModel.ASSOC_ORIGINAL.equals(targetAssocPair.getSecond().getTypeQName()))
                        {
                            workingCopyProperties.put(ContentModel.PROP_ARCHIVED_TARGET_ASSOCS, targetAssocPair.getSecond());
                        }
                    }
                    // Source associations
                    Collection<Pair<Long, AssociationRef>> sourceAssocs = nodeDAO.getSourceNodeAssocs(nodeId, null);
                    for (Pair<Long, AssociationRef> sourceAssocPair : sourceAssocs)
                    {
                        if (ContentModel.ASSOC_WORKING_COPY_LINK.equals(sourceAssocPair.getSecond().getTypeQName()))
                        {
                            workingCopyProperties.put(ContentModel.PROP_ARCHIVED_SOURCE_ASSOCS, sourceAssocPair.getSecond());
                        }
                    }

                    //update working copy node properties
                    nodeService.setProperties(workingCopyNodeRef, workingCopyProperties);
                }
            }
            finally
            {
                policyBehaviourFilter.enableBehaviour(checkedOutNodeRef, ContentModel.ASPECT_AUDITABLE);
            }

        }
        
    }
    
    /**
     * onRestoreNode policy behaviour
     * 
     * @param nodeRef
     *            the node reference that was restored
     */
    @SuppressWarnings("unchecked")
    @Override
    public void onRestoreNode(ChildAssociationRef childAssocRef)
    {
        NodeRef workingCopyNodeRef = childAssocRef.getChildRef();

        //check that node is working copy 
        if (nodeService.hasAspect(workingCopyNodeRef, ContentModel.ASPECT_WORKING_COPY))
        {
            try
            {

                NodeRef checkedOutNodeRef = null;
                policyBehaviourFilter.disableBehaviour(workingCopyNodeRef, ContentModel.ASPECT_AUDITABLE);

                Map<QName, Serializable> workingCopyProperties = nodeService.getProperties(workingCopyNodeRef);

                //get archived lock properties in order to be restored on the original node
                String lockOwner = (String) workingCopyProperties.get(ContentModel.PROP_ARCHIVED_LOCK_OWNER);
                workingCopyProperties.remove(ContentModel.PROP_ARCHIVED_LOCK_OWNER);
                Date expiryDate = (Date) workingCopyProperties.get(ContentModel.PROP_ARCHIVED_EXPIRY_DATE);
                workingCopyProperties.remove(ContentModel.PROP_ARCHIVED_EXPIRY_DATE);
                String lockTypeStr = (String) workingCopyProperties.get(ContentModel.PROP_ARCHIVED_LOCK_TYPE);
                workingCopyProperties.remove(ContentModel.PROP_ARCHIVED_LOCK_TYPE);
                LockType lockType = lockTypeStr != null ? LockType.valueOf(lockTypeStr) : null;
                String lifetimeStr = (String) workingCopyProperties.get(ContentModel.PROP_ARCHIVED_LOCK_LIFETIME);
                workingCopyProperties.remove(ContentModel.PROP_ARCHIVED_LOCK_LIFETIME);
                Lifetime lifetime = lifetimeStr != null ? Lifetime.valueOf(lifetimeStr) : null;
                String additionalInfo = (String) workingCopyProperties.get(ContentModel.PROP_ARCHIVED_LOCK_ADDITIONAL_INFO);
                workingCopyProperties.remove(ContentModel.PROP_ARCHIVED_LOCK_ADDITIONAL_INFO);

                List<AssociationRef> targetAssocList = (ArrayList<AssociationRef>) workingCopyProperties
                        .get(ContentModel.PROP_ARCHIVED_TARGET_ASSOCS);
                if (targetAssocList != null && targetAssocList.get(0) != null)
                {
                    AssociationRef targetAssoc = (AssociationRef) targetAssocList.get(0);
                    checkedOutNodeRef = targetAssoc.getSourceRef();
                    nodeService.createAssociation( targetAssoc.getSourceRef(),targetAssoc.getTargetRef(), ContentModel.ASSOC_ORIGINAL);

                }
                workingCopyProperties.remove( ContentModel.PROP_ARCHIVED_TARGET_ASSOCS);

                ArrayList<AssociationRef> sourceAssocList = (ArrayList<AssociationRef>) workingCopyProperties
                        .get(ContentModel.PROP_ARCHIVED_SOURCE_ASSOCS);
                if (sourceAssocList != null && sourceAssocList.get(0) != null)
                {
                    AssociationRef sourceAssoc = (AssociationRef) sourceAssocList.get(0);
                    checkedOutNodeRef = sourceAssoc.getSourceRef();
                    nodeService.createAssociation(sourceAssoc.getSourceRef(), sourceAssoc.getTargetRef(),
                            ContentModel.ASSOC_WORKING_COPY_LINK);
                }
                workingCopyProperties.remove(ContentModel.PROP_ARCHIVED_SOURCE_ASSOCS);

                //clean up the archived aspect and properties for working copy node
                nodeService.removeAspect(workingCopyNodeRef, ContentModel.ASPECT_ARCHIVE_LOCKABLE);
                nodeService.setProperties(workingCopyNodeRef, workingCopyProperties);

                //restore properties on original node
                nodeService.addAspect(checkedOutNodeRef, ContentModel.ASPECT_LOCKABLE, null);
                Map<QName, Serializable> checkedOutNodeProperties = nodeService.getProperties(checkedOutNodeRef);

                checkedOutNodeProperties.put(ContentModel.PROP_LOCK_OWNER, lockOwner);
                checkedOutNodeProperties.put(ContentModel.PROP_LOCK_TYPE, lockType);
                checkedOutNodeProperties.put(ContentModel.PROP_LOCK_LIFETIME, lifetime);
                checkedOutNodeProperties.put(ContentModel.PROP_EXPIRY_DATE, expiryDate);
                checkedOutNodeProperties.put(ContentModel.PROP_LOCK_ADDITIONAL_INFO, additionalInfo);

                nodeService.setProperties(checkedOutNodeRef, checkedOutNodeProperties);
            }
            finally
            {
                policyBehaviourFilter.enableBehaviour(workingCopyNodeRef, ContentModel.ASPECT_AUDITABLE);
            }

        }
    }

    @Override
    public void onRemoveAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        // This is simply not allowed.
        throw new UnsupportedOperationException("Use CheckOutCheckInservice to manipulate working copies.");
    }

    /**
     * @return              Returns CopyBehaviourCallback
     */
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return this.workingCopyAspectCopyBehaviourCallback;
    }

    /**
     * Dual behaviour to ensure that <b>cm:name</b> is not copied if the source node has the
     * <b>cm:workingCopy</b> aspect, and to prevent the <b>cm:workingCopy</b> aspect from
     * being carried to the new node.
     * 
     * @author Derek Hulley
     * @since 3.2
     */
    private class WorkingCopyAspectCopyBehaviourCallback extends DefaultCopyBehaviourCallback
    {
        /**
         * Disallows copying of the {@link ContentModel#ASPECT_WORKING_COPY <b>cm:workingCopy</b>} aspect.
         */
        @Override
        public boolean getMustCopy(QName classQName, CopyDetails copyDetails)
        {
            if (classQName.equals(ContentModel.ASPECT_WORKING_COPY))
            {
                return false;
            }
            else
            {
                return true;
            }
        }

        /**
         * Allows copy of workingCopy top-level node to be renamed 
         */
        @Override
        public boolean isTopLevelCanBeRenamed(QName classQName, CopyDetails copyDetails)
        {
            return true;
        }

        /**
         * Prevents copying off the {@link ContentModel#PROP_NAME <b>cm:name</b>} property.
         */
        @Override
        public Map<QName, Serializable> getCopyProperties(
                QName classQName,
                CopyDetails copyDetails,
                Map<QName, Serializable> properties)
        {
            if (classQName.equals(ContentModel.ASPECT_WORKING_COPY))
            {
                return Collections.emptyMap();
            }
            else if (copyDetails.getSourceNodeAspectQNames().contains(ContentModel.ASPECT_WORKING_COPY))
            {
                // Generate a new name for a new copy of a working copy
                String newName = null;

                if (copyDetails.isTargetNodeIsNew())
                {
                    // This is a copy of a working copy to a new node (not a check in). Try to derive a new name from the
                    // node it is checked out from
                    NodeRef checkedOutFrom = checkOutCheckInService.getCheckedOut(copyDetails.getSourceNodeRef());
                    if (checkedOutFrom != null)
                    {
                        String oldName = (String) nodeService.getProperty(checkedOutFrom, ContentModel.PROP_NAME);
                        int extIndex = oldName.lastIndexOf('.');
                        newName = extIndex == -1 ? oldName + "_" + GUID.generate() : oldName.substring(0, extIndex)
                                + "_" + GUID.generate() + oldName.substring(extIndex);
                    }
                }
                else
                {
                    // This is a check-in i.e. a copy to an existing node, so keep a null cm:name
                }

                if (newName == null)
                {
                    properties.remove(ContentModel.PROP_NAME);
                }
                else
                {
                    properties.put(ContentModel.PROP_NAME, newName);
                }
                return properties;
            }
            else
            {
                return properties;
            }
        }
    }
}
