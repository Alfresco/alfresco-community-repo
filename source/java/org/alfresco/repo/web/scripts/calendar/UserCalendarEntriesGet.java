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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.calendar.CalendarServiceImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.calendar.CalendarEntryDTO;
import org.alfresco.service.cmr.calendar.CalendarRecurrenceHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the slingshot calendar userevents.get webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class UserCalendarEntriesGet extends AbstractCalendarWebScript
{
   private static final String RESULT_NAME = "name"; 
   private static final String RESULT_TITLE = "title"; 
   private static final String RESULT_START = "start"; 
   private static final String RESULT_END = "end"; 
   
   @Override
   protected Map<String, Object> executeImpl(WebScriptRequest req,
         Status status, Cache cache) 
   {
      Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
      
      // Site is optional
      SiteInfo site = null;
      String siteName = templateVars.get("site");
      if(siteName != null)
      {
         site = siteService.getSite(siteName);
      }
      
      return executeImpl(site, null, req, null, status, cache);
   }
   
   @Override
   protected Map<String, Object> executeImpl(SiteInfo singleSite, String eventName,
         WebScriptRequest req, JSONObject json, Status status, Cache cache) {
      // Did they restrict by date?
      Date fromDate = parseDate(req.getParameter("from"));
      Date toDate = parseDate(req.getParameter("to"));
      
      // What should we do about repeating events? First or all?
      boolean repeatingFirstOnly = true;
      if(fromDate != null)
      {
         // TODO Find a better way to do this...
         String fromDateS = req.getParameter("from");
         if(fromDateS.indexOf('-') != -1)
         {
            // Apparently this is the site calendar dashlet...
            repeatingFirstOnly = true;
         }
         if(fromDateS.indexOf('/') != -1)
         {
            // This is something else, wants all events in range
            repeatingFirstOnly = false;
         }
      }
      
      // One site, or all the user's ones?
      List<SiteInfo> sites = new ArrayList<SiteInfo>();
      if(singleSite != null)
      {
         // Just one
         sites.add(singleSite);
      }
      else
      {
         // All their sites
         sites = siteService.listSites(AuthenticationUtil.getRunAsUser());
      }
      
      // We need to know the Site Names, and the NodeRefs of the calendar containers
      String[] siteShortNames = new String[sites.size()];
      Map<NodeRef, SiteInfo> containerLookup = new HashMap<NodeRef, SiteInfo>();
      for(int i=0; i<sites.size(); i++)
      {
         SiteInfo site = sites.get(i);
         siteShortNames[i] = site.getShortName();
         containerLookup.put(
             siteService.getContainer(site.getShortName(), CalendarServiceImpl.CALENDAR_COMPONENT), 
             site
         );
      }
      
      
      // Get the entries for the list
      PagingRequest paging = buildPagingRequest(req);
      PagingResults<CalendarEntry> entries = 
         calendarService.listCalendarEntries(siteShortNames, fromDate, toDate, paging);

      List<Map<String, Object>> results = new ArrayList<Map<String,Object>>();
      for(CalendarEntry entry : entries.getPage())
      {
         // Build the object
         Map<String, Object> result = new HashMap<String, Object>();
         result.put(RESULT_NAME,   entry.getSystemName());
         result.put(RESULT_TITLE,  entry.getTitle());
         result.put("description", entry.getDescription());
         result.put("where",       entry.getLocation());
         result.put(RESULT_START,  entry.getStart());
         result.put(RESULT_END,    entry.getEnd());
         result.put("duration", buildDuration(entry));
         result.put("tags", entry.getTags());
         result.put("isoutlook", entry.isOutlook());
         result.put("allday", CalendarEntryDTO.isAllDay(entry));
         
         // Identify the site
         SiteInfo site = containerLookup.get(entry.getContainerNodeRef());
         result.put("site", site);
         result.put("siteName", site.getShortName());
         result.put("siteTitle", site.getTitle());
         
         // Replace nulls with blank strings for the JSON
         for(String key : result.keySet())
         {
            if(result.get(key) == null)
            {
               result.put(key, "");
            }
         }
         
         // Save this one
         results.add(result);
         
         // Handle recurring as needed
         handleRecurring(entry, result, results, fromDate, repeatingFirstOnly);
      }
      
      // All done
      Map<String, Object> model = new HashMap<String, Object>();
      model.put("events", results);
      return model;
   }

   private static final long DURATION_SECOND = 1000; 
   private static final long DURATION_MINUTE = 60 * DURATION_SECOND;
   private static final long DURATION_HOUR = 60 * DURATION_MINUTE;
   private static final long DURATION_DAY = 24 * DURATION_HOUR;
   private static final long DURATION_WEEK = 7 * DURATION_DAY;
   
   /**
    * Builds the duration in iCal format, eg PT2H15M
    */
   private String buildDuration(CalendarEntry entry)
   {
      StringBuffer duration = new StringBuffer();
      duration.append("P");
      
      long timeDiff = entry.getEnd().getTime() - entry.getStart().getTime();
      
      int weeks = (int)Math.floor(timeDiff / DURATION_WEEK);
      if(weeks > 0)
      {
         duration.append(weeks);
         duration.append("W");
         timeDiff -= weeks * DURATION_WEEK;
      }
      
      int days = (int)Math.floor(timeDiff / DURATION_DAY);
      if(days > 0)
      {
         duration.append(days);
         duration.append("D");
         timeDiff -= days * DURATION_DAY;
      }
      
      duration.append("T");
      
      int hours = (int)Math.floor(timeDiff / DURATION_HOUR);
      if(hours > 0)
      {
         duration.append(hours);
         duration.append("H");
         timeDiff -= hours * DURATION_HOUR;
      }
      
      int minutes = (int)Math.floor(timeDiff / DURATION_MINUTE);
      if(minutes > 0)
      {
         duration.append(minutes);
         duration.append("M");
         timeDiff -= minutes * DURATION_MINUTE;
      }
      
      int seconds = (int)Math.floor(timeDiff / DURATION_SECOND);
      if(seconds > 0)
      {
         duration.append(seconds);
         timeDiff -= minutes * DURATION_MINUTE;
      }
      
      return duration.toString();
   }
   
   /**
    * Do what's needed for recurring events 
    */
   private void handleRecurring(CalendarEntry entry, Map<String, Object> entryResult, 
         List<Map<String, Object>> allResults, Date from, boolean repeatingFirstOnly)
   {
      if(entry.getRecurrenceRule() == null)
      {
         // Nothing to do
         return;
      }
      
      // Should we limit ourselves?
      Date until = null;
      if(!repeatingFirstOnly)
      {
         // Only repeating instances for the next 60 days
         until = new Date(from.getTime() + 24*60*60*1000);
      }
      
      // How long is it?
      long duration = entry.getEnd().getTime() - entry.getStart().getTime();
      
      // Get it's recurring instances
      List<Date> dates = CalendarRecurrenceHelper.getRecurrencesOnOrAfter(
            entry, from, until, repeatingFirstOnly);
      
      // If we got no dates, then no recurrences in the period so zap
      if(dates == null || dates.size() == 0)
      {
         allResults.remove(entryResult);
         return;
      }

      // Always update the live entry
      updateRepeatingStartEnd(dates.get(0), duration, entryResult);
      
      // If first result only, alter title and finish
      if(repeatingFirstOnly)
      {
         entryResult.put(RESULT_TITLE, entry.getTitle() + " (Repeating)");
         return;
      }
      
      // Otherwise generate one entry per extra date
      for(int i=1; i<dates.size(); i++)
      {
         // Clone the properties
         Map<String, Object> newResult = new HashMap<String, Object>(entryResult);
         
         // Generate start and end based on this date
         updateRepeatingStartEnd(dates.get(i), duration, newResult);
         
         // Save as a new event
         allResults.add(newResult);
      }
   }
   
   private void updateRepeatingStartEnd(Date newStart, long duration, Map<String, Object> result)
   {
      Date newEnd = new Date(newStart.getTime() + duration);
      result.put(RESULT_START, newStart);
      result.put(RESULT_END, newEnd);
   }
}
