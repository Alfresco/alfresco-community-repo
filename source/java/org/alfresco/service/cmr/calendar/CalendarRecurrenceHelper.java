/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.service.cmr.calendar;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * This class provides helper functions for when working 
 *  with recurring {@link CalendarEntry} instances.
 * It provides support for working with key parts of the
 *  Outlook/SharePoint recurrence rules 
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class CalendarRecurrenceHelper 
{
   private static Log logger = LogFactory.getLog(CalendarRecurrenceHelper.class);
   
   private static Map<String,Integer> d2cd;
   static {
      d2cd = new HashMap<String, Integer>();
      d2cd.put("SU", Calendar.SUNDAY);
      d2cd.put("MO", Calendar.MONDAY);
      d2cd.put("TU", Calendar.TUESDAY);
      d2cd.put("WE", Calendar.WEDNESDAY);
      d2cd.put("TH", Calendar.THURSDAY);
      d2cd.put("FR", Calendar.FRIDAY);
      d2cd.put("SA", Calendar.SATURDAY);
   }
   /**
    * The lookup from the day strings to Calendar Day entries
    */
   public static final Map<String,Integer> DAY_NAMES_TO_CALENDAR_DAYS =
      Collections.unmodifiableMap(d2cd);
   
   /**
    * Returns a lookup from recurrence rule days of the week, to
    *  the proper days of the week in the specified locale
    */
   public static Map<String,String> buildLocalRecurrenceDaysOfTheWeek(Locale locale)
   {
      // Get our days of the week, in the current locale
      DateFormatSymbols dates = new DateFormatSymbols(I18NUtil.getLocale());
      String[] weekdays = dates.getWeekdays();
      
      // And map them based on the outlook two letter codes
      Map<String,String> days = new HashMap<String, String>();
      for(Map.Entry<String,Integer> e : DAY_NAMES_TO_CALENDAR_DAYS.entrySet())
      {
         days.put(e.getKey(), weekdays[e.getValue()]);
      }
      return days;
   }
   
   /**
    * Returns the parsed calendar recurrence rule
    * WARNING - Alfresco use only. Return type will likely shift to
    *  a real object in the near future 
    */
   public static Map<String,String> extractRecurrenceRule(CalendarEntry entry)
   {
      String recurrence = entry.getRecurrenceRule();
      if(recurrence == null)
      {
         return null;
      }
      
      // Turn the string into a useful map
      Map<String,String> params = new HashMap<String, String>();
      for(String rule : recurrence.split(";"))
      {
         String[] parts = rule.split("=");
         if(parts.length != 2)
         {
            logger.warn("Invalid rule '" + rule + "' in recurrence: " + recurrence);
         }
         else
         {
            params.put(parts[0], parts[1]);
         }
      }
      
      return params;
   }
   
   /**
    * For the given Calendar Entry, return its subsequent Recurrence on or after
    *  the specified date, until the given limit. If it doesn't have any recurrences 
    *  on or after the start date (either no recurrence rules, or the last recurrence 
    *  date is before then), null will be returned.
    * If requested, can stop after the first hit
    * @return The next recurrence on or after the given date, or null if there aren't any 
    */
   public static List<Date> getRecurrencesOnOrAfter(CalendarEntry entry, Date onOrAfter,
                                                    Date until, boolean firstOnly)
   {
      String recurrence = entry.getRecurrenceRule(); 
      if(recurrence == null)
      {
         // No recurrence
         return null;
      }
      
      // See if we're past the last recurrence date
      // Note - we rely on this being set for us, rather than checking the count
      Date lastRecurrence = entry.getLastRecurrence();
      if(lastRecurrence != null && lastRecurrence.before(onOrAfter))
      {
         // Recurrence has stopped by this point
         return null;
      }
      
      // Work until the earlier of the last event and the limit
      if(lastRecurrence != null)
      {
         if(until == null)
         {
            until = lastRecurrence;
         }
         else
         {
            if(lastRecurrence.before(until))
            {
               // Last recurrence is earlier, use that
               until = lastRecurrence;
            }
         }
      }
      
      // Safety limit - don't recurse for ever!
      if(lastRecurrence == null && !firstOnly && until == null)
      {
         logger.info("No end date set on the recurring event, and no end date " +
         		"specified, only fetching first instance");
         firstOnly = true;
      }
      
      // To hold our events
      List<Date> dates = new ArrayList<Date>();
      
      // Handle the different frequencies
      Map<String,String> params = extractRecurrenceRule(entry);
      if(params.containsKey("FREQ"))
      {
         String freq = params.get("FREQ");
         String intervalS = params.get("INTERVAL");
         int interval = 1;
         if(intervalS == null)
         {
            try
            {
               interval = Integer.parseInt(intervalS);
            }
            catch(NumberFormatException e)
            {
               logger.warn("Invalid interval " + intervalS);
            }
         }
         
         Calendar currentDate = Calendar.getInstance();
         currentDate.setTime(entry.getStart());
         
         if ("DAILY".equals(freq))
         {
            buildDailyRecurrences(currentDate, dates, params, onOrAfter, until, firstOnly, interval);
         }
         else if ("WEEKLY".equals(freq))
         {
            buildWeeklyRecurrences(currentDate, dates, params, onOrAfter, until, firstOnly, interval);
         }
         else if ("MONTHLY".equals(freq))
         {
            buildMonthlyRecurrences(currentDate, dates, params, onOrAfter, until, firstOnly, interval);
         }
         else if ("YEARLY".equals(freq))
         {
            buildYearlyRecurrences(currentDate, dates, params, onOrAfter, until, firstOnly, interval);
         }
         else
         {
            logger.warn("Unsupported recurrence frequency " + freq);
         }
         
         // Return what we've got
         return dates;
      }
      else
      {
         logger.warn("No frequency found, possible invalid rule? " + recurrence);
         return null;
      }
   }
   
   protected static void buildDailyRecurrences(Calendar currentDate, List<Date> dates, 
         Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int interval)
   {
      // Nice and easy
      while(currentDate.getTime().before(onOrAfter))
      {
         currentDate.add(Calendar.DATE, 1);
      }
      
      if(firstOnly)
      {
         // Save the first date, if valid
         if(until != null)
         {
            if(currentDate.getTime().before(until))
            {
               dates.add(currentDate.getTime());
            }
         }
         else
         {
            dates.add(currentDate.getTime());
         }
      }
      else
      {
         // Run until the end
         while(currentDate.getTime().before(until))
         {
            dates.add(currentDate.getTime());
            currentDate.add(Calendar.DATE, 1);
         }
      }
   }
   
   protected static void buildWeeklyRecurrences(Calendar currentDate, List<Date> dates, 
         Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int interval)
   {
      // Get a sorted list of the days it applies to
      List<Integer> daysOfWeek = new ArrayList<Integer>(); 
      for(String dayS : params.get("BYDAY").split(","))
      {
         Integer day = DAY_NAMES_TO_CALENDAR_DAYS.get(dayS);
         if(day == null)
         {
            logger.warn("Invalid day " + dayS);
         }
         else
         {
            daysOfWeek.add(day);
         }
      }
      Collections.sort(daysOfWeek);
      
      // Wind forward
      boolean going = true;
      boolean valid = false;
      Date origDate = currentDate.getTime();
      while(going)
      {
         // Check each day
         for(int day : daysOfWeek)
         {
            currentDate.set(Calendar.DAY_OF_WEEK, day);
            if(!valid)
            {
               if(currentDate.getTime().before(onOrAfter))
               {
                  // To early
               }
               else if(currentDate.getTime().before(origDate))
               {
                  // Too early
               }
               else
               {
                  // Now in the right range
                  valid = true;
               }
            }
            if(valid)
            {
               if(until != null)
               {
                  if(currentDate.getTime().after(until))
                  {
                     // Too late
                     going = false;
                     break;
                  }
               }
               dates.add(currentDate.getTime());
               if(firstOnly) 
               {
                  going = false;
                  break;
               }
            }
         }
         
         // Wind on to the next week
         currentDate.set(Calendar.DAY_OF_WEEK, daysOfWeek.get(0));
         currentDate.add(Calendar.DATE, interval*7);
      }
   }
   
   protected static void buildMonthlyRecurrences(Calendar currentDate, List<Date> dates, 
         Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int interval)
   {
      if (params.get("BYMONTHDAY") != null)
      {
         // eg the 15th of each month
         int dayOfMonth = Integer.parseInt(params.get("BYMONTHDAY"));
         if(currentDate.get(Calendar.DAY_OF_MONTH) > dayOfMonth)
         {
            // Move forward to start of the next month
            addMonthToDayOfMonth(currentDate, dayOfMonth);
         }
         else
         {
            // Move to that date in this month
            currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
         }
         
         // Go until in the ok range
         while(currentDate.getTime().before(onOrAfter))
         {
            addMonthToDayOfMonth(currentDate, dayOfMonth);
         }
         while(true)
         {
            if(until != null)
            {
               if(currentDate.getTime().after(until))
               {
                  break;
               }
            }
            
            dates.add(currentDate.getTime());
            if(firstOnly)
            {
               break;
            }
            
            addMonthToDayOfMonth(currentDate, dayOfMonth);
         }
      }
      else if (params.get("BYSETPOS") != null)
      {
         // eg the first Thursday of the month
         int dayOfWeek = DAY_NAMES_TO_CALENDAR_DAYS.get(params.get("BYSETPOS"));
         if(currentDate.get(Calendar.DAY_OF_MONTH) > 8)
         {
            // Move to start, in next month
            addMonthToFirstDayOfWeek(currentDate, dayOfWeek);
         }
         else if(currentDate.get(Calendar.DAY_OF_WEEK) != dayOfWeek)
         {
            // Move forward to start
            Date t = currentDate.getTime();
            currentDate.set(Calendar.DAY_OF_WEEK, dayOfWeek);
            if(currentDate.getTime().before(t))
            {
               currentDate.add(Calendar.DATE, 7);
            }
         }
         
         while(currentDate.getTime().before(onOrAfter))
         {
            addMonthToFirstDayOfWeek(currentDate, dayOfWeek);
         }
         while(true)
         {
            if(until != null)
            {
               if(currentDate.getTime().after(until))
               {
                  break;
               }
            }
            
            dates.add(currentDate.getTime());
            if(firstOnly)
            {
               break;
            }
            
            addMonthToFirstDayOfWeek(currentDate, dayOfWeek);
         }
      }
   }
   
   protected static void buildYearlyRecurrences(Calendar currentDate, List<Date> dates, 
         Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int interval)
   {
      int month = Integer.parseInt(params.get("BYMONTH"));
      
      if (params.get("BYMONTHDAY") != null)
      {
         // eg the 2nd of March every year
         int dayOfMonth = Integer.parseInt(params.get("BYMONTHDAY"));
         if(currentDate.get(Calendar.MONTH) == month &&
            currentDate.get(Calendar.DAY_OF_MONTH) == dayOfMonth)
         {
            // Correct start time
         }
         else
         {
            currentDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR) + 1);
            currentDate.set(Calendar.MONTH, month);
            currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
         }
         
         while(currentDate.before(onOrAfter))
         {
            currentDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR) + 1);
            currentDate.set(Calendar.MONTH, month);
            currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
         }
         while(true)
         {
            if(until != null)
            {
               if(currentDate.after(until))
               {
                  break;
               }
            }
            
            dates.add(currentDate.getTime());
            if(firstOnly)
            {
               break;
            }
            
            currentDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR) + 1);
            currentDate.set(Calendar.MONTH, month);
            currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
         }
      }
      else
      {
         // eg the first Tuesday in February every year
         int dayOfWeek = DAY_NAMES_TO_CALENDAR_DAYS.get(params.get("BYSETPOS"));
         // TODO
      }
   }
   
   
   private static void addMonthToDayOfMonth(Calendar c, int dayOfMonth)
   {
      // Set it to the 1st
      c.set(Calendar.DAY_OF_MONTH, 1);
      // Add 33 days, will be on the 2nd-6th
      c.add(Calendar.DATE, 33);
      // Set to the requred day in the month
      c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
   }
   private static void addMonthToFirstDayOfWeek(Calendar c, int dayOfWeek)
   {
      // Go forward to the 1st of next month
      addMonthToDayOfMonth(c, 1);
      
      // Set the day of the week
      Date t = c.getTime();
      c.set(Calendar.DAY_OF_WEEK, dayOfWeek);
      // If we went back, go forward a week
      if(c.getTime().before(t))
      {
         c.add(Calendar.DATE, 7);
      }
   }
}
