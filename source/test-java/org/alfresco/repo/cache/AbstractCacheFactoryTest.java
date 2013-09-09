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
