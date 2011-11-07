/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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
