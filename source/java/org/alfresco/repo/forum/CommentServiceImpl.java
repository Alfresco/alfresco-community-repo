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

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Neil Mc Erlean
 * @since 4.0
 */
public class CommentServiceImpl implements CommentService
{
    /**
     * Naming convention for Share comment model. fm:forum contains fm:topic
     */
    private static final QName FORUM_TO_TOPIC_ASSOC_QNAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Comments");
    
    // Injected services
    private NodeService nodeService;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    @Override
    public NodeRef getDiscussableAncestor(NodeRef descendantNodeRef)
    {
        // For "Share comments" i.e. fm:post nodes created via the Share commenting UI, the containment structure is as follows:
        // fm:discussable
        //    - fm:forum
        //        - fm:topic
        //            - fm:post
        // For other fm:post nodes the ancestor structure may be slightly different. (cf. Share discussions, which don't have 'forum')
        //
        // In order to ensure that we only return the discussable ancestor relevant to Share comments, we'll climb the
        // containment tree in a controlled manner.
        // We could navigate up looking for the first fm:discussable ancestor, but that might not find the correct node - it could,
        // for example, find a parent folder which was discussable.
        
        NodeRef result = null;
        if (nodeService.getType(descendantNodeRef).equals(ForumModel.TYPE_POST))
        {
            NodeRef topicNode = nodeService.getPrimaryParent(descendantNodeRef).getParentRef();
            
            if (nodeService.getType(topicNode).equals(ForumModel.TYPE_TOPIC))
            {
                NodeRef forumNode = nodeService.getPrimaryParent(topicNode).getParentRef();
                
                if (nodeService.getType(forumNode).equals(ForumModel.TYPE_FORUM))
                {
                    NodeRef discussableNode = nodeService.getPrimaryParent(forumNode).getParentRef();
                    
                    if (nodeService.hasAspect(discussableNode, ForumModel.ASPECT_DISCUSSABLE))
                    {
                        result = discussableNode;
                    }
                }
            }
        }
        
        return result;
    }
    
    @Override
    public NodeRef getShareCommentsTopic(NodeRef discussableNode)
    {
        NodeRef result = null;
        
        if (nodeService.hasAspect(discussableNode, ForumModel.ASPECT_DISCUSSABLE))
        {
            // We navigate down the "Share comments" containment model, which is based on the more general forum model,
            // but with certain naming conventions.
            List<ChildAssociationRef> fora = nodeService.getChildAssocs(discussableNode, ForumModel.ASSOC_DISCUSSION, ForumModel.ASSOC_DISCUSSION, true);
            
            // There should only be one such assoc.
            if ( !fora.isEmpty())
            {
                final NodeRef firstForumNode = fora.get(0).getChildRef();
                List<ChildAssociationRef> topics = nodeService.getChildAssocs(firstForumNode, ContentModel.ASSOC_CONTAINS, FORUM_TO_TOPIC_ASSOC_QNAME, true);
                
                // Likewise, only one.
                if ( !topics.isEmpty())
                {
                    final NodeRef firstTopicNode = topics.get(0).getChildRef();
                    result = firstTopicNode;
                }
            }
        }
        
        return result;
    }
}
