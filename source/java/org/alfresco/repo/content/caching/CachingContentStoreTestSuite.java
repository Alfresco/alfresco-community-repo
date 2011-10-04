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
package org.alfresco.repo.content.caching;

import org.alfresco.repo.content.caching.cleanup.CachedContentCleanupJobTest;
import org.alfresco.repo.content.caching.quota.StandardQuotaStrategyMockTest;
import org.alfresco.repo.content.caching.quota.StandardQuotaStrategyTest;
import org.alfresco.repo.content.caching.quota.UnlimitedQuotaStrategyTest;
import org.alfresco.repo.content.caching.test.ConcurrentCachingStoreTest;
import org.alfresco.repo.content.caching.test.SlowContentStoreTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for all the CachingContentStore test classes.
 * 
 * @author Matt Ward
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
{
    CachedContentCleanupJobTest.class,
    StandardQuotaStrategyMockTest.class,
    StandardQuotaStrategyTest.class,
    UnlimitedQuotaStrategyTest.class,
    ConcurrentCachingStoreTest.class,
    SlowContentStoreTest.class,
    // TODO: CachingContentStoreSpringTest doesn't seem to be like being run in a suite,
    //       will fix later but please run separately for now.
    //CachingContentStoreSpringTest.class,
    CachingContentStoreTest.class,
    ContentCacheImplTest.class,
    FullTest.class
})
public class CachingContentStoreTestSuite
{

}
