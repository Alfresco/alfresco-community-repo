package org.alfresco.repo.web.scripts.comments;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the comments.post web script.
 * 
 * @author Sergey Scherbovich (based on existing JavaScript webscript controller)
 * @since 4.1.7.1
 */

public class CommentsPost extends AbstractCommentsWebScript
{
    /**
     *  Overrides AbstractCommentsWebScript to add comment
     */
    @Override
    protected Map<String, Object> executeImpl(NodeRef nodeRef, WebScriptRequest req, Status status, Cache cache)
    {
        // get json object from request
        JSONObject json = parseJSON(req);

        /* MNT-10231, MNT-9771 fix */
        this.behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);

        try
        {
            // add a comment
            NodeRef commentNodeRef = addComment(nodeRef, json);

            // generate response model for a comment node
            Map<String, Object> model = generateModel(nodeRef, commentNodeRef);

            // post an activity item
            postActivity(json, req, nodeRef, COMMENT_CREATED_ACTIVITY);

            return model;
        }
        finally
        {
            this.behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
        }
    }

    /**
     * add the comment from json to given nodeRef
     * 
     * @param nodeRef
     * @param json
     * @return
     */
    private NodeRef addComment(NodeRef nodeRef, JSONObject json)
    {
        // fetch the parent to add the node to
        NodeRef commentsFolder = getOrCreateCommentsFolder(nodeRef);
        
        // get a unique name
        String name = getUniqueChildName("comment");
        
        // create the comment
        NodeRef commentNodeRef = nodeService.createNode(commentsFolder, 
                ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)), 
                ForumModel.TYPE_POST).getChildRef();
            
        // fetch the title required to create a comment
        String title = getOrNull(json, JSON_KEY_TITLE);
        HashMap<QName, Serializable> props = new HashMap<QName, Serializable>(1, 1.0f);
        props.put(ContentModel.PROP_TITLE, title != null ? title : "");
        nodeService.addProperties(commentNodeRef, props);
        
        ContentWriter writer = contentService.getWriter(commentNodeRef, ContentModel.PROP_CONTENT, true);
        // fetch the content of a comment
        String contentString = getOrNull(json, JSON_KEY_CONTENT);
        
        writer.setMimetype(MimetypeMap.MIMETYPE_HTML);
        writer.putContent(contentString);
        
        return commentNodeRef;
    }
    
    /**
     * generates an comment item value
     * 
     * @param commentNodeRef
     * @return
     */
    private Map<String, Object> generateItemValue(NodeRef commentNodeRef)
    {
        Map<String, Object> result = new HashMap<String, Object>(4, 1.0f);
        
        String creator = (String)this.nodeService.getProperty(commentNodeRef, ContentModel.PROP_CREATOR);
        
        Serializable created = this.nodeService.getProperty(commentNodeRef, ContentModel.PROP_CREATED);
        Serializable modified = this.nodeService.getProperty(commentNodeRef, ContentModel.PROP_MODIFIED);
        
        boolean isUpdated = false;
        if (created instanceof Date && modified instanceof Date)
        {
           isUpdated = ((Date)modified).getTime() - ((Date)created).getTime() > 5000;
        }
        
        Serializable owner = this.nodeService.getProperty(commentNodeRef, ContentModel.PROP_OWNER);
        String currentUser = this.serviceRegistry.getAuthenticationService().getCurrentUserName();
        
        boolean isSiteManager = this.permissionService.hasPermission(commentNodeRef, SiteModel.SITE_MANAGER) == (AccessStatus.ALLOWED);
        boolean isCoordinator = this.permissionService.hasPermission(commentNodeRef, PermissionService.COORDINATOR) == (AccessStatus.ALLOWED);
        boolean canEditComment = isSiteManager || isCoordinator || currentUser.equals(creator) || currentUser.equals(owner);
        
        result.put("node", commentNodeRef);
        result.put("author", this.personService.getPerson(creator));
        result.put("isUpdated", isUpdated);
        result.put("canEditComment", canEditComment);
        
        return result;
    }
    
    /**
     * generates the response model for adding a comment
     * 
     * @param nodeRef
     * @param commentNodeRef
     * @return
     */
    private Map<String, Object> generateModel(NodeRef nodeRef, NodeRef commentNodeRef)
    {
        Map<String, Object> model = new HashMap<String, Object>(2, 1.0f);

        model.put(PARAM_NODE, nodeRef);
        model.put(PARAM_ITEM, generateItemValue(commentNodeRef));

        return model;
    }

    /**
     * 
     * @param nodeRef
     * @return
     */
    private NodeRef getOrCreateCommentsFolder(NodeRef nodeRef)
    {
        NodeRef commentsFolder = getCommentsFolder(nodeRef);
        // create a comment folder if it doesn't exist
        if (commentsFolder == null)
        {
            commentsFolder = createCommentsFolder(nodeRef);
        }
        return commentsFolder;
    }
    
    /**
     * returns the nodeRef of the existing one
     * 
     * @param nodeRef
     * @return
     */
    private NodeRef getCommentsFolder(NodeRef nodeRef)
    {
        if (nodeService.hasAspect(nodeRef, ForumModel.ASPECT_DISCUSSABLE))
        {
            List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, ForumModel.ASSOC_DISCUSSION, RegexQNamePattern.MATCH_ALL);
            ChildAssociationRef firstAssoc = assocs.get(0);
            
            return nodeService.getChildByName(firstAssoc.getChildRef(), ContentModel.ASSOC_CONTAINS, COMMENTS_TOPIC_NAME);
        }
        else
        {
            return null;
        }
    }

    private String getUniqueChildName(String prefix)
    {
        return prefix + "-" + System.currentTimeMillis();
    }
    
    /**
     * creates the comments folder if it does not exists
     * 
     * @param nodeRef
     * @return
     */
    private NodeRef createCommentsFolder(final NodeRef nodeRef)
    {
        NodeRef commentsFolder = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                NodeRef commentsFolder = null;
                AuthenticationUtil.pushAuthentication();
                
                // ALF-5240: turn off auditing round the discussion node creation to prevent
                // the source document from being modified by the first user leaving a comment
                behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
                
                try
                {  
                    // MNT-12082: set System user for creating forumFolder and commentsFolder nodes
                    AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
                    
                    nodeService.addAspect(nodeRef, QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "discussable"), null);
                    nodeService.addAspect(nodeRef, QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "commentsRollup"), null);
                    List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "discussion"), RegexQNamePattern.MATCH_ALL);
                    if (assocs.size() != 0)
                    {
                        NodeRef forumFolder = assocs.get(0).getChildRef();
                        
                        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1, 1.0f);
                        props.put(ContentModel.PROP_NAME, COMMENTS_TOPIC_NAME);
                        commentsFolder = nodeService.createNode(
                                forumFolder,
                                ContentModel.ASSOC_CONTAINS, 
                                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, COMMENTS_TOPIC_NAME), 
                                QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "topic"),
                                props).getChildRef();
                    }
                }
                finally
                {
                    AuthenticationUtil.popAuthentication();
                    behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
                }
                
                return commentsFolder;
            }
    
        }, AuthenticationUtil.getSystemUserName()); 
        
        return commentsFolder;
    }

}
