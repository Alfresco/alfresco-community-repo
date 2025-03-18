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
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.alfresco.repo.calendar.CalendarModel;
import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.calendar.CalendarRecurrenceHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;

/**
 * This class provides functionality common across the webscripts which list events.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public abstract class AbstractCalendarListingWebScript extends AbstractCalendarWebScript
{
    protected static final String RESULT_EVENT = "event";
    protected static final String RESULT_NAME = "name";
    protected static final String RESULT_TITLE = "title";
    protected static final String RESULT_START = "start";
    protected static final String RESULT_END = "end";

    /**
     * Returns a Comparator for (re-)sorting events, typically used after expanding out recurring instances.
     */
    protected static Comparator<Map<String, Object>> getEventDetailsSorter()
    {
        return new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> resultA,
                    Map<String, Object> resultB)
            {
                DateTimeFormatter fmtNoTz = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
                DateTimeFormatter fmtTz = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");

                String startA = (String) resultA.get(RESULT_START);
                String startB = (String) resultB.get(RESULT_START);

                startA = startA.replace("Z", "+00:00");
                startB = startB.replace("Z", "+00:00");

                // check and parse iso8601 date without time zone (All day events are stripped of time zone)
                DateTime sa = startA.length() > 23 ? fmtTz.parseDateTime(startA) : fmtNoTz.parseDateTime(startA);
                DateTime sb = startB.length() > 23 ? fmtTz.parseDateTime(startB) : fmtNoTz.parseDateTime(startB);

                int cmp = sa.compareTo(sb);
                if (cmp == 0)
                {
                    String endA = (String) resultA.get(RESULT_END);
                    String endB = (String) resultB.get(RESULT_END);

                    DateTime ea = endA.length() > 23 ? fmtTz.parseDateTime(endA) : fmtNoTz.parseDateTime(endA);
                    DateTime eb = endB.length() > 23 ? fmtTz.parseDateTime(endB) : fmtNoTz.parseDateTime(endB);

                    cmp = ea.compareTo(eb);
                    if (cmp == 0)
                    {
                        String nameA = (String) resultA.get(RESULT_NAME);
                        String nameB = (String) resultB.get(RESULT_NAME);
                        return nameA.compareTo(nameB);
                    }
                    return cmp;
                }
                return cmp;
            }
        };
    }

    /**
     * Do what's needed for recurring events.
     * 
     * @return If dates have been tweaked, and a sort may be required
     */
    protected boolean handleRecurring(CalendarEntry entry, Map<String, Object> entryResult,
            List<Map<String, Object>> allResults, Date from, Date until, boolean repeatingFirstOnly)
    {
        if (entry.getRecurrenceRule() == null)
        {
            // Nothing to do
            return false;
        }

        // If no date is given, start looking for occurrences from the event itself
        if (from == null)
        {
            from = entry.getStart();
        }

        // Do we need to limit ourselves?
        // Should we limit ourselves?
        if (!repeatingFirstOnly)
        {
            if (until == null)
            {
                // If no end date was given, only allow repeating instances
                // for next 60 days, to keep the list sane
                // (It's normally only used for a month view anyway)
                Calendar c = Calendar.getInstance();
                c.setTime(from);
                c.add(Calendar.DATE, 60);
                until = c.getTime();
            }
        }

        // How long is it?
        long duration = entry.getEnd().getTime() - entry.getStart().getTime();

        // if some instances were deleted from series ignore them
        Set<QName> childNodeTypeQNames = new HashSet<QName>();
        childNodeTypeQNames.add(CalendarModel.TYPE_IGNORE_EVENT);
        List<ChildAssociationRef> ignoreEventList = nodeService.getChildAssocs(entry.getNodeRef(), childNodeTypeQNames);
        Set<Date> ignoredDates = new HashSet<Date>();
        for (ChildAssociationRef ignoreEvent : ignoreEventList)
        {
            NodeRef nodeRef = ignoreEvent.getChildRef();
            Date ignoredDate = (Date) nodeService.getProperty(nodeRef, CalendarModel.PROP_IGNORE_EVENT_DATE);
            ignoredDates.add(ignoredDate);
        }

        // Get it's recurring instances
        List<Date> dates = CalendarRecurrenceHelper.getRecurrencesOnOrAfter(
                entry, from, until, repeatingFirstOnly, ignoredDates);
        if (dates == null)
        {
            dates = new ArrayList<Date>();
        }

        // Add on the original event time itself if needed
        if (entry.getStart().getTime() >= from.getTime())
        {
            if (dates.size() == 0 || dates.get(0).getTime() != entry.getStart().getTime())
            {
                // Original event is after the start time, and not on the recurring list
                dates.add(0, entry.getStart());
            }
        }

        // If we got no dates, then no recurrences in the period so zap
        if (dates.size() == 0)
        {
            allResults.remove(entryResult);
            return false; // Remains sorted despite delete
        }

        // if some instances were updated
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        childNodeTypeQNames = new HashSet<QName>();
        childNodeTypeQNames.add(CalendarModel.TYPE_UPDATED_EVENT);
        List<ChildAssociationRef> updatedEventList = nodeService.getChildAssocs(entry.getNodeRef(), childNodeTypeQNames);
        Map<String, Object> updatedDates = new HashMap<String, Object>();
        for (ChildAssociationRef updatedEvent : updatedEventList)
        {
            NodeRef nodeRef = updatedEvent.getChildRef();
            Date updatedDate = (Date) nodeService.getProperty(nodeRef, CalendarModel.PROP_UPDATED_EVENT_DATE);
            Date newStart = (Date) nodeService.getProperty(nodeRef, CalendarModel.PROP_UPDATED_START);
            Date newEnd = (Date) nodeService.getProperty(nodeRef, CalendarModel.PROP_UPDATED_END);
            String newWhere = (String) nodeService.getProperty(nodeRef, CalendarModel.PROP_UPDATED_WHERE);
            String newWhat = (String) nodeService.getProperty(nodeRef, CalendarModel.PROP_UPDATED_WHAT);
            updatedDates.put(fmt.format(updatedDate), new Date[]{newStart, newEnd});
            updatedDates.put(fmt.format(updatedDate).toString() + "where", newWhere);
            updatedDates.put(fmt.format(updatedDate).toString() + "what", newWhat);
        }

        // first occurrence can be edited as separate event
        Date liveEntry = dates.get(0);

        // If first result only, alter title and finish
        if (repeatingFirstOnly)
        {
            entryResult.put(RESULT_TITLE, entry.getTitle() + " (Repeating)");

            updateRepeating(entry, updatedDates, entryResult, duration, fmt, liveEntry);
            return true; // Date has been changed
        }
        else
        {
            // Otherwise generate one entry per extra date
            for (int i = 1; i < dates.size(); i++)
            {
                // Clone the properties
                Map<String, Object> newResult = new HashMap<String, Object>(entryResult);

                Date extra = dates.get(i);

                updateRepeating(entry, updatedDates, newResult, duration, fmt, extra);

                // Save as a new event
                allResults.add(newResult);
            }

            updateRepeating(entry, updatedDates, entryResult, duration, fmt, liveEntry);
        }

        // TODO Skip ignored instances

        // New dates have been added
        return true;
    }

    private void updateRepeatingStartEnd(Date newStart, long duration, Map<String, Object> result)
    {
        Date newEnd = new Date(newStart.getTime() + duration);
        result.put(RESULT_START, ISO8601DateFormat.format(newStart));
        result.put(RESULT_END, ISO8601DateFormat.format(newEnd));
        String legacyDateFormat = "yyyy-MM-dd";
        SimpleDateFormat ldf = new SimpleDateFormat(legacyDateFormat);
        String legacyTimeFormat = "HH:mm";
        SimpleDateFormat ltf = new SimpleDateFormat(legacyTimeFormat);
        result.put("legacyDateFrom", ldf.format(newStart));
        result.put("legacyTimeFrom", ltf.format(newStart));
        result.put("legacyDateTo", ldf.format(newEnd));
        result.put("legacyTimeTo", ltf.format(newEnd));
    }

    private void updateRepeating(CalendarEntry entry, Map<String, Object> updatedDates, Map<String, Object> entryResult, long duration, SimpleDateFormat fmt, Date date)
    {
        if (updatedDates.keySet().contains(fmt.format(date)))
        {
            // there is day that was edited
            Date[] newValues = (Date[]) updatedDates.get(fmt.format(date));
            long newDuration = newValues[1].getTime() - newValues[0].getTime();

            entryResult.put(RESULT_TITLE, (String) updatedDates.get(fmt.format(date).toString() + "what"));
            entryResult.put("where", (String) updatedDates.get(fmt.format(date).toString() + "where"));

            updateRepeatingStartEnd(newValues[0], newDuration, entryResult);
        }
        else
        {
            // Update entry
            updateRepeatingStartEnd(date, duration, entryResult);
        }
    }
}
