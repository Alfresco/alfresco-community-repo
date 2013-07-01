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
package org.alfresco.repo.content.transform;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Deque;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for TransformerLogger.
 * 
 * @author Alan Davis
 */
public class TransformerLoggerTest
{
    @Mock
    private TransformerDebug transformerDebug;
    
    @Mock
    private TransformerConfig transformerConfig;
    
    private TransformerLogger<String> log;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        
        log = newLogger(new AtomicInteger(1), transformerDebug, transformerConfig);
    }

    private TransformerLogger<String> newLogger(final AtomicInteger numberOfMessagesToAdd,
            TransformerDebug transformerDebug, TransformerConfig transformerConfig)
    {
        TransformerLogger<String> log = new TransformerLogger<String>()
        {
            @Override
            protected void addOrModify(Deque<String> entries, Object message)
            {
                int i = numberOfMessagesToAdd.get();
                if (i > 0)
                {
                    for (; i > 0; i--)
                    {
                        entries.add((String)message);
                    }
                }
                else if (i < 0)
                {
                    for (; i < 0; i++)
                    {
                        entries.removeLast();
                    }
                }
                else
                {
                    entries.removeLast();
                    entries.add((String)message);
                }
            }

            @Override
            protected int getUpperMaxEntries()
            {
                return 176;
            }

            @Override
            protected String getPropertyName()
            {
                return "property.name";
            }
        };
        log.setTransformerDebug(transformerDebug);
        log.setTransformerConfig(transformerConfig);
        return log;
    }

    @Test
    public void propertyExistsTest()
    {
        when(transformerConfig.getProperty("property.name")).thenReturn("5");
        assertEquals("property.name=5  # default=0", log.getPropertyAndValue(null));
    }

    @Test
    public void propertyExistsAndDefaultSetTest()
    {
        when(transformerConfig.getProperty("property.name")).thenReturn("5");
        Properties properties = new Properties();
        properties.setProperty("property.name", "23");
        assertEquals("property.name=5  # default=23", log.getPropertyAndValue(properties));
    }

    @Test
    public void propertyDoesNotExistTest()
    {
        assertEquals("# property.name=0", log.getPropertyAndValue(null));
    }

    @Test
    public void propertyNegativeTest()
    {
        when(transformerConfig.getProperty("property.name")).thenReturn("-2");
        assertEquals("# property.name=0", log.getPropertyAndValue(null));
    }

    @Test
    public void propertyGreaterThanMax()
    {
        when(transformerConfig.getProperty("property.name")).thenReturn("1000");
        assertEquals("property.name=176  # default=0", log.getPropertyAndValue(null));
    }

    @Test
    public void propertyBadTest()
    {
        when(transformerConfig.getProperty("property.name")).thenReturn("abc");
        assertEquals("# property.name=0", log.getPropertyAndValue(null));
    }
    
    @Test
    public void isDebugEnabled0EntriesTest()
    {
        // when(transformerConfig.getProperty("property.name")).thenReturn("0"); - default to this
        assertFalse(log.isDebugEnabled());
    }
    
    @Test
    public void isDebugEnabled5EntriesTest()
    {
        when(transformerConfig.getProperty("property.name")).thenReturn("5");
        assertTrue(log.isDebugEnabled());
    }
    
    @Test
    public void isDebugEnabledHasStringBuilderTest()
    {
        when(transformerDebug.getStringBuilder()).thenReturn(new StringBuilder());
        when(transformerConfig.getProperty("property.name")).thenReturn("5");
        assertFalse(log.isDebugEnabled());
    }
    
    @Test
    public void noEntriesTest()
    {
        when(transformerConfig.getProperty("property.name")).thenReturn("3");
        assertArrayEquals(new String[] {}, log.getEntries(10));
    }
    
    @Test
    public void noEntriesDisabledTest()
    {
        assertArrayEquals(new String[] {"No entries are available. property.name must be set to a number between 1 and 176"}, log.getEntries(10));
    }
    
    @Test
    public void oneEntryTest()
    {
        when(transformerConfig.getProperty("property.name")).thenReturn("3");
        log.debug("one");
        
        assertArrayEquals(new String[] {"one"}, log.getEntries(10));
    }
    
    @Test
    // newest entry first
    public void fiveEntryTest()
    {
        when(transformerConfig.getProperty("property.name")).thenReturn("3");

        log.debug("one");
        log.debug("two");
        log.debug("three");
        log.debug("four");
        log.debug("five");
        
        assertArrayEquals(new String[] {"five", "four", "three"}, log.getEntries(10));
    }
    
    @Test
    // <= 0 indicates return all
    public void limit0Test()
    {
        when(transformerConfig.getProperty("property.name")).thenReturn("3");

        log.debug("one");
        log.debug("two");
        log.debug("three");
        
        assertArrayEquals(new String[] {"three", "two", "one"}, log.getEntries(0));
    }
    
    
    @Test
    // < 0 indicates return all - most current first
    public void limitAllTest()
    {
        when(transformerConfig.getProperty("property.name")).thenReturn("3");

        log.debug("one");
        log.debug("two");
        log.debug("three");
        
        assertArrayEquals(new String[] {"three", "two", "one"}, log.getEntries(-1));
    }
    
    @Test
    // Returns latest
    public void limit1Test()
    {
        when(transformerConfig.getProperty("property.name")).thenReturn("3");

        log.debug("one");
        log.debug("two");
        log.debug("three");
        
        assertArrayEquals(new String[] {"three"}, log.getEntries(1));
    }
    
    @Test
    public void limitMoreThanEntriesTest()
    {
        when(transformerConfig.getProperty("property.name")).thenReturn("3");

        log.debug("one");
        log.debug("two");
        log.debug("three");
        
        assertArrayEquals(new String[] {"three", "two", "one"}, log.getEntries(5));
    }
    
    @Test
    // Adds two entries on each debug call, but logger must still limit size!
    public void addTwoEntriesTest()
    {
        AtomicInteger numberOfMessagesToAdd = new AtomicInteger(2);
        log = newLogger(numberOfMessagesToAdd, transformerDebug, transformerConfig);
        when(transformerConfig.getProperty("property.name")).thenReturn("5");

        log.debug("one");
        assertArrayEquals(new String[] {"one", "one"}, log.getEntries(10));
        
        log.debug("two");
        assertArrayEquals(new String[] {"two", "two", "one", "one"}, log.getEntries(10));
        
        log.debug("three");
        assertArrayEquals(new String[] {"three", "three", "two", "two", "one"}, log.getEntries(10));
    }

    @Test
    public void addRemoveModifyTest()
    {
        // Same as addTwoEntriesTest but without hitting the buffer limit
        AtomicInteger numberOfMessagesToAdd = new AtomicInteger(2);
        log = newLogger(numberOfMessagesToAdd, transformerDebug, transformerConfig);
        when(transformerConfig.getProperty("property.name")).thenReturn("10");
        log.debug("one");
        log.debug("two");
        log.debug("three");
        assertArrayEquals(new String[] {"three", "three", "two", "two", "one", "one"}, log.getEntries(10));

        // Remove both threes and a two
        numberOfMessagesToAdd.set(-3);
        log.debug("four");
        assertArrayEquals(new String[] {"two", "one", "one"}, log.getEntries(10));

        // Add a five
        numberOfMessagesToAdd.set(1);
        log.debug("five");
        assertArrayEquals(new String[] {"five", "two", "one", "one"}, log.getEntries(10));

        // Replace the five
        numberOfMessagesToAdd.set(0);
        log.debug("six");
        assertArrayEquals(new String[] {"six", "two", "one", "one"}, log.getEntries(10));
    }
}
