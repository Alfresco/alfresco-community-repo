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
package org.alfresco.repo.calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.alfresco.service.cmr.calendar.CalendarEntryDTO;
import org.alfresco.service.cmr.calendar.CalendarRecurrenceHelper;
import org.alfresco.service.cmr.calendar.CalendarService;
import org.junit.Test;

/**
 * Test cases for the helpers relating to the {@link CalendarService},
 *  but which don't need a full repo
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class CalendarHelpersTest
{
   private static SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd");
   
   @Test public void allDayDetection()
   {
      TimeZone UTC = TimeZone.getTimeZone("UTC");
      TimeZone NewYork = TimeZone.getTimeZone("America/New_York");
      
      Calendar c20110719_0000 = Calendar.getInstance(UTC);
      Calendar c20110719_1000 = Calendar.getInstance(UTC);
      Calendar c20110720_0000 = Calendar.getInstance(UTC);
      Calendar c20110721_0000 = Calendar.getInstance(UTC);
      c20110719_0000.set(2011, 07, 19, 0, 0, 0);
      c20110719_1000.set(2011, 07, 19, 1, 0, 0);
      c20110720_0000.set(2011, 07, 20, 0, 0, 0);
      c20110721_0000.set(2011, 07, 21, 0, 0, 0);
      
      Calendar c20110721_0000ny = Calendar.getInstance(NewYork);
      Calendar c20110721_2000ny = Calendar.getInstance(NewYork);
      c20110721_0000ny.set(2011, 07, 21, 0, 0, 0);
      c20110721_2000ny.set(2011, 07, 21, 2, 0, 0);
      
      CalendarEntryDTO entry = new CalendarEntryDTO();
      
      
      // First up, do tests in the default locale with all the times in UTC
      // (We now create all-day events against UTC)
      
      // Start and end at the same midnight
      entry.setStart(c20110719_0000.getTime());
      entry.setEnd(  c20110719_0000.getTime());
      assertTrue(CalendarEntryDTO.isAllDay(entry));
      
      // Start and end at the next midnight
      entry.setStart(c20110719_0000.getTime());
      entry.setEnd(  c20110720_0000.getTime());
      assertTrue(CalendarEntryDTO.isAllDay(entry));
      
      // Start and end at the midnight after
      entry.setStart(c20110719_0000.getTime());
      entry.setEnd(  c20110721_0000.getTime());
      assertTrue(CalendarEntryDTO.isAllDay(entry));
      
      // One is midnight, one not
      entry.setStart(c20110719_0000.getTime());
      entry.setEnd(  c20110719_1000.getTime());
      assertFalse(CalendarEntryDTO.isAllDay(entry));
      
      entry.setStart(c20110719_1000.getTime());
      entry.setEnd(  c20110720_0000.getTime());
      assertFalse(CalendarEntryDTO.isAllDay(entry));
      
      // Neither midnight
      entry.setStart(c20110719_1000.getTime());
      entry.setEnd(  c20110719_1000.getTime());
      assertFalse(CalendarEntryDTO.isAllDay(entry));
      
      
      // Switch the timezone of the machine to elsewhere
      // Ensure that we still accept UTC dates for all-day
      // Also check that local ones are OK for backwards compatibility
      
      // Switch the timezone
      TimeZone defaultTimezone = TimeZone.getDefault();
      TimeZone.setDefault(NewYork);
      
      // In another timezone, local midnight is OK
      entry.setStart( c20110721_0000ny.getTime());
      entry.setEnd( c20110721_0000ny.getTime());
      assertTrue(CalendarEntryDTO.isAllDay(entry));
      
      // But non midnight isn't
      entry.setStart(  c20110721_2000ny.getTime());
      entry.setEnd(  c20110721_2000ny.getTime());
      assertFalse(CalendarEntryDTO.isAllDay(entry));
      
      // UTC midnight is still accepted
      entry.setStart(c20110719_0000.getTime());
      entry.setEnd(  c20110719_0000.getTime());
      assertTrue(CalendarEntryDTO.isAllDay(entry));
      
      // But UTC non-midnight still isn't (unless it happened to be local midnight!)
      entry.setStart(c20110719_0000.getTime());
      entry.setEnd(  c20110719_1000.getTime());
      assertFalse(CalendarEntryDTO.isAllDay(entry));
      
      
      // Put things back
      TimeZone.setDefault(defaultTimezone);
   }
   
   @Test public void dailyRecurrenceDates()
   {
      List<Date> dates = new ArrayList<Date>();
      Calendar currentDate = Calendar.getInstance();
      
      
      // Dates in the past, get nothing
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildDailyRecurrences(
            currentDate, dates, null,
            date(2011,7,10), date(2011,7,15),
            true, 1);
      assertEquals(0, dates.size());
      
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildDailyRecurrences(
            currentDate, dates, null,
            date(2011,7,10), date(2011,7,15),
            false, 1);
      assertEquals(0, dates.size());
      
      
      // From today
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildDailyRecurrences(
            currentDate, dates, null,
            date(2011,7,19), date(2011,7,25),
            true, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-07-19", dateFmt.format(dates.get(0)));
      
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildDailyRecurrences(
            currentDate, dates, null,
            date(2011,7,19), date(2011,7,25),
            false, 1);
      assertEquals(6, dates.size());
      assertEquals("2011-07-19", dateFmt.format(dates.get(0)));
      assertEquals("2011-07-24", dateFmt.format(dates.get(5)));
      
      
      // Dates in the future, goes from then
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildDailyRecurrences(
            currentDate, dates, null,
            date(2011,7,20), date(2011,7,30),
            true, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-07-20", dateFmt.format(dates.get(0)));
      
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildDailyRecurrences(
            currentDate, dates, null,
            date(2011,7,20), date(2011,7,30),
            false, 1);
      assertEquals(10, dates.size());
      assertEquals("2011-07-20", dateFmt.format(dates.get(0)));
      assertEquals("2011-07-29", dateFmt.format(dates.get(9)));
      
      
      // From before today, full time set
      dates.clear();
      currentDate.set(2011,11-1,24,10,30);
      RecurrenceHelper.buildDailyRecurrences(
            currentDate, dates, null,
            date(2011,11,22,12,30), date(2011,11,27,12,30),
            false, 1);
      assertEquals(4, dates.size());
      assertEquals("2011-11-24", dateFmt.format(dates.get(0))); // Thu
      assertEquals("2011-11-25", dateFmt.format(dates.get(1))); // Fri
      assertEquals("2011-11-26", dateFmt.format(dates.get(2))); // Sat
      assertEquals("2011-11-27", dateFmt.format(dates.get(3))); // Sun
      
      // From before today, with an interval
      // Repeats are 24th, 27th, (30th - too far)
      dates.clear();
      currentDate.set(2011,11-1,24,10,30);
      RecurrenceHelper.buildDailyRecurrences(
            currentDate, dates, null,
            date(2011,11,22,12,30), date(2011,11,27,12,30),
            false, 3);
      assertEquals(2, dates.size());
      assertEquals("2011-11-24", dateFmt.format(dates.get(0))); // Thu
      assertEquals("2011-11-27", dateFmt.format(dates.get(1))); // Sun
      
      
      // With no end date but only first, check it behaves
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildDailyRecurrences(
            currentDate, dates, null,
            date(2011,7,19), null,
            true, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-07-19", dateFmt.format(dates.get(0)));
      
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildDailyRecurrences(
            currentDate, dates, null,
            date(2011,7,20), null,
            true, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-07-20", dateFmt.format(dates.get(0)));
   }
   
   @Test public void weeklyRecurrenceDates()
   {
      List<Date> dates = new ArrayList<Date>();
      Calendar currentDate = Calendar.getInstance();
      
      Map<String,String> params = new HashMap<String, String>();
      params.put("BYDAY", "MO,TH");
      
      
      // Dates in the past, get nothing
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildWeeklyRecurrences(
            currentDate, dates, params,
            date(2011,7,10), date(2011,7,15),
            true, 1);
      assertEquals(0, dates.size());
      
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildWeeklyRecurrences(
            currentDate, dates, params,
            date(2011,7,10), date(2011,7,15),
            false, 1);
      assertEquals(0, dates.size());
      
      
      // Just before today
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildWeeklyRecurrences(
            currentDate, dates, params,
            date(2011,7,17), date(2011,7,26),
            true, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-07-21", dateFmt.format(dates.get(0))); // Thursday
      
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildWeeklyRecurrences(
            currentDate, dates, params,
            date(2011,7,19), date(2011,7,26),
            false, 1);
      assertEquals(2, dates.size());
      assertEquals("2011-07-21", dateFmt.format(dates.get(0))); // Thu
      assertEquals("2011-07-25", dateFmt.format(dates.get(1))); // Mon
      
      
      // Just before today, full time set
      dates.clear();
      currentDate.set(2011,11-1,24,10,30);
      RecurrenceHelper.buildWeeklyRecurrences(
            currentDate, dates, params,
            date(2011,11,22,12,30), date(2011,11,25,12,30),
            false, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-11-24", dateFmt.format(dates.get(0))); // Thu
      
      
      // From today
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildWeeklyRecurrences(
            currentDate, dates, params,
            date(2011,7,19), date(2011,7,26),
            true, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-07-21", dateFmt.format(dates.get(0))); // Thursday
      
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildWeeklyRecurrences(
            currentDate, dates, params,
            date(2011,7,19), date(2011,7,26),
            false, 1);
      assertEquals(2, dates.size());
      assertEquals("2011-07-21", dateFmt.format(dates.get(0))); // Thu
      assertEquals("2011-07-25", dateFmt.format(dates.get(1))); // Mon
      
      
      // Dates in the future, goes from then
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildWeeklyRecurrences(
            currentDate, dates, params,
            date(2011,7,20), date(2011,7,30),
            true, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-07-21", dateFmt.format(dates.get(0))); // Thu
      
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildWeeklyRecurrences(
            currentDate, dates, params,
            date(2011,7,20), date(2011,7,30),
            false, 1);
      assertEquals(3, dates.size());
      assertEquals("2011-07-21", dateFmt.format(dates.get(0)));
      assertEquals("2011-07-25", dateFmt.format(dates.get(1)));
      assertEquals("2011-07-28", dateFmt.format(dates.get(2)));
      
      
      // Multi-week skip
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildWeeklyRecurrences(
            currentDate, dates, params,
            date(2011,7,20), date(2011,8,30),
            true, 3);
      assertEquals(1, dates.size());
      assertEquals("2011-07-21", dateFmt.format(dates.get(0))); // Thu
      
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildWeeklyRecurrences(
            currentDate, dates, params,
            date(2011,7,20), date(2011,8,30),
            false, 3);
      assertEquals(4, dates.size());
      assertEquals("2011-07-21", dateFmt.format(dates.get(0)));
      // Not the 25th or 28th
      // Not the 1st or the 4th
      assertEquals("2011-08-08", dateFmt.format(dates.get(1)));
      assertEquals("2011-08-11", dateFmt.format(dates.get(2)));
      // Not the 15th or 18th
      // Not the 22nd or 25th
      assertEquals("2011-08-29", dateFmt.format(dates.get(3)));
      
      
      // With no end date but only first, check it behaves
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildWeeklyRecurrences(
            currentDate, dates, params,
            date(2011,7,19), null,
            true, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-07-21", dateFmt.format(dates.get(0))); // Thu
      
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildWeeklyRecurrences(
            currentDate, dates, params,
            date(2011,7,22), null,
            true, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-07-25", dateFmt.format(dates.get(0)));
   }
   
   /**
    * eg on the 2nd of the month
    */
   @Test public void monthlyRecurrenceByDateInMonth()
   {
      List<Date> dates = new ArrayList<Date>();
      Calendar currentDate = Calendar.getInstance();
      
      Map<String,String> params = new HashMap<String, String>();
      params.put("BYMONTHDAY", "2");
      
      
      // Dates in the past, get nothing
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,10), date(2011,7,15),
            true, 1);
      assertEquals(0, dates.size());
      
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,10), date(2011,7,15),
            false, 1);
      assertEquals(0, dates.size());
      
      
      // With this month
      dates.clear();
      currentDate.set(2011,7-1,1,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,1), date(2011,7,26),
            true, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-07-02", dateFmt.format(dates.get(0)));
      
      dates.clear();
      currentDate.set(2011,7-1,1,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,1), date(2011,7,26),
            false, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-07-02", dateFmt.format(dates.get(0)));
      
      
      // From the day of the month 
      dates.clear();
      currentDate.set(2011,7-1,2,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,2), date(2011,7,26),
            true, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-07-02", dateFmt.format(dates.get(0)));
      
      dates.clear();
      currentDate.set(2011,7-1,2,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,2), date(2011,7,26),
            false, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-07-02", dateFmt.format(dates.get(0)));
      
      
      // Dates in the future, goes from then
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,20), date(2011,9,20),
            true, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-08-02", dateFmt.format(dates.get(0)));
      
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,20), date(2011,9,20),
            false, 1);
      assertEquals(2, dates.size());
      assertEquals("2011-08-02", dateFmt.format(dates.get(0)));
      assertEquals("2011-09-02", dateFmt.format(dates.get(1)));
      
      
      // Now with a recurrence interval of only every 2 months
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,20), date(2011,9,20),
            false, 2);
      assertEquals(1, dates.size());
      assertEquals("2011-09-02", dateFmt.format(dates.get(0)));
      
      
      // With no end date but only first, check it behaves
      dates.clear();
      currentDate.set(2011,7-1,2,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,1), null,
            true, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-07-02", dateFmt.format(dates.get(0)));
      
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,19), null,
            true, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-08-02", dateFmt.format(dates.get(0)));
   }
   
   /**
    * on the 1st Tuesday of the month
    */
   @Test public void monthlyRecurrenceByDayOfWeek()
   {
      List<Date> dates = new ArrayList<Date>();
      Calendar currentDate = Calendar.getInstance();
      
      Map<String,String> params = new HashMap<String, String>();
      params.put("BYSETPOS", "TU");
      
      
      // Dates in the past, get nothing
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,10), date(2011,7,15),
            true, 1);
      assertEquals(0, dates.size());
      
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,10), date(2011,7,15),
            false, 1);
      assertEquals(0, dates.size());
      
      
      // With this month
      dates.clear();
      currentDate.set(2011,7-1,1,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,1), date(2011,7,26),
            true, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-07-05", dateFmt.format(dates.get(0))); // Tuesday 5th
      
      dates.clear();
      currentDate.set(2011,7-1,1,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,1), date(2011,7,26),
            false, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-07-05", dateFmt.format(dates.get(0))); // Tuesday 5th
      
      
      // From the day of the month 
      dates.clear();
      currentDate.set(2011,7-1,2,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,2), date(2011,7,26),
            true, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-07-05", dateFmt.format(dates.get(0))); // Tuesday 5th
      
      dates.clear();
      currentDate.set(2011,7-1,2,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,2), date(2011,7,26),
            false, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-07-05", dateFmt.format(dates.get(0))); // Tuesday 5th
      
      
      // Dates in the future, goes from then
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,20), date(2011,9,20),
            true, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-08-02", dateFmt.format(dates.get(0))); // Tuesday 2nd
      
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,20), date(2011,9,20),
            false, 1);
      assertEquals(2, dates.size());
      assertEquals("2011-08-02", dateFmt.format(dates.get(0))); // Tuesday 2nd
      assertEquals("2011-09-06", dateFmt.format(dates.get(1))); // Tuesday 6th
      
      
      // With no end date but only first, check it behaves
      dates.clear();
      currentDate.set(2011,7-1,2,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,1), null,
            true, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-07-05", dateFmt.format(dates.get(0)));
      
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,19), null,
            true, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-08-02", dateFmt.format(dates.get(0)));
   }
   
   private static class RecurrenceHelper extends CalendarRecurrenceHelper
   {
      protected static void buildDailyRecurrences(Calendar currentDate, List<Date> dates, 
            Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int interval)
      {
         CalendarRecurrenceHelper.buildDailyRecurrences(
               currentDate, dates, params, onOrAfter, until, firstOnly, interval);
      }
      
      protected static void buildWeeklyRecurrences(Calendar currentDate, List<Date> dates, 
            Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int interval)
      {
         CalendarRecurrenceHelper.buildWeeklyRecurrences(
               currentDate, dates, params, onOrAfter, until, firstOnly, interval);
      }
      
      protected static void buildMonthlyRecurrences(Calendar currentDate, List<Date> dates, 
            Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int interval)
      {
         CalendarRecurrenceHelper.buildMonthlyRecurrences(
               currentDate, dates, params, onOrAfter, until, firstOnly, interval);
      }
      
      protected static void buildYearlyRecurrences(Calendar currentDate, List<Date> dates, 
            Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int interval)
      {
         CalendarRecurrenceHelper.buildYearlyRecurrences(
               currentDate, dates, params, onOrAfter, until, firstOnly, interval);
      }
   }
   
   private static Date date(int year, int month, int day)
   {
      return date(year, month, day, 0, 0);
   }
   
   private static Date date(int year, int month, int day, int hour, int minute)
   {
      Calendar c = Calendar.getInstance();
      c.set(year, month-1, day, hour, minute, 0);
      c.set(Calendar.MILLISECOND, 0);
      return c.getTime();
   }
}
