/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.comments;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.FileFilterMode.Client;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the comments.post web script.
 * 
 * @author Sergey Scherbovich (based on existing JavaScript webscript controller)
 * @since 4.1.7.1
 */

public class CommentsPost extends DeclarativeWebScript {
    private final static String COMMENTS_TOPIC_NAME = "Comments";
    
    private static Log logger = LogFactory.getLog(CommentsPost.class);
    
    private final static String JSON_KEY_SITE = "site";
    private final static String JSON_KEY_ITEM_TITLE = "itemTitle";
    private final static String JSON_KEY_PAGE = "page";
    private final static String JSON_KEY_TITLE = "title";
    private final static String JSON_KEY_PAGE_PARAMS = "pageParams";
    private final static String JSON_KEY_NODEREF = "nodeRef";
    private final static String JSON_KEY_CONTENT = "content";
    
    private ServiceRegistry serviceRegistry;
    private NodeService nodeService;
    private ContentService contentService;
    private PersonService personService;
    private PermissionService permissionService;
    private ActivityService activityService;
    
    private BehaviourFilter behaviourFilter;
    
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) 
    {
        // get requested node
        NodeRef nodeRef = parseRequestForNodeRef(req);
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
            postActivity(json, nodeRef);

            return model;
        }
        finally
        {
            this.behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
        }
    }
    
    private void postActivity(JSONObject json, NodeRef nodeRef)
    {
        // post an activity item, but only if we've got a site
        if(json.containsKey(JSON_KEY_SITE) && json.containsKey(JSON_KEY_ITEM_TITLE) && json.containsKey(JSON_KEY_PAGE))
        {
            String siteId = getOrNull(json, JSON_KEY_SITE);
            if (siteId != null && siteId != "")
            {
                try
                {
                    org.json.JSONObject params = new org.json.JSONObject(getOrNull(json, JSON_KEY_PAGE_PARAMS));
                    String strParams = "";
                    
                    Iterator<?> itr = params.keys();
                    while (itr.hasNext())
                    {
                        String strParam = itr.next().toString();
                        strParams += strParam + "=" + params.getString(strParam) + "&";
                    }
                    String page = getOrNull(json, JSON_KEY_PAGE) + "?" + (strParams != "" ? strParams.substring(0, strParams.length()-1) : "");
                    String title = getOrNull(json, JSON_KEY_ITEM_TITLE);
                    String strNodeRef = nodeRef.toString();
                    
                    JSONWriter jsonWriter = new JSONStringer().object();
                    
                    jsonWriter.key(JSON_KEY_TITLE).value(title);
                    jsonWriter.key(JSON_KEY_PAGE).value(page);
                    jsonWriter.key(JSON_KEY_NODEREF).value(strNodeRef);
                    
                    String jsonActivityData = jsonWriter.endObject().toString();
                    
                    activityService.postActivity("org.alfresco.comments.comment-created", siteId, "comments", jsonActivityData, Client.webclient);
                }
                catch(Exception e)
                {
                   logger.warn("Error adding comment to activities feed", e);
                }
            }
        }
    
    }
    
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
    
    private Map<String, Object> generateModel(NodeRef nodeRef, NodeRef commentNodeRef)
    {
        Map<String, Object> model = new HashMap<String, Object>(2, 1.0f);
        
        model.put("node", nodeRef);
        model.put("item", generateItemValue(commentNodeRef));
        
        return model;
    }
    
    private String getOrNull(JSONObject json, String key)
    {
       if (json.containsKey(key))
       {
          return (String)json.get(key);
       }
       return null;
    }
    
    private JSONObject parseJSON(WebScriptRequest req)
    {
        JSONObject json = null;
        String contentType = req.getContentType();
        if (contentType != null && contentType.indexOf(';') != -1)
        {
           contentType = contentType.substring(0, contentType.indexOf(';'));
        }
        if (MimetypeMap.MIMETYPE_JSON.equals(contentType))
        {
           JSONParser parser = new JSONParser();
           try
           {
              json = (JSONObject)parser.parse(req.getContent().getContent());
           }
           catch (IOException io)
           {
              throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + io.getMessage());
           }
           catch(ParseException pe)
           {
              throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + pe.getMessage());
           }
        }        
        return json;
    }
    
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
    
    private NodeRef parseRequestForNodeRef(WebScriptRequest req)
    {
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String storeType = templateVars.get("store_type");
        String storeId = templateVars.get("store_id");
        String nodeId = templateVars.get("id");
        
        // create the NodeRef and ensure it is valid
        StoreRef storeRef = new StoreRef(storeType, storeId);
        return new NodeRef(storeRef, nodeId);
    }
    
    private String getUniqueChildName(String prefix)
    {
        return prefix + "-" + System.currentTimeMillis();
    }
    
    private NodeRef createCommentsFolder(final NodeRef nodeRef)
    {
        NodeRef commentsFolder = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                NodeRef commentsFolder = null;
                
                // ALF-5240: turn off auditing round the discussion node creation to prevent
                // the source document from being modified by the first user leaving a comment
                behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
                
                try
                {
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
                    behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
                }
                
                return commentsFolder;
            }
    
        }, AuthenticationUtil.getSystemUserName()); 
        
        return commentsFolder;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.nodeService = serviceRegistry.getNodeService();
        this.contentService = serviceRegistry.getContentService();
        this.personService = serviceRegistry.getPersonService();
        this.permissionService = serviceRegistry.getPermissionService();
    }
    
    public void setBehaviourFilter(BehaviourFilter behaviourFilter) {
        this.behaviourFilter = behaviourFilter;
    }

    public void setActivityService(ActivityService activityService) {
        this.activityService = activityService;
    }
}
