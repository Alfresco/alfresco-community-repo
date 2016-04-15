/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.service.cmr.calendar;

import java.util.Date;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.NotAuditable;

/**
 * The Calendar service.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public interface CalendarService 
{
   /**
    * Stores a new {@link CalendarEntry} into the given site.
    * The concrete class {@link CalendarEntryDTO} can be used
    *  to create a {@link CalendarEntry} instance for this.
    *  
    * @return The newly created CalendarEntry
    */
   @NotAuditable
   CalendarEntry createCalendarEntry(String siteShortName, CalendarEntry entry);
   
   /**
    * Updates an existing {@link CalendarEntry} in the repository.
    *  
    * @return The updated CalendarEntry
    */
   @NotAuditable
   CalendarEntry updateCalendarEntry(CalendarEntry entry);
   
   /**
    * Deletes an existing {@link CalendarEntry} from the repository
    */
   @NotAuditable
   void deleteCalendarEntry(CalendarEntry entry);
   
   /**
    * Retrieves an existing {@link CalendarEntry} from the repository
    */
   @NotAuditable
   CalendarEntry getCalendarEntry(String siteShortName, String entryName);

   /**
    * Retrieves all {@link CalendarEntry} instances in the repository
    *  for the given site.
    */
   @NotAuditable
   PagingResults<CalendarEntry> listCalendarEntries(String siteShortName, PagingRequest paging);

   /**
    * Retrieves all {@link CalendarEntry} instances in the repository
    *  for the given sites.
    */
   @NotAuditable
   PagingResults<CalendarEntry> listCalendarEntries(String[] siteShortNames, PagingRequest paging);

   /**
    * Retrieves all {@link CalendarEntry} instances in the repository
    *  for the given sites, between the specified date range.
    */
   @NotAuditable
   PagingResults<CalendarEntry> listCalendarEntries(String[] siteShortNames, Date from, Date to, PagingRequest paging);

   /**
    * Retrieves all Outlook based {@link CalendarEntry} instances in the repository
    *  for the given site, optionally filtered by the Outlook Event UID.
    */
   @NotAuditable
   PagingResults<CalendarEntry> listOutlookCalendarEntries(String siteShortName, String outlookUID, PagingRequest paging);
}
