/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;

public class WorkingCopyAspect implements CopyServicePolicies.OnCopyNodePolicy
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
     * The working copy aspect copy behaviour callback.
     * */
    private WorkingCopyAspectCopyBehaviourCallback workingCopyAspectCopyBehaviourCallback = new WorkingCopyAspectCopyBehaviourCallback();
    
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
                QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
                ContentModel.TYPE_CMOBJECT,
                new JavaBehaviour(this, "getCopyCallback"));
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
                ContentModel.ASPECT_WORKING_COPY,
                new JavaBehaviour(this, "getCopyCallback"));
        
        // register onBeforeDelete class behaviour for the working copy aspect
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
                ContentModel.ASPECT_WORKING_COPY,
                new JavaBehaviour(this, "beforeDeleteNode"));
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
               if (this.lockService.getLockStatus(origNodeRef).equals(LockStatus.NO_LOCK) == false)
               {               
                   // Release the lock on the origional node
                   this.lockService.unlock(origNodeRef);
               }
            }
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
        public Map<QName, Serializable> getCopyProperties(QName classQName, CopyDetails copyDetails,
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

                // This is a copy of a working copy to a new node (not a check in). Try to derive a new name from the
                // node it is checked out from
                if (copyDetails.isTargetNodeIsNew() && copyDetails.getSourceNodeAspectQNames().contains(ContentModel.ASPECT_COPIEDFROM))
                {
                    NodeRef checkedOutFrom = (NodeRef) copyDetails.getSourceNodeProperties().get(
                            ContentModel.PROP_COPY_REFERENCE);
                    if (nodeService.exists(checkedOutFrom))
                    {
                        String oldName = (String) nodeService.getProperty(checkedOutFrom, ContentModel.PROP_NAME);
                        int extIndex = oldName.lastIndexOf('.');
                        newName = extIndex == -1 ? oldName + "_" + GUID.generate() : oldName.substring(0, extIndex)
                                + "_" + GUID.generate() + oldName.substring(extIndex);
                    }
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
