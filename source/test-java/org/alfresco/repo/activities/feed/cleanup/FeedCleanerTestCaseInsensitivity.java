package org.alfresco.repo.activities.feed.cleanup;

public class FeedCleanerTestCaseInsensitivity extends AbstractFeedCleanerTest
{
    static
    {
        System.setProperty("user.name.caseSensitive", "false");
    }
    
}
