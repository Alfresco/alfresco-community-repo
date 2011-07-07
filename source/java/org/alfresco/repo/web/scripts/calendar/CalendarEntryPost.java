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

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.calendar.CalendarEntryDTO;
import org.alfresco.service.cmr.site.SiteInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the slingshot calendar event.post webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class CalendarEntryPost extends AbstractCalendarWebScript
{
   private static Log logger = LogFactory.getLog(CalendarEntryPost.class);
   
   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, String eventName,
         WebScriptRequest req, JSONObject json, Status status, Cache cache) {
      CalendarEntry entry = new CalendarEntryDTO();
      
      // TODO Handle All Day events properly, including timezones
      boolean isAllDay = false;

      try
      {
         // Grab the properties
         entry.setTitle(getOrNull(json, "what"));
         entry.setDescription(getOrNull(json, "desc"));
         entry.setLocation(getOrNull(json, "where"));
         entry.setSharePointDocFolder(getOrNull(json, "docfolder"));
         
         // Handle the dates
         if(json.has("startAt") && json.has("endAt"))
         {
            // New style ISO8601 dates
            entry.setStart(extractDate(json.getString("startAt")));
            entry.setEnd(extractDate(json.getString("endAt")));
            if(json.has("allday"))
            {
               // TODO Handle All Day events properly, including timezones
               isAllDay = true;
            }
         }
         else if(json.has("allday"))
         {
            // Old style all-day event
            entry.setStart(extractDate(getOrNull(json, "from")));
            entry.setEnd(extractDate(getOrNull(json, "to")));
            isAllDay = true;
         }
         else
         {
            // Old style regular event
            entry.setStart(extractDate(json.getString("from") + " " + json.getString("start")));
            entry.setEnd(extractDate(json.getString("to") + " " + json.getString("end")));
         }
         
         // Handle tags
         if(json.has("tags"))
         {
            StringTokenizer st = new StringTokenizer(json.getString("tags"), " ");
            while(st.hasMoreTokens())
            {
               entry.getTags().add(st.nextToken());
            }
         }
      }
      catch(JSONException je)
      {
         return buildError("Invalid JSON: " + je.getMessage());
      }
      
      if(entry == null)
      {
         return buildError("Could not find event: " + eventName);
      }
      
      
      // Have it added
      entry = calendarService.createCalendarEntry(site.getShortName(), entry);
      
      
      // Generate the activity feed for this
      SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
      String dateOpt = "?date=" + fmt.format(entry.getStart());
      try
      {
         JSONObject activity = new JSONObject();
         activity.put("title", entry.getTitle());
         activity.put("page", req.getParameter("page") + dateOpt);
         
         activityService.postActivity(
               "org.alfresco.calendar.event-created",
               site.getShortName(),
               CALENDAR_SERVICE_ACTIVITY_APP_NAME,
               activity.toString()
         );
      }
      catch(Exception e)
      {
         // Warn, but carry on
         logger.warn("Error adding event deletion to activities feed", e);
      }
      
      
      // Build the return object
      Map<String, Object> result = new HashMap<String, Object>();
      result.put("name", entry.getTitle());
      result.put("desc", entry.getDescription());
      result.put("where", entry.getLocation());
      result.put("from", entry.getStart());
      result.put("to", entry.getEnd());
      result.put("uri", "calendar/event/" + site.getShortName() + "/" +
                        entry.getSystemName() + dateOpt);
      
      result.put("tags", entry.getTags());
      result.put("allday", isAllDay);
      result.put("docfolder", entry.getSharePointDocFolder());
      
      // Replace nulls with blank strings for the JSON
      for(String key : result.keySet())
      {
         if(result.get(key) == null)
         {
            result.put(key, "");
         }
      }
      
      // All done
      Map<String, Object> model = new HashMap<String, Object>();
      model.put("result", result);
      return model;
   }
   
   private String getOrNull(JSONObject json, String key) throws JSONException
   {
      if(json.has(key))
      {
         return json.getString(key);
      }
      return null;
   }
}
