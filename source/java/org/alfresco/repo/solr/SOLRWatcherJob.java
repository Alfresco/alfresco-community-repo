package org.alfresco.repo.solr;

import org.alfresco.repo.solr.SOLRAdminClient.SolrTracker;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * A Quartz job that pings Solr to determine if it is alive
 * 
 * @since 4.0
 *
 */
public class SOLRWatcherJob implements Job
{
	public SOLRWatcherJob()
    {
        super();
    }
	
    /*
     * (non-Javadoc)
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException
    {
        SolrTracker solrTracker = (SolrTracker)jec.getJobDetail().getJobDataMap().get("SOLR_TRACKER");
    	solrTracker.pingSolr();
    }
}
