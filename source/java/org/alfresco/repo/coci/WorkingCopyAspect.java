/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.alfresco.repo.coci;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class WorkingCopyAspect
{    
    /**
     * Policy component
     */
    private PolicyComponent policyComponent;
    
    /**
     * The node service
     */
    private NodeService nodeService;
    
    /**
     * The lock service
     */
    private LockService lockService;
    
    /**
     * Sets the policy component
     * 
     * @param policyComponent  the policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent) 
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * Set the node service
     * 
     * @param nodeService   the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the lock service
     * 
     * @param lockService   the lock service
     */
    public void setLockService(LockService lockService)
    {
        this.lockService = lockService;
    }
    
    /**
     * Initialise method
     */
    public void init()
    {
        // Register copy behaviour for the working copy aspect
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCopyNode"),
                ContentModel.ASPECT_WORKING_COPY,
                new JavaBehaviour(this, "onCopy"));
        
        // register onBeforeDelete class behaviour for the working copy aspect
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
                ContentModel.ASPECT_WORKING_COPY,
                new JavaBehaviour(this, "beforeDeleteNode"));
    }
    
    /**
     * onCopy policy behaviour
     * 
     * @see org.alfresco.repo.copy.CopyServicePolicies.OnCopyNodePolicy#onCopyNode(QName, NodeRef, StoreRef, boolean, PolicyScope)
     */
    public void onCopy(
            QName sourceClassRef, 
            NodeRef sourceNodeRef, 
            StoreRef destinationStoreRef,
            boolean copyToNewNode,
            PolicyScope copyDetails)
    {
        if (copyToNewNode == false)
        {
            // Make sure that the name of the node is not updated with the working copy name
            copyDetails.removeProperty(ContentModel.PROP_NAME);
        }
        
        // NOTE: the working copy aspect is not added since it should not be copyied
    }
    
    /**
     * beforeDeleteNode policy behaviour
     * 
     * @param nodeRef   the node reference about to be deleted
     */
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        // Prior to deleting a working copy the lock on the origional node should be released
        // Note: we do not call cancelCheckOut since this will also attempt to delete the node is question
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY) == true &&
            this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_COPIEDFROM) == true)
        {
            // Get the origional node
            NodeRef origNodeRef = (NodeRef)this.nodeService.getProperty(nodeRef, ContentModel.PROP_COPY_REFERENCE);
            if (origNodeRef != null)
            {                  
               // Release the lock on the origional node
               this.lockService.unlock(origNodeRef);                
            }
        }
    }

}
