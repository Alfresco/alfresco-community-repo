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

package org.alfresco.repo.coci;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;

public class WorkingCopyAspect implements CopyServicePolicies.OnCopyNodePolicy
{    
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private LockService lockService;
    private CheckOutCheckInService checkOutCheckInService;
    
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
        
        // register onBeforeDelete class behaviour for the working copy aspect
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.BeforeDeleteNodePolicy.QNAME,
                ContentModel.ASPECT_WORKING_COPY,
                new JavaBehaviour(this, "beforeDeleteWorkingCopy"));
        // register onBeforeDelete class behaviour for the checked-out aspect
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
            lockService.unlock(checkedOutNodeRef);
            nodeService.removeAspect(checkedOutNodeRef, ContentModel.ASPECT_CHECKED_OUT);
        }
    }
    
    /**
     * @return              Returns {@link WorkingCopyAspectCopyBehaviourCallback}
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
