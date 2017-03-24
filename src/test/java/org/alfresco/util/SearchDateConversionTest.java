/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.util;

import static org.junit.Assert.*;
import org.alfresco.service.cmr.search.IntervalSet;
import org.joda.time.DateTimeZone;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Basic calls
 */
public class SearchDateConversionTest
{
    SearchDateConversion subject = new SearchDateConversion();

    @Test
    public void parseDateString() throws Exception
    {
        setDefaults();
        Pair<Date, Integer>  result = subject.parseDateString("2017");
        assertEquals(Calendar.YEAR, result.getSecond().intValue());
        assertEquals(1483228800000l, result.getFirst().getTime());

        result = subject.parseDateString("2017-12");
        assertEquals(Calendar.MONTH, result.getSecond().intValue());
        assertEquals(1512086400000l, result.getFirst().getTime());

        result = subject.parseDateString("2017-12-12");
        assertEquals(Calendar.DAY_OF_MONTH, result.getSecond().intValue());
        assertEquals(1513036800000l, result.getFirst().getTime());

        result = subject.parseDateString("NOW");
        assertEquals(Calendar.MILLISECOND, result.getSecond().intValue());

        result = subject.parseDateString("MIN");
        assertEquals(Calendar.MILLISECOND, result.getSecond().intValue());

        result = subject.parseDateString("TODAY");
        assertEquals(Calendar.DAY_OF_MONTH, result.getSecond().intValue());

        result = subject.parseDateString("MAX");
        assertEquals(Calendar.MILLISECOND, result.getSecond().intValue());

        result = subject.parseDateString("NONSENSE");
        assertNull(result);

        result = subject.parseDateString("NOW/YEAR");
        assertNull(result);

        result = subject.parseDateString("*");
        assertNull(result);
    }

    @Test
    public void getDateEnd() throws Exception
    {
        setDefaults();
        Pair<Date, Integer> result = subject.parseDateString("2017-12");
        assertEquals("2017-12-31T23:59:59.999Z", subject.getDateEnd(result));

        result = subject.parseDateString("2017-12-12");
        assertEquals("2017-12-12T23:59:59.999Z", subject.getDateEnd(result));

        result = subject.parseDateString("2017");
        assertEquals("2017-12-31T23:59:59.999Z", subject.getDateEnd(result));
    }

    @Test
    public void getDateStart() throws Exception
    {
        setDefaults();
        Pair<Date, Integer> result = subject.parseDateString("2017-12");
        assertEquals("2017-12-01T00:00:00.000Z", subject.getDateStart(result));

        result = subject.parseDateString("2017-12-12");
        assertEquals("2017-12-12T00:00:00.000Z", subject.getDateStart(result));

        result = subject.parseDateString("2017");
        assertEquals("2017-01-01T00:00:00.000Z", subject.getDateStart(result));
    }

    @Test
    public void testIntervalDates() throws UnsupportedEncodingException
    {
        setDefaults();
        IntervalSet intervalSet = new IntervalSet("1", "10", "just numbers", false, true);
        IntervalSet validated = subject.parseDateInterval(intervalSet, false);
        assertEquals(intervalSet, validated);

        intervalSet = new IntervalSet("2006", "2010", "years", true, true);
        validated = subject.parseDateInterval(intervalSet, true);
        assertEquals("2006-01-01T00:00:00.000Z", validated.getStart());
        assertTrue(validated.isStartInclusive());
        assertEquals("2010-12-31T23:59:59.999Z", validated.getEnd());
        assertTrue(validated.isEndInclusive());

        intervalSet = new IntervalSet("2006", "2010", "years", false, false);
        validated = subject.parseDateInterval(intervalSet, true);
        assertEquals("2006-12-31T23:59:59.999Z", validated.getStart());
        assertFalse(validated.isStartInclusive());
        assertEquals("2010-01-01T00:00:00.000Z", validated.getEnd());
        assertFalse(validated.isEndInclusive());

        intervalSet = new IntervalSet("2006-09", "2010-03", "months", true, true);
        validated = subject.parseDateInterval(intervalSet, true);
        assertEquals("2006-09-01T00:00:00.000Z", validated.getStart());
        assertTrue(validated.isStartInclusive());
        assertEquals("2010-03-31T23:59:59.999Z", validated.getEnd());
        assertTrue(validated.isEndInclusive());

        intervalSet = new IntervalSet("2006-09", "2010-03", "months", false, false);
        validated = subject.parseDateInterval(intervalSet, true);
        assertEquals("2006-09-30T23:59:59.999Z", validated.getStart());
        assertFalse(validated.isStartInclusive());
        assertEquals("2010-03-01T00:00:00.000Z", validated.getEnd());
        assertFalse(validated.isEndInclusive());

        intervalSet = new IntervalSet("2017-09-01", "2017-09-30", "sept", true, true);
        validated = subject.parseDateInterval(intervalSet, true);
        assertEquals("2017-09-01T00:00:00.000Z", validated.getStart());
        assertTrue(validated.isStartInclusive());
        assertEquals("2017-09-30T23:59:59.999Z", validated.getEnd());
        assertTrue(validated.isEndInclusive());

        intervalSet = new IntervalSet("2017-08-31", "2017-10-01", "sept", false, false);
        validated = subject.parseDateInterval(intervalSet, true);
        assertEquals("2017-08-31T23:59:59.999Z", validated.getStart());
        assertFalse(validated.isStartInclusive());
        assertEquals("2017-10-01T00:00:00.000Z", validated.getEnd());
        assertFalse(validated.isEndInclusive());
    }


    protected void setDefaults()
    {
        DateTimeZone.setDefault(DateTimeZone.UTC); //Joda
        Locale.setDefault(Locale.UK);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}