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
package org.alfresco.repo.content.caching.quota;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.alfresco.repo.content.caching.cleanup.CachedContentCleaner;
import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for the StandardQuotaStrategy.
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
public class StandardQuotaStrategyMockTest
{
    private StandardQuotaStrategy quota;
    
    @Mock
    private CachedContentCleaner cleaner;
    
    @Before
    public void setUp() throws Exception
    {
        quota = new StandardQuotaStrategy();
        // 1000 Bytes max. - unrealistic value but makes the figures easier.
        quota.setMaxUsageBytes(1000);
        quota.setMaxFileSizeMB(100);
        quota.setCleaner(cleaner);
    }
   
    @Test
    public void testCanSetMaxUsageInMB() throws IllegalAccessException
    {
        quota.setMaxUsageMB(0);
        assertEquals(0, ((Long) FieldUtils.readDeclaredField(quota, "maxUsageBytes", true)).longValue());
        
        quota.setMaxUsageMB(500);
        assertEquals(524288000, ((Long) FieldUtils.readDeclaredField(quota, "maxUsageBytes", true)).longValue());
        
        // 1 GB
        quota.setMaxUsageMB(1024);
        assertEquals(1073741824, ((Long) FieldUtils.readDeclaredField(quota, "maxUsageBytes", true)).longValue());
    }
    
    @Test
    public void testPanicThresholdForBeforeWritingCacheFile()
    {
        quota.setCurrentUsageBytes(0);
        assertTrue("Should allow writing of cache file", quota.beforeWritingCacheFile(899));
        assertFalse("Should not allow writing of cache file", quota.beforeWritingCacheFile(900));
        
        quota.setCurrentUsageBytes(890);
        assertTrue("Should allow writing of cache file", quota.beforeWritingCacheFile(9));
        assertFalse("Should not allow writing of cache file", quota.beforeWritingCacheFile(10));
        
        quota.setCurrentUsageBytes(600);
        assertTrue("Should allow writing of cache file", quota.beforeWritingCacheFile(299));
        assertFalse("Should not allow writing of cache file", quota.beforeWritingCacheFile(300));
        
        quota.setCurrentUsageBytes(899);
        assertTrue("Should allow writing of cache file", quota.beforeWritingCacheFile(0));
        assertFalse("Should not allow writing of cache file", quota.beforeWritingCacheFile(1));
        
        
        // When the usage is already exceeding 100% of what is allowed
        quota.setCurrentUsageBytes(2345);
        assertFalse("Should not allow writing of cache file", quota.beforeWritingCacheFile(0));
        assertFalse("Should not allow writing of cache file", quota.beforeWritingCacheFile(1));
        assertFalse("Should not allow writing of cache file", quota.beforeWritingCacheFile(12300));
    }


        
    @Test
    public void afterWritingCacheFileDiskUsageUpdatedCorrectly()
    {
        quota.setCurrentUsageBytes(410);
        quota.afterWritingCacheFile(40);
        assertEquals("Incorrect usage estimate", 450, quota.getCurrentUsageBytes());
        
        quota.afterWritingCacheFile(150);
        assertEquals("Incorrect usage estimate", 600, quota.getCurrentUsageBytes());
    }

    
    @Test
    // Is the cleaner started when disk usage is over correct threshold?
    public void testThresholdsAfterWritingCacheFile()
    {
        quota.setCurrentUsageBytes(0);
        quota.afterWritingCacheFile(700);
        Mockito.verify(cleaner, Mockito.never()).execute("quota (clean threshold)");
        
        quota.setCurrentUsageBytes(700);
        quota.afterWritingCacheFile(100);
        Mockito.verify(cleaner).execute("quota (clean threshold)");
        
        quota.setCurrentUsageBytes(999);
        quota.afterWritingCacheFile(1);
        Mockito.verify(cleaner).executeAggressive("quota (limit reached)", 700);
    }
    
    
    @Test
    public void testThresholdsBeforeWritingCacheFile()
    {
        quota.setCurrentUsageBytes(800);
        quota.beforeWritingCacheFile(0);
        Mockito.verify(cleaner, Mockito.never()).execute("quota (clean threshold)");
        
        quota.setCurrentUsageBytes(900);
        quota.beforeWritingCacheFile(0);
        Mockito.verify(cleaner).execute("quota (panic threshold)");
    }
    
    @Test
    public void canGetMaxFileSizeBytes()
    {
        quota.setMaxFileSizeMB(1024);
        assertEquals("1GB incorrect", 1073741824L, quota.getMaxFileSizeBytes());
        
        quota.setMaxFileSizeMB(0);
        assertEquals("0MB incorrect", 0L, quota.getMaxFileSizeBytes());
    }
    
    @Test
    public void attemptToWriteFileExceedingMaxFileSizeIsVetoed()
    {
        // Make sure the maxUsageMB doesn't interfere with the tests - set large value.
        quota.setMaxUsageMB(4096);
        
        // Zero for no max file size
        quota.setMaxFileSizeMB(0);
        assertTrue("File should be written", quota.beforeWritingCacheFile(1));
        assertTrue("File should be written", quota.beforeWritingCacheFile(20971520));
        
        // Anything > 0 should result in limit being applied
        quota.setMaxFileSizeMB(1);
        assertTrue("File should be written", quota.beforeWritingCacheFile(1048576));
        assertFalse("File should be vetoed - too large", quota.beforeWritingCacheFile(1048577));
        
        // Realistic scenario, 20 MB cutoff. 
        quota.setMaxFileSizeMB(20);
        assertTrue("File should be written", quota.beforeWritingCacheFile(20971520));
        assertFalse("File should be vetoed - too large", quota.beforeWritingCacheFile(20971521));
        // Unknown (in advance) file size should always result in write
        assertTrue("File should be written", quota.beforeWritingCacheFile(0));
    }
    
    @Test
    public void afterFileWrittenExceedingMaxFileSizeFileIsDeleted()
    {
        // Zero for no max file size
        quota.setMaxFileSizeMB(0);
        assertTrue("File should be kept", quota.afterWritingCacheFile(1));
        assertTrue("File should be kept", quota.afterWritingCacheFile(20971520));
        // Both files were kept
        assertEquals("Incorrect usage estimate", 20971521, quota.getCurrentUsageBytes());
        
        // Realistic scenario, 20 MB cutoff. 
        quota.setMaxFileSizeMB(20);
        quota.setCurrentUsageBytes(0);
        assertTrue("File should be kept", quota.afterWritingCacheFile(20971520));
        assertFalse("File should be removed", quota.afterWritingCacheFile(20971521));
        // Only the first file was kept
        assertEquals("Incorrect usage estimate", 20971520, quota.getCurrentUsageBytes());
    }
    
    @Test
    public void testCurrentUsageMB()
    {
        quota.setCurrentUsageBytes(524288);
        assertEquals(0.5f, quota.getCurrentUsageMB(), 0);
        
        quota.setCurrentUsageBytes(1048576);
        assertEquals(1.0f, quota.getCurrentUsageMB(), 0);

        quota.setCurrentUsageBytes(53262546);
        assertEquals(50.795f, quota.getCurrentUsageMB(), 0.001);
    }
}
