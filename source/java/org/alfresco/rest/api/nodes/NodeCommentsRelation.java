/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.rest.api.nodes;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.rest.api.Comments;
import org.alfresco.rest.api.model.Comment;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.WebApiParameters;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 *
 * @author Gethin James
 * @author Steve Glover
 */
@RelationshipResource(name = "comments",  entityResource = NodesEntityResource.class, title = "Document or folder comments")
public class NodeCommentsRelation implements RelationshipResourceAction.Read<Comment>, RelationshipResourceAction.Create<Comment>, RelationshipResourceAction.Update<Comment>,  
    RelationshipResourceAction.Delete, InitializingBean
{
	private Comments comments;

	public void setComments(Comments comments)
	{
		this.comments = comments;
	}

	@Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("comments", this.comments);
    }

	/**
	 * Create a comment for the node given by nodeId.
	 * 
	 * THOR-1153: "F314: Add a comment to a folder or document"
     * 
	 */
    @Override
    @WebApiDescription(title="Creates comments for the node 'nodeId'.")
    public List<Comment> create(String nodeId, List<Comment> entity, Parameters parameters)
    {
        List<Comment> result = new ArrayList<Comment>(entity.size());
        for (Comment comment : entity)
        {
           result.add(comments.createComment(nodeId, comment));
        }
        return result;
    }

    /**
     * 
     * Returns a paged list of comments for the document/folder identified by nodeId, sorted chronologically with the newest first.
     *  
     * THOR-1152: “F313: For a folder or document, get the list of associated comments”
     * 
     * If nodeId does not exist, EntityNotFoundException (status 404).
     * If nodeId does not represent a document or folder, InvalidArgumentException (status 400).
     */
    @Override
    @WebApiDescription(title = "Returns a paged list of comments for the document/folder identified by nodeId, sorted chronologically with the newest first.")
    public CollectionWithPagingInfo<Comment> readAll(String nodeId, Parameters parameters)
    {
        return comments.getComments(nodeId, parameters.getPaging());
    }

	@Override
    @WebApiDescription(title = "Updates the comment with the given id.")
	public Comment update(String nodeId, Comment entity, Parameters parameters)
	{
		return comments.updateComment(nodeId, entity);
	}

    @Override
    @WebApiDescription(title = "Delete the comment with the given commentNodeId.")
    @WebApiParameters({
                @WebApiParam(name="nodeId", title="The unique id of the parent Node being addressed", description="A single node id"),
                @WebApiParam(name="commentNodeId", title="The unique id of the comment Node being addressed", description="A single node id")})
    public void delete(String nodeId, String commentNodeId, Parameters parameters)
    {
        comments.deleteComment(nodeId, commentNodeId);
    }

}
