/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.workflow.jbpm;

import org.alfresco.repo.transaction.TransactionUtil;
import org.jbpm.JbpmConfiguration;
import org.jbpm.job.Job;
import org.jbpm.job.executor.JobExecutorThread;


/**
 * Alfresco Job Executor Thread
 * 
 * @author davidc
 */
public class AlfrescoJobExecutorThread extends JobExecutorThread
{
    private AlfrescoJobExecutor alfrescoJobExecutor;
    
    /**
     * Constructor
     * 
     * @param name
     * @param jobExecutor
     * @param jbpmConfiguration
     * @param idleInterval
     * @param maxIdleInterval
     * @param maxLockTime
     * @param maxHistory
     */
    public AlfrescoJobExecutorThread(String name, AlfrescoJobExecutor jobExecutor, JbpmConfiguration jbpmConfiguration, int idleInterval, int maxIdleInterval, long maxLockTime, int maxHistory)
    {
        super(name, jobExecutor, jbpmConfiguration, idleInterval, maxIdleInterval, maxLockTime, maxHistory);
        this.alfrescoJobExecutor = jobExecutor;
    }

    /* (non-Javadoc)
     * @see org.jbpm.job.executor.JobExecutorThread#executeJob(org.jbpm.job.Job)
     */
    @Override
    protected void executeJob(Job job)
    {
        TransactionUtil.executeInUserTransaction(alfrescoJobExecutor.getTransactionService(), new TransactionJob(job));
    }
    
    /**
     * Helper class for holding Job reference
     * 
     * @author davidc
     */
    private class TransactionJob implements TransactionUtil.TransactionWork<Object>
    {
        private Job job;

        /**
         * Constructor
         * 
         * @param job
         */
        public TransactionJob(Job job)
        {
            this.job = job;
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.transaction.TransactionUtil.TransactionWork#doWork()
         */
        public Object doWork() throws Throwable
        {
            AlfrescoJobExecutorThread.super.executeJob(job);
            return null;
        }
    }
    
}
