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

import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.calendar.CalendarEntryDTO;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
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
      
      Map<String, Object> model = new HashMap<String, Object>();
      model.put("name", entry.getSystemName());
      model.put("what", entry.getTitle());
      model.put("description", entry.getDescription());
      model.put("location", entry.getLocation());
      model.put("from", entry.getStart());
      model.put("to", entry.getEnd());
      model.put("tags", entry.getTags());
      model.put("isoutlook", entry.isOutlook());
      model.put("outlookuid", entry.getOutlookUID());
      model.put("allday", CalendarEntryDTO.isAllDay(entry));
      model.put("recurrence", null); // TODO
      model.put("docfolder", null); // TODO
      return model;
   }
}
