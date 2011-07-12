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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.calendar.CalendarModel;
import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the slingshot calendar eventList.get webscript.
 * 
 * TODO Improve what we give to the FTL, and have the FTL include iso8601 dates too
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class CalendarEntriesListGet extends AbstractCalendarWebScript
{
   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, String eventName,
         WebScriptRequest req, JSONObject json, Status status, Cache cache) {
      SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy"); // Evil...
      
      // Get the entries for the list
      PagingRequest paging = buildPagingRequest(req);
      PagingResults<CalendarEntry> entries = 
         calendarService.listCalendarEntries(site.getShortName(), paging);
      
      // For each one in our page, grab details of any ignored instances
      List<Map<String,Object>> results = new ArrayList<Map<String,Object>>();
      for (CalendarEntry entry : entries.getPage())
      {
         Map<String, Object> result = new HashMap<String, Object>();
         result.put("event", entry);
         result.put("fromDate", entry.getStart());
         result.put("tags", entry.getTags());
         
         List<ChildAssociationRef> ignores = nodeService.getChildAssocs(
               entry.getNodeRef(), CalendarModel.TYPE_IGNORE_EVENT, 
               ContentModel.ASSOC_CONTAINS, true
         );
         List<String> ignoreEvents = new ArrayList<String>();
         List<Date> ignoreEventDates = new ArrayList<Date>();
         for (ChildAssociationRef ref : ignores)
         {
            Date date = (Date)nodeService.getProperty(ref.getChildRef(), CalendarModel.PROP_IGNORE_EVENT_DATE);
            if (date != null)
            {
               ignoreEventDates.add(date);
               ignoreEvents.add(formatter.format(date));
            }
         }
         result.put("ignoreEvents", ignoreEvents);
         result.put("ignoreEventDates", ignoreEventDates);
         
         results.add(result);
      }
      
      // All done
      Map<String, Object> model = new HashMap<String, Object>();
      model.put("events", results);
      model.put("siteId", site.getShortName());
      model.put("site", site);
      return model;
   }
}
