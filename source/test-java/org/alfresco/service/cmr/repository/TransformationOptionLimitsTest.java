/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.service.cmr.repository;


import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Test TransformationOptionLimits
 */
public class TransformationOptionLimitsTest
{
    private TransformationOptionLimits limits;
    
    @Before
    public void setUp() throws Exception
    {
        limits = new TransformationOptionLimits();
    }

    @Test
    public void testTimeoutMs() throws Exception
    {
        long value = 1234;
        limits.setTimeoutMs(value);
        long actual = limits.getTimeoutMs();
        assertEquals("Getter did not return set value", value, actual);
    }

    @Test
    public void testReadLimitTimeMs() throws Exception
    {
        long value = 1234;
        limits.setReadLimitTimeMs(value);
        long actual = limits.getReadLimitTimeMs();
        assertEquals("Getter did not return set value", value, actual);
    }

    @Test
    public void testMaxSourceSizeKBytes() throws Exception
    {
        long value = 1234;
        limits.setMaxSourceSizeKBytes(value);
        long actual = limits.getMaxSourceSizeKBytes();
        assertEquals("Getter did not return set value", value, actual);
    }

    @Test
    public void testReadLimitKBytes() throws Exception
    {
        long value = 1234;
        limits.setReadLimitKBytes(value);
        long actual = limits.getReadLimitKBytes();
        assertEquals("Getter did not return set value", value, actual);
    }

    @Test
    public void testMaxPages() throws Exception
    {
        int value = 1234;
        limits.setMaxPages(value);
        int actual = limits.getMaxPages();
        assertEquals("Getter did not return set value", value, actual);
    }

    @Test
    public void testPageLimit() throws Exception
    {
        int value = 1234;
        limits.setPageLimit(value);
        int actual = limits.getPageLimit();
        assertEquals("Getter did not return set value", value, actual);
    }
    
    @Test
    public void testTimeException() throws Exception
    {
        String message = null;
        limits.setTimeoutMs(1);
        try
        {
            limits.setReadLimitTimeMs(1);
        }
        catch (IllegalArgumentException e)
        {
            message = e.getMessage();
        }
        assertEquals("Wrong exception message", TransformationOptionLimits.TIME_MESSAGE, message);
    }
    
    @Test
    public void testKBytesException() throws Exception
    {
        String message = null;
        limits.setMaxSourceSizeKBytes(1);
        try
        {
            limits.setReadLimitKBytes(1);
        }
        catch (IllegalArgumentException e)
        {
            message = e.getMessage();
        }
        assertEquals("Wrong exception message", TransformationOptionLimits.KBYTES_MESSAGE, message);
    }
    
    @Test
    public void testPageException() throws Exception
    {
        String message = null;
        limits.setPageLimit(1);
        try
        {
            limits.setMaxPages(1);
        }
        catch (IllegalArgumentException e)
        {
            message = e.getMessage();
        }
        assertEquals("Wrong exception message", TransformationOptionLimits.PAGES_MESSAGE, message);
    }

    @Test
    public void testMapMax() throws Exception
    {
        limits.setTimeoutMs(123);
        limits.setMaxSourceSizeKBytes(456);
        limits.setMaxPages(789);
        
        Map<String, Object> optionsMap = new HashMap<String, Object>();
        limits.toMap(optionsMap);
        
        TransformationOptionLimits actual = new TransformationOptionLimits();
        actual.set(optionsMap);

        assertEquals("Did not match original values", limits, actual);
    }

    @Test
    public void testMapLimit() throws Exception
    {
        limits.setReadLimitTimeMs(123);
        limits.setReadLimitKBytes(456);
        limits.setPageLimit(789);
        
        Map<String, Object> optionsMap = new HashMap<String, Object>();
        limits.toMap(optionsMap);
        
        TransformationOptionLimits actual = new TransformationOptionLimits();
        actual.set(optionsMap);

        assertEquals("Did not match original values", limits, actual);
    }

    @Test
    public void testTimePair() throws Exception
    {
        int value = 1234;
        limits.setTimeoutMs(value);
        
        long actual = limits.getTimePair().getMax();
        
        assertEquals("Returned TransformationOptionPair did not contain set value", value, actual);
    }


    @Test
    public void testKBytesPair() throws Exception
    {
        int value = 1234;
        limits.setMaxSourceSizeKBytes(value);
        
        long actual = limits.getKBytesPair().getMax();
        
        assertEquals("Returned TransformationOptionPair did not contain set value", value, actual);
    }


    @Test
    public void testPagePair() throws Exception
    {
        int value = 1234;
        limits.setMaxPages(value);
        
        long actual = limits.getPagesPair().getMax();
        
        assertEquals("Returned TransformationOptionPair did not contain set value", value, actual);
    }
    
    @Test
    public void testCombineOrder() throws Exception
    {
        limits.setReadLimitTimeMs(123);
        limits.setReadLimitKBytes(45);
        limits.setMaxPages(789);

        TransformationOptionLimits second = new TransformationOptionLimits();
        second.setReadLimitTimeMs(12);
        second.setReadLimitKBytes(456);
        second.setMaxPages(789);
        
        TransformationOptionLimits combined = limits.combine(second);
        TransformationOptionLimits combinedOtherWay = second.combine(limits);
        assertEquals("The combine order should not matter", combined, combinedOtherWay);
    }
    
    @Test
    public void testCombine() throws Exception
    {
        limits.setReadLimitTimeMs(123);
        limits.setReadLimitKBytes(45);
        limits.setPageLimit(789);

        TransformationOptionLimits second = new TransformationOptionLimits();
        second.setTimeoutMs(12);
        second.setMaxSourceSizeKBytes(456);
        second.setMaxPages(789);
        
        TransformationOptionLimits combined = limits.combine(second);

        assertEquals("Expected the lower value", 12, combined.getTimeoutMs());       // max <
        assertEquals("Expected the lower value", 45, combined.getReadLimitKBytes()); // limit <
        assertEquals("Expected the lower value", 789, combined.getMaxPages());       // max =
        
        assertEquals("Expected -1 as max is set", -1, combined.getReadLimitTimeMs());       // max <
        assertEquals("Expected -1 as limit is set", -1, combined.getMaxSourceSizeKBytes()); // limit <
        assertEquals("Expected -1 as limit is the same", -1, combined.getPageLimit());      // max =
    }
    
    @Test
    public void testCombineLimits() throws Exception
    {
        limits.setReadLimitTimeMs(123);
        limits.setReadLimitKBytes(45);
        limits.setPageLimit(789);

        TransformationOptionLimits second = new TransformationOptionLimits();
        second.setReadLimitTimeMs(12);
        second.setReadLimitKBytes(-1);
        second.setPageLimit(789);
        
        TransformationOptionLimits combined = limits.combine(second);

        assertEquals("Expected the lower value", 12, combined.getReadLimitTimeMs());
        assertEquals("Expected the lower value", 45, combined.getReadLimitKBytes());
        assertEquals("Expected the lower value", 789, combined.getPageLimit());
    }
    
    @Test
    public void testCombineUpper() throws Exception
    {
        limits.setReadLimitTimeMs(123);
        limits.setReadLimitKBytes(45);
        limits.setPageLimit(789);

        TransformationOptionLimits second = new TransformationOptionLimits();
        second.setTimeoutMs(12);
        second.setMaxSourceSizeKBytes(456);
        second.setMaxPages(789);
        
        TransformationOptionLimits combined = limits.combineUpper(second);

        assertEquals("Expected -1 as only one max value was set", -1, combined.getTimeoutMs());
        assertEquals("Expected -1 as only one max value was set", -1, combined.getMaxSourceSizeKBytes());
        assertEquals("Expected -1 as only one max value was set", -1, combined.getMaxPages());
        
        assertEquals("Expected -1 as only one limit value was set", -1, combined.getReadLimitTimeMs());
        assertEquals("Expected -1 as only one limit value was set", -1, combined.getReadLimitKBytes());
        assertEquals("Expected -1 as only one limit value was set", -1, combined.getPageLimit());
    }
    
    @Test
    public void testCombineUpperLimits() throws Exception
    {
        limits.setReadLimitTimeMs(123);
        limits.setReadLimitKBytes(45);
        limits.setPageLimit(789);

        TransformationOptionLimits second = new TransformationOptionLimits();
        second.setReadLimitTimeMs(12);
        second.setReadLimitKBytes(-1);
        second.setPageLimit(789);
        
        TransformationOptionLimits combined = limits.combineUpper(second);

        assertEquals("Expected the higher value", 123, combined.getReadLimitTimeMs());
        assertEquals("Expected -1 as only one limit value was set", -1, combined.getReadLimitKBytes());
        assertEquals("Expected the higher value", 789, combined.getPageLimit());
    }
    
    @Test
    public void testCombineDynamic() throws Exception
    {
        limits.setReadLimitTimeMs(123);
        limits.setReadLimitKBytes(45);
        limits.setMaxPages(789);

        TransformationOptionLimits second = new TransformationOptionLimits();
        second.setReadLimitTimeMs(12);
        second.setReadLimitKBytes(456);
        second.setMaxPages(789);
        
        TransformationOptionLimits combined = limits.combine(second);
        
        // Test dynamic change of value
        limits.setReadLimitKBytes(4560);
        assertEquals("Expected the lower value", 456, combined.getReadLimitKBytes());
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void testCombineSetTimeoutMs() throws Exception
    {
        TransformationOptionLimits combined = limits.combine(limits); // may combine with itself
        combined.setTimeoutMs(1);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testCombineSetReadLimitTimeMs() throws Exception
    {
        TransformationOptionLimits combined = limits.combine(limits); // may combine with itself
        combined.setReadLimitTimeMs(1);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testCombineSetMaxSourceSizeKBytes() throws Exception
    {
        TransformationOptionLimits combined = limits.combine(limits); // may combine with itself
        combined.setMaxSourceSizeKBytes(1);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testCombineSetReadLimitKBytes() throws Exception
    {
        TransformationOptionLimits combined = limits.combine(limits); // may combine with itself
        combined.setReadLimitKBytes(1);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testCombineSetMaxPages() throws Exception
    {
        TransformationOptionLimits combined = limits.combine(limits); // may combine with itself
        combined.setMaxPages(1);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testCombineSetPageLimit() throws Exception
    {
        TransformationOptionLimits combined = limits.combine(limits); // may combine with itself
        combined.setPageLimit(1);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testCombineSetMap() throws Exception
    {
        TransformationOptionLimits combined = limits.combine(limits); // may combine with itself
        combined.set(null);
    }
}

