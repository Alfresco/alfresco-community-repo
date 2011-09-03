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
package org.alfresco.repo.web.scripts.wiki;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.query.PagingRequest;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.wiki.WikiPageInfo;
import org.alfresco.service.cmr.wiki.WikiService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.json.JSONWriter;

/**
 * @author Nick Burch
 * @since 4.0
 */
public abstract class AbstractWikiWebScript extends DeclarativeWebScript
{
    public static final String WIKI_SERVICE_ACTIVITY_APP_NAME = "wiki";
    
    /**
     * When no maximum or paging info is given, what should we use?
     */
    protected static final int MAX_QUERY_ENTRY_COUNT = 1000;
    
    private static Log logger = LogFactory.getLog(AbstractWikiWebScript.class);
    
    // Injected services
    protected NodeService nodeService;
    protected SiteService siteService;
    protected WikiService wikiService;
    protected PersonService personService;
    protected ActivityService activityService;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    public void setWikiService(WikiService wikiService)
    {
        this.wikiService = wikiService;
    }
    
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setActivityService(ActivityService activityService)
    {
        this.activityService = activityService;
    }
    
    
    protected String getOrNull(JSONObject json, String key)
    {
       if (json.containsKey(key))
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
       int pageNumber = 1;
       
       String pageSizeS = req.getParameter("pageSize");
       if (pageSizeS != null)
       {
          try
          {
             pageSize = Integer.parseInt(pageSizeS);
          }
          catch (NumberFormatException e)
          {
             throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Paging size parameters invalid");
          }
       }
       
       String pageNumberS = req.getParameter("page");
       if (pageNumberS != null)
       {
          try
          {
             pageNumber = Integer.parseInt(pageNumberS);
          }
          catch (NumberFormatException e)
          {
             throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Paging size parameters invalid");
          }
       }

       PagingRequest paging = new PagingRequest( (pageNumber-1) * pageSize, pageSize );
       paging.setRequestTotalCountMax( Math.max(10, pageNumber) * pageSize );
       return paging;
    }
    
    /**
     * Generates an activity entry for the link
     */
    protected void addActivityEntry(String event, WikiPageInfo wikiPage, SiteInfo site, 
          WebScriptRequest req, JSONObject json)
    {
       // What page is this for?
       String page = req.getParameter("page");
       if (page == null && json != null)
       {
          if (json.containsKey("page"))
          {
             page = (String)json.get("page");
          }
       }
       if (page == null)
       {
          // Default
          page = "wiki";
       }
       
       try
       {
          StringWriter activityJson = new StringWriter();
          JSONWriter activity = new JSONWriter(activityJson);
          activity.startObject();
          activity.writeValue("title", wikiPage.getTitle());
          activity.writeValue("page", page + "?title=" + wikiPage.getSystemName());
          activity.endObject();
          
          activityService.postActivity(
                "org.alfresco.wiki.page-" + event,
                site.getShortName(),
                WIKI_SERVICE_ACTIVITY_APP_NAME,
                activityJson.toString());
       }
       catch (Exception e)
       {
          // Warn, but carry on
          logger.warn("Error adding wiki page " + event + " to activities feed", e);
       }
    }
    
    protected Object buildPerson(String username)
    {
       if (username == null || username.length() == 0)
       {
          // Empty string needed
          return "";
       }
       
       // Will turn into a Script Node needed of the person
       NodeRef person = personService.getPerson(username);
       return person;
    }
    
    protected Map<String, Object> renderWikiPage(WikiPageInfo page)
    {
       Map<String, Object> res = new HashMap<String, Object>();
       res.put("page", page);
       res.put("node", page.getNodeRef());
       res.put("name", page.getSystemName());
       res.put("title", page.getTitle());
       res.put("contents", page.getContents());
       res.put("tags", page.getTags());
       
       // Both forms used for dates
       res.put("createdOn", page.getCreatedAt());
       res.put("modifiedOn", page.getModifiedAt());
       res.put("created", page.getCreatedAt());
       res.put("modified", page.getModifiedAt());
       
       // FTL needs a script node of the people
       res.put("createdBy", buildPerson(page.getCreator()));
       res.put("modifiedBY", buildPerson(page.getModifier()));
       
       // We want blank instead of null
       for (String key : res.keySet())
       {
          if (res.get(key) == null)
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
       if (templateVars == null)
       {
          String error = "No parameters supplied";
          throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
       }
       
       
       // Parse the JSON, if supplied
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
          catch (ParseException pe)
          {
             throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + pe.getMessage());
          }
       }
       
       
       // Get the site short name. Try quite hard to do so...
       String siteName = templateVars.get("siteId");
       if (siteName == null)
       {
          siteName = req.getParameter("site");
       }
       if (siteName == null && json != null)
       {
          if (json.containsKey("siteid"))
          {
             siteName = (String)json.get("siteid");
          }
          else if (json.containsKey("siteId"))
          {
             siteName = (String)json.get("siteId");
          }
          else if(json.containsKey("site"))
          {
             siteName = (String)json.get("site");
          }
       }
       if (siteName == null)
       {
          String error = "No site given";
          throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
       }
       
       // Grab the requested site
       SiteInfo site = siteService.getSite(siteName);
       if (site == null)
       {
          String error = "Could not find site: " + siteName;
          throw new WebScriptException(Status.STATUS_NOT_FOUND, error);
       }
       
       // Page name is optional
       // Note - it's really the Name, even if it's called "Title"
       String pageName = templateVars.get("pageTitle");
       
       // Have the real work done
       return executeImpl(site, pageName, req, json, status, cache); 
    }
    
    protected abstract Map<String, Object> executeImpl(SiteInfo site, 
          String pageName, WebScriptRequest req, JSONObject json, 
          Status status, Cache cache);
}
