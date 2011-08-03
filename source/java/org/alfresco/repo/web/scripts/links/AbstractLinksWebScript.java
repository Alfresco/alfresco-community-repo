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
package org.alfresco.repo.web.scripts.links;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.query.PagingRequest;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.links.LinkInfo;
import org.alfresco.service.cmr.links.LinksService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
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
public abstract class AbstractLinksWebScript extends DeclarativeWebScript
{
    public static final String LINKS_SERVICE_ACTIVITY_APP_NAME = "links";
    
    private static Log logger = LogFactory.getLog(AbstractLinksWebScript.class);
    
    // Injected services
    protected NodeService nodeService;
    protected SiteService siteService;
    protected LinksService linksService;
    protected ActivityService activityService;
    protected ServiceRegistry serviceRegistry;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    public void setLinksService(LinksService linksService)
    {
        this.linksService = linksService;
    }
    
    public void setActivityService(ActivityService activityService)
    {
        this.activityService = activityService;
    }
    
    public void setServiceRegistry(ServiceRegistry serviceRegistry) 
    {
        this.serviceRegistry = serviceRegistry;
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
       String pageNumberS = req.getParameter("page");
       String pageSizeS = req.getParameter("pageSize");
       if(pageNumberS == null || pageSizeS == null)
       {
          throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Paging size parameters missing");
       }
       
       int pageNumber;
       int pageSize;
       try
       {
          pageNumber = Integer.parseInt(pageNumberS);
          pageSize = Integer.parseInt(pageSizeS);
       }
       catch(NumberFormatException e)
       {
          throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Paging size parameters invalid");
       }

       PagingRequest paging = new PagingRequest( (pageNumber-1) * pageSize, pageSize );
       paging.setRequestTotalCountMax( Math.max(10, pageNumber) * pageSize );
       return paging;
    }
    
    /**
     * Generates an activity entry for the link
     */
    protected void addActivityEntry(String event, LinkInfo link, SiteInfo site, 
          WebScriptRequest req, JSONObject json)
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
          page = "links";
       }
       
       try
       {
          JSONObject activity = new JSONObject();
          activity.put("title", link.getTitle());
          activity.put("page", page + "?linkId=" + link.getSystemName());
          
          activityService.postActivity(
                "org.alfresco.links.link-" + event,
                site.getShortName(),
                LINKS_SERVICE_ACTIVITY_APP_NAME,
                activity.toString()
          );
       }
       catch(Exception e)
       {
          // Warn, but carry on
          logger.warn("Error adding link " + event + " to activities feed", e);
       }
    }
    
    protected Map<String, Object> renderLink(LinkInfo link)
    {
       Map<String, Object> res = new HashMap<String, Object>();
       res.put("node", link.getNodeRef());
       res.put("name", link.getSystemName());
       res.put("title", link.getTitle());
       res.put("description", link.getDescription());
       res.put("url", link.getURL());
       res.put("createdOn", link.getCreatedAt());
       res.put("modifiedOn", link.getModifiedAt());
       res.put("tags", link.getTags());
       res.put("internal", link.isInternal());
       
       // FTL needs a script node of the person
       String creator = link.getCreator();
       Object creatorO;
       if(creator == null)
       {
          creatorO = "";
       }
       else
       {
          NodeRef person = serviceRegistry.getPersonService().getPerson(creator);
          creatorO = new ScriptNode(person, serviceRegistry);
       }
       res.put("creator", creatorO);
       
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
       
       
       // Get the site short name. Try quite hard to do so...
       String siteName = templateVars.get("site");
       if(siteName == null)
       {
          siteName = req.getParameter("site");
       }
       if(siteName == null && json != null)
       {
          try
          {
             if(json.has("siteid"))
             {
                siteName = json.getString("siteid");
             }
             else if(json.has("site"))
             {
                siteName = json.getString("site");
             }
          }
          catch(JSONException e) {}
       }
       if(siteName == null)
       {
          String error = "No site given";
          throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
       }
       
       // Grab the requested site
       SiteInfo site = siteService.getSite(siteName);
       if(site == null)
       {
          String error = "Could not find site: " + siteName;
          throw new WebScriptException(Status.STATUS_NOT_FOUND, error);
       }
       
       // Link name is optional
       String linkName = templateVars.get("path");
       
       // Have the real work done
       return executeImpl(site, linkName, req, json, status, cache); 
    }
    
    protected abstract Map<String, Object> executeImpl(SiteInfo site, 
          String linkName, WebScriptRequest req, JSONObject json, 
          Status status, Cache cache);
    
}
