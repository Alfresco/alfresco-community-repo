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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.calendar.CalendarEntryDTO;
import org.alfresco.service.cmr.calendar.CalendarRecurrenceHelper;
import org.alfresco.service.cmr.site.SiteInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the slingshot calendar event.get webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class CalendarEntryGet extends AbstractCalendarWebScript
{
   private static Log logger = LogFactory.getLog(CalendarEntryGet.class);
   
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
      
      // Build the object
      Map<String, Object> result = new HashMap<String, Object>();
      result.put("name", entry.getSystemName());
      result.put("what", entry.getTitle());
      result.put("description", entry.getDescription());
      result.put("location", entry.getLocation());
      result.put("from", entry.getStart());
      result.put("to", entry.getEnd());
      result.put("tags", entry.getTags());
      result.put("isoutlook", entry.isOutlook());
      result.put("outlookuid", entry.getOutlookUID());
      result.put("allday", CalendarEntryDTO.isAllDay(entry));
      result.put("docfolder", entry.getSharePointDocFolder());
      result.put("recurrence", buildRecurrenceString(entry));
      
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
    * This method replicates the pre-existing behaviour for recurring events. 
    * Rather than try to render the text for them on the client, we instead
    *  statically render the description text here on the server.
    * When we properly support recurring events in the client (and not just
    *  for SharePoint ones), this can be replaced.
    */
   protected String buildRecurrenceString(CalendarEntry event)
   {
      // If there's no recurrence rules, then there's nothing to do
      String recurrence = event.getRecurrenceRule();
      if(recurrence == null || recurrence.trim().length() == 0)
      {
         return null;
      }
      
      // Get our days of the week, in the current locale, for each outlook two letter code
      Map<String,String> days = 
         CalendarRecurrenceHelper.buildLocalRecurrenceDaysOfTheWeek(I18NUtil.getLocale());
      
      // Turn the string into a useful map
      Map<String,String> params = CalendarRecurrenceHelper.extractRecurrenceRule(event);
      
      // To hold our result
      StringBuffer text = new StringBuffer();
      
      // Handle the different frequencies
      if(params.containsKey("FREQ"))
      {
         String freq = params.get("FREQ");
         String interval = params.get("INTERVAL");
         if(interval == null)
         {
            interval = "1";
         }
         
         if ("WEEKLY".equals(freq))
         {
            if ("1".equals(interval))
            {
               text.append("Occurs each week on ");
            }
            else
            {
               text.append("Occurs every " + interval + " weeks on ");
            }
            
            for(String day : params.get("BYDAY").split(","))
            {
               text.append(days.get(day));
               text.append(", ");
            }
         }
         else if ("DAILY".equals(freq))
         {
            text.append("Occurs every day ");
         }
         else if ("MONTHLY".equals(freq))
         {
            if (params.get("BYMONTHDAY") != null)
            {
               text.append("Occurs day " + params.get("BYMONTHDAY"));
            }
            else if (params.get("BYSETPOS") != null)
            {
               text.append("Occurs the ");
               text.append(days.get(params.get("BYSETPOS")));
            }
            text.append(" of every " + interval + " month(s) ");
         }
         else if ("YEARLY".equals(freq))
         {
            if (params.get("BYMONTHDAY") != null)
            {
               text.append("Occurs every " + params.get("BYMONTHDAY"));
               text.append("." + params.get("BYMONTH") + " ");
            }
            else
            {
              text.append("Occurs the ");
              text.append(days.get(params.get("BYSETPOS")));
              text.append(" of " +  params.get("BYMONTH") + " month ");
            }
         }
         else
         {
            logger.warn("Unsupported recurrence frequency " + freq);
         }
      }
      
      // And the rest
      DateFormat dFormat = SimpleDateFormat.getDateInstance(
            SimpleDateFormat.MEDIUM, I18NUtil.getLocale()
      );
      DateFormat tFormat = SimpleDateFormat.getTimeInstance(
            SimpleDateFormat.SHORT, I18NUtil.getLocale()
      );
      text.append("effective " + dFormat.format(event.getStart()));
      
      if (params.containsKey("COUNT"))
      {
         // Nothing to do, is already handled in the recurrence date 
      }
      if (event.getLastRecurrence() != null)
      {
         text.append(" until " + dFormat.format(event.getLastRecurrence()));
      }
      
      text.append(" from " + tFormat.format(event.getStart()));
      text.append(" to " + tFormat.format(event.getEnd()));
      
      // All done
      return text.toString();
   }
}
