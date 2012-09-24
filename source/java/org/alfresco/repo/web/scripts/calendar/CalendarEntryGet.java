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

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.calendar.CalendarEntryDTO;
import org.alfresco.service.cmr.calendar.CalendarRecurrenceHelper;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
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
   private PermissionService permissionService;
   
   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, String eventName,
         WebScriptRequest req, JSONObject json, Status status, Cache cache) 
   {
      final ResourceBundle rb = getResources();

      CalendarEntry entry = calendarService.getCalendarEntry(
            site.getShortName(), eventName);
      
      if (entry == null)
      {
         String message = rb.getString(MSG_EVENT_NOT_FOUND);
         return buildError(MessageFormat.format(message, eventName));
      }
      
      // Build the object
      Map<String, Object> result = new HashMap<String, Object>();
      result.put("name", entry.getSystemName());
      result.put("what", entry.getTitle());
      result.put("description", entry.getDescription());
      result.put("location", entry.getLocation());
      boolean isAllDay = CalendarEntryDTO.isAllDay(entry);
      boolean removeTimezone = isAllDay && !entry.isOutlook();
      result.put("from", removeTimeZoneIfRequired(entry.getStart(), isAllDay, removeTimezone));
      result.put("to", removeTimeZoneIfRequired(entry.getEnd(), isAllDay, removeTimezone));
      
      String legacyDateFormat = "M/d/yyyy";
      String legacyTimeFormat ="HH:mm";
      result.put("legacyDateFrom", removeTimeZoneIfRequired(entry.getStart(), isAllDay, removeTimezone, legacyDateFormat));
      result.put("legacyTimeFrom", removeTimeZoneIfRequired(entry.getStart(), isAllDay, removeTimezone, legacyTimeFormat));
      result.put("legacyDateTo", removeTimeZoneIfRequired(entry.getEnd(), isAllDay, removeTimezone, legacyDateFormat));
      result.put("legacyTimeTo", removeTimeZoneIfRequired(entry.getEnd(), isAllDay, removeTimezone, legacyTimeFormat));
      
      result.put("tags", entry.getTags());
      result.put("isoutlook", entry.isOutlook());
      result.put("outlookuid", entry.getOutlookUID());
      result.put("allday", isAllDay);
      result.put("docfolder", entry.getSharePointDocFolder());
      result.put("recurrence", buildRecurrenceString(entry));
      
      // Replace nulls with blank strings for the JSON
      for (String key : result.keySet())
      {
         if (result.get(key) == null)
         {
            result.put(key, "");
         }
      }
      
      // Check the permissions the user has on the entry
      AccessStatus canEdit = permissionService.hasPermission(entry.getNodeRef(), PermissionService.WRITE);
      AccessStatus canDelete = permissionService.hasPermission(entry.getNodeRef(), PermissionService.DELETE);
      result.put("canEdit", (canEdit == AccessStatus.ALLOWED));
      result.put("canDelete", (canDelete == AccessStatus.ALLOWED));
      
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
      if (recurrence == null || recurrence.trim().length() == 0)
      {
         return null;
      }
      
      // Get our days of the week, in the current locale, for each outlook two letter code
      Map<String,String> days = 
         CalendarRecurrenceHelper.buildLocalRecurrenceDaysOfTheWeek(I18NUtil.getLocale());
      
      // Get our weeks names, in the current locale
      Map<Integer, String> weeks =
    		  CalendarRecurrenceHelper.buildLocalRecurrenceWeekNames(I18NUtil.getLocale());
      
      // Turn the string into a useful map
      Map<String,String> params = CalendarRecurrenceHelper.extractRecurrenceRule(event);
      
      // To hold our result
      StringBuffer text = new StringBuffer();
      
      // Handle the different frequencies
      if (params.containsKey("FREQ"))
      {
         String freq = params.get("FREQ");
         String interval = params.get("INTERVAL");
         if (interval == null)
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
            
            for (String day : params.get("BYDAY").split(","))
            {
               text.append(days.get(day));
               text.append(", ");
            }
         }
         else if ("DAILY".equals(freq))
         {
            if ("1".equals(interval))
            {
               text.append("Occurs every day ");
            }
            else
            {
               text.append("Occurs every " + interval + " days ");
            }
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
            	text.append(weeks.get((Integer.parseInt(params.get("BYSETPOS")))) + " ");
                text.append(days.get(params.get("BYDAY")));
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
              text.append(weeks.get((Integer.parseInt(params.get("BYSETPOS")))) + " ");
              text.append(days.get(params.get("BYDAY")) + " ");
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
            SimpleDateFormat.MEDIUM, I18NUtil.getLocale());
      
      DateFormat tFormat = SimpleDateFormat.getTimeInstance(
            SimpleDateFormat.SHORT, I18NUtil.getLocale());
      
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
      
      // Add timezone in which recurrence rule was parsed
      TimeZone timeZone = TimeZone.getDefault();
	  boolean daylight = timeZone.inDaylightTime(new Date());
	  String tzDisplayName = timeZone.getDisplayName(daylight, TimeZone.SHORT);
	  
      text.append(" ("+tzDisplayName+")");
      
      // All done
      return text.toString();
   }
   
   public void setPermissionService(PermissionService permissionService)
   {
      this.permissionService = permissionService;
   }
}
