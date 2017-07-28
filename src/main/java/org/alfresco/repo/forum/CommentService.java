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
package org.alfresco.repo.forum;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ForumModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.Map;

/**
 * A service for handling comments.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 */
public interface CommentService
{
    /**
     * Thi method retrieves the ancestor in the repository containment hierarchy having the
     * {@link ForumModel#ASPECT_DISCUSSABLE fm:discussable} aspect.
     * 
     * @param descendantNodeRef The nodeRef which descends from the f:discussable node.
     * @return the fm:discussable ancestor if there is one, else <tt>null</tt>
     * @throws AlfrescoRuntimeException if the specified expectedNodeType is not correct.
     */
    NodeRef getDiscussableAncestor(NodeRef descendantNodeRef);
    
    /**
     * This method retrieves the {@link ForumModel#TYPE_TOPIC fm:topic} NodeRef which holds the Share comments for
     * the specified {@link ForumModel#ASPECT_DISCUSSABLE fm:discussable} node.
     * 
     * @param discussableNode the node whose Share comments are sought.
     * @return the fm:topic NodeRef, if one exists, else <tt>null</tt>.
     */
    NodeRef getShareCommentsTopic(NodeRef discussableNode);

    /**
     * Creates a comment for the discussableNode
     * 
     * @param discussableNode the node in Share which is being commented on .
     * @param title - title of the comment
     * @param comment - body of the comment
     * @param suppressRollups - should it suppressRollups
     * @return NodeRef - the created node reference
     */
    NodeRef createComment(NodeRef discussableNode, String title, String comment, boolean suppressRollups);
    
    /**
     * Updates the comment
     * 
     * @param commentNodeRef the comment node.
     * @param title - title of the comment
     * @param comment - body of the comment
     */
    void updateComment(NodeRef commentNodeRef, String title, String comment);

    /**
     * Returns a paged list of comments.
     * 
     * @param discussableNode the node which is being commented on .
     * @param paging paging.
     * @return a list of comment nodes
     */
    PagingResults<NodeRef> listComments(NodeRef discussableNode, PagingRequest paging);

    /**
     * Deletes the comment for the discussableNode
     * 
     * @param commentNodeRef the node in Share which is being commented on.
     */
    void deleteComment(NodeRef commentNodeRef);

    /**
     * canEdit / canDelete
     * 
     * @param discussableNode
     * @param commentNodeRef
     * @return
     */
    Map<String, Boolean> getCommentPermissions(NodeRef discussableNode, NodeRef commentNodeRef);
    
    String CAN_EDIT = "canEdit";
    String CAN_DELETE = "canDelete";
}
