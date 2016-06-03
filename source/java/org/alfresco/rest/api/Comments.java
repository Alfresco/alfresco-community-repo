package org.alfresco.rest.api;

import org.alfresco.rest.api.model.Comment;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;

/**
 * 
 * @author steveglover
 * @since publicapi1.0
 */
public interface Comments
{
    public Comment createComment(String nodeId, Comment comment);
    public Comment updateComment(String nodeId, Comment comment);
    public void deleteComment(String nodeId, String commentNodeId);
    public CollectionWithPagingInfo<Comment> getComments(String nodeId, Paging paging);
}
