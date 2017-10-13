/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.service.cmr.repository;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * 
 * @see org.alfresco.service.cmr.repository.Period
 * @author Andreea Nechifor
 *
 */
public class PeriodTest extends TestCase
{

    public void testMapToPeriod() throws ParseException
    {
        Map<String, String> periodArgs = new HashMap<String, String>();
        periodArgs.put("periodType", "day");
        Period period = new Period(periodArgs);
        assertNotNull(period.getPeriodType());
        assertEquals("day", period.getPeriodType());
        assertNull(period.getExpression());

        periodArgs.put("expression", "0");
        period = new Period(periodArgs);
        assertNotNull(period.getPeriodType());
        assertNotNull(period.getExpression());
        assertEquals("day", period.getPeriodType());
        assertEquals("0", period.getExpression());

        try
        {
            periodArgs = null;
            period = new Period(periodArgs);
            fail("the source map must not be null");
        }
        catch (IllegalArgumentException e)
        {
            // Check the exception is source.
            assertTrue(e.getMessage().contains("Cannot create Period."));
        }

        try
        {
            periodArgs = new HashMap<String, String>();
            period = new Period(periodArgs);
            fail("the periodType must not be null");
        }
        catch (IllegalArgumentException e)
        {
            // Check the exception is periodType.
            assertTrue(e.getMessage().contains("Cannot create Period with null periodType"));
        }

    }

}
