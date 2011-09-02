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

import java.util.Map;

import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.site.SiteInfo;
import org.json.simple.JSONObject;
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
         // Have an ignored event generated
         createIgnoreEvent(req, entry);
         
         // Mark as ignored
         status.setCode(Status.STATUS_NO_CONTENT, "Recurring entry ignored");
         return null;
      }
      
      // Delete the calendar entry
      calendarService.deleteCalendarEntry(entry);
      
      // Record this in the activity feed
      addActivityEntry("deleted", entry, site, req, json);

      // All done
      status.setCode(Status.STATUS_NO_CONTENT, "Entry deleted");
      return null;
   }
}
