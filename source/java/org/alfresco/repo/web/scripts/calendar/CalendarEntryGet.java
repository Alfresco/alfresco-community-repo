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

import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.calendar.CalendarEntryDTO;
import org.alfresco.service.cmr.site.SiteInfo;
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
   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, String eventName,
         WebScriptRequest req, Status status, Cache cache) {
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
      result.put("recurrence", null); // TODO
      
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
}
