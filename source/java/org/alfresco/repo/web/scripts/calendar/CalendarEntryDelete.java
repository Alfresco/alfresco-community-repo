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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.calendar.CalendarModel;
import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the slingshot calendar event.delete webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class CalendarEntryDelete extends AbstractCalendarWebScript
{
   private static Log logger = LogFactory.getLog(CalendarEntryDelete.class);
   
   /**
    * This WebScript uses HTTP status codes for errors
    */
   @Override
   protected boolean useJSONErrors() {
      return false;
   }

   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, String eventName,
         WebScriptRequest req, JSONObject json, Status status, Cache cache) {
      CalendarEntry entry = calendarService.getCalendarEntry(
            site.getShortName(), eventName
      );
      
      if(entry == null)
      {
         status.setCode(Status.STATUS_NOT_FOUND);
         return null;
      }
      
      // Special case for "deleting" an instance of a recurring event 
      if(req.getParameter("date") != null && entry.getRecurrenceRule() != null)
      {
         // Get the date to be ignored
         Map<QName,Serializable> props = new HashMap<QName, Serializable>();
         Date date = parseDate(req.getParameter("date"));
         props.put(CalendarModel.PROP_IGNORE_EVENT_DATE, date);
         
         // Create a child node of the event
         nodeService.createNode(
               entry.getNodeRef(), CalendarModel.ASSOC_IGNORE_EVENT_LIST,
               QName.createQName(GUID.generate()), CalendarModel.TYPE_IGNORE_EVENT, props
         );
         
         // Mark as ignored
         status.setCode(Status.STATUS_NO_CONTENT, "Recurring entry ignored");
         return null;
      }
      
      // Delete the calendar entry
      calendarService.deleteCalendarEntry(entry);
      
      // Record this in the activity feed
      try
      {
         JSONObject activity = new JSONObject();
         activity.put("title", entry.getTitle());
         activity.put("page", req.getParameter("page"));
         
         activityService.postActivity(
               "org.alfresco.calendar.event-deleted",
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

      // All done
      status.setCode(Status.STATUS_NO_CONTENT, "Entry deleted");
      return null;
   }
}
