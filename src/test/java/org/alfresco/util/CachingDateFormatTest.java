/*
 * Copyright (C) 2005-2018 Alfresco Software Limited.
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CachingDateFormatTest
{
    private final LocalDateTime REFERENCE_DATE_TIME = LocalDateTime.of(2018, 4, 1, 10, 0); //2018-04-01 at 10:00am
    private final Locale defaultLocale = Locale.getDefault();

    @Before
    public void setUp()
    {
        CachingDateFormat.S_LOCAL_SOLR_DATETIME.remove();
    }

    @Test
    public void solrDatetimeFormat_shouldFormatTheMinDate()
    {
        Date shanghaiDate = testDate("Asia/Shanghai");
        SimpleDateFormat solrDatetimeFormat = CachingDateFormat.getSolrDatetimeFormatWithoutMsecs();

        String formattedDate = solrDatetimeFormat.format(shanghaiDate);

        assertThat(formattedDate,is("2018-04-01T02:00:00Z"));
    }

    @Test
    public void solrDatetimeFormat_allLocales_shouldReturnISO8601DateString()
    {
        for(Locale currentLocale:Locale.getAvailableLocales())
        {
            CachingDateFormat.S_LOCAL_SOLR_DATETIME.remove();
            Locale.setDefault(currentLocale);

            Date utcDate = testDate("UTC");
            SimpleDateFormat solrDatetimeFormat = CachingDateFormat.getSolrDatetimeFormat();

            String formattedDate = solrDatetimeFormat.format(utcDate);

            assertThat(formattedDate, is("2018-04-01T10:00:00.000Z"));
        }
    }

    @Test
    public void onlyDateFormatReturnsOnlyTheDatePart()
    {
        Date utcDate = testDate("UTC");

        SimpleDateFormat formatter = CachingDateFormat.getDateOnlyFormat();
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        assertEquals("2018-04-01", formatter.format(utcDate));
    }

    @Test
    public void onlyTimeFormatShouldReturnOnlyTheTimePart()
    {
        Date utcDate = testDate("UTC");

        SimpleDateFormat formatter = CachingDateFormat.getTimeOnlyFormat();
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        assertEquals("10:00:00", formatter.format(utcDate));
    }

    @Test
    public void dateTimeFormatShouldReturnDateAndTime()
    {
        Date utcDate = testDate("UTC");

        SimpleDateFormat formatter = CachingDateFormat.getDateFormat();
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        assertEquals("2018-04-01T10:00:00", formatter.format(utcDate));
    }

    @Test
    public void utcWithoutMsecsDatetimeFormat_shouldReturnStringsWithoutMsecs()
    {
        Date utcDate = testDate("UTC");

        SimpleDateFormat formatter = CachingDateFormat.getSolrDatetimeFormatWithoutMsecs();

        assertEquals("2018-04-01T10:00:00Z", formatter.format(utcDate));
    }

    @After 
    public void tearDown()
    {
       Locale.setDefault(defaultLocale); 
    }

    /**
     * Creates a test date using the given timezone id.
     *
     * @param zoneId the timezone id.
     * @return a test date using the given timezone id.
     */
    private Date testDate(String zoneId)
    {
        Instant utcInstant = REFERENCE_DATE_TIME.atZone(ZoneId.of(zoneId)).toInstant();
        return Date.from(utcInstant);
    }
}
