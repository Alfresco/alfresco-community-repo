/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.calendar.CalendarEntryDTO;
import org.alfresco.service.cmr.site.SiteInfo;
import org.json.JSONException;
import org.json.simple.JSONObject;
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
   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, String eventName,
         WebScriptRequest req, JSONObject json, Status status, Cache cache) 
   {
      final ResourceBundle rb = getResources();
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
         isAllDay = extractDates(entry, json);
         
         // Handle tags
         if (json.containsKey("tags"))
         {
            StringTokenizer st = new StringTokenizer((String)json.get("tags"), ",");
            while (st.hasMoreTokens())
            {
               entry.getTags().add(st.nextToken());
            }
         }
      }
      catch (JSONException je)
      {
         String message = rb.getString(MSG_INVALID_JSON);
         return buildError(MessageFormat.format(message, je.getMessage()));
      }
      
      // Have it added
      entry = calendarService.createCalendarEntry(site.getShortName(), entry);
      
      
      // Generate the activity feed for this
      String dateOpt = addActivityEntry("created", entry, site, req, json);
      
      
      // Build the return object
      Map<String, Object> result = new HashMap<String, Object>();
      result.put("name", entry.getTitle());
      result.put("desc", entry.getDescription());
      result.put("where", entry.getLocation());
      
      result.put("from", removeTimeZoneIfRequired(entry.getStart(), isAllDay, isAllDay));
      result.put("to", removeTimeZoneIfRequired(entry.getEnd(), isAllDay, isAllDay));
      
      String legacyDateFormat = "yyyy-MM-dd";
      String legacyTimeFormat ="HH:mm";
      result.put("legacyDateFrom", removeTimeZoneIfRequired(entry.getStart(), isAllDay, isAllDay, legacyDateFormat));
      result.put("legacyTimeFrom", removeTimeZoneIfRequired(entry.getStart(), isAllDay, isAllDay, legacyTimeFormat));
      result.put("legacyDateTo", removeTimeZoneIfRequired(entry.getEnd(), isAllDay, isAllDay, legacyDateFormat));
      result.put("legacyTimeTo", removeTimeZoneIfRequired(entry.getEnd(), isAllDay, isAllDay, legacyTimeFormat));
      
      result.put("uri", "calendar/event/" + site.getShortName() + "/" +
                        entry.getSystemName() + dateOpt);
      
      result.put("tags", entry.getTags());
      result.put("allday", isAllDay);
      result.put("docfolder", entry.getSharePointDocFolder());
      
      // Replace nulls with blank strings for the JSON
      for (String key : result.keySet())
      {
         if (result.get(key) == null)
         {
            result.put(key, "");
         }
      }
      
      // All done
      Map<String, Object> model = new HashMap<String, Object>();
      model.put("result", result);
      return model;
   }
}
