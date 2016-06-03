package org.alfresco.repo.activities.feed;

import org.quartz.JobExecutionException;

/**
 * Interface for feed generator
 */
public interface FeedGenerator
{
    public void execute() throws JobExecutionException;
    
    public int getEstimatedGridSize();
}
