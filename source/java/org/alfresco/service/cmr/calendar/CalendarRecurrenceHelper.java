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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;

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
   

   @SuppressWarnings("serial")
   protected final static Map<Integer,String> WEEK_NUMBER_TO_WEEK_NAME = 
      Collections.unmodifiableMap(new HashMap<Integer, String>() {{
         put(1, "first");
         put(2, "second");
         put(3, "third");
         put(4, "fourth");
         put(-1, "last");
      }});
   
   
   /**
    * The lookup from the week in month number to week 
   * in month name in the specified locale
    */
   public static Map<Integer, String> buildLocalRecurrenceWeekNames(Locale locale){
      return WEEK_NUMBER_TO_WEEK_NAME;
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
    * 
    * TODO: This method modifies the input, returning the Map perhaps implies a copy is returned.
    *       Decide whether this should be of return type 'void', or make a defensive copy before modification.
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

             // ALF-18928: The interval is likely present, we should change it from months to years
             // See org.alfresco.module.vti.web.ws.AbstractMeetingFromICalEndpoint.getLastMeeting()
             String intervalString = params.get("INTERVAL");
             if(intervalString != null)
             {
                 int interval = Integer.parseInt(intervalString);
                 params.put("INTERVAL", String.valueOf(interval/12));
             }

             // Outlook will sometimes do nth of the month (eg 17) instead as
             //  BYDAY={any}, BYSETPOS=n
             if (params.containsKey("BYDAY") && params.containsKey("BYSETPOS"))
             {
                 int days = params.get("BYDAY").split(",").length;

                 if (days == 7 && !"-1".equals(params.get("BYSETPOS")))
                 {
                     // Make it normal
                     params.put("BYMONTHDAY", params.get("BYSETPOS"));
                     params.remove("BYDAY");
                     params.remove("BYSETPOS");
                 }
                 else
                 {
                     buildParams(params, days);
                 }
             }
         }
         // MNT-10006 fix. Added the support for recurrences rule "WEEKDAY", "WEEKEND DAY"
          else if ("MONTHLY".equals(params.get("FREQ")) &&
                 (params.containsKey("BYDAY") && params.containsKey("BYSETPOS")))
         {
                 int days = params.get("BYDAY").split(",").length;
                 buildParams(params, days);
         }
      }
      return params;
   }

    /**
     * Builds correct params for recurrences 'weekday', 'weekend day'
     * @param params the recurrence rule
     * @param days the appropriate amount of days for recurrences 'day', 'weekday' weekend day'
     */
    private static void buildParams(Map<String, String> params, int days)
    {
        // building recurrence rule for recurrence pattern 'day'
        if (days == 7)
        {
            // Make it normal
            params.put("BYANYDAY", params.get("BYSETPOS"));
            params.put("DAY", params.get("BYDAY"));
            params.remove("BYDAY");
            params.remove("BYSETPOS");
        }
        // building recurrence rule for recurrence pattern 'weekday'
        else if (days == 5)
        {
            params.put("BYWEEKDAY", params.get("BYSETPOS"));
            params.put("WEEKDAYS", params.get("BYDAY"));
            params.remove("BYDAY");
            params.remove("BYSETPOS");
        }
        // building recurrence rule for recurrence pattern 'weekend day'
        else if (days == 2)
        {
            params.put("BYWEEKENDDAY", params.get("BYSETPOS"));
            params.put("WEEKENDS", params.get("BYDAY"));
            params.remove("BYDAY");
            params.remove("BYSETPOS");
        }
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
                                                    Date until, boolean firstOnly, Set<Date> ignoredDates)
   {
      return getRecurrencesOnOrAfter(
            entry.getRecurrenceRule(), entry.getStart(), entry.getEnd(), 
            entry.getLastRecurrence(), onOrAfter, until, firstOnly, ignoredDates);
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
                                                    Date onOrAfter, Date until, boolean firstOnly,
                                                    Set<Date> ignoredDates)
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
         long duration = eventEnd.getTime() - eventStart.getTime();
         
         if ("DAILY".equals(freq))
         {
            buildDailyRecurrences(currentDate, duration, dates, params, onOrAfter, until, firstOnly, interval);
         }
         else if ("WEEKLY".equals(freq))
         {
            buildWeeklyRecurrences(currentDate, duration, dates, params, onOrAfter, until, firstOnly, interval);
         }
         else if ("MONTHLY".equals(freq))
         {
            buildMonthlyRecurrences(currentDate, duration, dates, params, onOrAfter, until, firstOnly, interval);
         }
         else if ("YEARLY".equals(freq))
         {
            buildYearlyRecurrences(currentDate, duration, dates, params, onOrAfter, until, firstOnly, interval);
         }
         else
         {
            logger.warn("Unsupported recurrence frequency " + freq);
         }

         SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
         for (Date ignoredDate : ignoredDates)
         {
             Iterator<Date> i = dates.iterator();
             while (i.hasNext())
             {
                 Date date = i.next();
                 if (fmt.format(date).equals(fmt.format(ignoredDate)))
                 {
                     // occurrence is on the same day to ignore
                     i.remove();
                 }
             }
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
   
   protected static void buildDailyRecurrences(Calendar currentDate, long duration, List<Date> dates, 
         Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int interval)
   {
       if (onOrAfter.before(currentDate.getTime()))
       {
           onOrAfter = currentDate.getTime();
       }
       
      // Nice and easy
      while (currentDate.getTime().before(onOrAfter))
      {
         currentDate.add(Calendar.DATE, interval);
      }
      
      currentDate.add(Calendar.DATE, -1 * interval * 2);
      Date currentEventEnd = new Date(currentDate.getTimeInMillis() + duration);
      while(currentEventEnd.before(onOrAfter))
      {
          currentDate.add(Calendar.DATE, interval);
          currentEventEnd = new Date(currentDate.getTimeInMillis() + duration);
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
   
   protected static void buildWeeklyRecurrences(Calendar currentDate, long duration, List<Date> dates, 
         Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int interval)
   {
       if (onOrAfter.before(currentDate.getTime()))
       {
           onOrAfter = currentDate.getTime();
       }
       
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
      currentDate.add(Calendar.DATE, -1 * interval * 7 * 2);
      while (going)
      {
         // Check each day
         for (int day : daysOfWeek)
         {
            currentDate.set(Calendar.DAY_OF_WEEK, day);
            if (!valid)
            {
               Date currentEventEnd = new Date(currentDate.getTimeInMillis() + duration);
               if (currentEventEnd.before(onOrAfter))
               {
                  // To early
               }
               else if (currentEventEnd.before(origDate))
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
   
   protected static void buildMonthlyRecurrences(Calendar currentDate, long duration, List<Date> dates, 
         Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int monthInterval)
   {
       if (onOrAfter.before(currentDate.getTime()))
       {
           onOrAfter = currentDate.getTime();
       }
       
      if (params.get("BYMONTHDAY") != null)
      {
          currentDate.add(Calendar.MONTH, -1 * monthInterval * 2);
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
            if (currentDate.get(Calendar.DAY_OF_MONTH) != dayOfMonth)
            {
                currentDate.add(Calendar.DAY_OF_MONTH, -1);
            }
         }
         
         
         Date currentEventEnd = new Date(currentDate.getTimeInMillis() + duration);
         // Go until in the ok range
         while (currentEventEnd.before(onOrAfter))
         {
            addMonthToDayOfMonth(currentDate, dayOfMonth, monthInterval);
            currentEventEnd = new Date(currentDate.getTimeInMillis() + duration);
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
         
         currentDate.add(Calendar.MONTH, -1 * monthInterval * 2);
         toDayOfWeekInMonth(currentDate, dayOfWeek, instanceInMonth);
         Date currentEventEnd = new Date(currentDate.getTimeInMillis() + duration);
         
         // If the instance in this month is in the past, go
         //  forward to the point in the next month
         if (currentEventEnd.before(origDate))
         {
            addMonthToFirstDayOfWeek(currentDate, dayOfWeek, monthInterval);
            toDayOfWeekInMonth(currentDate, dayOfWeek, instanceInMonth);
            currentEventEnd = new Date(currentDate.getTimeInMillis() + duration);
         }
         
         // Move forward to the required date
         while (currentEventEnd.before(onOrAfter))
         {
            addMonthToFirstDayOfWeek(currentDate, dayOfWeek, monthInterval);
            toDayOfWeekInMonth(currentDate, dayOfWeek, instanceInMonth);
            currentEventEnd = new Date(currentDate.getTimeInMillis() + duration);
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
      // MNT-10006 fix. Added the support for recurrences rule "WEEKDAY", "WEEKEND DAY"
      if (params.get("BYWEEKDAY") != null || params.get("BYWEEKENDDAY") != null || params.get("BYANYDAY") != null)
      {
          buildWeekdayAndWeekEndRecurence(currentDate, dates, params, until, monthInterval);
      }
   }

    /**
     * Build the recurrences for recurrence rules 'weekday', 'weekend day'
     * @param currentDate the Calendar for current event
     * @param dates  Map for recurrence events dates
     * @param params recurrence rules
     * @param until  date when the current event ends
     */
    private static void buildWeekdayAndWeekEndRecurence(Calendar currentDate, List<Date> dates, Map<String, String> params, Date until, int intervalInMonths)
    {
        String dayPosStr;
        String dayWeekType;

        // founds which of the recurrence pattern is used "weekday" or "weekend day"
        if (params.get("BYWEEKDAY") != null)
        {
            dayPosStr = params.get("BYWEEKDAY");
            dayWeekType = "WEEKDAYS";
        }
        else if (params.get("BYWEEKENDDAY") != null)
        {
            dayPosStr = params.get("BYWEEKENDDAY");
            dayWeekType = "WEEKENDS";
        }
        else
        {
            dayPosStr = params.get("BYANYDAY");
            dayWeekType = "DAY";
        }

        List<Integer> daysOfWeek = getDaysOfWeek(params, dayWeekType);

        boolean isCurrentDateAfterUntil = false;
        int firstMonthDay = 1;

        while (!isCurrentDateAfterUntil)
        {
            // Setting the current date to the first day of month
            currentDate.set(Calendar.DAY_OF_MONTH, firstMonthDay);
            if (currentDate.getTime().before(until))
            {
                int currentDayOfWeek;

                // The sequence number for "BYSETPOS" parameter from recurrence rule for current date.
                int dayCount = 0;
                // week day position, e.q.: first, second. third, forth, last. If the weekday position is 'last' the weekDayPos
                // value will less then '0'
                int weekDayPos = Integer.parseInt(dayPosStr);

                if (weekDayPos > 0)
                {
                    // Setting the current date to the first day of month
                    currentDate.set(Calendar.DAY_OF_MONTH, firstMonthDay);
                    // Walk forward from the first day of the month to the required day position according the recurrence
                    // rule, skipping the unnecessary  days. F.ex, if we need only weekdays then weekends days should be skipped.
                    while (dayCount != weekDayPos)
                    {
                        currentDayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK);

                        if (daysOfWeek.contains(currentDayOfWeek))
                        {
                            dayCount++;
                        }

                        // If dayCount is not what we need go to the next day of the current month
                        if (dayCount != weekDayPos)
                        {
                            currentDate.add(Calendar.DAY_OF_MONTH, 1);
                        }
                    }
                }
                //when weekday position is 'last'
                else
                {
                    // Sets the last day of moth and retrieves the weekday number
                    currentDate.set(Calendar.DAY_OF_MONTH, currentDate.getActualMaximum(Calendar.DAY_OF_MONTH));
                    currentDayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK);

                    // walk back from the last day of the month to last weekend day
                    while (!daysOfWeek.contains(currentDayOfWeek))
                    {
                        currentDate.add(Calendar.DAY_OF_MONTH, -1);
                        currentDayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK);
                    }
                }
                dates.add(currentDate.getTime());
                currentDate.add(Calendar.MONTH, intervalInMonths);
            }
            else
            {
                // The currentDate is after 'until' date.
                isCurrentDateAfterUntil = true;
            }
        }
    }

    /**
     * Returns the sorted List of weekdays by numbers
     * @param params recurrence rule
     * @param dayWeekType "WEEKDAY" or "WEEKEND" day
     */
    private static List<Integer> getDaysOfWeek(Map<String, String> params, String dayWeekType)
    {
        String[] weekDays = params.get(dayWeekType).split(",");
        List<Integer> daysOfWeek = new ArrayList<Integer>();

        for (String day : weekDays)
        {
            Integer dayNumber = DAY_NAMES_TO_CALENDAR_DAYS.get(day);

            if (dayNumber == null)
            {
                logger.warn("Invalid day " + day);
            }
            else
            {
                daysOfWeek.add(dayNumber);
            }
        }

        Collections.sort(daysOfWeek);

        return daysOfWeek;
    }

    protected static void buildYearlyRecurrences(Calendar currentDate, long duration, List<Date> dates,
         Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int interval)
   {
      if (onOrAfter.before(currentDate.getTime()))
      {
          onOrAfter = currentDate.getTime();
      }
      int realMonth = Integer.parseInt(params.get("BYMONTH"));
      int month = realMonth - 1; // Java months count from zero
      
      if (params.get("BYMONTHDAY") != null)
      {
          currentDate.add(Calendar.YEAR, -1 * interval * 2);
          
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
         
         Date currentEventEnd = new Date(currentDate.getTimeInMillis() + duration);
         while (currentEventEnd.before(onOrAfter))
         {
            currentDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR) + interval);
            currentDate.set(Calendar.MONTH, month);
            currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            currentEventEnd = new Date(currentDate.getTimeInMillis() + duration);
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
      // MNT-10006 fix. Added the support for recurrences rule "WEEKDAY", "WEEKEND DAY"
      else if (null != params.get("BYWEEKDAY")  || null != params.get("BYWEEKENDDAY") || null != params.get("BYANYDAY"))
      {
          int intervalInMonths = interval * 12;
          buildWeekdayAndWeekEndRecurence(currentDate, dates, params, until, intervalInMonths);
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
          
          currentDate.add(Calendar.YEAR, -1 * interval * 2);
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
          
          Date currentEventEnd = new Date(currentDate.getTimeInMillis() + duration);
          // Move forward to the required date
          while (currentEventEnd.before(onOrAfter))
          {
              currentDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR) + interval);
              currentDate.set(Calendar.MONTH, month);
              currentDate.set(Calendar.DAY_OF_MONTH, 1);
              toDayOfWeekInMonth(currentDate, dayOfWeek, instanceInMonth);
              currentEventEnd = new Date(currentDate.getTimeInMillis() + duration);
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
         
         // If we set 31th of April, then calendar instance will be on 1st of May
         // So, set the last day of moth
         if (c.get(Calendar.DAY_OF_MONTH) != dayOfMonth)
         {
             c.set(Calendar.DAY_OF_MONTH, 1);
             c.add(Calendar.DAY_OF_MONTH, -1);
         }

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
      //set it to the first day
      c.set(Calendar.DATE, 1);
      //move to the day we need
      c.set(Calendar.DAY_OF_WEEK, dayOfWeek);
      //and then to the month
      c.set(Calendar.DAY_OF_WEEK_IN_MONTH, weekInMonth);
   }
}
