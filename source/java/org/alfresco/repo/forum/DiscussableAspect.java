/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.repo.forum;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.service.cmr.coci.CheckOutCheckInServiceException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DiscussableAspect
{
    private static final Log logger = LogFactory.getLog(DiscussableAspect.class);
    
    /**
     * Policy component
     */
    private PolicyComponent policyComponent;
    
    /**
     * The node service
     */
    private NodeService nodeService;
    
    /**
     * The file folder service
     */
    private FileFolderService fileFolderService;
    
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
     * Set the file folder service
     * 
     * @param fileFolderService   the file folder service
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }
    
    /**
     * Initialise method
     */
    public void init()
    {
        // Register copy behaviour for the discussable aspect
        this.policyComponent.bindClassBehaviour(
        QName.createQName(NamespaceService.ALFRESCO_URI, "onCopyNode"),
                ForumModel.ASPECT_DISCUSSABLE,
                new JavaBehaviour(this, "onCopy"));
        
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCopyComplete"),
                ForumModel.ASPECT_DISCUSSABLE,
                new JavaBehaviour(this, "onCopyComplete"));
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
        // NOTE: we intentionally don't do anything in here, this stops the discussable
        //       aspect from being added to the new copied node - the behaviour we want.
    }
    
    public void onCopyComplete(
         QName classRef,
         NodeRef sourceNodeRef,
         NodeRef destinationRef,
         boolean copyNewNode,
         Map<NodeRef, NodeRef> copyMap)
    {
        // if the copy is not a new node it is a checkin, we therefore
        // need to copy any discussions from the working copy document
        // to the document being checked in
        if (copyNewNode == false)
        {
            List<ChildAssociationRef> sourceChildren = this.nodeService.getChildAssocs(sourceNodeRef, 
                    ForumModel.ASSOC_DISCUSSION, RegexQNamePattern.MATCH_ALL);
          
            if (sourceChildren.size() != 1)
            {
               throw new CheckOutCheckInServiceException(
                     "The source node has the discussable aspect but does not have 1 child, it has " + 
                     sourceChildren.size() + " children!");
            }
            
            NodeRef sourceForum = sourceChildren.get(0).getChildRef();
          
            // get the forum for the destination node, it's created if necessary
            NodeRef destinationForum = getDestinationForum(destinationRef);
              
            // copy any topics from the source forum to the destination forum
            int copied = 0;
            List<ChildAssociationRef> sourceForums = this.nodeService.getChildAssocs(sourceForum);
            for (ChildAssociationRef childRef : sourceForums)
            {
                String topicName = null;
                NodeRef childNode = childRef.getChildRef();
                if (this.nodeService.getType(childNode).equals(ForumModel.TYPE_TOPIC))
                {
                    try
                    {
                        // work out the name for the copied topic
                        String childName = this.nodeService.getProperty(childNode, 
                              ContentModel.PROP_NAME).toString();
                        Serializable labelProp = this.nodeService.getProperty(destinationRef, 
                              ContentModel.PROP_VERSION_LABEL);
                        if (labelProp == null)
                        {
                            topicName = childName + " - " + new Date();
                        }
                        else
                        {
                            topicName = childName + " (" + labelProp.toString() + ")";
                        }

                        this.fileFolderService.copy(childNode, destinationForum, topicName);
                        copied++;
                    }
                    catch (FileNotFoundException fnfe)
                    {
                        throw new CheckOutCheckInServiceException(
                              "Failed to copy topic from working copy to checked out content", fnfe);
                    }
                    catch (FileExistsException fee)
                    {
                        throw new CheckOutCheckInServiceException("Failed to checkin content as a topic called " + 
                              topicName + " already exists on the checked out content", fee);
                    }
                }
            }
              
            if (logger.isDebugEnabled())
                logger.debug("Copied " + copied + " topics from the working copy to the checked out content");
        }
    }
    
    /**
     * Retrieves or creates the forum node for the given destination node
     * 
     * @param destNodeRef The node to get the forum for
     * @return NodeRef representing the forum
     */
    private NodeRef getDestinationForum(NodeRef destNodeRef)
    {
        NodeRef destinationForum = null;
        
        if (this.nodeService.hasAspect(destNodeRef, ForumModel.ASPECT_DISCUSSABLE))
        {
            List<ChildAssociationRef> destChildren = this.nodeService.getChildAssocs(destNodeRef, 
                ForumModel.ASSOC_DISCUSSION, RegexQNamePattern.MATCH_ALL);
            
            if (destChildren.size() != 1)
            {
                throw new IllegalStateException("Locked node has the discussable aspect but does not have 1 child, it has " + 
                                                destChildren.size() + " children!");
            }
            
            destinationForum = destChildren.get(0).getChildRef();
        }
        else
        {
           // create the forum - TODO: Move this to a repo discussion service so that it can
           //                          be shared between here and the discussion wizard
           
           // add the discussable aspect
           this.nodeService.addAspect(destNodeRef, ForumModel.ASPECT_DISCUSSABLE, null);
         
           // create a child forum space using the child association just introduced by
           // adding the discussable aspect
           String name = (String)this.nodeService.getProperty(destNodeRef, 
               ContentModel.PROP_NAME);
           String forumName = I18NUtil.getMessage("coci_service.discussion_for", new Object[] {name});
         
           Map<QName, Serializable> forumProps = new HashMap<QName, Serializable>(1);
           forumProps.put(ContentModel.PROP_NAME, forumName);
           
           ChildAssociationRef childRef = this.nodeService.createNode(destNodeRef, 
               ForumModel.ASSOC_DISCUSSION,
               QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "discussion"), 
               ForumModel.TYPE_FORUM, forumProps);
         
           destinationForum = childRef.getChildRef();

           // apply the uifacets aspect
           Map<QName, Serializable> uiFacetsProps = new HashMap<QName, Serializable>(5);
           uiFacetsProps.put(ApplicationModel.PROP_ICON, "forum");
           this.nodeService.addAspect(destinationForum, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);
        }
        
        return destinationForum;
    }
}
