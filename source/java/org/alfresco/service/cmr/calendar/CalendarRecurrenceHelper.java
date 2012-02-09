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
   
   @SuppressWarnings("serial")
   protected final static Map<String,Integer> d2cd = 
      Collections.unmodifiableMap(new HashMap<String, Integer>() {{
         put("SU", Calendar.SUNDAY);
         put("MO", Calendar.MONDAY);
         put("TU", Calendar.TUESDAY);
         put("WE", Calendar.WEDNESDAY);
         put("TH", Calendar.THURSDAY);
         put("FR", Calendar.FRIDAY);
         put("SA", Calendar.SATURDAY);
      }});
   
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
      return extractRecurrenceRule(entry.getRecurrenceRule());
   }
   
   /**
    * Returns the parsed calendar recurrence rule
    * WARNING - Alfresco use only. Return type will likely shift to
    *  a real object in the near future 
    */
   private static Map<String,String> extractRecurrenceRule(String recurrenceRule)
   {
      if (recurrenceRule == null)
      {
         return null;
      }
      
      // Turn the string into a useful map
      Map<String,String> params = new HashMap<String, String>();
      for (String rule : recurrenceRule.split(";"))
      {
         String[] parts = rule.split("=");
         if (parts.length != 2)
         {
            logger.warn("Invalid rule '" + rule + "' in recurrence: " + recurrenceRule);
         }
         else
         {
            params.put(parts[0], parts[1]);
         }
      }
      
      return params;
   }
   
   /**
    * Outlook does some crazy stuff, which is only just about permitted by
    *  the specification, and is hard to parse, especially for yearly events.
    * Fix these to more normal cases where possible
    */
   protected static Map<String,String> fixOutlookRecurrenceQuirks(Map<String,String> params)
   {
      if (params.containsKey("FREQ"))
      {
         // Is it really yearly?
         if ("MONTHLY".equals(params.get("FREQ")) &&
             params.get("BYMONTH") != null)
         {
             // Outlook can be "delightfully" different, and likes to generate
             //  events that recur yearly on a specific date+month as FREQ=MONTHLY
             // Detect those cases, and treat as YEARLY as per the spec
             params.put("FREQ", "YEARLY");
             
             // Outlook will sometimes do nth of the month (eg 17) instead as
             //  BYDAY={any}, BYSETPOS=n
             if (params.containsKey("BYDAY") && params.containsKey("BYSETPOS"))
             {
                 int days = params.get("BYDAY").split(",").length;
                 if (days == 7)
                 {
                     // Make it normal
                     params.put("BYMONTHDAY", params.get("BYSETPOS"));
                     params.remove("BYDAY");
                     params.remove("BYSETPOS");
                 }
             }
         }                 
      }
      return params;
   }
   
   
   /**
    * For the given Calendar Entry, return its subsequent Recurrence on or after
    *  the specified date, until the given limit. If it doesn't have any recurrences 
    *  on or after the start date (either no recurrence rules, or the last recurrence 
    *  date is before then), null will be returned. (The onOrAfter and until dates
    *  are treat as inclusive)
    * If requested, can stop after the first hit
    * @return The next recurrence on or after the given date, or null if there aren't any 
    */
   public static List<Date> getRecurrencesOnOrAfter(CalendarEntry entry, Date onOrAfter,
                                                    Date until, boolean firstOnly)
   {
      return getRecurrencesOnOrAfter(
            entry.getRecurrenceRule(), entry.getStart(), entry.getEnd(), 
            entry.getLastRecurrence(), onOrAfter, until, firstOnly);
   }
   
   /**
    * For the given Calendar Entry, return its subsequent Recurrence on or after
    *  the specified date, until the given limit. If it doesn't have any recurrences 
    *  on or after the start date (either no recurrence rules, or the last recurrence 
    *  date is before then), null will be returned. (The onOrAfter and until dates
    *  are treat as inclusive)
    * If requested, can stop after the first hit
    * @return The next recurrence on or after the given date, or null if there aren't any 
    */
   public static List<Date> getRecurrencesOnOrAfter(String recurrenceRule, Date eventStart, 
                                                    Date eventEnd, Date lastRecurrence,
                                                    Date onOrAfter, Date until, boolean firstOnly)
   {
      if (recurrenceRule == null)
      {
         // No recurrence
         return null;
      }
      
      // See if we're past the last recurrence date
      // Note - we rely on this being set for us, rather than checking the count
      if (lastRecurrence != null && lastRecurrence.before(onOrAfter))
      {
         // Recurrence has stopped by this point
         return null;
      }
      
      // Work until the earlier of the last event and the limit
      if (lastRecurrence != null)
      {
         if (until == null)
         {
            until = lastRecurrence;
         }
         else
         {
            if (lastRecurrence.before(until))
            {
               // Last recurrence is earlier, use that
               until = lastRecurrence;
            }
         }
      }
      
      // Safety limit - don't recurse for ever!
      if (lastRecurrence == null && !firstOnly && until == null)
      {
         logger.info("No end date set on the recurring event, and no end date " +
         		"specified, only fetching first instance");
         firstOnly = true;
      }
      
      // To hold our events
      List<Date> dates = new ArrayList<Date>();
      
      // Extract out the rule into its parts
      Map<String,String> params = extractRecurrenceRule(recurrenceRule);
      
      // Outlook does some crazy stuff, which is only just about
      //  permitted by the specification, and is hard to parse
      // Fix these to more normal cases where possible
      params = fixOutlookRecurrenceQuirks(params);
      
      // Fetch the frequency and interval
      if (params.containsKey("FREQ"))
      {
         String freq = params.get("FREQ");
         String intervalS = params.get("INTERVAL");
         int interval = 1;
         if (intervalS != null)
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
         
         // Start with today, and roll forward
         Calendar currentDate = Calendar.getInstance();
         currentDate.setTime(eventStart);
         
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
         logger.warn("No frequency found, possible invalid rule? " + recurrenceRule);
         return null;
      }
   }
   
   protected static void buildDailyRecurrences(Calendar currentDate, List<Date> dates, 
         Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int interval)
   {
      // Nice and easy
      while (currentDate.getTime().before(onOrAfter))
      {
         currentDate.add(Calendar.DATE, interval);
      }
      
      if (firstOnly)
      {
         // Save the first date, if valid
         if (until != null)
         {
            if (currentDate.getTime().before(until))
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
         while (! currentDate.getTime().after(until))
         {
            dates.add(currentDate.getTime());
            currentDate.add(Calendar.DATE, interval);
         }
      }
   }
   
   protected static void buildWeeklyRecurrences(Calendar currentDate, List<Date> dates, 
         Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int interval)
   {
      // Get a sorted list of the days it applies to
      List<Integer> daysOfWeek = new ArrayList<Integer>(); 
      for (String dayS : params.get("BYDAY").split(","))
      {
         Integer day = DAY_NAMES_TO_CALENDAR_DAYS.get(dayS);
         if (day == null)
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
      while (going)
      {
         // Check each day
         for (int day : daysOfWeek)
         {
            currentDate.set(Calendar.DAY_OF_WEEK, day);
            if (!valid)
            {
               if (currentDate.getTime().before(onOrAfter))
               {
                  // To early
               }
               else if (currentDate.getTime().before(origDate))
               {
                  // Too early
               }
               else
               {
                  // Now in the right range
                  valid = true;
               }
            }
            if (valid)
            {
               if (until != null)
               {
                  if (currentDate.getTime().after(until))
                  {
                     // Too late
                     going = false;
                     break;
                  }
               }
               dates.add(currentDate.getTime());
               if (firstOnly) 
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
         Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int monthInterval)
   {
      if (params.get("BYMONTHDAY") != null)
      {
         // eg the 15th of each month
         int dayOfMonth = Integer.parseInt(params.get("BYMONTHDAY"));
         if (currentDate.get(Calendar.DAY_OF_MONTH) > dayOfMonth)
         {
            // Move forward to start of the next month
            addMonthToDayOfMonth(currentDate, dayOfMonth, monthInterval);
         }
         else
         {
            // Move to that date in this month
            currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
         }
         
         // Go until in the ok range
         while (currentDate.getTime().before(onOrAfter))
         {
            addMonthToDayOfMonth(currentDate, dayOfMonth, monthInterval);
         }
         while (true)
         {
            if (until != null)
            {
               if (currentDate.getTime().after(until))
               {
                  break;
               }
            }
            
            dates.add(currentDate.getTime());
            if(firstOnly)
            {
               break;
            }
            
            addMonthToDayOfMonth(currentDate, dayOfMonth, monthInterval);
         }
      }
      else if (params.get("BYSETPOS") != null)
      {
         // eg the first Thursday of the month, or the third Saturday
         int dayOfWeek = -1;
         int instanceInMonth = 1;
         
         // There are two forms...
         if (params.containsKey("BYDAY"))
         {
            dayOfWeek = DAY_NAMES_TO_CALENDAR_DAYS.get(params.get("BYDAY"));
            instanceInMonth = Integer.parseInt(params.get("BYSETPOS"));
         }
         else
         {
            // Implies the first one in the month
            dayOfWeek = DAY_NAMES_TO_CALENDAR_DAYS.get(params.get("BYSETPOS"));
            instanceInMonth = 1;
         }
         
         // Move to the date in this month
         Date origDate = currentDate.getTime();
         toDayOfWeekInMonth(currentDate, dayOfWeek, instanceInMonth);
         
         // If the instance in this month is in the past, go
         //  forward to the point in the next month
         if (currentDate.getTime().before(origDate))
         {
            addMonthToFirstDayOfWeek(currentDate, dayOfWeek, monthInterval);
            toDayOfWeekInMonth(currentDate, dayOfWeek, instanceInMonth);
         }
         
         // Move forward to the required date
         while (currentDate.getTime().before(onOrAfter))
         {
            addMonthToFirstDayOfWeek(currentDate, dayOfWeek, monthInterval);
            toDayOfWeekInMonth(currentDate, dayOfWeek, instanceInMonth);
         }
         // Roll on until we get valid matches
         while (true)
         {
            if (until != null)
            {
               if (currentDate.getTime().after(until))
               {
                  break;
               }
            }
            
            dates.add(currentDate.getTime());
            if (firstOnly)
            {
               break;
            }
            
            addMonthToFirstDayOfWeek(currentDate, dayOfWeek, monthInterval);
            toDayOfWeekInMonth(currentDate, dayOfWeek, instanceInMonth);
         }
      }
   }
   
   protected static void buildYearlyRecurrences(Calendar currentDate, List<Date> dates, 
         Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int interval)
   {
      int realMonth = Integer.parseInt(params.get("BYMONTH"));
      int month = realMonth - 1; // Java months count from zero
      
      if (params.get("BYMONTHDAY") != null)
      {
         // eg the 2nd of March every year
         int dayOfMonth = Integer.parseInt(params.get("BYMONTHDAY"));
         if (currentDate.get(Calendar.MONTH) == month &&
            currentDate.get(Calendar.DAY_OF_MONTH) == dayOfMonth)
         {
            // Correct start time
         }
         else if (currentDate.get(Calendar.MONTH) < month ||
                  (currentDate.get(Calendar.MONTH) == month &&
                   currentDate.get(Calendar.DAY_OF_MONTH) < dayOfMonth))
         {
            // The current date is before the requested date this year
            // Move forward to it in this year
            currentDate.set(Calendar.MONTH, month);
            currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
         }
         else
         {
            // The current date is after the date this year, move to next year
             currentDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR) + interval);
             currentDate.set(Calendar.MONTH, month);
             currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
         }
         
         while (currentDate.getTime().before(onOrAfter))
         {
            currentDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR) + interval);
            currentDate.set(Calendar.MONTH, month);
            currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
         }
         while (true)
         {
            if (until != null)
            {
               if (currentDate.getTime().after(until))
               {
                  break;
               }
            }
            
            dates.add(currentDate.getTime());
            if (firstOnly)
            {
               break;
            }
            
            currentDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR) + interval);
            currentDate.set(Calendar.MONTH, month);
            currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
         }
      }
      else
      {
          // eg the third Tuesday in February every year
          int dayOfWeek = -1;
          int instanceInMonth = 1;
          
          // There are two forms...
          if (params.containsKey("BYDAY"))
          {
             dayOfWeek = DAY_NAMES_TO_CALENDAR_DAYS.get(params.get("BYDAY"));
             instanceInMonth = Integer.parseInt(params.get("BYSETPOS"));
          }
          else
          {
             // Implies the first one in the month
             dayOfWeek = DAY_NAMES_TO_CALENDAR_DAYS.get(params.get("BYSETPOS"));
             instanceInMonth = 1;
          }
          
          
          // Find when it is this year 
          Date origDate = currentDate.getTime();
          currentDate.set(Calendar.MONTH, month);
          currentDate.set(Calendar.DAY_OF_MONTH, 1);
          toDayOfWeekInMonth(currentDate, dayOfWeek, instanceInMonth);
          Date thisYear = currentDate.getTime();
          currentDate.setTime(origDate);
          
          // Have we missed it for the year? If so, go to next year
          if (currentDate.getTime().after(thisYear))
          {
              currentDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR) + interval);
              currentDate.set(Calendar.MONTH, month);
              currentDate.set(Calendar.DAY_OF_MONTH, 1);
              toDayOfWeekInMonth(currentDate, dayOfWeek, instanceInMonth);
          }
          else
          {
              // Otherwise move to it
              currentDate.setTime(thisYear);
          }
          
          
          // Move forward to the required date
          while (currentDate.getTime().before(onOrAfter))
          {
              currentDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR) + interval);
              currentDate.set(Calendar.MONTH, month);
              currentDate.set(Calendar.DAY_OF_MONTH, 1);
              toDayOfWeekInMonth(currentDate, dayOfWeek, instanceInMonth);
          }
          
          // Roll on until we get valid matches
          while (true)
          {
             if (until != null)
             {
                if (currentDate.getTime().after(until))
                {
                   break;
                }
             }
             
             dates.add(currentDate.getTime());
             if (firstOnly)
             {
                break;
             }
             
             currentDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR) + interval);
             currentDate.set(Calendar.MONTH, month);
             currentDate.set(Calendar.DAY_OF_MONTH, 1);
             toDayOfWeekInMonth(currentDate, dayOfWeek, instanceInMonth);
          }
       }
   }
   
   private static void addMonthToDayOfMonth(Calendar c, int dayOfMonth, int monthInterval)
   {
      for (int i=0; i<monthInterval; i++)
      {
         // Set it to the 1st
         c.set(Calendar.DAY_OF_MONTH, 1);
         // Add 33 days, will be on the 2nd-6th
         c.add(Calendar.DATE, 33);
         // Set to the requred day in the month
         c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
      }
   }
   
   private static void addMonthToFirstDayOfWeek(Calendar c, int dayOfWeek, int monthInterval)
   {
      // Go forward to the 1st of next month
      addMonthToDayOfMonth(c, 1, monthInterval);
      
      // Set the day of the week
      toDayOfWeekInMonth(c, dayOfWeek, 1);
   }
   
   /**
    * Takes you to eg the 2nd Thursday in the month, which may
    *  involve going back before the current date  
    */
   private static void toDayOfWeekInMonth(Calendar c, int dayOfWeek, int weekInMonth)
   {
      // First up, move to the start of the month
      c.set(Calendar.DATE, 1);
      
      // Now, move to the 1st instance of the day of the week
      Date t = c.getTime();
      c.set(Calendar.DAY_OF_WEEK, dayOfWeek);
      // If we went back, go forward a week
      if (c.getTime().before(t))
      {
         c.add(Calendar.DATE, 7);
      }
      
      // Now move to the required week
      if (weekInMonth > 1)
      {
         c.add(Calendar.DATE, 7 * (weekInMonth-1));
      }
   }
}
