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
