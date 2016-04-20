package org.alfresco.repo.activities.feed.cleanup;

public class FeedCleanerTestCaseSensitivity extends AbstractFeedCleanerTest
{
    static
    {
        System.setProperty("user.name.caseSensitive", "true");
    }
    
}
