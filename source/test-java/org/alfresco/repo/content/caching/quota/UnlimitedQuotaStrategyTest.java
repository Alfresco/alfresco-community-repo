package org.alfresco.repo.content.caching.quota;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;


/**
 * Tests for the UnlimitedQuotaStrategy class.
 * 
 * @author Matt Ward
 */
public class UnlimitedQuotaStrategyTest
{
    private UnlimitedQuotaStrategy quota;
    
    @Before
    public void setUp()
    {
        quota = new UnlimitedQuotaStrategy();
    }
    
    @Test
    public void beforeWritingCacheFile()
    {
        assertTrue("Should always allow caching", quota.beforeWritingCacheFile(0));
        assertTrue("Should always allow caching", quota.beforeWritingCacheFile(Long.MAX_VALUE));
    }
    
    @Test
    public void afterWritingCacheFile()
    {
        assertTrue("Should always allow cache file to remain", quota.afterWritingCacheFile(0));
        assertTrue("Should always allow cache file to remain", quota.afterWritingCacheFile(Long.MAX_VALUE));
    }
}
