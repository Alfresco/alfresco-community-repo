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
package org.alfresco.repo.forum;

import org.alfresco.model.ForumModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * This class registers behaviours for the {@link ForumModel#TYPE_POST fm:post} content type.
 * These behaviours maintain the correct value for the {@link ForumModel#PROP_COMMENT_COUNT comment count rollup property}.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 */
public class ForumPostBehaviours implements NodeServicePolicies.OnCreateNodePolicy,
                                            NodeServicePolicies.BeforeDeleteNodePolicy
{
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    
    public void setPolicyComponent(PolicyComponent policyComponent) 
    {
        this.policyComponent = policyComponent;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Initialise method
     */
    public void init()
    {
        this.policyComponent.bindClassBehaviour(
                OnCreateNodePolicy.QNAME,
                ForumModel.TYPE_POST,
                new JavaBehaviour(this, "onCreateNode"));
        this.policyComponent.bindClassBehaviour(
                BeforeDeleteNodePolicy.QNAME,
                ForumModel.TYPE_POST,
                new JavaBehaviour(this, "beforeDeleteNode"));
    }
    
    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        // We have a new comment under a discussable node.
        // We need to find the fm:commentsCount ancestor to this comment node and increment its commentCount
        NodeRef commentsRollupNode = getCommentsRollupAncestor(childAssocRef.getParentRef());
        
        if (commentsRollupNode != null)
        {
            int existingCommentCount = (Integer) nodeService.getProperty(commentsRollupNode, ForumModel.PROP_COMMENT_COUNT);
            nodeService.setProperty(commentsRollupNode, ForumModel.PROP_COMMENT_COUNT, existingCommentCount + 1);
        }
    }

    @Override
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        // We have one less comment under a discussable node.
        // We need to find the fm:commentsRollup ancestor to this comment node and decrement its commentCount
        NodeRef topicNode = nodeService.getPrimaryParent(nodeRef).getParentRef();
        NodeRef commentsRollupNode = getCommentsRollupAncestor(topicNode);
        
        if (commentsRollupNode != null)
        {
            int existingCommentCount = (Integer) nodeService.getProperty(commentsRollupNode, ForumModel.PROP_COMMENT_COUNT);
            int newCommentCount = Math.max(0, existingCommentCount - 1); // Negative values should not occur, but we'll stop them anyway.
            nodeService.setProperty(commentsRollupNode, ForumModel.PROP_COMMENT_COUNT, newCommentCount);
        }
    }
    
    /**
     * This method navigates up the primary parent containment path to find the ancestor with the
     * {@link ForumModel#ASPECT_COMMENTS_ROLLUP commentsRollup} aspect.
     * 
     * @param topicNode
     * @return the NodeRef of the commentsRollup ancestor if there is one, else <code>null</code>.
     */
    private NodeRef getCommentsRollupAncestor(NodeRef topicNode)
    {
        // We are specifically trying to roll up "comment" counts here. In other words the number of "comments" on a node
        // as applied through the Share UI.
        // We are not trying to roll up generic fm:post counts. Although, of course, comments are modelled as fm:post nodes.
        // So there are two scenarios in which we do not want to roll up changes to the count.
        // 1. When the fm:post node is not a Share comment.
        // 2. When the node is being deleted as part of a cascade delete.
        // If an ancestor node to an fm:post is deleted then the parent structure may have been flattened within the archive store.
        //
        NodeRef result = null;
        
        NodeRef forumNode = nodeService.getPrimaryParent(topicNode).getParentRef();
        if (ForumModel.TYPE_FORUM.equals(nodeService.getType(forumNode)) && !forumNode.getStoreRef().equals(StoreRef.PROTOCOL_ARCHIVE))
        {
            NodeRef commentsRollupNode = nodeService.getPrimaryParent(forumNode).getParentRef();
            
            if (!commentsRollupNode.getStoreRef().equals(StoreRef.PROTOCOL_ARCHIVE))
            {
                if (! nodeService.hasAspect(commentsRollupNode, ForumModel.ASPECT_COMMENTS_ROLLUP))
                {
                    result = null;
                }
                else
                {
                    result = commentsRollupNode;
                }
            }
        }
        return result;
    }
}
