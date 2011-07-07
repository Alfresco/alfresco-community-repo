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
package org.alfresco.repo.web.scripts.calendar;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.calendar.CalendarService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.ISO8601DateFormat;
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
public abstract class AbstractCalendarWebScript extends DeclarativeWebScript
{
    public static final String CALENDAR_SERVICE_ACTIVITY_APP_NAME = "calendar";
   
    // Injected services
    protected NodeService nodeService;
    protected SiteService siteService;
    protected ActivityService activityService;
    protected CalendarService calendarService;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    public void setActivityService(ActivityService activityService)
    {
        this.activityService = activityService;
    }
    
    public void setCalendarService(CalendarService calendarService)
    {
        this.calendarService = calendarService;
    }
    
    /**
     * Gets the date from the String, trying the various formats
     *  (New and Legacy) until one works...
     */
    protected Date extractDate(String date)
    {
       // Is there one at all?
       if(date == null || date.length() == 0)
       {
          return null;
       }
       
       // Try as ISO8601
       try
       {
          return ISO8601DateFormat.parse(date);
       }
       catch(Exception e) {}
       
       // Try YYYY/MM/DD
       SimpleDateFormat slashtime = new SimpleDateFormat("yyyy/MM/dd HH:mm");
       SimpleDateFormat slash = new SimpleDateFormat("yyyy/MM/dd");
       try
       {
          return slashtime.parse(date);
       }
       catch(ParseException e) {}
       try
       {
          return slash.parse(date);
       }
       catch(ParseException e) {}
       
       // Try YYYY-MM-DD
       SimpleDateFormat dashtime = new SimpleDateFormat("yyyy-MM-dd HH:mm");
       SimpleDateFormat dash = new SimpleDateFormat("yyyy-MM-dd");
       try
       {
          return dashtime.parse(date);
       }
       catch(ParseException e) {}
       try
       {
          return dash.parse(date);
       }
       catch(ParseException e) {}

       // We don't know what it is, object
       throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid date '" + date + "'");
    }
    
    /**
     * Normally the Calendar webscripts return a 200 with JSON
     *  containing the error message. Override this to switch to
     *  using HTTP status codes instead
     */
    protected boolean useJSONErrors()
    {
       return true;
    }
    
    /**
     * Equivalent of <i>jsonError</i> in the old JavaScript controllers
     */
    protected Map<String,Object> buildError(String message)
    {
       HashMap<String, Object> result = new HashMap<String, Object>();
       result.put("error", message);
       
       HashMap<String, Object> model = new HashMap<String, Object>();
       model.put("error", message);
       model.put("result", result);
       
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
          if(useJSONErrors())
          {
             return buildError(error);
          }
          else
          {
             throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
          }
       }
       
       
       // Parse the JSON, if supplied
       JSONObject json = null;
       if(MimetypeMap.MIMETYPE_JSON.equals( req.getContentType() ))
       {
          try
          {
             json = new JSONObject(new JSONTokener(req.getContent().getContent()));
          }
          catch(IOException io)
          {
             return buildError("Invalid JSON: " + io.getMessage());
          }
          catch(JSONException je)
          {
             return buildError("Invalid JSON: " + je.getMessage());
          }
       }
       
       
       // Get the site short name. Try quite hard to do so...
       String siteName = templateVars.get("siteid");
       if(siteName == null)
       {
          siteName = templateVars.get("site");
       }
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
          if(useJSONErrors())
          {
             return buildError("No site given");
          }
          else
          {
             throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
          }
       }
       
       // Grab the requested site
       SiteInfo site = siteService.getSite(siteName);
       if(site == null)
       {
          String error = "Could not find site: " + siteName;
          if(useJSONErrors())
          {
             return buildError(error);
          }
          else
          {
             throw new WebScriptException(Status.STATUS_NOT_FOUND, error);
          }
       }
       
       // Event name is optional
       String eventName = templateVars.get("eventname");
       
       // Have the real work done
       return executeImpl(site, eventName, req, json, status, cache); 
    }
    
    protected abstract Map<String, Object> executeImpl(SiteInfo site, 
          String eventName, WebScriptRequest req, JSONObject json, 
          Status status, Cache cache);
    
}
