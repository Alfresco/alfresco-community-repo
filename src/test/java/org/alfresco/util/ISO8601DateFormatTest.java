/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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
package org.alfresco.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;

public class ISO8601DateFormatTest extends TestCase
{
    public void testConversion()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        String test = "2005-09-16T17:01:03.456+01:00";
        String test2 = "1801-09-16T17:01:03.456+01:00";
        // convert to a date
        Date date = ISO8601DateFormat.parse(test);
        Date date2 = ISO8601DateFormat.parse(test2);
        // get the string form
        String strDate = ISO8601DateFormat.format(date);
        String strDate2 = ISO8601DateFormat.format(date2);
        // convert back to a date from the converted string
        Date dateAfter = ISO8601DateFormat.parse(strDate);
        Date dateAfter2 = ISO8601DateFormat.parse(strDate2);
        // make sure the date objects match, test this instead of the
        // string as the string form will be different in different
        // locales
        assertEquals(date, dateAfter);
        assertEquals(date2, dateAfter2);
    }
    
    public void testGetCalendarMethod()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        Calendar calendarGMT = ISO8601DateFormat.getCalendar();
        
        TimeZone.setDefault(TimeZone.getTimeZone("BST"));
        Calendar calendarBST = ISO8601DateFormat.getCalendar();
        
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        Calendar calendarGMT1 = ISO8601DateFormat.getCalendar();
        
        assertNotSame(calendarGMT, calendarBST);
        assertSame(calendarGMT, calendarGMT1);
    }

    public void testDateParser()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        String test = "2005-09-16T17:01:03.456+01:00";
        String test2 = "1801-09-16T17:01:03.456+01:00";

        String isoFormattedDate = "2005-09-16T16:01:03.456Z";
        String isoFormattedDate2 = "1801-09-16T16:01:03.456Z";

        Date testDate = getDateValue(2005, 9, 16, 17, 1, 3, 456, 60);
        Date testDate2 = getDateValue(1801, 9, 16, 17, 1, 3, 456, 60);

        // convert to a date
        Date date = ISO8601DateFormat.parse(test);
        Date date2 = ISO8601DateFormat.parse(test2);
        // check converted to date value
        assertEquals(testDate, date);
        assertEquals(testDate2, date2);

        // get the string form
        String strDate = ISO8601DateFormat.format(date);
        String strDate2 = ISO8601DateFormat.format(date2);
        // check the date converted to sting
        assertEquals(isoFormattedDate, strDate);
        assertEquals(isoFormattedDate2, strDate2);
    }

    private Date getDateValue(int year, int month, int day, int hours, int minutes, int sec, int msec, int offsetInMinutes)
    {
        // minute in millis
        int millisInMinute = 1000 * 60;

        GregorianCalendar gc = new GregorianCalendar();

        // set correct offset
        String[] tzArray = TimeZone.getAvailableIDs(millisInMinute * offsetInMinutes);
        if (tzArray.length > 0)
        {
            gc.setTimeZone(TimeZone.getTimeZone(tzArray[0]));
        }

        // set date
        gc.set(GregorianCalendar.YEAR, year);
        gc.set(GregorianCalendar.MONTH, month - 1);
        gc.set(GregorianCalendar.DAY_OF_MONTH, day);
        gc.set(GregorianCalendar.HOUR_OF_DAY, hours);
        gc.set(GregorianCalendar.MINUTE, minutes);
        gc.set(GregorianCalendar.SECOND, sec);
        gc.set(GregorianCalendar.MILLISECOND, msec);

        return gc.getTime();
    }

    public void testMiliseconds()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        // ALF-3803 bug fix, milliseconds are optional
       String testA   = "2005-09-16T17:01:03.456Z";
       String testB   = "2005-09-16T17:01:03Z";
       String testBms = "2005-09-16T17:01:03.000Z";
       String testC   = "1801-09-16T17:01:03Z";
       String testCms = "1801-09-16T17:01:03.000Z";
       
       Date dateA = ISO8601DateFormat.parse(testA);
       Date dateB = ISO8601DateFormat.parse(testB);
       Date dateC = ISO8601DateFormat.parse(testC);
       
       assertEquals(testA, ISO8601DateFormat.format(dateA));
      
       assertEquals(testBms, ISO8601DateFormat.format(dateB));
       
       assertEquals(testCms, ISO8601DateFormat.format(dateC));
       
       // The official ISO 8601.2004 spec doesn't say much helpful about milliseconds
       // The W3C version <http://www.w3.org/TR/NOTE-datetime> says it's up to different
       //  implementations to put bounds on them
       // We can silently ignore anything beyond 3 digits, see ALF-14687
       String testCms3 = "2005-09-16T17:01:03.123+01:00";
       String testCms4 = "2005-09-16T17:01:03.1234+01:00";
       String testCms5 = "2005-09-16T17:01:03.12345+01:00";
       String testCms6 = "2005-09-16T17:01:03.123456+01:00";
       String testCms7 = "2005-09-16T17:01:03.1234567+01:00";
       
       Date testCDate = ISO8601DateFormat.parse(testCms3);
       assertEquals(testCDate, ISO8601DateFormat.parse(testCms4));
       assertEquals(testCDate, ISO8601DateFormat.parse(testCms5));
       assertEquals(testCDate, ISO8601DateFormat.parse(testCms6));
       assertEquals(testCDate, ISO8601DateFormat.parse(testCms7));
    }
    
    public void testTimezones()
    {
       TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
       Date date = null;
       
       TimeZone  tz = TimeZone.getTimeZone("Australia/Sydney");
       String testSydney = "2011-02-04T16:13:14";
       String testUTC    = "2011-02-04T05:13:14.000Z";
       
       //Sydney
       date = ISO8601DateFormat.parse(testSydney, tz);
       assertEquals(testUTC, ISO8601DateFormat.format(date));
       
       // Check with ms too
       date = ISO8601DateFormat.parse(testSydney + ".000", tz);
       assertEquals(testUTC, ISO8601DateFormat.format(date));
       
       //Sydney with an offset and timezone
       date = ISO8601DateFormat.parse(testSydney+"+11:00", tz);
       assertEquals(testUTC, ISO8601DateFormat.format(date));
       
       // Check with ms too
       date = ISO8601DateFormat.parse(testSydney + ".000"+"+11:00", tz);
       assertEquals(testUTC, ISO8601DateFormat.format(date));
    }
    
    public void testToZulu(){
        String base = "2011-02-04T16:13:14.000";
        String zulu = base + "Z";
        String utc0 = base + "+00:00";
        String utc1 = "2011-02-04T17:13:14" + "+01:00";
        String utcMinus1 = "2011-02-04T15:13:14" + "-01:00";
        
        assertEquals(zulu, ISO8601DateFormat.formatToZulu(zulu));
        assertEquals(zulu, ISO8601DateFormat.formatToZulu(utc1));
        assertEquals(zulu, ISO8601DateFormat.formatToZulu(utc0));
        assertEquals(zulu, ISO8601DateFormat.formatToZulu(utcMinus1));
    }
    
    public void testDayOnly()
    {
        Date date = null;
        
        // Test simple parsing
        TimeZone tz = TimeZone.getTimeZone("Europe/London");
        date = ISO8601DateFormat.parseDayOnly("2012-05-21", tz);
        
        Calendar cal = Calendar.getInstance(tz);
        cal.setTime(date);
        
        // Check date and time component
        assertEquals(2012, cal.get(Calendar.YEAR));
        assertEquals(4, cal.get(Calendar.MONTH));
        assertEquals(21, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, cal.get(Calendar.HOUR));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
        
        // Check time is ignored on full ISO8601-string
        date = ISO8601DateFormat.parseDayOnly("2012-05-21T12:13:14Z", tz);
        cal = Calendar.getInstance(tz);
        cal.setTime(date);
        
        assertEquals(2012, cal.get(Calendar.YEAR));
        assertEquals(4, cal.get(Calendar.MONTH));
        assertEquals(21, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, cal.get(Calendar.HOUR));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
        
        // Check year signs
        date = ISO8601DateFormat.parseDayOnly("+2012-05-21", tz);
        cal = Calendar.getInstance(tz);
        cal.setTime(date);
        assertEquals(GregorianCalendar.AD, cal.get(Calendar.ERA));
        
        date = ISO8601DateFormat.parseDayOnly("-2012-05-21", tz);
        cal = Calendar.getInstance(tz);
        cal.setTime(date);
        assertEquals(GregorianCalendar.BC, cal.get(Calendar.ERA));
        
        
        // Check illegal format
        try
        {
           ISO8601DateFormat.parseDayOnly("2011-02-0", tz);
           fail("Exception expected on illegal format");
        }
        catch(AlfrescoRuntimeException e) {}
        try
        {
           ISO8601DateFormat.parseDayOnly("201a-02-02", tz);
           fail("Exception expected on illegal format");
        }
        catch(AlfrescoRuntimeException e) {}
    }
    
    public void testDSTParser()
    {
        TimeZone tz = TimeZone.getTimeZone("America/Sao_Paulo");
        TimeZone.setDefault(tz);
        // MNT-15454: This date is invalid as the 00:00 hour became 01:00 because of daylight saving time.
        String test1 = "2014-10-19T";
        String test2 = "2014-10-19T00:01:01.000";
        
        String isoFormattedDate = "2014-10-19T03:00:00.000Z";
        
        // Sun Oct 19 01:00:00 BRST 2014
        Date testDate = getDateValue(2014, 10, 19, 0, 0, 0, 0, - 3*60);
        // convert to a date
        Date date = ISO8601DateFormat.parse(test1, tz);
        // Check converted to date value
        assertEquals(testDate, date);
        
        // Convert to a date
        date = ISO8601DateFormat.parse(test2, tz);
        // Check converted to date value
        assertEquals(testDate, date);
        
        // Get the string form
        String strDate = ISO8601DateFormat.format(date);
        // Check the date converted to sting
        assertEquals(isoFormattedDate, strDate);
    }
}
