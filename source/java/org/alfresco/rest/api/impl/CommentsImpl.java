/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api.impl;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.rest.api.Comments;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Comment;
import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TypeConstraint;

/**
 * Centralises access to comment services and maps between representations.
 * 
 * @author steveglover
 * @since publicapi1.0
 */
public class CommentsImpl implements Comments
{
    private Nodes nodes;
    private NodeService nodeService;
    private CommentService commentService;
    private ContentService contentService;
    private TypeConstraint typeConstraint;

	public void setTypeConstraint(TypeConstraint typeConstraint)
	{
		this.typeConstraint = typeConstraint;
	}

	public void setNodes(Nodes nodes)
	{
		this.nodes = nodes;
	}
    
	public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
	
	public void setCommentService(CommentService commentService)
	{
		this.commentService = commentService;
	}

	public void setContentService(ContentService contentService)
	{
		this.contentService = contentService;
	}

	private Comment toComment(NodeRef nodeRef, NodeRef commentNodeRef)
    {
        Map<QName, Serializable> nodeProps = nodeService.getProperties(commentNodeRef);

        ContentReader reader = contentService.getReader(commentNodeRef, ContentModel.PROP_CONTENT);
        if(reader != null)
        {
	        String content = reader.getContentString();
	        nodeProps.put(Comment.PROP_COMMENT_CONTENT, content);
	        nodeProps.remove(ContentModel.PROP_CONTENT);
        }


        Map<String, Boolean> map = commentService.getCommentPermissions(nodeRef, commentNodeRef);
        boolean canEdit = map.get(CommentService.CAN_EDIT);
        boolean canDelete =  map.get(CommentService.CAN_DELETE);


        Comment comment = new Comment(commentNodeRef.getId(), nodeProps, canEdit, canDelete);
        return comment;
    }
    
    public Comment createComment(String nodeId, Comment comment)
    {
		NodeRef nodeRef = nodes.validateNode(nodeId);

		if(!typeConstraint.matches(nodeRef))
		{
			throw new UnsupportedResourceOperationException("Cannot comment on this node");
		}

		try
		{
	        NodeRef commentNode = commentService.createComment(nodeRef, comment.getTitle(), comment.getContent(), false);
	        return toComment(nodeRef, commentNode);
	    }
	    catch(IllegalArgumentException e)
	    {
	    	throw new InvalidArgumentException(e.getMessage());
	    }
    }

    public Comment updateComment(String nodeId, Comment comment)
    {
    	try
    	{
	    	NodeRef nodeRef = nodes.validateNode(nodeId);
	    	String commentNodeId = comment.getId();
			NodeRef commentNodeRef = nodes.validateNode(commentNodeId);
			
			String title = comment.getTitle();
			String content = comment.getContent();
			
			if(content == null)
			{
				throw new InvalidArgumentException();
			}
            
            commentService.updateComment(commentNodeRef, title, content);
	        return toComment(nodeRef, commentNodeRef);
		}
		catch(IllegalArgumentException e)
		{
			throw new ConstraintViolatedException(e.getMessage());
		}
    }

    public CollectionWithPagingInfo<Comment> getComments(String nodeId, Paging paging)
    {
		final NodeRef nodeRef = nodes.validateNode(nodeId);
        
        /* MNT-10536 : fix */
        final Set<QName> contentAndFolders = 
                new HashSet<QName>(Arrays.asList(ContentModel.TYPE_FOLDER, ContentModel.TYPE_CONTENT));
        if (!nodes.nodeMatches(nodeRef, contentAndFolders, null))
        {
            throw new InvalidArgumentException("NodeId of folder or content is expected");
        }

    	PagingRequest pagingRequest = Util.getPagingRequest(paging);
        final PagingResults<NodeRef> pagingResults = commentService.listComments(nodeRef, pagingRequest);
        
		final List<NodeRef> page = pagingResults.getPage();
		List<Comment> comments = new AbstractList<Comment>()
		{
			@Override
			public Comment get(int index)
			{
				return toComment(nodeRef, page.get(index));
			}

			@Override
			public int size()
			{
				return page.size();
			}
		};

        return CollectionWithPagingInfo.asPaged(paging, comments, pagingResults.hasMoreItems(), pagingResults.getTotalResultCount().getFirst());
    }

    @Override
    public void deleteComment(String nodeId, String commentNodeId)
    {
    	try
    	{
            NodeRef nodeRef = nodes.validateNode(nodeId);
	        NodeRef commentNodeRef = nodes.validateNode(commentNodeId);
            
            if (! nodeRef.equals(commentService.getDiscussableAncestor(commentNodeRef)))
            {
                throw new InvalidArgumentException("Unexpected "+nodeId+","+commentNodeId);
            }
            
            commentService.deleteComment(commentNodeRef);
		}
		catch(IllegalArgumentException e)
		{
			throw new ConstraintViolatedException(e.getMessage());
		}
    }
}
