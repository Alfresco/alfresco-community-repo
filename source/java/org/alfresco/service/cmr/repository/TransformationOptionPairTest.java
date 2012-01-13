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
package org.alfresco.service.cmr.repository;


import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.TransformationOptionPair.Action;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals; 

/**
 * Test TransformationOptionPair
 */
public class TransformationOptionPairTest
{
    TransformationOptionPair pair;
    
    @Before
    public void setUp() throws Exception
    {
        pair = new TransformationOptionPair();
    }

    @Test
    public void testUnset() throws Exception
    {
        long value = -1;
        long actual = pair.getMax();

        assertEquals("Getter did not return unset value", value, actual);
        assertEquals("getValue() did not return set value", value, pair.getValue());
        assertEquals("Expected action not returned", null, pair.getAction());
    }

    @Test
    public void testMax() throws Exception
    {
        long value = 1234;
        pair.setMax(value, null);
        long actual = pair.getMax();

        assertEquals("Getter did not return set value", value, actual);
        assertEquals("getValue() did not return set value", value, pair.getValue());
        assertEquals("Expected action not returned", Action.THROW_EXCEPTION, pair.getAction());
    }

    @Test
    public void testLimit() throws Exception
    {
        long value = 1234;
        pair.setLimit(value, null);
        long actual = pair.getLimit();
        assertEquals("Getter did not return set value", value, actual);
        assertEquals("getValue() did not return set value", value, pair.getValue());
        assertEquals("Expected action not returned", Action.RETURN_EOF, pair.getAction());
    }

    @Test
    public void testMaxAlreadySet() throws Exception
    {
        String message = "Oh no the other value is set";
        String actual = null;
        pair.setMax(1234, null);
        try
        {
            pair.setLimit(111, message);
        }
        catch (IllegalArgumentException e)
        {
            actual = e.getMessage();
        }
        assertEquals("Expected an IllegalArgumentException message", message, actual);
    }

    @Test
    public void testLimitAlreadySet() throws Exception
    {
        String message = "Oh no the other value is set";
        String actual = null;
        pair.setLimit(1234, null);
        try
        {
            pair.setMax(111, message);
        }
        catch (IllegalArgumentException e)
        {
            actual = e.getMessage();
        }
        assertEquals("Expected an IllegalArgumentException message", message, actual);
    }

    @Test
    public void testSetMaxMultipleTimes() throws Exception
    {
        long value = 1234;
        pair.setMax(1, null);
        pair.setMax(2, null); // Should be no exception
        pair.setMax(value, null);
        long actual = pair.getMax();
        assertEquals("Getter did not return set value", value, actual);
    }

    @Test
    public void testSetLimitMultipleTimes() throws Exception
    {
        long value = 1234;
        pair.setLimit(1, null);
        pair.setLimit(2, null); // Should be no exception
        pair.setLimit(value, null);
        long actual = pair.getLimit();
        assertEquals("Getter did not return set value", value, actual);
    }
    
    @Test
    public void testSetClearSet() throws Exception
    {
        // Test there is no exception if we clear the other value first
        String message = "Oh no the other value is set";
        pair.setLimit(1, message);
        pair.setLimit(-1, message);
        pair.setMax(1, message);
        pair.setMax(-1, message);
        pair.setLimit(1, message);
    }
    
    @Test
    public void testMapMax() throws Exception
    {
        String maxKey = "Max";
        String limitKey = "Limit";
        String message = "Oh no the other value is set";
        pair.setMax(123, null);
        
        Map<String, Object> optionsMap = new HashMap<String, Object>();
        pair.toMap(optionsMap, maxKey, limitKey);
        
        TransformationOptionPair actual = new TransformationOptionPair();
        actual.set(optionsMap, maxKey, limitKey, message);

        assertEquals("Did not match original values", pair, actual);
    }
    
    @Test
    public void testMapLimit() throws Exception
    {
        String maxKey = "Max";
        String limitKey = "Limit";
        String message = "Oh no the other value is set";
        pair.setLimit(123, null);
        
        Map<String, Object> optionsMap = new HashMap<String, Object>();
        pair.toMap(optionsMap, maxKey, limitKey);
        
        TransformationOptionPair actual = new TransformationOptionPair();
        actual.set(optionsMap, maxKey, limitKey, message);

        assertEquals("Did not match original values", pair, actual);
    }
    
    @Test
    public void testMapBothSet() throws Exception
    {
        String maxKey = "Max";
        String limitKey = "Limit";
        String message = "Oh no the other value is set";
        pair.setLimit(123, null);
        
        Map<String, Object> optionsMap = new HashMap<String, Object>();
        pair.toMap(optionsMap, maxKey, limitKey);
        optionsMap.put(maxKey, 456L); // Introduce error
        
        String actual = null;
        TransformationOptionPair pair2 = new TransformationOptionPair();
        try
        {
            pair2.set(optionsMap, maxKey, limitKey, message);
        }
        catch (IllegalArgumentException e)
        {
            actual = e.getMessage();
        }
        assertEquals("Expected an IllegalArgumentException message", message, actual);
    }
    
    @Test
    public void testMapNeitherSet() throws Exception
    {
        // Original value should not be changed if keys don't exist
        long value = 1234;
        String maxKey = "Max";
        String limitKey = "Limit";
        String message = "Oh no the other value is set";
        pair.setLimit(value, null);
        
        Map<String, Object> optionsMap = new HashMap<String, Object>();
        optionsMap.put("AnotherKey", 456L);
        
        pair.set(optionsMap, maxKey, limitKey, message);
        long actual = pair.getLimit();
        assertEquals("Original value should not be changed", value, actual);
    }
    
    @Test
    public void testCombineOrder() throws Exception
    {
        TransformationOptionPair second = new TransformationOptionPair();

        pair.setMax(123, null);
        second.setMax(12, null);
        TransformationOptionPair combined = pair.combine(second);
       
        TransformationOptionPair combinedOtherWay = second.combine(pair);
        assertEquals("The combine order should not matter", combined, combinedOtherWay);
    }
    
    @Test
    public void testCombineMax() throws Exception
    {
        TransformationOptionPair second = new TransformationOptionPair();

        pair.setMax(123, null);
        second.setMax(12, null);
        TransformationOptionPair combined = pair.combine(second);

        assertEquals("Expected the lower value", 12, combined.getValue());
        assertEquals("Expected the lower value", 12, combined.getMax());
    }
    
    @Test
    public void testCombineLimit() throws Exception
    {
        TransformationOptionPair second = new TransformationOptionPair();

        pair.setLimit(123, null);
        second.setLimit(12, null);
        TransformationOptionPair combined = pair.combine(second);

        assertEquals("Expected the lower value", 12, combined.getValue());
        assertEquals("Expected the lower value", 12, combined.getLimit());
    }
    
    @Test
    public void testCombineMaxWins() throws Exception
    {
        // Try both max and limit values where max is lower
        TransformationOptionPair second = new TransformationOptionPair();

        pair.setLimit(123, null);
        second.setMax(12, null);
        TransformationOptionPair combined = pair.combine(second);

        assertEquals("Expected the lower value", 12, combined.getValue());
        assertEquals("Expected the lower value", 12, combined.getMax());
        assertEquals("Expected unset value", -1, combined.getLimit());
    }
    
    @Test
    public void testCombineLimitWins() throws Exception
    {
        // Try both max and limit values where limit is lower
        TransformationOptionPair second = new TransformationOptionPair();

        pair.setMax(123, null);
        second.setLimit(12, null);
        TransformationOptionPair combined = pair.combine(second);

        assertEquals("Expected the lower value", 12, combined.getValue());
        assertEquals("Expected the lower value", 12, combined.getLimit());
        assertEquals("Expected unset value", -1, combined.getMax());
    }
    
    @Test
    public void testCombineDynamicChange() throws Exception
    {
        TransformationOptionPair second = new TransformationOptionPair();

        pair.setMax(123, null);
        second.setMax(1234, null);
        TransformationOptionPair combined = pair.combine(second);

        // Test dynamic changes of value
        pair.setMax(45, null);
        assertEquals("Expected the lower value", 45, combined.getMax());
        assertEquals("Expected an unset value", -1, combined.getLimit());

        second.setMax(-1, null);
        second.setLimit(10, null);
        assertEquals("Expected an unset value", -1, combined.getMax());
        assertEquals("Expected the lower value", 10, combined.getLimit());
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testCombineSetMax() throws Exception
    {
        TransformationOptionPair combined = pair.combine(pair); // may combine with itself
        combined.setMax(1, null);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testCombineSetLimit() throws Exception
    {
        TransformationOptionPair combined = pair.combine(pair); // may combine with itself
        combined.setLimit(1, null);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testCombineSetMap() throws Exception
    {
        TransformationOptionPair combined = pair.combine(pair); // may combine with itself
        combined.set(null, null, null, null);
    }
}
