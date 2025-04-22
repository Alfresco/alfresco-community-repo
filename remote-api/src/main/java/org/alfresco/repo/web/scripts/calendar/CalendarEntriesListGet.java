/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.web.scripts.calendar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.calendar.CalendarModel;
import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.calendar.CalendarEntryDTO;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.site.SiteInfo;

/**
 * This class is the controller for the slingshot calendar eventList.get webscript.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class CalendarEntriesListGet extends AbstractCalendarListingWebScript
{
    @Override
    protected Map<String, Object> executeImpl(SiteInfo site, String eventName,
            WebScriptRequest req, JSONObject json, Status status, Cache cache)
    {
        // Evil format needed for compatibility with old API...
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

        // Decide on date ranges and repeating rules
        Date fromDate = parseDate(req.getParameter("from"));
        Date toDate = parseDate(req.getParameter("to"));

        boolean resortNeeded = false;
        boolean repeatingFirstOnly = true;
        String repeatingEvents = req.getParameter("repeating");
        if (repeatingEvents != null)
        {
            if ("first".equals(repeatingEvents))
            {
                repeatingFirstOnly = true;
            }
            else if ("all".equals(repeatingEvents))
            {
                repeatingFirstOnly = false;
                resortNeeded = true;
            }
        }

        // Get the entries for the list
        PagingRequest paging = buildPagingRequest(req);
        PagingResults<CalendarEntry> entries = calendarService.listCalendarEntries(
                new String[]{site.getShortName()}, fromDate, toDate, paging);

        // For each one in our page, grab details of any ignored instances
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        for (CalendarEntry entry : entries.getPage())
        {
            Map<String, Object> result = new HashMap<String, Object>();
            result.put(RESULT_EVENT, entry);
            result.put(RESULT_NAME, entry.getSystemName());
            result.put(RESULT_TITLE, entry.getTitle());
            boolean isAllDay = CalendarEntryDTO.isAllDay(entry);
            boolean removeTimezone = isAllDay && !entry.isOutlook();
            result.put(RESULT_START, removeTimeZoneIfRequired(entry.getStart(), isAllDay, removeTimezone));
            result.put(RESULT_END, removeTimeZoneIfRequired(entry.getEnd(), isAllDay, removeTimezone));
            if (isAllDay)
            {
                long dayLong = 86400000;
                Date newDay = new Date(entry.getEnd().getTime() + dayLong);
                result.put("allDayEnd", newDay);
            }

            String legacyDateFormat = "M/d/yyyy";
            String legacyTimeFormat = "HH:mm";
            result.put("legacyDateFrom", removeTimeZoneIfRequired(entry.getStart(), isAllDay, removeTimezone, legacyDateFormat));
            result.put("legacyTimeFrom", removeTimeZoneIfRequired(entry.getStart(), isAllDay, removeTimezone, legacyTimeFormat));
            result.put("legacyDateTo", removeTimeZoneIfRequired(entry.getEnd(), isAllDay, removeTimezone, legacyDateFormat));
            result.put("legacyTimeTo", removeTimeZoneIfRequired(entry.getEnd(), isAllDay, removeTimezone, legacyTimeFormat));

            result.put("fromDate", entry.getStart());
            result.put("tags", entry.getTags());

            List<ChildAssociationRef> ignores = nodeService.getChildAssocs(
                    entry.getNodeRef(), CalendarModel.TYPE_IGNORE_EVENT,
                    ContentModel.ASSOC_CONTAINS, true);

            List<String> ignoreEvents = new ArrayList<String>();
            List<Date> ignoreEventDates = new ArrayList<Date>();
            for (ChildAssociationRef ref : ignores)
            {
                Date date = (Date) nodeService.getProperty(ref.getChildRef(), CalendarModel.PROP_IGNORE_EVENT_DATE);
                if (date != null)
                {
                    ignoreEventDates.add(date);
                    ignoreEvents.add(formatter.format(date));
                }
            }
            result.put("ignoreEvents", ignoreEvents);
            result.put("ignoreEventDates", ignoreEventDates);

            // For repeating events, push forward if needed
            boolean orderChanged = handleRecurring(entry, result, results, fromDate, toDate, repeatingFirstOnly);
            if (orderChanged)
            {
                resortNeeded = true;
            }

            // All done with this one
            results.add(result);
        }

        // If they asked for repeating events to be expanded, then do so
        if (resortNeeded)
        {
            Collections.sort(results, getEventDetailsSorter());
        }

        // All done
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("events", results);
        model.put("siteId", site.getShortName());
        model.put("site", site);
        return model;
    }
}
