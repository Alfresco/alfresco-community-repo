/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * Tests for the {@link CalendarRecurrenceHelper} class.
 * 
 * @author Matt Ward
 */
public class CalendarRecurrenceHelperTest
{
    @Test
    public void fixOutlookRecurrenceQuirks_GenuineMonthlyRecurrenceParamsLeftAsIs()
    {
        Map<String, String> in = Params.fromKVPs("BYMONTHDAY=24", "FREQ=MONTHLY", "INTERVAL=1");
        Map<String, String> out = CalendarRecurrenceHelper.fixOutlookRecurrenceQuirks(Params.fromMap(in));
        
        // A monthly recurring event specified correctly, will remain untouched.
        assertEquals(in, out);
        // Double check we're not just comparing in with itself.
        assertNotSame(in, out);
    }
    
    /**
     * ALF-18928: Yearly recurring event specified incorrectly (as monthly) by outlook.
     */
    @Test
    public void fixOutlookRecurrenceQuirks_MonthlyRecurrenceThatShouldBeYearly()
    {
        Map<String, String> in = Params.fromKVPs("BYMONTHDAY=25", "FREQ=MONTHLY", "INTERVAL=24", "BYMONTH=7");
        Map<String, String> out = CalendarRecurrenceHelper.fixOutlookRecurrenceQuirks(Params.fromMap(in));
        
        // A yearly recurring event that has been incorrectly specified as monthly, is fixed up.
        // Note FREQ and INTERVAL have been corrected.
        assertEquals(Params.fromKVPs("BYMONTHDAY=25", "FREQ=YEARLY", "INTERVAL=2", "BYMONTH=7"), out);
    }
    
    @Test
    public void fixOutlookRecurrenceQuirks_YearlyRecurrenceParamsLeftAsIs()
    {
        Map<String, String> in = Params.fromKVPs("BYMONTHDAY=25", "FREQ=YEARLY", "INTERVAL=2", "BYMONTH=7");
        Map<String, String> out = CalendarRecurrenceHelper.fixOutlookRecurrenceQuirks(Params.fromMap(in));
        
        // A yearly recurring event specified correctly, will remain untouched.
        assertEquals(in, out);
        // Double check we're not just comparing in with itself.
        assertNotSame(in, out);
    }

    // MNT-10006 fix. Validates the patterns 'weekday', 'weekend day', 'day'
    @Test
    public void fixOutLookRecurrenceQuirks_weekdayRecurrenceRule()
    {
        Map<String, String> recurrenceRule = Params.fromKVPs("FREQ=MONTHLY", "BYDAY=MO,TU,WE,TH,FR", "INTERVAL=1", "BYSETPOS=1");
        Map<String, String> fixedRecurrenceRule = CalendarRecurrenceHelper.fixOutlookRecurrenceQuirks(Params.fromMap(recurrenceRule));

        assertEquals(null, fixedRecurrenceRule.get("BYDAY"));
        assertEquals(null, fixedRecurrenceRule.get("BYSETPOS"));
        assertEquals("1", fixedRecurrenceRule.get("BYWEEKDAY"));
        assertEquals("MO,TU,WE,TH,FR", fixedRecurrenceRule.get("WEEKDAYS"));
    }

    @Test
    public void fixOutLookRecurrenceQuirks_weekendDayRecurrenceRule()
    {
        Map<String, String> recurrenceRule = Params.fromKVPs("FREQ=MONTHLY", "BYDAY=SU,SA", "INTERVAL=1", "BYSETPOS=1");
        Map<String, String> fixedRecurrenceRule = CalendarRecurrenceHelper.fixOutlookRecurrenceQuirks(Params.fromMap(recurrenceRule));

        assertEquals(null, fixedRecurrenceRule.get("BYDAY"));
        assertEquals(null, fixedRecurrenceRule.get("BYSETPOS"));
        assertEquals("1", fixedRecurrenceRule.get("BYWEEKENDDAY"));
        assertEquals("SU,SA", fixedRecurrenceRule.get("WEEKENDS"));
    }

    @Test
    public void fixOutLookRecurrenceQuirks_dayRecurrenceRule()
    {
        Map<String, String> recurrenceRule = Params.fromKVPs("FREQ=MONTHLY", "BYDAY=SU,MO,TU,WE,TH,FR,SA", "INTERVAL=1", "BYSETPOS=1");
        Map<String, String> fixedRecurrenceRule = CalendarRecurrenceHelper.fixOutlookRecurrenceQuirks(Params.fromMap(recurrenceRule));

        assertEquals("1", fixedRecurrenceRule.get("BYANYDAY"));
        assertEquals(null, fixedRecurrenceRule.get("BYSETPOS"));
        assertEquals(null, fixedRecurrenceRule.get("BYMONTHDAY"));
        assertEquals("SU,MO,TU,WE,TH,FR,SA", fixedRecurrenceRule.get("DAY"));
    }

    /**
     * Inner class just here to make the tests more readable.
     */
    private static class Params extends HashMap<String, String>
    {
        private static final long serialVersionUID = 1648766671461600951L;

        private Params()
        {
        }
        
        private Params(Map<String, String> params)
        {
            super(params);
        }
        
        private static Params fromMap(Map<String, String> params)
        {
            return new Params(params);
        }
        
        private static Params fromKVPs(String... keyValuePairs)
        {
            Params params = new Params();
            for (String kvp : keyValuePairs)
            {
                String[] split = kvp.split("=");
                assertEquals("Key/value pair is not valid: " + kvp, 2, split.length);
                params.put(split[0], split[1]);
            }
            return params;
        }
    }
}
