package org.alfresco.repo.activities.feed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Responsible for splitting the feed task into feed jobs (to be executed locally or on a grid)
 */
public class FeedTaskSplit
{
    private static Log logger = LogFactory.getLog(FeedTaskSplit.class);
    
    public Collection<JobSettings> split(int gridSize, JobSettings splitSettings)
    {
        long maxSequence = splitSettings.getMaxSeq();
        
        if (logger.isDebugEnabled())
        {
            logger.debug("split: start - gridSize = " + gridSize + ", maxSequence = " + maxSequence);
        }
        
        long minSequence = maxSequence - splitSettings.getMaxItemsPerCycle() + 1;
        
        splitSettings.setMinSeq((minSequence >= 0L ? minSequence : 0L));
            
        List<JobSettings> jobs = new ArrayList<JobSettings>(gridSize);
        
        int maxNodeHash = splitSettings.getJobTaskNode();
        
        // note: gridSize may change between runs, hence use maximum node hash/bucket for this cycle
        for (int n = 1; n <= maxNodeHash; n++)
        {
            // every job gets its own copy of the jobSettings (with different nodeHash) as an argument.
            JobSettings jobSettings = splitSettings.clone();
            jobSettings.setJobTaskNode(n);
            
            jobs.add(jobSettings);
        }

        return jobs;
    }
}
