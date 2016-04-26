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
