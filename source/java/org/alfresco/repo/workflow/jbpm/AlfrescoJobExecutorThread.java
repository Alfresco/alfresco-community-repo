/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmConfiguration;
import org.jbpm.job.Job;
import org.jbpm.job.executor.JobExecutorThread;


/**
 * Alfresco Job Executor Thread
 * 
 * @author davidc, janv
 */
public class AlfrescoJobExecutorThread extends JobExecutorThread
{
    /** The name of the lock used to ensure that job executor does not run on more than one node at the same time. */
    private static final QName LOCK_QNAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI,
            "AlfrescoJbpmJobExecutor");
    
    private static Log logger = LogFactory.getLog(AlfrescoJobExecutorThread.class);
    
    private AlfrescoJobExecutor alfrescoJobExecutor;
    private boolean isActive = true;
    
    private long jbpmMaxLockTime;
    
    private long jobLockTTL = 0;
    private String jobLockToken = null;
    
    @Override
    public void setActive(boolean isActive)
    {
        this.isActive = isActive;
    }
    
    /**
     * Constructor
     */
    public AlfrescoJobExecutorThread(String name, AlfrescoJobExecutor jobExecutor, JbpmConfiguration jbpmConfiguration, int idleInterval, int maxIdleInterval, long maxLockTime, int maxHistory)
    {
        super(name, jobExecutor, jbpmConfiguration, idleInterval, maxIdleInterval, maxLockTime, maxHistory);
        this.alfrescoJobExecutor = jobExecutor;
        this.jbpmMaxLockTime = maxLockTime;
        
        this.jobLockTTL = jbpmMaxLockTime+(1000 * 60 * 10);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Collection acquireJobs()
    {
        Collection jobs = Collections.EMPTY_LIST;
        
        if ((isActive) && (! alfrescoJobExecutor.getTransactionService().isReadOnly()))
        {
            try
            {
                jobs = alfrescoJobExecutor.getTransactionService().getRetryingTransactionHelper().doInTransaction(
                    new RetryingTransactionHelper.RetryingTransactionCallback<Collection>() {
                        public Collection execute() throws Throwable
                        {
                            if (jobLockToken != null)
                            {
                                refreshExecutorLock(jobLockToken);
                            }
                            else
                            {
                                jobLockToken = getExecutorLock();
                            }
                            
                            try
                            {
                                return AlfrescoJobExecutorThread.super.acquireJobs();
                            }
                            catch (Throwable t)
                            {
                                logger.error("Failed to acquire jobs");
                                releaseExecutorLock(jobLockToken);
                                jobLockToken = null;
                                throw t;
                            }
                        }
                });
                
                if (jobs != null)
                {
                    if (logger.isDebugEnabled() && (! logger.isTraceEnabled()) && (! jobs.isEmpty()))
                    {
                        logger.debug("acquired "+jobs.size()+" job"+((jobs.size() != 1) ? "s" : ""));
                    }
                    
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("acquired "+jobs.size()+" job"+((jobs.size() != 1) ? "s" : "")+((jobs.size() > 0) ? ": "+jobs.toString() : ""));
                    }
                    
                    if (jobs.size() == 0)
                    {
                        releaseExecutorLock(jobLockToken);
                        jobLockToken = null;
                    }
                }
            }
            catch (LockAcquisitionException e)
            {
                // ignore
                jobLockToken = null;
            }
        }
        
        return jobs;
    }

    @Override
    protected Date getNextDueDate()
    {
        if (!isActive)
        {
            return null;
        }
        
        return alfrescoJobExecutor.getTransactionService().getRetryingTransactionHelper().doInTransaction(
            new RetryingTransactionHelper.RetryingTransactionCallback<Date>() {
                public Date execute() throws Throwable
                {
                    return AlfrescoJobExecutorThread.super.getNextDueDate();
                }
            }, true);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void executeJob(Job job)
    {
        if ((!isActive) || (alfrescoJobExecutor.getTransactionService().isReadOnly()))
        {
            return;
        }
        
        try
        {
            alfrescoJobExecutor.getTransactionService().getRetryingTransactionHelper().doInTransaction(new TransactionJob(job));
        }
        catch (LockAcquisitionException e)
        {
            // ignore
            jobLockToken = null;
        }
    }
    
    private String getExecutorLock()
    {
        String jobLockToken = null;
        
        if (alfrescoJobExecutor.getJobExecutorLockEnabled())
        {
            try
            {
                jobLockToken = alfrescoJobExecutor.getJobLockService().getLock(LOCK_QNAME, jobLockTTL, 3000, 10);
                
                if (logger.isTraceEnabled())
                {
                    logger.trace(Thread.currentThread().getName()+" got lock token: "+jobLockToken);
                }
            }
            catch (LockAcquisitionException e)
            {
                if (logger.isTraceEnabled())
                {
                    logger.trace("Failed to get Alfresco Job Executor lock - may already running in another thread");
                }
                throw e;
            }
        }
        
        return jobLockToken;
    }
    
    private void refreshExecutorLock(String jobLockToken)
    {
        if (jobLockToken != null)
        {
            try
            {
                alfrescoJobExecutor.getJobLockService().refreshLock(jobLockToken, LOCK_QNAME, jobLockTTL);
                
                if (logger.isTraceEnabled())
                {
                    logger.trace(Thread.currentThread().getName()+" refreshed lock token: "+jobLockToken);
                }
            }
            catch (LockAcquisitionException e)
            {
                if (logger.isTraceEnabled())
                {
                    logger.trace("Failed to refresh Alfresco Job Executor lock  - may no longer exist ("+jobLockToken+")");
                }
                throw e;
            }
        }
    }
    
    private void releaseExecutorLock(String jobLockToken)
    {
        if (jobLockToken != null)
        {
            try
            {
                alfrescoJobExecutor.getJobLockService().releaseLock(jobLockToken, LOCK_QNAME);
                
                if (logger.isTraceEnabled())
                {
                    logger.trace(Thread.currentThread().getName()+" released lock token: "+jobLockToken);
                }
            }
            catch (LockAcquisitionException e)
            {
                if (logger.isTraceEnabled())
                {
                    logger.trace("Failed to release Alfresco Job Executor lock - may no longer exist ("+jobLockToken+")");
                }
                throw e;
            }
        }
    }
    
    /**
     * Helper class for holding Job reference
     * 
     * @author davidc
     */
    private class TransactionJob implements RetryingTransactionCallback<Object>
    {
        private Job job;
        
        /**
         * Constructor
         * 
         * @param job       the job to execute
         */
        public TransactionJob(Job job)
        {
            this.job = job;
        }
        
        public Object execute() throws Throwable
        {
            refreshExecutorLock(jobLockToken);
            
            AlfrescoJobExecutorThread.super.executeJob(job);
            
            if (logger.isDebugEnabled())
            {
                logger.debug("executed job: "+job);
            }
            
            return null;
        }
    }
}
