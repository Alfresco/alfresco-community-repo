package org.alfresco.repo.web.scripts.blogs;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * This class is a port of a previous JavaScript library used by the blog webscript containers.
 * 
 * @author Neil Mc Erlean (based on existing JavaScript code)
 * @since 4.0
 */
class CommentsLibJs
{
    // TODO It will likely be refactored into the Blog REST API class framework.
    
    private static final String COMMENTS_TOPIC_NAME = "Comments";

    public static int getCommentsCount(NodeRef node, ServiceRegistry services)
    {
       return getComments(node, services).size();
    }
    
    /**
     * Returns all comment nodes for a given node.
     * @return an array of comments.
     */
    public static List<ChildAssociationRef> getComments(NodeRef node, ServiceRegistry services)
    {
        List<ChildAssociationRef> result = new ArrayList<ChildAssociationRef>();
        
        NodeRef commentsFolder = getCommentsFolder(node, services);
        if (commentsFolder != null)
        {
            List<ChildAssociationRef> children = services.getNodeService().getChildAssocs(commentsFolder, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
            if (!children.isEmpty())
            {
                result = children;
            }
        }
        
        return result;
    }
    
    /**
     * Returns the folder that contains all the comments.
     * 
     * We currently use the fm:discussable aspect where we
     * add a "Comments" topic to it.
     */
    public static NodeRef getCommentsFolder(NodeRef node, ServiceRegistry services)
    {
        //FIXME These methods are from the original JavaScript. Should use the (soon to arrive) CommentService.
        NodeRef result = null;
        if (services.getNodeService().hasAspect(node, ForumModel.ASPECT_DISCUSSABLE))
        {
            List<ChildAssociationRef> forumFolders = services.getNodeService().getChildAssocs(node, ForumModel.ASSOC_DISCUSSION, RegexQNamePattern.MATCH_ALL);
            // The JavaScript was retrieving the first child under this child-assoc so we'll do the same.
            NodeRef forumFolder = forumFolders.get(0).getChildRef();
            
            List<ChildAssociationRef> topicFolder = services.getNodeService().getChildAssocs(forumFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, COMMENTS_TOPIC_NAME));
            result = topicFolder.isEmpty() ? null : topicFolder.get(0).getChildRef();
        }
        return result;
    }
}
