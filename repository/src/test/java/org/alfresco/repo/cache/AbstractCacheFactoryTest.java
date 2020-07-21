/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.cache;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class AbstractCacheFactoryTest
{
    // The class under test.
    private AbstractCacheFactory<Integer, String> cacheFactory;
    private Properties properties;
    
    @Before
    public void setUp() throws Exception
    {
        cacheFactory = new CacheFactoryTestImpl();
        
        properties = new Properties();
        cacheFactory.setProperties(properties);
    }

    @Test
    public void nullPropertyYieldsDefault()
    {
        // Null property, null default
        assertNull(cacheFactory.getProperty("the.cache.name", "noProperty", null));
        
        // Null property, non-empty default
        assertEquals("non-empty-default", cacheFactory.getProperty("the.cache.name", "noProperty", "non-empty-default")); 
    }
    
    @Test
    public void emptyPropertyYieldsDefault()
    {
       // Empty property, empty default
       properties.setProperty("the.cache.name.emptyProperty", "");
       assertEquals("", cacheFactory.getProperty("the.cache.name", "emptyProperty", ""));
       
       // Empty property, null default
       properties.setProperty("the.cache.name.emptyProperty", "");
       assertEquals(null, cacheFactory.getProperty("the.cache.name", "emptyProperty", null));
       
       // Empty property, non-empty default
       properties.setProperty("the.cache.name.emptyProperty", "");
       assertEquals("non-empty-default", cacheFactory.getProperty("the.cache.name", "emptyProperty", "non-empty-default"));

       // Empty/whitespace property
       properties.setProperty("the.cache.name.emptyProperty", "  \t  ");
       assertEquals("default", cacheFactory.getProperty("the.cache.name", "emptyProperty", "default"));
    }
    
    @Test
    public void nonEmptyPropertyIsReturned()
    {
        // Non-empty property
        properties.setProperty("the.cache.name.nonEmpty", "this has a non-empty value");
        assertEquals("this has a non-empty value", cacheFactory.getProperty("the.cache.name", "nonEmpty", "default"));
    }
    
    @Test
    public void nonEmptyPropertyIsTrimmedOfWhitespace()
    {
        properties.setProperty("the.cache.name.nonEmpty", "   \t  value    \t");
        assertEquals("value", cacheFactory.getProperty("the.cache.name", "nonEmpty", "default"));
    }

    
    private static class CacheFactoryTestImpl extends AbstractCacheFactory<Integer, String>
    {
        @Override
        public SimpleCache createCache(String cacheName)
        {
            return null;
        }
    }
}
