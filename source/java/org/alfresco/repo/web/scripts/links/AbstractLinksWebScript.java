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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.query.PagingRequest;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.links.LinkInfo;
import org.alfresco.service.cmr.links.LinksService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
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
import org.springframework.extensions.webscripts.json.JSONWriter;

/**
 * @author Nick Burch
 * @since 4.0
 */
public abstract class AbstractLinksWebScript extends DeclarativeWebScript
{
    public static final String LINKS_SERVICE_ACTIVITY_APP_NAME = "links";

    protected static final String PARAM_MESSAGE = "message";
    protected static final String PARAM_ITEM = "item";
    
    private static Log logger = LogFactory.getLog(AbstractLinksWebScript.class);
    
    // Injected services
    protected NodeService nodeService;
    protected SiteService siteService;
    protected LinksService linksService;
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
    
    public void setLinksService(LinksService linksService)
    {
        this.linksService = linksService;
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
    
    protected List<String> getTags(JSONObject json)
    {
       List<String> tags = null;
       if (json.containsKey("tags"))
       {
          // Is it "tags":"" or "tags":[...] ?
          if (json.get("tags") instanceof String)
          {
             // This is normally an empty string, skip
             String tagsS = (String)json.get("tags");
             if ("".equals(tagsS))
             {
                // No tags were given
                return null;
             }
             else
             {
                // Log, and treat as empty
                // (We don't support "tags":"a,b,c" in these webscripts)
                logger.warn("Unexpected tag data: " + tagsS);
                return null;
             }
          }
          else
          {
             tags = new ArrayList<String>();
             JSONArray jsTags = (JSONArray)json.get("tags");
             for (int i=0; i<jsTags.size(); i++)
             {
                tags.add( (String)jsTags.get(i) );
             }
          }
       }
       return tags;
    }
    
    /**
     * Builds up a listing Paging request, based on the arguments
     *  specified in the URL
     */
    protected PagingRequest buildPagingRequest(WebScriptRequest req)
    {
       String pageNumberS = req.getParameter("page");
       String pageSizeS = req.getParameter("pageSize");
       if (pageNumberS == null || pageSizeS == null)
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
       catch (NumberFormatException e)
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
          page = "links";
       }
       
       try
       {
          StringWriter activityJson = new StringWriter();
          JSONWriter activity = new JSONWriter(activityJson);
          activity.startObject();
          activity.writeValue("title", link.getTitle());
          activity.writeValue("page", page + "?linkId=" + link.getSystemName());
          activity.endObject();
          
          activityService.postActivity(
                "org.alfresco.links.link-" + event,
                site.getShortName(),
                LINKS_SERVICE_ACTIVITY_APP_NAME,
                activityJson.toString());
       }
       catch (Exception e)
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
       if (creator == null)
       {
          creatorO = "";
       }
       else
       {
          NodeRef person = personService.getPerson(creator);
          creatorO = person;
       }
       res.put("creator", creatorO);
       
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
          catch(ParseException pe)
          {
             throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + pe.getMessage());
          }
       }
       
       
       // Get the site short name. Try quite hard to do so...
       String siteName = templateVars.get("site");
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
          else if (json.containsKey("site"))
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
       
       // Link name is optional
       String linkName = templateVars.get("path");
       
       // Have the real work done
       return executeImpl(site, linkName, req, json, status, cache); 
    }
    
    protected abstract Map<String, Object> executeImpl(SiteInfo site, 
          String linkName, WebScriptRequest req, JSONObject json, 
          Status status, Cache cache);
    
}
