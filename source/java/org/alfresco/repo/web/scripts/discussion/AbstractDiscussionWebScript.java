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
package org.alfresco.repo.web.scripts.discussion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.discussion.DiscussionServiceImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteServiceImpl;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.discussion.DiscussionService;
import org.alfresco.service.cmr.discussion.PostInfo;
import org.alfresco.service.cmr.discussion.TopicInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Nick Burch
 * @since 4.0
 */
public abstract class AbstractDiscussionWebScript extends DeclarativeWebScript
{
    public static final String DISCUSSIONS_SERVICE_ACTIVITY_APP_NAME = "discussions";
    
    /**
     * When no maximum or paging info is given, what should we use?
     */
    protected static final int MAX_QUERY_ENTRY_COUNT = 1000;
    
    private static Log logger = LogFactory.getLog(AbstractDiscussionWebScript.class);
    
    protected static final String KEY_POSTDATA = "postData";
    protected static final String KEY_IS_TOPIC_POST = "isTopicPost";
    protected static final String KEY_TOPIC = "topic";
    protected static final String KEY_POST = "post";
    protected static final String KEY_CAN_EDIT = "canEdit";
    protected static final String KEY_AUTHOR = "author";
    
    // Injected services
    protected NodeService nodeService;
    protected SiteService siteService;
    protected PersonService personService;
    protected ActivityService activityService;
    protected DiscussionService discussionService;
    protected PermissionService permissionService;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setActivityService(ActivityService activityService)
    {
        this.activityService = activityService;
    }
    
    public void setDiscussionService(DiscussionService discussionService)
    {
        this.discussionService = discussionService;
    }
    
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    
    protected String getOrNull(JSONObject json, String key)
    {
       if(json.containsKey(key))
       {
          return (String)json.get(key);
       }
       return null;
    }
    
    /**
     * Builds up a listing Paging request, based on the arguments
     *  specified in the URL
     */
    protected PagingRequest buildPagingRequest(WebScriptRequest req)
    {
       int pageSize = MAX_QUERY_ENTRY_COUNT;
       int startIndex = 0;
       
       String pageSizeS = req.getParameter("pageSize");
       if(pageSizeS != null)
       {
          try
          {
             pageSize = Integer.parseInt(pageSizeS);
          }
          catch(NumberFormatException e)
          {
             throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Paging size parameters invalid");
          }
       }
       
       String startIndexS = req.getParameter("startIndex");
       if(startIndexS != null)
       {
          try
          {
             startIndex = Integer.parseInt(startIndexS);
          }
          catch(NumberFormatException e)
          {
             throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Paging size parameters invalid");
          }
       }

       PagingRequest paging = new PagingRequest( startIndex, pageSize );
       paging.setRequestTotalCountMax( Math.max(10*pageSize,startIndex+2*pageSize) );
       return paging;
    }
    
    protected List<String> getTags(JSONObject json)
    {
       List<String> tags = null;
       if(json.containsKey("tags"))
       {
          // Is it "tags":"" or "tags":[...] ?
          if(json.get("tags") instanceof String)
          {
             // This is normally an empty string, skip
             String tagsS = (String)json.get("tags");
             if("".equals(tagsS))
             {
                // No tags were given
                return null;
             }
             else
             {
                // Log, and treat as empty
                logger.warn("Unexpected tag data: " + tagsS);
                return null;
             }
          }
          else
          {
             tags = new ArrayList<String>();
             JSONArray jsTags = (JSONArray)json.get("tags");
             for(int i=0; i<jsTags.size(); i++)
             {
                tags.add( (String)jsTags.get(i) );
             }
          }
       }
       return tags;
    }
    
    /**
     * Generates an activity entry for the discussion item
     * 
     * @param thing Either post or reply
     * @param event One of created, updated, deleted
     */
    protected void addActivityEntry(String thing, String event, TopicInfo topic, 
          PostInfo post, SiteInfo site, WebScriptRequest req, JSONObject json)
    {
       // What page is this for?
       String page = req.getParameter("page");
       if(page == null && json != null)
       {
          if(json.containsKey("page"))
          {
             page = (String)json.get("page");
          }
       }
       if(page == null)
       {
          // Default
          page = "discussions-topicview";
       }
       
       // Get the title
       String title = topic.getTitle();
       if(post != null)
       {
          String postTitle = post.getTitle();
          if(postTitle != null && postTitle.length() > 0)
          {
             title = postTitle;
          }
       }
       
       try
       {
          JSONObject params = new JSONObject();
          params.put("topicId", topic.getSystemName());
          
          JSONObject activity = new JSONObject();
          activity.put("title", title);
          activity.put("page", page + "?topicId=" + topic.getSystemName());
          activity.put("params", params);
          
          activityService.postActivity(
                "org.alfresco.discussions." + thing + "-" + event,
                site.getShortName(),
                DISCUSSIONS_SERVICE_ACTIVITY_APP_NAME,
                activity.toString()
          );
       }
       catch(Exception e)
       {
          // Warn, but carry on
          logger.warn("Error adding discussions " + thing + " " + event + " to activities feed", e);
       }
    }
    
    /**
     * Is the current user allowed to edit this post?
     * In order to be deemed allowed, you first need write
     *  permissions on the underlying node of the post.
     * You then also need to either be the cm:creator of
     *  the post node, or a site manager
     */
    protected boolean canUserEditPost(PostInfo post, SiteInfo site)
    {
       // Are they OK on the node?
       AccessStatus canEdit = permissionService.hasPermission(post.getNodeRef(), PermissionService.WRITE); 
       if(canEdit == AccessStatus.ALLOWED)
       {
          // Only the creator and site managers may edit
          String user = AuthenticationUtil.getFullyAuthenticatedUser();
          if(post.getCreator().equals(user))
          {
             // It's their post
             return true;
          }
          if(site != null)
          {
             String role = siteService.getMembersRole(site.getShortName(), user);
             if(SiteServiceImpl.SITE_MANAGER.equals(role))
             {
                // Managers may edit
                return true;
             }
          }
       }
       
       // If in doubt, you may not edit
       return false;
    }
    
    protected Object buildPerson(String username)
    {
       if(username == null || username.length() == 0)
       {
          // Empty string needed
          return "";
       }
       
       // Will turn into a Script Node needed of the person
       NodeRef person = personService.getPerson(username);
       return person;
    }
    
    /*
     * Was topicpost.lib.js getReplyPostData
     * 
     * TODO Switch the FTL to prefer the Info object rather than the ScriptNode
     */
    protected Map<String, Object> renderPost(PostInfo post, SiteInfo site)
    {
       Map<String, Object> item = new HashMap<String, Object>();
       item.put(KEY_IS_TOPIC_POST, false);
       item.put(KEY_POST, post.getNodeRef());
       item.put(KEY_CAN_EDIT, canUserEditPost(post, site));
       item.put(KEY_AUTHOR, buildPerson(post.getCreator()));
       return item;
    }
    
    /*
     * Was topicpost.lib.js getTopicPostData / getTopicPostDataFromTopicAndPosts
     * 
     * TODO Switch the FTL to prefer the Info object rather than the ScriptNode
     */
    protected Map<String, Object> renderTopic(TopicInfo topic, SiteInfo site)
    {
       // Fetch the primary post
       PostInfo primaryPost = discussionService.getPrimaryPost(topic);
       if(primaryPost == null)
       {
          throw new WebScriptException(Status.STATUS_PRECONDITION_FAILED,
                 "First (primary) post was missing from the topic, can't fetch");
       }
       
       // Fetch the most recent reply
       PostInfo mostRecentPost = discussionService.getMostRecentPost(topic);
       
       // Find out how many replies there are
       int numReplies;
       if(mostRecentPost.getNodeRef().equals( primaryPost.getNodeRef() ))
       {
          // Only the one post in the topic
          mostRecentPost = null;
          numReplies = 0;
       }
       else
       {
          // Use this trick to get the number of posts in the topic, 
          //  but without needing to get lots of data and objects
          PagingRequest paging = new PagingRequest(1);
          paging.setRequestTotalCountMax(MAX_QUERY_ENTRY_COUNT);
          PagingResults<PostInfo> posts = discussionService.listPosts(topic, paging);
          
          // The primary post is in the list, so exclude from the reply count 
          numReplies = posts.getTotalResultCount().getFirst() - 1;
       }
       
       // Build the details
       Map<String, Object> item = new HashMap<String, Object>();
       item.put(KEY_IS_TOPIC_POST, true);
       item.put(KEY_TOPIC, topic.getNodeRef());
       item.put(KEY_POST, primaryPost.getNodeRef());
       item.put(KEY_CAN_EDIT, canUserEditPost(primaryPost, site));
       item.put(KEY_AUTHOR, buildPerson(topic.getCreator()));
       
       // The reply count is one less than all posts (first is the primary one)
       item.put("totalReplyCount", numReplies);
       
       // We want details on the most recent post
       if(mostRecentPost != null)
       {
          item.put("lastReply", mostRecentPost.getNodeRef());
          item.put("lastReplyBy", buildPerson(mostRecentPost.getCreator()));
       }
       
       // Include the tags
       item.put("tags", topic.getTags());
       
       // All done
       return item;
    }
    
    /*
     * Renders out the list of topics
     * TODO Fetch the post data in one go, rather than one at a time
     */
    protected Map<String, Object> renderTopics(PagingResults<TopicInfo> topics,
          PagingRequest paging, SiteInfo site)
    {
       return renderTopics(topics.getPage(), topics.getTotalResultCount(), paging, site);
    }
    /*
     * Renders out the list of topics
     * TODO Fetch the post data in one go, rather than one at a time
     */
    protected Map<String, Object> renderTopics(List<TopicInfo> topics, 
          Pair<Integer,Integer> size, PagingRequest paging, SiteInfo site)
    {
       Map<String, Object> model = new HashMap<String, Object>();
       
       // Paging info
       model.put("total", size.getFirst());
       model.put("pageSize", paging.getMaxItems());
       model.put("startIndex", paging.getSkipCount());
       model.put("itemCount", topics.size());
       
       // Data
       List<Map<String,Object>> items = new ArrayList<Map<String,Object>>();
       for(TopicInfo topic : topics)
       {
          items.add(renderTopic(topic, site));
       }
       model.put("items", items);
       
       // All done
       return model;
    }
    
    protected Map<String, Object> buildCommonModel(SiteInfo site, TopicInfo topic, 
          PostInfo post, WebScriptRequest req)
    {
       // Build the common model parts
       Map<String, Object> model = new HashMap<String, Object>();
       model.put(KEY_TOPIC, topic);
       model.put(KEY_POST, post);
       
       // Capture the site details only if site based
       if(site != null)
       {
          model.put("siteId", site.getShortName());
          model.put("site", site);
       }
       
       // The limit on the length of the content to be returned
       int contentLength = -1;
       String contentLengthS = req.getParameter("contentLength");
       if(contentLengthS != null)
       {
          try
          {
             contentLength = Integer.parseInt(contentLengthS);
          }
          catch(NumberFormatException e)
          {
             logger.info("Skipping invalid length " + contentLengthS); 
          }
       }
       model.put("contentLength", contentLength);
       
       // All done
       return model;
    }
    
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req,
          Status status, Cache cache) 
    {
       Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
       if(templateVars == null)
       {
          String error = "No parameters supplied";
          throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
       }
       
       
       // Parse the JSON, if supplied
       JSONObject json = null;
       String contentType = req.getContentType();
       if(contentType != null && contentType.indexOf(';') != -1)
       {
          contentType = contentType.substring(0, contentType.indexOf(';'));
       }
       if(MimetypeMap.MIMETYPE_JSON.equals(contentType))
       {
          JSONParser parser = new JSONParser();
          try
          {
             json = (JSONObject)parser.parse(req.getContent().getContent());
          }
          catch(IOException io)
          {
             throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + io.getMessage());
          }
          catch(ParseException pe)
          {
             throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + pe.getMessage());
          }
       }
       
       
       // Did they request it by node reference or site?
       NodeRef nodeRef = null;
       SiteInfo site = null;
       TopicInfo topic = null;
       PostInfo post = null;
       
       if(templateVars.containsKey("site"))
       {
          // Site, and optionally topic
          String siteName = templateVars.get("site");
          site = siteService.getSite(siteName);
          if(site == null)
          {
             String error = "Could not find site: " + siteName;
             throw new WebScriptException(Status.STATUS_NOT_FOUND, error);
          }
          
          // Did they give a topic name too?
          if(templateVars.containsKey("path"))
          {
             String name = templateVars.get("path");
             topic = discussionService.getTopic(site.getShortName(), name);
             
             if(topic == null)
             {
                String error = "Could not find topic '" + name + "' for site '" + 
                               site.getShortName() + "'";
                throw new WebScriptException(Status.STATUS_NOT_FOUND, error);
             }
             nodeRef = topic.getNodeRef();
          }
          else
          {
             // The NodeRef is the container (if it exists)
             if(siteService.hasContainer(siteName, DiscussionServiceImpl.DISCUSSION_COMPONENT))
             {
                nodeRef = siteService.getContainer(siteName, DiscussionServiceImpl.DISCUSSION_COMPONENT);
             }
          }
       }
       else if(templateVars.containsKey("store_type") && 
               templateVars.containsKey("store_id") &&
               templateVars.containsKey("id"))
       {
          // NodeRef, normally Topic or Discussion
          StoreRef store = new StoreRef(
                templateVars.get("store_type"),
                templateVars.get("store_id")
          );
          nodeRef = new NodeRef(store, templateVars.get("id"));
          if(! nodeService.exists(nodeRef))
          {
             String error = "Could not find node: " + nodeRef;
             throw new WebScriptException(Status.STATUS_NOT_FOUND, error);
          }
          
          // Try to build the appropriate object for it
          Pair<TopicInfo,PostInfo> objects = discussionService.getForNodeRef(nodeRef);
          if(objects != null)
          {
             topic = objects.getFirst();
             post = objects.getSecond();
          }
       }
       else
       {
          String error = "Unsupported template parameters found";
          throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
       }
       
       // Have the real work done
       return executeImpl(site, nodeRef, topic, post, req, json, status, cache); 
    }
    
    protected abstract Map<String, Object> executeImpl(SiteInfo site,
          NodeRef nodeRef, TopicInfo topic, PostInfo post,
          WebScriptRequest req, JSONObject json, Status status, Cache cache);
    
}
