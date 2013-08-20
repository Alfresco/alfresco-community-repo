/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.admin.patch.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.calendar.CalendarModel;
import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.calendar.CalendarEntryDTO;
import org.alfresco.service.cmr.calendar.CalendarService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.log4j.Logger;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * This patch adjusts dates for Calendar Events. Share application in 3.4.x versions doesn't adjust specified in form dates with time zone offset value to. Web Script which saves a
 * new event always performs correction of the dates in accordance with time zone for 'All Day' events. Date becomes on a day before, if time for date is set to '00:00' in this
 * case. Share in 4.x does this adjustment automatically before sending request to the Web Script.<br />
 * <br />
 * See "<a href="https://issues.alfresco.com/jira/browse/MNT-8977">CMIS (OpenCMIS version) is sharing security context with other functionality in Alfresco</a>" for more details
 * 
 * @since 4.1.5
 * @author Dmitry Velichkevich
 */
public class CalendarAllDayEventDatesCorrectingPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.calendarAllDayEventDatesCorrectingPatch.result";

    private static final Logger LOGGER = Logger.getLogger(CalendarAllDayEventDatesCorrectingPatch.class);

    private int batchSize = 1000;

    private boolean batchEnabled = true;

    private SiteService siteService;

    private CalendarService calendarService;

    public CalendarAllDayEventDatesCorrectingPatch()
    {
    }

    public void setBatchSize(int batchSize)
    {
        this.batchSize = batchSize;
    }

    public void setBatchEnabled(boolean batchEnabled)
    {
        this.batchEnabled = batchEnabled;
    }

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    public void setCalendarService(CalendarService calendarService)
    {
        this.calendarService = calendarService;
    }

    @Override
    protected String applyInternal() throws Exception
    {
        int updatedEventsAmount = 0;

        NodeRef siteRoot = siteService.getSiteRoot();

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Site root: " + siteRoot);
        }

        List<ChildAssociationRef> allSites = (null != siteRoot) ? (nodeService.getChildAssocs(siteRoot)) : (null);

        if ((null != allSites) && !allSites.isEmpty())
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Starting processing of " + allSites.size() + " sites...");
            }

            PagingResults<CalendarEntry> entries = null;
            String queryId = null;

            int maxItems = (batchEnabled) ? (batchSize) : (Integer.MAX_VALUE);

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Batching info:\n\t- batching enabled: " + batchEnabled + ";\n\t-batch size: " + batchSize);
            }

            for (ChildAssociationRef siteAssoc : allSites)
            {
                SiteInfo site = siteService.getSite(siteAssoc.getChildRef());

                if (null != site)
                {
                    int skipCount = 0;

                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Processing a site: [short name: " + site.getShortName() + ", title: " + site.getTitle() + ", visibility: " + site.getVisibility() + "]");
                    }

                    do
                    {
                        PagingRequest paging = new PagingRequest(skipCount, maxItems, queryId);
                        entries = calendarService.listCalendarEntries(site.getShortName(), paging);

                        List<CalendarEntry> page = (null != entries) ? (entries.getPage()) : (null);

                        if (null != page)
                        {
                            if (LOGGER.isDebugEnabled())
                            {
                                LOGGER.debug("Processing " + page.size() + " Calendar Events...");
                            }

                            queryId = entries.getQueryExecutionId();

                            for (CalendarEntry entry : page)
                            {
                                if (isAllDay(entry))
                                {
                                    updatedEventsAmount++;

                                    if (LOGGER.isDebugEnabled())
                                    {
                                        LOGGER.debug("'All Day' Calendar event has been detected: [title: " + entry.getTitle() + ", start: " + entry.getStart() + ", end: "
                                                + entry.getEnd() + ", isOutlook: " + entry.isOutlook() + "]");
                                    }

                                    nodeService.setProperty(entry.getNodeRef(), CalendarModel.PROP_TO_DATE, adjustOldDate(entry.getEnd()));
                                    nodeService.setProperty(entry.getNodeRef(), CalendarModel.PROP_FROM_DATE, adjustOldDate(entry.getStart()));
                                }
                            }

                            skipCount += maxItems;
                        }
                    } while (batchEnabled && entries.hasMoreItems());
                }
                else
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Not site object has been detected. Skipping...");
                    }
                }
            }
        }
        else
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("No one site has been found! Skipping patch execution...");
            }
        }

        return I18NUtil.getMessage(MSG_SUCCESS, updatedEventsAmount);
    }

    /**
     * Increases (or decreases) <code>propertyId</code> date-property by the extracted (from the specified property value) time zone offset
     * 
     * @param oldDate - {@link Date} instance, which represents not adjusted date for an 'All Day' event
     * @return {@link Date} instance, which represents adjusted date-property in accordance with time zone offset
     */
    private Date adjustOldDate(Date oldDate)
    {
        Calendar result = Calendar.getInstance();
        result.setTime(oldDate);
        int offset = result.getTimeZone().getOffset(result.getTimeInMillis());
        result.add(Calendar.MILLISECOND, offset);
        return result.getTime();
    }
    
    /**
     * Does the given {@link CalendarEntry} define an all-day
     *  event?
     * An All Day Event is defined as one starting at midnight
     *  on a day, and ending at midnight.
     *  
     * For a single day event, the start and end dates should be
     *  the same, and the times for both are UTC midnight.
     * For a multi day event, the start and end times are UTC midnight,
     *  for the first and last days respectively.
     */
    public static boolean isAllDay(CalendarEntry entry)
    {
       if (entry.getStart() == null || entry.getEnd() == null)
       {
          // One or both dates is missing
          return false;
       }

       // Pre-4.0, the midnights were local time...
       Calendar startLocal = Calendar.getInstance();
       Calendar endLocal = Calendar.getInstance();
       startLocal.setTime(entry.getStart());
       endLocal.setTime(entry.getEnd());

          if (startLocal.get(Calendar.HOUR_OF_DAY) == 0 &&
         	startLocal.get(Calendar.MINUTE) == 0 &&
         	startLocal.get(Calendar.SECOND) == 0 &&
         	endLocal.get(Calendar.HOUR_OF_DAY) == 0 &&
         	endLocal.get(Calendar.MINUTE) == 0 &&
         	endLocal.get(Calendar.SECOND) == 0)
          {
             // Both at midnight, counts as all day
             return true;
          }
       
       
       // In any other case, it isn't an all-day
       return false;
    }
}
