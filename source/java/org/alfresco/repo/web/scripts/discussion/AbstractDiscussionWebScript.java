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
import java.util.HashMap;
import java.util.Map;

import org.alfresco.query.PagingRequest;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.discussion.DiscussionService;
import org.alfresco.service.cmr.discussion.PostInfo;
import org.alfresco.service.cmr.discussion.TopicInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
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
    
    // Injected services
    protected NodeService nodeService;
    protected SiteService siteService;
    protected PersonService personService;
    protected ActivityService activityService;
    protected DiscussionService discussionService;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    public void setDiscussionService(DiscussionService discussionService)
    {
        this.discussionService = discussionService;
    }
    
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setActivityService(ActivityService activityService)
    {
        this.activityService = activityService;
    }
    
    
    protected String getOrNull(JSONObject json, String key) throws JSONException
    {
       if(json.has(key))
       {
          return json.getString(key);
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
       paging.setRequestTotalCountMax( Math.max(10,startIndex+1) * pageSize );
       return paging;
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
          if(json.has("page"))
          {
             try
             {
                page = json.getString("page");
             }
             catch(JSONException e) {}
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
    
    // TODO Is this needed?
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
    
    // TODO Match JS
    protected Map<String, Object> renderTopic(TopicInfo topic)
    {
       Map<String, Object> res = new HashMap<String, Object>();
       res.put("topic", topic);
       res.put("node", topic.getNodeRef());
       res.put("name", topic.getSystemName());
       res.put("title", topic.getTitle());
       res.put("tags", topic.getTags());
       
       // Both forms used for dates
       res.put("createdOn", topic.getCreatedAt());
       res.put("modifiedOn", topic.getModifiedAt());
       res.put("created", topic.getCreatedAt());
       res.put("modified", topic.getModifiedAt());
       
       // FTL needs a script node of the people
       res.put("createdBy", buildPerson(topic.getCreator()));
       res.put("modifiedBY", buildPerson(topic.getModifier()));
       
       // We want blank instead of null
       for(String key : res.keySet())
       {
          if(res.get(key) == null)
          {
             res.put(key, "");
          }
       }
       
       return res;
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
          try
          {
             json = new JSONObject(new JSONTokener(req.getContent().getContent()));
          }
          catch(IOException io)
          {
             throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + io.getMessage());
          }
          catch(JSONException je)
          {
             throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + je.getMessage());
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
