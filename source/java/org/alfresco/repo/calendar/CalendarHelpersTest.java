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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.calendar.CalendarEntryDTO;
import org.alfresco.service.cmr.calendar.CalendarRecurrenceHelper;
import org.alfresco.service.cmr.calendar.CalendarService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

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
      Calendar c20110719_0000 = Calendar.getInstance();
      Calendar c20110719_1000 = Calendar.getInstance();
      Calendar c20110720_0000 = Calendar.getInstance();
      Calendar c20110721_0000 = Calendar.getInstance();
      c20110719_0000.set(2011, 07, 19, 0, 0, 0);
      c20110719_1000.set(2011, 07, 19, 1, 0, 0);
      c20110720_0000.set(2011, 07, 20, 0, 0, 0);
      c20110721_0000.set(2011, 07, 21, 0, 0, 0);
      
      CalendarEntryDTO entry = new CalendarEntryDTO();
      
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
            true, 1
      );
      assertEquals(0, dates.size());
      
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildDailyRecurrences(
            currentDate, dates, null,
            date(2011,7,10), date(2011,7,15),
            false, 1
      );
      assertEquals(0, dates.size());
      
      
      // From today
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildDailyRecurrences(
            currentDate, dates, null,
            date(2011,7,19), date(2011,7,25),
            true, 1
      );
      assertEquals(1, dates.size());
      assertEquals("2011-07-19", dateFmt.format(dates.get(0)));
      
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildDailyRecurrences(
            currentDate, dates, null,
            date(2011,7,19), date(2011,7,25),
            false, 1
      );
      assertEquals(6, dates.size());
      assertEquals("2011-07-19", dateFmt.format(dates.get(0)));
      assertEquals("2011-07-24", dateFmt.format(dates.get(5)));
      
      
      // Dates in the future, goes from then
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildDailyRecurrences(
            currentDate, dates, null,
            date(2011,7,20), date(2011,7,30),
            true, 1
      );
      assertEquals(1, dates.size());
      assertEquals("2011-07-20", dateFmt.format(dates.get(0)));
      
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildDailyRecurrences(
            currentDate, dates, null,
            date(2011,7,20), date(2011,7,30),
            false, 1
      );
      assertEquals(10, dates.size());
      assertEquals("2011-07-20", dateFmt.format(dates.get(0)));
      assertEquals("2011-07-29", dateFmt.format(dates.get(9)));
      
      
      // With no end date but only first, check it behaves
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildDailyRecurrences(
            currentDate, dates, null,
            date(2011,7,19), null,
            true, 1
      );
      assertEquals(1, dates.size());
      assertEquals("2011-07-19", dateFmt.format(dates.get(0)));
      
      dates.clear();
      currentDate.set(2011,7-1,19,10,30);
      RecurrenceHelper.buildDailyRecurrences(
            currentDate, dates, null,
            date(2011,7,20), null,
            true, 1
      );
      assertEquals(1, dates.size());
      assertEquals("2011-07-20", dateFmt.format(dates.get(0)));
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
   
   private static class RecurrenceHelper extends CalendarRecurrenceHelper
   {
      protected static void buildDailyRecurrences(Calendar currentDate, List<Date> dates, 
            Map<String,String> params, Date onOrAfter, Date until, boolean firstOnly, int interval)
      {
         CalendarRecurrenceHelper.buildDailyRecurrences(
               currentDate, dates, params, onOrAfter, until, firstOnly, interval);
      }      
   }
}
