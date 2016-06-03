package org.alfresco.repo.activities.feed;

import java.io.Serializable;

/**
 * Interface for feed grid job
 */
public interface FeedGridJob
{
    public void setArgument(JobSettings arg);
    
    public JobSettings getArgument();
    
    public Serializable execute() throws Exception;
}
