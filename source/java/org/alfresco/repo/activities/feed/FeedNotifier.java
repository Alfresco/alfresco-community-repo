package org.alfresco.repo.activities.feed;


/**
 * Interface for feed notifier
 * 
 * @since 3.5
 */
public interface FeedNotifier
{
    /**
     * 
     * @param repeatIntervalMins system-wide job repeat interval (in minutes)
     */
    public void execute(int repeatIntervalMins);
}
