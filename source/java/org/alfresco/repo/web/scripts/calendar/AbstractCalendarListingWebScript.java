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
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.calendar.CalendarRecurrenceHelper;

/**
 * This class provides functionality common across the webscripts
 *  which list events.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public abstract class AbstractCalendarListingWebScript extends AbstractCalendarWebScript
{
   protected static final String RESULT_EVENT = "event"; 
   protected static final String RESULT_NAME  = "name"; 
   protected static final String RESULT_TITLE = "title"; 
   protected static final String RESULT_START = "start"; 
   protected static final String RESULT_END = "end"; 
   
   /**
    * Returns a Comparator for (re-)sorting events, typically used after
    *  expanding out recurring instances.
    */
   protected static Comparator<Map<String, Object>> getEventDetailsSorter()
   {
      return new Comparator<Map<String, Object>>() 
      {
         public int compare(Map<String, Object> resultA,
               Map<String, Object> resultB) 
         {
            Date startA = (Date)resultA.get(RESULT_START);
            Date startB = (Date)resultB.get(RESULT_START);

            int cmp = startA.compareTo(startB);
            if (cmp == 0)
            {
               Date endA = (Date)resultA.get(RESULT_END);
               Date endB = (Date)resultB.get(RESULT_END);
               cmp = endA.compareTo(endB);
               if (cmp == 0)
               {
                  String nameA = (String)resultA.get(RESULT_NAME);
                  String nameB = (String)resultB.get(RESULT_NAME);
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
      
      // Get it's recurring instances
      List<Date> dates = CalendarRecurrenceHelper.getRecurrencesOnOrAfter(
            entry, from, until, repeatingFirstOnly);
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

      // Always update the live entry
      updateRepeatingStartEnd(dates.get(0), duration, entryResult);
      
      // If first result only, alter title and finish
      if (repeatingFirstOnly)
      {
         entryResult.put(RESULT_TITLE, entry.getTitle() + " (Repeating)");
         return true; // Date has been changed
      }
      
      // Otherwise generate one entry per extra date
      for (int i=1; i<dates.size(); i++)
      {
         // Clone the properties
         Map<String, Object> newResult = new HashMap<String, Object>(entryResult);
         
         // Generate start and end based on this date
         updateRepeatingStartEnd(dates.get(i), duration, newResult);
         
         // Save as a new event
         allResults.add(newResult);
      }
      
      // TODO Skip ignored instances
      
      // New dates have been added
      return true;
   }
   
   private void updateRepeatingStartEnd(Date newStart, long duration, Map<String, Object> result)
   {
      Date newEnd = new Date(newStart.getTime() + duration);
      result.put(RESULT_START, newStart);
      result.put(RESULT_END, newEnd);
   }
}
