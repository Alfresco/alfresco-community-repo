package org.alfresco.repo.activities.feed.local;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.repo.activities.feed.FeedGridJob;
import org.alfresco.repo.activities.feed.FeedTaskProcessor;
import org.alfresco.repo.activities.feed.FeedTaskSplit;
import org.alfresco.repo.activities.feed.JobSettings;

/**
 * The local feed task splitter is responsible for splitting the feed task into feed jobs
 */
public class LocalFeedTaskSplitter
{   
    private FeedTaskProcessor feedTaskProcessor;
    
    public void setFeedTaskProcessor(FeedTaskProcessor feedTaskProcessor)
    {
        this.feedTaskProcessor = feedTaskProcessor;
    }
    
    public Collection<FeedGridJob> split(int gridSize, Object o) throws Exception
    {
        FeedTaskSplit feedSplitter = new FeedTaskSplit();
        Collection<JobSettings> jobs = feedSplitter.split(gridSize, (JobSettings)o);
        
        List<FeedGridJob> gridJobs = new ArrayList<FeedGridJob>(jobs.size());
        for (JobSettings job : jobs)
        {
            LocalFeedGridJob gridJob = new LocalFeedGridJob();
            gridJob.setFeedTaskProcessor(feedTaskProcessor);
            gridJob.setArgument(job);
            gridJobs.add(gridJob);
        }
        return gridJobs;
        //return (Collection<FeedGridJob>)feedSplitter.split(gridSize, (JobSettings)o, new LocalFeedGridJob());
    }
}
