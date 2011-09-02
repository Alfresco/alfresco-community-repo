/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.forum;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * Discussion-specific behaviours.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class DiscussableAspect implements
            NodeServicePolicies.OnAddAspectPolicy,
            CopyServicePolicies.OnCopyNodePolicy,
            CopyServicePolicies.OnCopyCompletePolicy
{
    private static final String KEY_WORKING_COPIES = DiscussableAspect.class.getName() + ".WorkingCopies";
    
    private static final Log logger = LogFactory.getLog(DiscussableAspect.class);
    
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    
    public void setPolicyComponent(PolicyComponent policyComponent) 
    {
        this.policyComponent = policyComponent;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public final void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    /**
     * Initialise method
     */
    public void init()
    {
        // All forum-related copy behaviour uses the same copy callback
        this.policyComponent.bindClassBehaviour(
        QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"),
                ForumModel.ASPECT_DISCUSSABLE,
                new JavaBehaviour(this, "onAddAspect"));
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
                ForumModel.ASPECT_DISCUSSABLE,
                new JavaBehaviour(this, "getCopyCallback"));
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCopyComplete"),
                ForumModel.ASPECT_DISCUSSABLE,
                new JavaBehaviour(this, "onCopyComplete"));
    }
    
    /**
     * @return              Returns {@link DiscussableAspectCopyBehaviourCallback}
     */
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return DiscussableAspectCopyBehaviourCallback.INSTANCE;
    }
    
    /**
     * Copy behaviour for the <b>fm:discussable</b> aspect.
     * <p>
     * Only the aspect is copied (to get the default behaviour).  All topics are copied later.
     * 
     * @author Derek Hulley
     * @since 3.2
     */
    private static class DiscussableAspectCopyBehaviourCallback extends DoNothingCopyBehaviourCallback
    {
        private static final CopyBehaviourCallback INSTANCE = new DiscussableAspectCopyBehaviourCallback();
        
        /**
         * Copy the aspect over only if the source document has also been checked out
         */
        @Override
        public boolean getMustCopy(QName classQName, CopyDetails copyDetails)
        {
            if (copyDetails.getSourceNodeAspectQNames().contains(ContentModel.ASPECT_WORKING_COPY) &&
                    !copyDetails.isTargetNodeIsNew())
            {
                // We are copying back from a working copy to the original node (probably)
                // We need to do a full merge of the discussions.  Keep track of the nodes
                // that need this behaviour and complete the copy after the copy completes.
                Set<NodeRef> nodeRefs = TransactionalResourceHelper.getSet(KEY_WORKING_COPIES);
                nodeRefs.add(copyDetails.getSourceNodeRef());
            }
            return false;
        }
    }
    
    /**
     * Ensure that the node has a <b>fm:forum</b> child node otherwise create one
     */
    public void onAddAspect(NodeRef discussableNodeRef, QName aspectTypeQName)
    {
        String name = (String)this.nodeService.getProperty(
                discussableNodeRef, 
                ContentModel.PROP_NAME);
        String forumName = I18NUtil.getMessage("discussion.discussion_for", new Object[] {name});
        
        NodeRef forumNodeRef = getForum(discussableNodeRef);
        
        if (forumNodeRef == null)
        {
            Map<QName, Serializable> forumProps = new HashMap<QName, Serializable>(1);
            forumProps.put(ContentModel.PROP_NAME, forumName);
            
            ChildAssociationRef childRef = nodeService.createNode(
                    discussableNodeRef, 
                ForumModel.ASSOC_DISCUSSION,
                QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "discussion"), 
                ForumModel.TYPE_FORUM, forumProps);
          
            forumNodeRef = childRef.getChildRef();
        }
        else
        {
            // Just adjust the name
            nodeService.setProperty(forumNodeRef, ContentModel.PROP_NAME, forumName);
        }
      
        // apply the uifacets aspect
        Map<QName, Serializable> uiFacetsProps = new HashMap<QName, Serializable>(5);
        uiFacetsProps.put(ApplicationModel.PROP_ICON, "forum");
        this.nodeService.addAspect(forumNodeRef, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);
        
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Created forum node for discussion: \n" +
                    "   Discussable Node: " + discussableNodeRef + "\n" +
                    "   Forum Node:       " + forumNodeRef);
        }
    }

    /**
     * Retrieves the forum node the the given discussable
     * 
     * @return          Returns the <b>fm:forum</b> node or <tt>null</tt>
     */
    private NodeRef getForum(NodeRef discussableNodeRef)
    {
        List<ChildAssociationRef> destChildren = nodeService.getChildAssocs(
                discussableNodeRef,
                ForumModel.ASSOC_DISCUSSION,
                RegexQNamePattern.MATCH_ALL);
        // Take the first one
        if (destChildren.size() == 0)
        {
            return null;
        }
        else
        {
            // We just take the first one
            ChildAssociationRef discussionAssoc = destChildren.get(0);
            return discussionAssoc.getChildRef();
        }
    }
//
//    /**
//     * Copies the discussions to the new node, whilst being sensitive to any existing discussions.
//     */
//    public void onCopyComplete(
//            QName classQName,
//            NodeRef sourceNodeRef,
//            NodeRef targetNodeRef,
//            boolean copyToNewNode,
//            Map<NodeRef,NodeRef> copyMap)
//    {
//        // if the copy is not a new node it is a checkin, we therefore
//        // need to copy any discussions from the working copy document
//        // to the document being checked in
//        if (copyToNewNode)
//        {
//            // We don't care about new copies, just copies to existing nodes
//            return;
//        }
//        List<ChildAssociationRef> sourceChildren = nodeService.getChildAssocs(sourceNodeRef, 
//                ForumModel.ASSOC_DISCUSSION, RegexQNamePattern.MATCH_ALL);
//      
//        if (sourceChildren.size() != 1)
//        {
//           throw new CheckOutCheckInServiceException(
//                 "The source node has the discussable aspect but does not have 1 child, it has " + 
//                 sourceChildren.size() + " children!");
//        }
//        
//        NodeRef sourceForum = sourceChildren.get(0).getChildRef();
//      
//        // get the forum for the destination node, it's created if necessary
//        NodeRef destinationForum = getDestinationForum(targetNodeRef);
//          
//        // copy any topics from the source forum to the destination forum
//        int copied = 0;
//        List<ChildAssociationRef> sourceForums = nodeService.getChildAssocs(sourceForum);
//        for (ChildAssociationRef childRef : sourceForums)
//        {
//            String topicName = null;
//            NodeRef childNode = childRef.getChildRef();
//            if (nodeService.getType(childNode).equals(ForumModel.TYPE_TOPIC))
//            {
//                try
//                {
//                    // work out the name for the copied topic
//                    String childName = nodeService.getProperty(childNode, 
//                          ContentModel.PROP_NAME).toString();
//                    Serializable labelProp = nodeService.getProperty(targetNodeRef, 
//                          ContentModel.PROP_VERSION_LABEL);
//                    if (labelProp == null)
//                    {
//                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
//                        topicName = childName + " - " + dateFormat.format(new Date());
//                    }
//                    else
//                    {
//                        topicName = childName + " (" + labelProp.toString() + ")";
//                    }
//
//                    fileFolderService.copy(childNode, destinationForum, topicName);
//                    copied++;
//                }
//                catch (FileNotFoundException fnfe)
//                {
//                    throw new CheckOutCheckInServiceException(
//                          "Failed to copy topic from working copy to checked out content", fnfe);
//                }
//                catch (FileExistsException fee)
//                {
//                    throw new CheckOutCheckInServiceException("Failed to checkin content as a topic called " + 
//                          topicName + " already exists on the checked out content", fee);
//                }
//            }
//        }
//          
//        if (logger.isDebugEnabled())
//            logger.debug("Copied " + copied + " topics from the working copy to the checked out content");
//    }
//

    public void onCopyComplete(
            QName classRef,
            NodeRef sourceNodeRef,
            NodeRef targetNodeRef,
            boolean copyToNewNode,
            Map<NodeRef, NodeRef> copyMap)
    {
        Set<NodeRef> workingCopyNodeRefs = TransactionalResourceHelper.getSet(KEY_WORKING_COPIES);
        if (!workingCopyNodeRefs.contains(sourceNodeRef))
        {
            // This is not one of the nodes that needs to have discussions copied over
            return;
        }
        
        // First check that the source node has forums
        NodeRef sourceForumNodeRef = getForum(sourceNodeRef);
        if (sourceForumNodeRef == null)
        {
            // Missing!  Clean the source node up!
            nodeService.removeAspect(sourceNodeRef, ForumModel.ASPECT_DISCUSSABLE);
            return;
        }
        
        // The aspect may or may not exist on the target node
        if (!nodeService.hasAspect(targetNodeRef, ForumModel.ASPECT_DISCUSSABLE))
        {
            // Add the aspect
            nodeService.addAspect(targetNodeRef, ForumModel.ASPECT_DISCUSSABLE, null);
        }
        // Get the forum node
        NodeRef targetForumNodeRef = getForum(targetNodeRef);
        // Merge the forum topics
        List<ChildAssociationRef> topicAssocRefs = nodeService.getChildAssocs(
                sourceForumNodeRef,
                Collections.singleton(ForumModel.TYPE_TOPIC));
        int copied = 0;
        for (ChildAssociationRef topicAssocRef : topicAssocRefs)
        {
            NodeRef topicNodeRef = topicAssocRef.getChildRef();
            try
            {
                // work out the name for the copied topic
                String topicName;
                String topicNodeName = nodeService.getProperty(topicNodeRef, ContentModel.PROP_NAME).toString();
                Serializable labelProp = nodeService.getProperty(targetNodeRef, ContentModel.PROP_VERSION_LABEL);
                if (labelProp == null)
                {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
                    topicName = topicNodeName + " - " + dateFormat.format(new Date());
                }
                else
                {
                    topicName = topicNodeName + " (" + labelProp.toString() + ")";
                }

                if (fileFolderService.searchSimple(targetForumNodeRef, topicName) != null)
                {
                    // A topic with that name already exists
                    continue;
                }
                fileFolderService.copy(topicNodeRef, targetForumNodeRef, topicName);
                copied++;
            }
            catch (FileExistsException e)
            {
                // We checked for this, so this is a concurrency condition
                throw new ConcurrencyFailureException("Target topic exists: " + e.getMessage(), e);
            }
            catch (FileNotFoundException e)
            {
                // The node was there, but now it's gone
                throw new ConcurrencyFailureException("Forum was deleted: " + e.getMessage(), e);
            }
        }
    }
}
