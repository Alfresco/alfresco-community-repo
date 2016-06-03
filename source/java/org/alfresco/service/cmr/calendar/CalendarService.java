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
