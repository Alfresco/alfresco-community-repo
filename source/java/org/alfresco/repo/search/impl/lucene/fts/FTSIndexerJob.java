package org.alfresco.repo.search.impl.lucene.fts;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Job to run the FTS indexer
 * @author andyh
 *
 */
public class FTSIndexerJob implements Job
{
    /**
     * 
     */
    public FTSIndexerJob()
    {
        super();
    }

    public void execute(JobExecutionContext executionContext) throws JobExecutionException
    {

        FullTextSearchIndexer indexer = (FullTextSearchIndexer)executionContext.getJobDetail().getJobDataMap().get("bean");
        if(indexer != null)
        {
           indexer.index();
        }

    }

   

}
