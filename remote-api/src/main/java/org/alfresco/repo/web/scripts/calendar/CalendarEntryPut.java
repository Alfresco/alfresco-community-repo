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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.calendar.CalendarEntryDTO;
import org.alfresco.service.cmr.site.SiteInfo;

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

        // TODO Handle All Day events properly, including timezones
        boolean isAllDay = false;

        try
        {
            // Doc folder is a bit special
            String docFolder = (String) json.get("docfolder");

            // Editing recurring events is special and a little bit odd...
            if (entry.getRecurrenceRule() != null && !json.containsKey("recurrenceRule"))
            {
                // Have an ignored event generated
                // Will allow us to override this one instance
                createIgnoreEvent(req, entry);

                // Create a new entry for this one case
                CalendarEntry newEntry = new CalendarEntryDTO();
                newEntry.setOutlook(true);

                if ("*NOT_CHANGE*".equals(docFolder))
                {
                    newEntry.setSharePointDocFolder(entry.getSharePointDocFolder());
                }

                // From here on, "edit" the new version
                entry = newEntry;
            }

            // Doc folder is a bit special
            if ("*NOT_CHANGE*".equals(docFolder))
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
            if (json.containsKey("recurrenceRule"))
            {
                if (json.get("recurrenceRule") == null)
                {
                    entry.setRecurrenceRule(null);
                }
                else
                {
                    entry.setRecurrenceRule((String) json.get("recurrenceRule"));
                }
            }
            if (json.containsKey("recurrenceLastMeeting"))
            {
                if (json.get("recurrenceLastMeeting") == null)
                {
                    entry.setLastRecurrence(null);
                }
                else
                {
                    entry.setLastRecurrence(
                            parseDate((String) json.get("recurrenceLastMeeting")));
                }
            }

            // Handle tags
            if (json.containsKey("tags"))
            {
                entry.getTags().clear();

                StringTokenizer st = new StringTokenizer((String) json.get("tags"), ",");
                while (st.hasMoreTokens())
                {
                    entry.getTags().add(st.nextToken());
                }
            }
        }
        catch (JSONException je)
        {
            String message = rb.getString(MSG_INVALID_JSON);
            return buildError(MessageFormat.format(message, je.getMessage()));
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
        boolean removeTimezone = isAllDay && !entry.isOutlook();
        result.put("dtstart", removeTimeZoneIfRequired(entry.getStart(), isAllDay, removeTimezone));
        result.put("dtend", removeTimeZoneIfRequired(entry.getEnd(), isAllDay, removeTimezone));

        String legacyDateFormat = "yyyy-MM-dd";
        String legacyTimeFormat = "HH:mm";
        result.put("legacyDateFrom", removeTimeZoneIfRequired(entry.getStart(), isAllDay, removeTimezone, legacyDateFormat));
        result.put("legacyTimeFrom", removeTimeZoneIfRequired(entry.getStart(), isAllDay, removeTimezone, legacyTimeFormat));
        result.put("legacyDateTo", removeTimeZoneIfRequired(entry.getEnd(), isAllDay, removeTimezone, legacyDateFormat));
        result.put("legacyTimeTo", removeTimeZoneIfRequired(entry.getEnd(), isAllDay, removeTimezone, legacyTimeFormat));

        result.put("uri", "calendar/event/" + site.getShortName() + "/" +
                entry.getSystemName() + dateOpt);

        result.put("tags", entry.getTags());
        result.put("allday", isAllDay);
        result.put("docfolder", entry.getSharePointDocFolder());

        // Replace nulls with blank strings for the JSON
        for (String key : result.keySet())
        {
            if (result.get(key) == null)
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
