/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.solr;

import static org.junit.Assert.*;

import org.alfresco.util.Pair;
import org.joda.time.LocalDate;
import org.junit.Test;

/**
 * Tests StatsGet Webscript
 *
 * @author Gethin James
 * @since 5.0
 */
public class StatsGetTest
{

    @Test
    public void testGetStartAndEndDates()
    {
        LocalDate currentDate = LocalDate.now();
        Pair<LocalDate, LocalDate> dates = StatsGet.getStartAndEndDates(null, null);
        assertNull(dates);
        
        String test1 = "2014-05-01";
        String test2 = "2015-06-30";
        dates = StatsGet.getStartAndEndDates(test1, null);
        assertNotNull(dates);
        assertEquals(2014, dates.getFirst().getYear());
        assertEquals(5, dates.getFirst().getMonthOfYear());
        assertEquals(1, dates.getFirst().getDayOfMonth());
        assertEquals(currentDate, dates.getSecond());
        
        dates = StatsGet.getStartAndEndDates(null, test2);
        assertNull(dates);
        
        dates = StatsGet.getStartAndEndDates(test1, test2);
        assertNotNull(dates);
        assertEquals(2014, dates.getFirst().getYear());
        assertEquals(5, dates.getFirst().getMonthOfYear());
        assertEquals(1, dates.getFirst().getDayOfMonth());
        assertNotNull(dates);
        assertEquals(2015, dates.getSecond().getYear());
        assertEquals(6, dates.getSecond().getMonthOfYear());
        assertEquals(30, dates.getSecond().getDayOfMonth());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testGetStartAndEndDatesWithRubbish()
    {      
        Pair<LocalDate, LocalDate> dates = StatsGet.getStartAndEndDates("rubbish", "more");
        assertNotNull(dates);
    }

}
