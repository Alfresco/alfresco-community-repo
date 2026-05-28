/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch.query.language.afts;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Unit tests for the {@link DateMathConverter}. */
public class DateMathConverterTest
{
    @Test
    public void testNoDateMath()
    {
        String esDateMath = DateMathConverter.convert("MIN");
        assertEquals("MIN", esDateMath);
    }

    @Test
    public void testRounding()
    {
        String esDateMath = DateMathConverter.convert("NOW/SECOND");
        assertEquals("now/s", esDateMath);
    }

    @Test
    public void testISODateGetsAnchor()
    {
        String esDateMath = DateMathConverter.convert("2021-11-22/MINUTE");
        assertEquals("2021-11-22||/m", esDateMath);
    }

    @Test
    public void testDoubleRounding()
    {
        String esDateMath = DateMathConverter.convert("NOW/WEEK/MONTH");
        assertEquals("now/w/M", esDateMath);
    }

    @Test
    public void testNextDay()
    {
        String esDateMath = DateMathConverter.convert("2021-11-22+1DAY");
        assertEquals("2021-11-22||+1d", esDateMath);
    }

    @Test
    public void testTwoHoursAgo()
    {
        String esDateMath = DateMathConverter.convert("2021-11-22T16:15:21Z-2HOURS");
        assertEquals("2021-11-22T16:15:21Z||-2h", esDateMath);
    }

    @Test
    public void testCombinationSingularDurations()
    {
        String esDateMath = DateMathConverter.convert("NOW/SECOND+1MINUTE/HOUR-1DAY/WEEK+1MONTH/YEAR");
        assertEquals("now/s+1m/h-1d/w+1M/y", esDateMath);
    }

    @Test
    public void testCombinationPluralDurations()
    {
        String esDateMath = DateMathConverter.convert("2021-11-22T16:15:21.123-99SECONDS/MINUTES-99HOURS/DAYS-99WEEKS/MONTHS-99YEARS");
        assertEquals("2021-11-22T16:15:21.123||-99s/m-99h/d-99w/M-99y", esDateMath);
    }
}
