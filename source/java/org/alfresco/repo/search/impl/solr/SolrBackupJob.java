package org.alfresco.repo.search.impl.solr;

import org.alfresco.error.AlfrescoRuntimeException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Triggers the back up of SOLR stores (the back up is made on the remote SOLR server)
 * {@link org.alfresco.repo.content.cleanup.ContentStoreCleaner}.
 * <p>
 * The following parameters are required:
 * <ul>
 *   <li><b>solrBackupClient</b>: The content store cleaner bean</li>
 * </ul>
 * 
 * @author Andy hind
 */
public class SolrBackupJob implements Job
{
    public SolrBackupJob()
    {
    }

    /**
     * Calls the cleaner to do its work
     */
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        // extract the SOLR backup client to use
        Object solrBackupClientObj = jobData.get("solrBackupClient");
        if (solrBackupClientObj == null || !(solrBackupClientObj instanceof SolrBackupClient))
        {
            throw new AlfrescoRuntimeException(
                    "SolrBackupJob data must contain valid 'solrBackupClient' reference");
        }
        SolrBackupClient solrBackupClient = (SolrBackupClient) solrBackupClientObj;
        solrBackupClient.execute();
    }
}
