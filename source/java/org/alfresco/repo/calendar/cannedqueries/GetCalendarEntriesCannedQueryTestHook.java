package org.alfresco.repo.calendar.cannedqueries;

import java.util.List;

/**
 * This class provides a way for a unit test to check up on what
 *  the {@link GetCalendarEntriesCannedQuery} does
 * 
 * @author Nick Burch
 * @since 4.0
 */
public interface GetCalendarEntriesCannedQueryTestHook
{
   void notifyComplete(List<CalendarEntity> full, List<CalendarEntity> filtered);
}
