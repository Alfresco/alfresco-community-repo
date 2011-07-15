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

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.calendar.CalendarEntryDTO;
import org.alfresco.service.cmr.site.SiteInfo;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the slingshot calendar event.put webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class CalendarEntryPut extends AbstractCalendarWebScript
{
   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, String eventName,
         WebScriptRequest req, JSONObject json, Status status, Cache cache) {
      CalendarEntry entry = calendarService.getCalendarEntry(
            site.getShortName(), eventName
      );
      
      if(entry == null)
      {
         return buildError("Could not find event: " + eventName);
      }
      
      // TODO Handle All Day events properly, including timezones
      boolean isAllDay = false;

      try
      {
         // Doc folder is a bit special
         String docFolder = json.getString("docfolder");
         
         // Editing recurring events is special and a little bit odd...
         if(entry.getRecurrenceRule() != null && !json.has("recurrenceRule"))
         {
            // Have an ignored event generated
            // Will allow us to override this one instance
            createIgnoreEvent(req, entry);
            
            // Create a new entry for this one case
            CalendarEntry newEntry = new CalendarEntryDTO();
            newEntry.setOutlook(true);
            
            if("*NOT_CHANGE*".equals(docFolder))
            {
               newEntry.setSharePointDocFolder(entry.getSharePointDocFolder());
            }
            
            // From here on, "edit" the new version
            entry = newEntry;
         }
         
         // Doc folder is a bit special
         if("*NOT_CHANGE*".equals(docFolder))
         {
            // Nothing to change
         }
         else
         {
            entry.setSharePointDocFolder(docFolder);
         }
            
         
         // Grab the properties
         entry.setTitle(getOrNull(json, "what"));
         entry.setDescription(getOrNull(json, "desc"));
         entry.setLocation(getOrNull(json, "where"));
         
         // Handle the dates
         isAllDay = extractDates(entry, json);
         
         // Recurring properties, only changed if keys present
         if (json.has("recurrenceRule"))
         {
            if (json.isNull("recurrenceRule"))
            {
               entry.setRecurrenceRule(null);
            }
            else
            {
               entry.setRecurrenceRule(json.getString("recurrenceRule"));
            }
         }
         if (json.has("recurrenceLastMeeting"))
         {
            if (json.isNull("recurrenceLastMeeting"))
            {
               entry.setLastRecurrence(null);
            }
            else
            {
               entry.setLastRecurrence(
                     parseDate(json.getString("recurrenceLastMeeting"))
               );
            }
         }
         
         // Handle tags
         if(json.has("tags"))
         {
            entry.getTags().clear();
            
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
      
      
      // Have it edited
      entry = calendarService.updateCalendarEntry(entry);
      
      
      // Generate the activity feed for this
      String dateOpt = addActivityEntry("updated", entry, site, req, json);
      
      
      // Build the return object
      Map<String, Object> result = new HashMap<String, Object>();
      result.put("summary", entry.getTitle());
      result.put("description", entry.getDescription());
      result.put("location", entry.getLocation());
      result.put("dtstart", entry.getStart());
      result.put("dtend", entry.getEnd());
      result.put("uri", "calendar/event/" + site.getShortName() + "/" +
                        entry.getSystemName() + dateOpt);
      
      result.put("tags", generateTagString(entry));
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
   
   /**
    * We use lists for tags internally, and the other webscripts
    *  return arrays too. This one is different, and it needs to 
    *  a single space separated string. This does the conversion
    */
   protected String generateTagString(CalendarEntry entry)
   {
      StringBuffer sb = new StringBuffer();
      if(entry.getTags() != null)
      {
         for(String tag : entry.getTags())
         {
            if(sb.length() > 0) sb.append(' ');
            sb.append(tag);
         }
      }
      return sb.toString();
   }
}
