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
import static org.junit.Assert.assertNotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.alfresco.service.cmr.calendar.CalendarEntryDTO;
import org.alfresco.service.cmr.calendar.CalendarRecurrenceHelper;
import org.alfresco.service.cmr.calendar.CalendarService;
import org.alfresco.service.cmr.calendar.CalendarTimezoneHelper;
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
   private final static long ONE_HOUR_MS = 1 * 60 * 60 * 1000L;
   private final static long TWO_DAYS_MS = 2 * 24 * ONE_HOUR_MS;
   
   private static SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd");
   
   /**
    * UTC+10, no daylight savings
    */
   private static final String ICAL_TZ_BRISBANE =
      "BEGIN:VTIMEZONE\n" +
      "TZID:Brisbane\n" +
      "BEGIN:STANDARD\n" +
      "DTSTART:16010101T000000\n" +
      "TZOFFSETFROM:+1100\n" +
      "TZOFFSETTO:+1000\n" +
      "END:STANDARD\n" +
      "END:VTIMEZONE\n";
   /**
    * UTC+10 April-October, Daylight UTC+11 October-April
    */
   private static final String ICAL_TZ_SYDNEY =
      "BEGIN:VTIMEZONE\n" +
      "TZID:Canberra\\, Melbourne\\, Sydney\n" +
      "BEGIN:STANDARD\n" +
      "DTSTART:16010401T030000\n" +
      "RRULE:FREQ=YEARLY;BYDAY=1SU;BYMONTH=4\n" +
      "TZOFFSETFROM:+1100\n" +
      "TZOFFSETTO:+1000\n" +
      "END:STANDARD\n" +
      "BEGIN:DAYLIGHT\n" +
      "DTSTART:16011007T020000\n" +
      "RRULE:FREQ=YEARLY;BYDAY=1SU;BYMONTH=10\n" +
      "TZOFFSETFROM:+1000\n" +
      "TZOFFSETTO:+1100\n" +
      "END:DAYLIGHT\n" +
      "END:VTIMEZONE\n";
   /**
    * UTC October-March, Daylight UTC+1 March-October
    */
   private static final String ICAL_TZ_LONDON = 
      "BEGIN:VTIMEZONE\n" +
      "TZID:Europe/London\n" +
      "X-LIC-LOCATION:Europe/London\n" +
      "BEGIN:DAYLIGHT\n" +
      "TZOFFSETFROM:+0000\n" +
      "TZOFFSETTO:+0100\n" +
      "TZNAME:BST\n" +
      "DTSTART:19700329T010000\n" +
      "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3\n" +
      "END:DAYLIGHT\n" +
      "BEGIN:STANDARD\n" +
      "TZOFFSETFROM:+0100\n" +
      "TZOFFSETTO:+0000\n" +
      "TZNAME:GMT\n" +
      "DTSTART:19701025T020000\n" +
      "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10\n" +
      "END:STANDARD\n" +
      "END:VTIMEZONE\n";

   
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
      
      
      //Rule that starts before interested date but ends after.
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildDailyRecurrences(
            currentDate, TWO_DAYS_MS, dates, null,
            date(2011,7,20), date(2011,7,30),
            true, 3);
      assertEquals(1, dates.size());
      assertEquals("2011-07-19", dateFmt.format(dates.get(0))); //This event starts before 2011-7-20, but it ends on 2001-07-21
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
      
      
      //Rule that starts before interested date but ends after.
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildWeeklyRecurrences(
              currentDate, TWO_DAYS_MS, dates, params,
              date(2011,7,22), null,
              true, 1);
        assertEquals(1, dates.size());
        assertEquals("2011-07-21", dateFmt.format(dates.get(0)));//This event starts before 2011-7-22, but it ends on 2001-07-23
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
      
      //Rule that starts before interested date but ends after.
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, TWO_DAYS_MS, dates, params,
            date(2011,8,3), null,
            true, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-08-02", dateFmt.format(dates.get(0))); //This event starts before 2011-8-03, but it ends on 2001-08-4   
   }
   
   /**
    * Test recurrence rules that occur monthly or yearly 
    * on the last day within the month 
    */
   @Test public void reccurenceByLastDay(){
	      List<Date> dates = new ArrayList<Date>();
	      Calendar currentDate = Calendar.getInstance();
	      currentDate.set(2012,7-1,15,10,30);
	      
	      //Recur Monthly on the last Monday
	      Map<String,String> params = new HashMap<String, String>();
	      params.put("FREQ", "MONTHLY");
	      params.put("BYDAY", "MO");
	      params.put("INTERVAL", "1");
	      params.put("BYSETPOS", "-1");
	      
	      dates.clear();
	      
	      RecurrenceHelper.buildMonthlyRecurrences(
	            currentDate, dates, params,
	            date(2012,7,1), date(2012,9,30),
	            false, 1);
	      assertEquals(3, dates.size());
	      assertEquals("2012-07-30", dateFmt.format(dates.get(0)));
	      assertEquals("2012-08-27", dateFmt.format(dates.get(1)));
	      assertEquals("2012-09-24", dateFmt.format(dates.get(2)));
	      
          //Rule that starts before interested date but ends after.
          dates.clear();
          currentDate.set(2012,7-1,15,10,30);
          RecurrenceHelper.buildMonthlyRecurrences(
                  currentDate, TWO_DAYS_MS, dates, params,
                  date(2012,7,31), null,
                  true, 1);
          assertEquals(1, dates.size());
          assertEquals("2012-07-30", dateFmt.format(dates.get(0))); //This event starts before 2012-7-31, but it ends on 2012-08-01
          
	    //Recur yearly on the last Monday in July
	      params = new HashMap<String, String>();
	      params.put("BYMONTH", "7");
	      params.put("BYDAY", "MO");
	      params.put("BYSETPOS", "-1");
	      
	      dates.clear();
          currentDate.set(2012,7-1,15,10,30);
	      RecurrenceHelper.buildYearlyRecurrences(
	            currentDate, dates, params,
	            date(2012,7,1), date(2015,10,1),
	            false, 1);
          assertEquals(4, dates.size());
          assertEquals("2012-07-30", dateFmt.format(dates.get(0)));
          assertEquals("2013-07-29", dateFmt.format(dates.get(1)));
          assertEquals("2014-07-28", dateFmt.format(dates.get(2)));
          assertEquals("2015-07-27", dateFmt.format(dates.get(3)));
          
          //Rule that starts before interested date but ends after.
          dates.clear();
          currentDate.set(2012,7-1,15,10,30);
          RecurrenceHelper.buildYearlyRecurrences(
                  currentDate, TWO_DAYS_MS, dates, params,
                  date(2012,7,31), null,
                  true, 1);
          assertEquals(1, dates.size());
          assertEquals("2012-07-30", dateFmt.format(dates.get(0))); //This event starts before 2012-7-31, but it ends on 2012-08-01
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
      
      //Rule that starts before interested date but ends after.
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, TWO_DAYS_MS, dates, params,
            date(2011,8,3), null,
            true, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-08-02", dateFmt.format(dates.get(0)));//This event starts before 2011-8-03, but it ends on 201-08-04
      
      // Alternate format, used by Outlook 2010 etc
      //  1st Monday of the Month
      params.clear();
      params.put("FREQ", "MONTHLY"); // Implied in call
      params.put("COUNT", "10");     // Implied in call
      params.put("INTERVAL", "1");   // Implied in call
      params.put("BYDAY", "MO");
      params.put("BYSETPOS", "1");

      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,19), date(2012,1,5),
            false, 1);
      assertEquals(6, dates.size());
      assertEquals("2011-08-01", dateFmt.format(dates.get(0)));
      assertEquals("2011-09-05", dateFmt.format(dates.get(1)));
      assertEquals("2011-10-03", dateFmt.format(dates.get(2)));
      assertEquals("2011-11-07", dateFmt.format(dates.get(3)));
      assertEquals("2011-12-05", dateFmt.format(dates.get(4)));
      assertEquals("2012-01-02", dateFmt.format(dates.get(5)));
      
      //Rule that starts before interested date but ends after.
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, TWO_DAYS_MS, dates, params,
            date(2011,8,2), null,
            true, 1);
      assertEquals(1, dates.size());
      assertEquals("2011-08-01", dateFmt.format(dates.get(0)));//This event starts before 2011-8-02, but it ends on 2001-08-03
      
      
      //  3rd Friday of the Month
      params.clear();
      params.put("FREQ", "MONTHLY"); // Implied in call
      params.put("COUNT", "10");     // Implied in call
      params.put("INTERVAL", "1");   // Implied in call
      params.put("BYDAY", "FR");
      params.put("BYSETPOS", "3");

      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,19), date(2012,1,25),
            false, 1);
      assertEquals(6, dates.size());
      assertEquals("2011-08-19", dateFmt.format(dates.get(0)));
      assertEquals("2011-09-16", dateFmt.format(dates.get(1)));
      assertEquals("2011-10-21", dateFmt.format(dates.get(2)));
      assertEquals("2011-11-18", dateFmt.format(dates.get(3)));
      assertEquals("2011-12-16", dateFmt.format(dates.get(4)));
      assertEquals("2012-01-20", dateFmt.format(dates.get(5)));

      
      //  3rd Friday of the Month, of every 3 months
      params.clear();
      params.put("FREQ", "MONTHLY"); // Implied in call
      params.put("COUNT", "10");     // Implied in call
      params.put("INTERVAL", "3");   // Implied in call
      params.put("BYDAY", "FR");
      params.put("BYSETPOS", "3");

      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,19), date(2012,1,25),
            false, 3);
      assertEquals(2, dates.size());
      assertEquals("2011-10-21", dateFmt.format(dates.get(0)));
      assertEquals("2012-01-20", dateFmt.format(dates.get(1)));

      
      // The third friday falls within the range for this month
      dates.clear();
      currentDate.set(2011,7-1,14,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,14), date(2012,1,25),
            false, 1);
      assertEquals(7, dates.size());
      assertEquals("2011-07-15", dateFmt.format(dates.get(0)));
      assertEquals("2011-08-19", dateFmt.format(dates.get(1)));
      assertEquals("2011-09-16", dateFmt.format(dates.get(2)));
      assertEquals("2011-10-21", dateFmt.format(dates.get(3)));
      assertEquals("2011-11-18", dateFmt.format(dates.get(4)));
      assertEquals("2011-12-16", dateFmt.format(dates.get(5)));
      assertEquals("2012-01-20", dateFmt.format(dates.get(6)));

      
      // The third friday falls within the range for this month, every 3 months
      dates.clear();
      currentDate.set(2011,7-1,14,10,30);
      RecurrenceHelper.buildMonthlyRecurrences(
            currentDate, dates, params,
            date(2011,7,14), date(2012,1,25),
            false, 3);
      assertEquals(3, dates.size());
      assertEquals("2011-07-15", dateFmt.format(dates.get(0)));
      assertEquals("2011-10-21", dateFmt.format(dates.get(1)));
      assertEquals("2012-01-20", dateFmt.format(dates.get(2)));
   }
   
   /**
    * eg the last Tuesday of the month
    */
   @Test public void monthlyRecurrenceByLastDayOfWeek()
   {
       List<Date> dates = new ArrayList<Date>();
       Calendar currentDate = Calendar.getInstance();
       
       // The last Tuesday of every 2nd month
       // FREQ=MONTHLY;INTERVAL=2;BYDAY=TU;BYSETPOS=-1
       Map<String,String> params = new HashMap<String, String>();
       params.put("FREQ", "MONTHLY");
       params.put("INTERVAL", "2");
       params.put("BYDAY", "TU");
       params.put("BYSETPOS", "-1");

       // TODO Add tests for this case, ALF-13287
   }
   
   /**
    * eg every 21st of February
    */
   @Test public void yearlyRecurrenceByDateInMonth()
   {
       List<Date> dates = new ArrayList<Date>();
       Calendar currentDate = Calendar.getInstance();
       
       // How Outlook ought to do it
       Map<String,String> params = new HashMap<String, String>();
       params.put("COUNT", "10");
       params.put("BYMONTH", "2");
       params.put("BYMONTHDAY", "21");
       
       // How many Outlook versions do do it...
       // FREQ=MONTHLY;COUNT=10;BYMONTH=2;INTERVAL=36;BYSETPOS=21;BYDAY=SU,MO,TU,WE,TH,FR,SA; 
       Map<String,String> paramsOUTLOOK = new HashMap<String, String>();
       paramsOUTLOOK.put("FREQ", "MONTHLY");
       paramsOUTLOOK.put("COUNT", "10");
       paramsOUTLOOK.put("BYMONTH", "2");
       paramsOUTLOOK.put("INTERVAL", "36");
       paramsOUTLOOK.put("BYSETPOS", "21");
       paramsOUTLOOK.put("BYDAY", "SU,MO,TU,WE,TH,FR,SA");
       
       // Check that the outlook crazy version gets fixed
       Map<String,String> paramsFIXED = RecurrenceHelper.fixOutlookRecurrenceQuirks(paramsOUTLOOK);
       assertEquals("YEARLY", paramsFIXED.get("FREQ"));
       assertEquals("2",  paramsFIXED.get("BYMONTH"));
       assertEquals("21", paramsFIXED.get("BYMONTHDAY"));
       assertEquals("10", paramsFIXED.get("COUNT"));
       assertEquals("3", paramsFIXED.get("INTERVAL"));
       assertEquals(null, paramsFIXED.get("BYDAY"));
       assertEquals(null, paramsFIXED.get("BYSETPOS"));

       
       // Dates in the past, get nothing
       dates.clear();
       currentDate.set(2012,1-1,19,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
             currentDate, dates, params,
             date(2012,2,10), date(2012,2,15),
             true, 1);
       assertEquals(0, dates.size());
       
       dates.clear();
       currentDate.set(2012,1-1,19,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
               currentDate, dates, params,
               date(2012,2,10), date(2012,2,15),
               false, 1);
       assertEquals(0, dates.size());
       
       
       // With the month that contains it
       dates.clear();
       currentDate.set(2012,2-1,1,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
             currentDate, dates, params,
             date(2012,2,1), date(2012,2,26),
             true, 1);
       assertEquals(1, dates.size());
       assertEquals("2012-02-21", dateFmt.format(dates.get(0)));
       
       dates.clear();
       currentDate.set(2012,2-1,1,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
             currentDate, dates, params,
             date(2012,2,1), date(2012,2,26),
             false, 1);
       assertEquals(1, dates.size());
       assertEquals("2012-02-21", dateFmt.format(dates.get(0)));
       
       
       // In the next month
       dates.clear();
       currentDate.set(2012,3-1,1,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
             currentDate, dates, params,
             date(2012,3,1), date(2012,3,26),
             true, 1);
       assertEquals(0, dates.size());
       
       dates.clear();
       currentDate.set(2012,3-1,1,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
             currentDate, dates, params,
             date(2012,3,1), date(2012,3,26),
             false, 1);
       assertEquals(0, dates.size());
       
       
       // From before, into the next year
       dates.clear();
       currentDate.set(2012,2-1,1,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
             currentDate, dates, params,
             date(2012,2,1), date(2013,3,26),
             true, 1);
       assertEquals(1, dates.size());
       assertEquals("2012-02-21", dateFmt.format(dates.get(0)));
       
       dates.clear();
       currentDate.set(2012,2-1,1,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
             currentDate, dates, params,
             date(2012,2,1), date(2013,3,26),
             false, 1);
       assertEquals(2, dates.size());
       assertEquals("2012-02-21", dateFmt.format(dates.get(0)));
       assertEquals("2013-02-21", dateFmt.format(dates.get(1)));
       
       
       // From next month, into the next year
       dates.clear();
       currentDate.set(2012,3-1,1,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
             currentDate, dates, params,
             date(2012,3,1), date(2013,2,26),
             true, 1);
       assertEquals(1, dates.size());
       assertEquals("2013-02-21", dateFmt.format(dates.get(0)));
       
       dates.clear();
       currentDate.set(2012,3-1,1,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
             currentDate, dates, params,
             date(2012,3,1), date(2013,3,26),
             false, 1);
       assertEquals(1, dates.size());
       assertEquals("2013-02-21", dateFmt.format(dates.get(0)));
       
       
       // With no end date but only first, check it behaves
       dates.clear();
       currentDate.set(2011,7-1,2,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
             currentDate, dates, params,
             date(2011,7,1), null,
             true, 1);
       assertEquals(1, dates.size());
       assertEquals("2012-02-21", dateFmt.format(dates.get(0)));
       
       dates.clear();
       currentDate.set(2012,7-1,19,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
             currentDate, dates, params,
             date(2011,7,19), null,
             true, 1);
       assertEquals(1, dates.size());
       assertEquals("2013-02-21", dateFmt.format(dates.get(0)));
       
       //Rule that starts before interested date but ends after.
       dates.clear();
       currentDate.set(2012,7-1,19,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
             currentDate, TWO_DAYS_MS, dates, params,
             date(2013,2,22), null,
             true, 1);
       assertEquals(1, dates.size());
       assertEquals("2013-02-21", dateFmt.format(dates.get(0)));//This event starts before 2013-2-22, but it ends on 2013-02-23
   }
   
   /**
    * eg the 2nd Thursday in every March
    */
   @Test public void yearlyRecurrenceByDayOfWeekInMonth()
   {
       List<Date> dates = new ArrayList<Date>();
       Calendar currentDate = Calendar.getInstance();
       
       // How Outlook ought to do it
       Map<String,String> params = new HashMap<String, String>();
       params.put("COUNT", "7");
       params.put("BYMONTH", "2");
       params.put("BYDAY", "SA");
       params.put("BYSETPOS", "2");
       
       // 2nd Saturday in February is 11th Feb 2012, 9th Feb 2013
       
       // Note - outlook seems to like to set these as monthly...
       // FREQ=MONTHLY;COUNT=7;BYDAY=SA;BYMONTH=2;BYSETPOS=2;INTERVAL=12
       // This is right except for the FREQ!
       Map<String,String> paramsOUTLOOK = new HashMap<String, String>();
       paramsOUTLOOK.put("FREQ", "MONTHLY");
       paramsOUTLOOK.put("COUNT", "7");
       paramsOUTLOOK.put("BYMONTH", "2");
       paramsOUTLOOK.put("BYDAY", "SA");
       paramsOUTLOOK.put("BYSETPOS", "2");
       paramsOUTLOOK.put("INTERVAL", "12");
       
       // Check that the outlook crazy version gets fixed
       Map<String,String> paramsFIXED = RecurrenceHelper.fixOutlookRecurrenceQuirks(paramsOUTLOOK);
       assertEquals("YEARLY", paramsFIXED.get("FREQ"));
       assertEquals("2",  paramsFIXED.get("BYMONTH"));
       assertEquals("SA", paramsFIXED.get("BYDAY"));
       assertEquals("2", paramsFIXED.get("BYSETPOS"));
       assertEquals("7", paramsFIXED.get("COUNT"));
       assertEquals("1", paramsFIXED.get("INTERVAL"));
       assertEquals(null, paramsFIXED.get("BYMONTHDAY"));

       

       
       // Dates in the past, get nothing
       dates.clear();
       currentDate.set(2012,1-1,19,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
             currentDate, dates, params,
             date(2012,2,4), date(2012,2,5),
             true, 1);
       assertEquals(0, dates.size());
       
       dates.clear();
       currentDate.set(2012,1-1,19,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
               currentDate, dates, params,
               date(2012,2,4), date(2012,2,5),
               false, 1);
       assertEquals(0, dates.size());
       
       
       // With the month that contains it
       dates.clear();
       currentDate.set(2012,2-1,1,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
             currentDate, dates, params,
             date(2012,2,1), date(2012,2,26),
             true, 1);
       assertEquals(1, dates.size());
       assertEquals("2012-02-11", dateFmt.format(dates.get(0)));
       
       dates.clear();
       currentDate.set(2012,2-1,1,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
             currentDate, dates, params,
             date(2012,2,1), date(2012,2,26),
             false, 1);
       assertEquals(1, dates.size());
       assertEquals("2012-02-11", dateFmt.format(dates.get(0)));
       
       
       // In the next month
       dates.clear();
       currentDate.set(2012,3-1,1,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
             currentDate, dates, params,
             date(2012,3,1), date(2012,3,26),
             true, 1);
       assertEquals(0, dates.size());
       
       dates.clear();
       currentDate.set(2012,3-1,1,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
             currentDate, dates, params,
             date(2012,3,1), date(2012,3,26),
             false, 1);
       assertEquals(0, dates.size());
       
       
       // From before, into the next year
       dates.clear();
       currentDate.set(2012,2-1,1,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
             currentDate, dates, params,
             date(2012,2,1), date(2013,3,26),
             true, 1);
       assertEquals(1, dates.size());
       assertEquals("2012-02-11", dateFmt.format(dates.get(0)));
       
       dates.clear();
       currentDate.set(2012,2-1,1,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
             currentDate, dates, params,
             date(2012,2,1), date(2013,3,26),
             false, 1);
       assertEquals(2, dates.size());
       assertEquals("2012-02-11", dateFmt.format(dates.get(0)));
       assertEquals("2013-02-09", dateFmt.format(dates.get(1)));
       
       
       // From next month, into the next year
       dates.clear();
       currentDate.set(2012,3-1,1,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
             currentDate, dates, params,
             date(2012,3,1), date(2013,2,26),
             true, 1);
       assertEquals(1, dates.size());
       assertEquals("2013-02-09", dateFmt.format(dates.get(0)));
       
       dates.clear();
       currentDate.set(2012,3-1,1,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
             currentDate, dates, params,
             date(2012,3,1), date(2013,3,26),
             false, 1);
       assertEquals(1, dates.size());
       assertEquals("2013-02-09", dateFmt.format(dates.get(0)));
       
       
       // With no end date but only first, check it behaves
       dates.clear();
       currentDate.set(2011,7-1,2,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
             currentDate, dates, params,
             date(2011,7,1), null,
             true, 1);
       assertEquals(1, dates.size());
       assertEquals("2012-02-11", dateFmt.format(dates.get(0)));
       
       dates.clear();
       currentDate.set(2012,7-1,19,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
             currentDate, dates, params,
             date(2011,7,19), null,
             true, 1);
       assertEquals(1, dates.size());
       assertEquals("2013-02-09", dateFmt.format(dates.get(0)));
       
       //Rule that starts before interested date but ends after.
       dates.clear();
       currentDate.set(2012,7-1,19,10,30);
       RecurrenceHelper.buildYearlyRecurrences(
             currentDate, TWO_DAYS_MS, dates, params,
             date(2013,2,10), null,
             true, 1);
       assertEquals(1, dates.size());
       assertEquals("2013-02-09", dateFmt.format(dates.get(0)));//This event starts before 2013-2-10, but it ends on 2013-02-11
   }

   /**
    * Checks we correctly build the Timezone for somewhere
    *  that doesn't have DST (eg Brisbane)
    */
   @Test public void simpleTimeZoneNoDST() 
   {
      SimpleTimeZone tz = CalendarTimezoneHelper.buildTimeZone(ICAL_TZ_BRISBANE);
      
      assertNotNull(tz);
      assertEquals("Brisbane", tz.getID());
      
      // Doesn't do DST
      assertEquals(false, tz.useDaylightTime());
      
      // Always 10 hours ahead
      assertEquals(10*60*60*1000, tz.getOffset(date(2011,3,1).getTime()));
      assertEquals(10*60*60*1000, tz.getOffset(date(2011,9,1).getTime()));
      assertEquals(10*60*60*1000, tz.getOffset(date(2011,11,1).getTime()));
   }
   
   /**
    * Checks we correctly build the Timezone for somewhere
    *  in the northern hemisphere with DST (eg London)
    */
   @Test public void simpleTimeZoneNorthern() 
   {
      SimpleTimeZone tz = CalendarTimezoneHelper.buildTimeZone(ICAL_TZ_LONDON);
      
      assertNotNull(tz);
      assertEquals("Europe/London", tz.getID());
      
      // Does do DST
      assertEquals(true, tz.useDaylightTime());
      
      // In 2003, DST was 30th March - 26th October
      assertEquals(0*60*60*1000, tz.getOffset(date(2003,3,1).getTime()));
      assertEquals(1*60*60*1000, tz.getOffset(date(2003,3,31).getTime()));
      assertEquals(1*60*60*1000, tz.getOffset(date(2003,9,1).getTime()));
      assertEquals(1*60*60*1000, tz.getOffset(date(2003,10,25).getTime()));
      assertEquals(0*60*60*1000, tz.getOffset(date(2003,11,1).getTime()));
      
      // In 2007, DST was 25th March - 28th October
      assertEquals(0*60*60*1000, tz.getOffset(date(2007,3,1).getTime()));
      assertEquals(1*60*60*1000, tz.getOffset(date(2007,3,26).getTime()));
      assertEquals(1*60*60*1000, tz.getOffset(date(2007,3,31).getTime()));
      assertEquals(1*60*60*1000, tz.getOffset(date(2007,9,1).getTime()));
      assertEquals(1*60*60*1000, tz.getOffset(date(2007,10,28).getTime()));
      assertEquals(0*60*60*1000, tz.getOffset(date(2007,10,29).getTime()));
      assertEquals(0*60*60*1000, tz.getOffset(date(2007,11,1).getTime()));
      
      // In 2011, DST was 27th March - 30th October 
      assertEquals(0*60*60*1000, tz.getOffset(date(2011,3,1).getTime()));
      assertEquals(1*60*60*1000, tz.getOffset(date(2011,3,31).getTime()));
      assertEquals(1*60*60*1000, tz.getOffset(date(2011,9,1).getTime()));
      assertEquals(1*60*60*1000, tz.getOffset(date(2011,10,25).getTime()));
      assertEquals(0*60*60*1000, tz.getOffset(date(2011,11,1).getTime()));
   }
   
   /**
    * Checks we correctly build the Timezone for somewhere
    *  in the southern hemisphere with DST (eg Sydney)
    * Note - Sydney is GMT+11 in December, GMT+10 in June
    */
   @Test public void simpleTimeZoneSouthern() 
   {
      SimpleTimeZone tz = CalendarTimezoneHelper.buildTimeZone(ICAL_TZ_SYDNEY);
      
      assertNotNull(tz);
      assertEquals("Canberra, Melbourne, Sydney", tz.getID());
      
      // Does do DST
      assertEquals(true, tz.useDaylightTime());
      
      // Note - things changed in 2008!
      // In 2002-2003, DST was 27th October 2002 - 30th March 2003 
      // In 2005-2006, DST was 30th October 2005 -  2nd April 2006 
      // In 2007-2008, DST was 28th October 2007 - 6th April 2008
      
      // In 2008-2009, DST was 5th October 2008 - 5th April 2009
      assertEquals(10*60*60*1000, tz.getOffset(date(2008,6,1).getTime()));
      assertEquals(10*60*60*1000, tz.getOffset(date(2008,10,1).getTime()));
      assertEquals(11*60*60*1000, tz.getOffset(date(2008,10,6).getTime()));
      assertEquals(11*60*60*1000, tz.getOffset(date(2008,12,1).getTime()));
      assertEquals(11*60*60*1000, tz.getOffset(date(2009,1,5).getTime()));
      assertEquals(11*60*60*1000, tz.getOffset(date(2009,4,4).getTime()));
      assertEquals(10*60*60*1000, tz.getOffset(date(2009,4,6).getTime()));
      assertEquals(10*60*60*1000, tz.getOffset(date(2009,5,1).getTime()));
      
      // In 2009-2010, DST was  4th October 2009 - 4th April 2010
      assertEquals(10*60*60*1000, tz.getOffset(date(2009,6,1).getTime()));
      assertEquals(10*60*60*1000, tz.getOffset(date(2009,10,1).getTime()));
      assertEquals(11*60*60*1000, tz.getOffset(date(2009,10,6).getTime()));
      assertEquals(11*60*60*1000, tz.getOffset(date(2009,12,1).getTime()));
      assertEquals(11*60*60*1000, tz.getOffset(date(2010,1,5).getTime()));
      assertEquals(11*60*60*1000, tz.getOffset(date(2010,4,3).getTime()));
      assertEquals(10*60*60*1000, tz.getOffset(date(2010,4,5).getTime()));
      assertEquals(10*60*60*1000, tz.getOffset(date(2010,5,1).getTime()));
      
      // In 2010-2011, DST was 3rd Oct 2010 - 3rd April 2011  
      assertEquals(10*60*60*1000, tz.getOffset(date(2010,6,1).getTime()));
      assertEquals(10*60*60*1000, tz.getOffset(date(2010,10,1).getTime()));
      assertEquals(11*60*60*1000, tz.getOffset(date(2010,10,6).getTime()));
      assertEquals(11*60*60*1000, tz.getOffset(date(2010,12,1).getTime()));
      assertEquals(11*60*60*1000, tz.getOffset(date(2011,1,5).getTime()));
      assertEquals(11*60*60*1000, tz.getOffset(date(2011,4,2).getTime()));
      assertEquals(10*60*60*1000, tz.getOffset(date(2011,4,4).getTime()));
      assertEquals(10*60*60*1000, tz.getOffset(date(2011,5,1).getTime()));
   }
   
   @Test
   public void testICalParams()
   {
       // Outlook sends 2 POST requests when we are going to update series (one updated occurrence takes place).
       String icalText = "BEGIN:VCALENDAR\r\nPRODID:-//Microsoft Corporation//Outlook 14.0 MIMEDIR//EN\r\nVERSION:2.0\r\nMETHOD:REQUEST\r\nX-MS-OLK-FORCEINSPECTOROPEN:TRUE\r\n"
               // timezone info
               + "BEGIN:VTIMEZONE\r\nTZID:E. Africa Standard Time\r\nBEGIN:STANDARD\r\nDTSTART:16010101T000000\r\nTZOFFSETFROM:+0400\r\nTZOFFSETTO:+0300\r\nEND:STANDARD\r\nEND:VTIMEZONE\r\n"
               // series event info
               + "BEGIN:VEVENT\r\nATTENDEE;CN=alfresco@admin.com;RSVP=TRUE:mailto:alfresco@admin.com\r\nCLASS:PUBLIC\r\nCREATED:20131111T115246Z\r\nDTEND;TZID=\"E. Africa Standard Time\":20131111T083000\r\nDTSTAMP:20131111T115246Z\r\nDTSTART;TZID=\"E. Africa Standard Time\":20131111T080000\r\nLAST-MODIFIED:20131111T115246Z\r\nLOCATION:SubjectLocation\r\nORGANIZER;CN=admin:mailto:admin@alfresco.com\r\nPRIORITY:5\r\nRRULE:FREQ=DAILY;COUNT=11\r\nSEQUENCE:2\r\nSUMMARY;LANGUAGE=ru:SeriesSubjectRenamed\r\nTRANSP:OPAQUE\r\nUID:040000008200E00074C5B7101A82E0080000000000E06362E6DECE010000000000000000100000001EEA71ED0E2CAC4DABF45387DD35DE90\r\nX-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\nX-MICROSOFT-CDO-IMPORTANCE:1\r\nX-MICROSOFT-DISALLOW-COUNTER:FALSE\r\nX-MS-OLK-APPTLASTSEQUENCE:2\r\nX-MS-OLK-APPTSEQTIME:20131111T115246Z\r\nX-MS-OLK-AUTOFILLLOCATION:FALSE\r\nX-MS-OLK-CONFTYPE:0\r\nX-MS-OLK-MWSURL:http://localhost:7070/alfresco/hello?calendar=calendar\r\nBEGIN:VALARM\r\nTRIGGER:-PT15M\r\nACTION:DISPLAY\r\nDESCRIPTION:Reminder\r\nEND:VALARM\r\nEND:VEVENT\r\n"
               // updated occurrence event info
               + "BEGIN:VEVENT\r\nCLASS:PUBLIC\r\nCREATED:20131111T115246Z\r\nDTEND:20131113T053000Z\r\nDTSTAMP:20131111T114214Z\r\nDTSTART:20131113T050000Z\r\nLAST-MODIFIED:20131111T115246Z\r\nRECURRENCE-ID:20131113T050000Z\r\nSEQUENCE:0\r\nSUMMARY:Occurrencehello\r\nTRANSP:OPAQUE\r\nUID:040000008200E00074C5B7101A82E0080000000000E06362E6DECE010000000000000000100000001EEA71ED0E2CAC4DABF45387DD35DE90\r\nX-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\nX-MS-OLK-APPTSEQTIME:20131111T114214Z\r\nBEGIN:VALARM\r\nTRIGGER:-PT15M\r\nACTION:DISPLAY\r\nDESCRIPTION:Reminder\r\nEND:VALARM\r\nEND:VEVENT\r\nEND:VCALENDAR\r\n";

       icalText = icalText.replaceAll("\r\n\t", "");
       icalText = icalText.replaceAll("\r\n ", "");
       Map<String, String> icalParams = ICalHelper.getICalParams(icalText);

       // Check that SUMMARY, DTEND, DTSTART parameters were not overridden by occurrence event
       assertTrue(icalParams.get("SUMMARY").equals("SeriesSubjectRenamed"));
       assertTrue(icalParams.get("DTEND").equals("20131111T083000"));
       assertTrue(icalParams.get("DTSTART").equals("20131111T080000"));

       // The second Outlook POST request that updates occurrence, which has been updated before. Outlook sets VTIMEZONE twice, we need the first one.
       icalText = "BEGIN:VCALENDAR\r\nPRODID:-//Microsoft Corporation//Outlook 14.0 MIMEDIR//EN\r\nVERSION:2.0\r\nMETHOD:REQUEST\r\nX-MS-OLK-FORCEINSPECTOROPEN:TRUE\r\n"
               + "BEGIN:VTIMEZONE\r\nTZID:E. Africa Standard Time\r\nBEGIN:STANDARD\r\nDTSTART:16010101T000000\r\nTZOFFSETFROM:+0400\r\nTZOFFSETTO:+0300\r\nEND:STANDARD\r\nEND:VTIMEZONE\r\n"
               + "BEGIN:VTIMEZONE\r\nTZID:Unnamed Time Zone 1\r\nBEGIN:STANDARD\r\nDTSTART:16010101T000000\r\nTZOFFSETFROM:+0300\r\nTZOFFSETTO:+0300\r\nEND:STANDARD\r\nEND:VTIMEZONE\r\n"
               + "BEGIN:VEVENT\r\nATTENDEE;CN=admin@alfresco.com;RSVP=TRUE:mailto:admin@alfresco.com\r\nCLASS:PUBLIC\r\nCREATED:20131111T135738Z\r\nDTEND;TZID=\"Unnamed Time Zone 1\":20131113T083000\r\nDTSTAMP:20131111T135739Z\r\nDTSTART;TZID=\"Unnamed Time Zone 1\":20131113T080000\r\nLAST-MODIFIED:20131111T135738Z\r\nLOCATION:location\r\nORGANIZER;CN=admin:mailto:admin@alfresco.com\r\nPRIORITY:5\r\nRECURRENCE-ID;TZID=\"E. Africa Standard Time\":20131113T080000\r\nSEQUENCE:9\r\nSUMMARY;LANGUAGE=ru:Occurrencehello\r\nTRANSP:OPAQUE\r\nUID:040000008200E00074C5B7101A82E0080000000000E06362E6DECE010000000000000000100000001EEA71ED0E2CAC4DABF45387DD35DE90\r\nX-MICROSOFT-CDO-BUSYSTATUS:BUSY\r\nX-MICROSOFT-CDO-IMPORTANCE:1\r\nX-MICROSOFT-DISALLOW-COUNTER:FALSE\r\nX-MS-OLK-APPTLASTSEQUENCE:8\r\nX-MS-OLK-APPTSEQTIME:20131111T135739Z\r\nX-MS-OLK-AUTOFILLLOCATION:FALSE\r\nX-MS-OLK-CONFTYPE:0\r\nX-MS-OLK-MWSURL:http://localhost:7070/alfresco/hello?calendar=calendar\r\nBEGIN:VALARM\r\nTRIGGER:-PT15M\r\nACTION:DISPLAY\r\nDESCRIPTION:Reminder\r\nEND:VALARM\r\nEND:VEVENT\r\nEND:VCALENDAR\r\n";

       icalText = icalText.replaceAll("\r\n\t", "");
       icalText = icalText.replaceAll("\r\n ", "");
       icalParams = ICalHelper.getICalParams(icalText);
       assertTrue(icalParams.get("TZ-TZID").equals("E. Africa Standard Time"));
   }

   private static class ICalHelper extends CalendarTimezoneHelper
   {
       protected static Map<String,String> getICalParams(String icalText)
       {
          return CalendarTimezoneHelper.getICalParams(icalText);
       }        
   }

   private static class RecurrenceHelper extends CalendarRecurrenceHelper
   {
      protected static void buildDailyRecurrences(Calendar currentDate, List<Date> dates, 
            Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int interval)
      {
          buildDailyRecurrences(currentDate, ONE_HOUR_MS, dates, params, onOrAfter, until, firstOnly, interval);
      }
      
      protected static void buildDailyRecurrences(Calendar currentDate, long duration, List<Date> dates, 
              Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int interval)
      {
         CalendarRecurrenceHelper.buildDailyRecurrences(
                 currentDate, duration, dates, params, onOrAfter, until, firstOnly, interval);
      }
      
      protected static void buildWeeklyRecurrences(Calendar currentDate, List<Date> dates, 
            Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int interval)
      {
          buildWeeklyRecurrences(currentDate, ONE_HOUR_MS, dates, params, onOrAfter, until, firstOnly, interval);
      }
      
      protected static void buildWeeklyRecurrences(Calendar currentDate, long duration, List<Date> dates, 
              Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int interval)
      {
         CalendarRecurrenceHelper.buildWeeklyRecurrences(
                 currentDate,  duration, dates, params, onOrAfter, until, firstOnly, interval);
      }
      
      protected static void buildMonthlyRecurrences(Calendar currentDate, List<Date> dates, 
            Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int interval)
      {
          buildMonthlyRecurrences(currentDate, ONE_HOUR_MS, dates, params, onOrAfter, until, firstOnly, interval);
      }
      
      protected static void buildMonthlyRecurrences(Calendar currentDate, long duration, List<Date> dates, 
              Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int interval)
      {
         CalendarRecurrenceHelper.buildMonthlyRecurrences(
                 currentDate,  duration, dates, params, onOrAfter, until, firstOnly, interval);
      }
      
      protected static void buildYearlyRecurrences(Calendar currentDate, List<Date> dates, 
            Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int interval)
      {
          buildYearlyRecurrences(currentDate, ONE_HOUR_MS, dates, params, onOrAfter, until, firstOnly, interval);
      }
      
      protected static void buildYearlyRecurrences(Calendar currentDate, long duration, List<Date> dates, 
              Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int interval)
      {
         CalendarRecurrenceHelper.buildYearlyRecurrences(
                 currentDate,  duration, dates, params, onOrAfter, until, firstOnly, interval);
      }
      
      protected static Map<String,String> fixOutlookRecurrenceQuirks(Map<String,String> params)
      {
          return CalendarRecurrenceHelper.fixOutlookRecurrenceQuirks(params);
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
