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

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class registers behaviours for the {@link ForumModel#TYPE_POST fm:post} content type.
 * These behaviours maintain the correct value for the {@link ForumModel#PROP_COMMENT_COUNT comment count rollup property}.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 */
public class ForumPostBehaviours implements NodeServicePolicies.OnCreateNodePolicy,
                                            NodeServicePolicies.BeforeDeleteNodePolicy,
                                            NodeServicePolicies.OnUpdatePropertiesPolicy
{
    public static final int COUNT_TRIGGER_VALUE = -1;
    
    private static final Log log = LogFactory.getLog(ForumPostBehaviours.class);
    
    private PolicyComponent policyComponent;
    private CommentService commentService;
    private NodeService nodeService;
    private NodeService rawNodeService;
    
    public void setPolicyComponent(PolicyComponent policyComponent) 
    {
        this.policyComponent = policyComponent;
    }
    
    public void setCommentService(CommentService commentService)
    {
        this.commentService = commentService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setRawNodeService(NodeService nodeService)
    {
        this.rawNodeService = nodeService;
    }

    /**
     * Initialise method
     */
    public void init()
    {
        this.policyComponent.bindClassBehaviour(
                OnUpdatePropertiesPolicy.QNAME,
                ForumModel.ASPECT_COMMENTS_ROLLUP,
                new JavaBehaviour(this, "onUpdateProperties"));
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
    public void onUpdateProperties(NodeRef commentsRollupNode,
            Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        // This method is only concerned with the value of fm:commentCount.
        // If it has been set to a trigger value, then we initiate a full recalculation of the comment count.
        Serializable newCommentCount = after.get(ForumModel.PROP_COMMENT_COUNT);
        if (newCommentCount != null)
        {
            Integer newCommentCountInt = (Integer)newCommentCount;
            if (newCommentCountInt == COUNT_TRIGGER_VALUE)
            {
                if (log.isDebugEnabled())
                {
                    StringBuilder msg = new StringBuilder();
                    msg.append(commentsRollupNode)
                       .append(" had its ").append(ForumModel.PROP_COMMENT_COUNT.getLocalName())
                       .append(" property set to ").append(newCommentCountInt);
                    log.debug(msg.toString());
                    log.debug("Triggering a comment recount...");
                }
                
                final Integer realCommentTotal = calculateCommentTotalByNodeCounting(commentsRollupNode);
                if (realCommentTotal != null && realCommentTotal != -1)
                {
                    nodeService.setProperty(commentsRollupNode, ForumModel.PROP_COMMENT_COUNT, realCommentTotal);
                }
            }
        }
    }

    /**
     * Calculate the comment total for the specified node.
     * 
     * @param discussableNode discussable node.
     * @return the recount value or <tt>null</tt> if it is not possible to calculate the total.
     */
    private Integer calculateCommentTotalByNodeCounting(NodeRef discussableNode)
    {
        // This method only counts "Share comments" and not Explorer comments or other fm:post nodes.
        Integer result = null;
        
        if (nodeService.hasAspect(discussableNode, ForumModel.ASPECT_DISCUSSABLE))
        {
            NodeRef topicNode = commentService.getShareCommentsTopic(discussableNode);
            
            if (log.isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Recounting comments for node ").append(discussableNode);
                log.debug(msg.toString());
                
                msg = new StringBuilder();
                msg.append("Topic node: ").append(topicNode);
                log.debug(msg.toString());
            }
            
            // We'll ignore discussable nodes which do not have an fm:topic in the correct
            // location - as used by "Share comments" - as opposed to e.g. Explorer client discussion fm:posts.
            if (topicNode != null)
            {
                // Need to recalculate by hand.
                
                //TODO This could be replaced with a GetChildrenCannedQuery.
                // Look for fm:post nodes only.
                Set<QName> childNodeTypeQNames = new HashSet<QName>();
                childNodeTypeQNames.add(ForumModel.TYPE_POST);
                
                // We'll use the raw, small 'n' nodeService as the big 'N' NodeService's interceptors would limit results.
                List<ChildAssociationRef> fmPostChildren = rawNodeService.getChildAssocs(topicNode, childNodeTypeQNames);
                result = new Integer(fmPostChildren.size());
            }
        }
        return result;
    }
    
    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        adjustCommentCount(childAssocRef.getChildRef(), true);
    }
    
    @Override
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        adjustCommentCount(nodeRef, false);
    }
    
    /**
     * This method adjusts the {@link ForumModel#PROP_COMMENT_COUNT} based on the supplied increment/decrement flag
     * .
     * @param fmPostNode the fm:post node (the comment node)
     * @param incrementing <tt>true</tt> if we're incrementing the count, else <tt>false</tt>.
     */
    private void adjustCommentCount(NodeRef fmPostNode, boolean incrementing)
    {
        // We have a new or a deleted comment under a discussable node.
        // We need to find the fm:commentsCount ancestor to this comment node (if there is one) and adjust its commentCount
        NodeRef discussableAncestor = commentService.getDiscussableAncestor(fmPostNode);
        
        if (discussableAncestor != null)
        {
            if (discussableNodeRequiresFullRecount(discussableAncestor))
            {
                Integer recount = calculateCommentTotalByNodeCounting(discussableAncestor);
                
                if (recount != null)
                {
                    nodeService.addAspect(discussableAncestor, ForumModel.ASPECT_COMMENTS_ROLLUP, null);
                    int newCountValue = recount;
                    // If the node is being deleted then the above node-count will include the to-be-deleted node.
                    // This is because the policies are onCreateNode and *before*DeleteNode
                    if ( !incrementing)
                    {
                        newCountValue--;
                    }
                    
                    if (log.isDebugEnabled())
                    {
                        log.debug(discussableAncestor + " newCountValue: " + newCountValue);
                    }
                    
                    nodeService.setProperty(discussableAncestor, ForumModel.PROP_COMMENT_COUNT, newCountValue);
                }
                
            }
            else
            {
                Integer existingCommentCount = (Integer) nodeService.getProperty(discussableAncestor, ForumModel.PROP_COMMENT_COUNT);
                int existingCommentCountInt = existingCommentCount == null ? 0 : existingCommentCount.intValue();
                
                int delta = incrementing ? 1 : -1;
                
                nodeService.setProperty(discussableAncestor, ForumModel.PROP_COMMENT_COUNT, existingCommentCountInt + delta);
            }
        }
    }
    
    /**
     * This method checks if a {@link ForumModel#ASPECT_DISCUSSABLE} node requires a full recount of its comments.
     * This will occur if any of the following are true:
     * <ul>
     *    <li>the node has no {@link ForumModel#ASPECT_COMMENTS_ROLLUP} aspect</li>
     *    <li>the {@link ForumModel#PROP_COMMENT_COUNT} is a negative number</li>
     * </ul>
     */
    private boolean discussableNodeRequiresFullRecount(NodeRef discussableNode)
    {
        boolean result;
        
        if ( !nodeService.hasAspect(discussableNode, ForumModel.ASPECT_DISCUSSABLE))
        {
            throw new AlfrescoRuntimeException("Node did not have fm:discussable aspect as expected.");
        }
        
        if ( !nodeService.hasAspect(discussableNode, ForumModel.ASPECT_COMMENTS_ROLLUP))
        {
            result = true;
        }
        else
        {
            Integer existingCommentCount = (Integer) nodeService.getProperty(discussableNode, ForumModel.PROP_COMMENT_COUNT);
            result = existingCommentCount == null || existingCommentCount <= COUNT_TRIGGER_VALUE;
        }
        
        if (log.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append(discussableNode).append(" does");
            if ( !result)
            {
                msg.append(" not");
            }
            msg.append(" require full comment recount");
            log.debug(msg.toString());
        }
        
        return result;
    }
}
