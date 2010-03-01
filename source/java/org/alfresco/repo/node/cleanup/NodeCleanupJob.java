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
package org.alfresco.repo.node.cleanup;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Scheduled job to call a {@link NodeCleanupWorker}.
 * <p>
 * Job data is: <b>nodeCleanupWorker</b>
 * 
 * @author Derek Hulley
 * @since 2.2SP2
 */
public class NodeCleanupJob implements Job
{
    private static Log logger = LogFactory.getLog(NodeCleanupJob.class);
    
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        // extract the content Cleanup to use
        Object nodeCleanupWorkerObj = jobData.get("nodeCleanupWorker");
        if (nodeCleanupWorkerObj == null || !(nodeCleanupWorkerObj instanceof NodeCleanupWorker))
        {
            throw new AlfrescoRuntimeException(
                    "NodeCleanupJob data must contain valid 'nodeCleanupWorker' reference");
        }
        NodeCleanupWorker nodeCleanupWorker = (NodeCleanupWorker) nodeCleanupWorkerObj;
        List<String> cleanupLog = nodeCleanupWorker.doClean();
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Node cleanup log:");
            for (String log : cleanupLog)
            {
                logger.debug(log);
            }
        }
    }
}
