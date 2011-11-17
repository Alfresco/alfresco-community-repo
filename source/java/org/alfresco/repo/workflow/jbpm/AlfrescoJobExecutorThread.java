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
package org.alfresco.repo.workflow.jbpm;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.DoNotRetryException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.db.JobSession;
import org.jbpm.job.Job;
import org.jbpm.job.executor.JobExecutorThread;
import org.jbpm.persistence.JbpmPersistenceException;
import org.jbpm.svc.Services;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * Alfresco Job Executor Thread
 * 
 * @author davidc, janv
 */
public class AlfrescoJobExecutorThread extends JobExecutorThread
{
    /**
     * The name of the lock used to ensure that job executor does not run on
     * more than one node at the same time.
     */
    private static final QName  LOCK_QNAME   = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI,
                                                     "AlfrescoJbpmJobExecutor");

    private static Log          logger       = LogFactory.getLog(AlfrescoJobExecutorThread.class);

    private AlfrescoJobExecutor alfrescoJobExecutor;
    private boolean             isActive     = true;

    private long                jbpmMaxLockTime;

    private long                jobLockTTL   = 0;
    private String              jobLockToken = null;

    private JbpmConfiguration   jbpmConfiguration;

    @Override
    public void setActive(boolean isActive)
    {
        this.isActive = isActive;
    }

    /**
     * Constructor
     */
    public AlfrescoJobExecutorThread(String name, AlfrescoJobExecutor jobExecutor, JbpmConfiguration jbpmConfiguration,
            int idleInterval, int maxIdleInterval, long maxLockTime, int maxHistory)
    {
        super(name, jobExecutor, jbpmConfiguration, idleInterval, maxIdleInterval, maxLockTime, maxHistory);
        this.alfrescoJobExecutor = jobExecutor;
        this.jbpmMaxLockTime = maxLockTime;

        this.jobLockTTL = jbpmMaxLockTime + (1000 * 60 * 10);

        this.jbpmConfiguration = jbpmConfiguration;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Collection acquireJobs()
    {
        Collection jobs = Collections.EMPTY_LIST;

        if ((isActive) && (!alfrescoJobExecutor.getTransactionService().isReadOnly()))
        {
            try
            {
                jobs = alfrescoJobExecutor.getTransactionService().getRetryingTransactionHelper()
                        .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Collection>()
                        {
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
                    if (logger.isDebugEnabled() && (!logger.isTraceEnabled()) && (!jobs.isEmpty()))
                    {
                        logger.debug("acquired " + jobs.size() + " job" + ((jobs.size() != 1) ? "s" : ""));
                    }

                    if (logger.isTraceEnabled())
                    {
                        logger.trace("acquired " + jobs.size() + " job" + ((jobs.size() != 1) ? "s" : "")
                                + ((jobs.size() > 0) ? ": " + jobs.toString() : ""));
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

        return alfrescoJobExecutor.getTransactionService().getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Date>()
                {
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
    protected void executeJob(final Job jobIn)
    {
        // execute the job as System (ALF-10776) so transaction commit level
        // operations have a security context.
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                executeJobImpl(jobIn);
                return null;
            }
        }, getActorId(jobIn));

        // clear authentication context for this thread
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    private String getActorId(final Job jobIn)
    {
        TaskInstance taskInstance = jobIn.getTaskInstance();

        if (taskInstance != null)
        {
            String actorId = taskInstance.getActorId();
            if (actorId != null && actorId.length() > 0)
            {
                return actorId;
            }
        }
        return AuthenticationUtil.getSystemUserName();
    }

    private void executeJobImpl(final Job jobIn)
    {
        if ((!isActive) || (alfrescoJobExecutor.getTransactionService().isReadOnly()))
        {
            return;
        }

        // based on JBPM 3.3.1 (JobExecutorThread.executeJob)
        // - wrap executeJob / deleteJob in Alfresco retries
        // - add setRollbackOnly warnings
        // - if Alfresco retries fail, attempt to set JBPM job exception/retries

        try
        {
            RetryingTransactionHelper tranHelper = alfrescoJobExecutor.getTransactionService().getRetryingTransactionHelper();
            tranHelper.doInTransaction(new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Throwable
                {
                    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
                    try
                    {
                        JobSession jobSession = jbpmContext.getJobSession();
                        Job job = jobSession.loadJob(jobIn.getId());

                        if (logger.isTraceEnabled())
                        {
                            logger.trace("executing " + job);
                        }

                        if (job.execute(jbpmContext))
                        {
                            jobSession.deleteJob(job);

                            if (logger.isDebugEnabled())
                            {
                                logger.debug("executed and deleted: " + job);
                            }
                        }

                        // if this job is locked too long
                        long totalLockTimeInMillis = System.currentTimeMillis() - job.getLockTime().getTime();
                        if (totalLockTimeInMillis > jbpmMaxLockTime)
                        {
                            logger.warn("setRollbackOnly: exceeded maxLockTime (" + jbpmMaxLockTime + ") " + job);
                            jbpmContext.setRollbackOnly();
                        }
                    }
                    catch(Exception e)
                    {
                        if(isPersistenceException(e))
                        {
                            throw new AlfrescoJbpmPersistenceException(e);
                        }
                        else
                        {
                            throw e;
                        }
                    }
                    finally
                    {
                        jbpmContext.close();
                    }
                    return null;
                }
            });
        }
        catch (LockAcquisitionException e)
        {
            // ignore
            jobLockToken = null;
        }
        catch(JbpmPersistenceException pe)
        {
            if(Services.isCausedByStaleState(pe))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("optimistic locking failed, couldn't complete job "+ jobIn, pe);
                }
            }
            else handleException(jobIn, pe);
        }
        catch (final Exception e)
        {
            handleException(jobIn, e);
        }
    }

    private void handleException(final Job jobIn, final Exception e)
    {
        
        if (logger.isErrorEnabled())
        {
            logger.error("failed to execute " + jobIn, e);
        }
        try
        {
            RetryingTransactionHelper tranHelper = alfrescoJobExecutor.getTransactionService().getRetryingTransactionHelper();
            tranHelper.doInTransaction(new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Throwable
                {
                    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
                    try
                    {
                        JobSession jobSession = jbpmContext.getJobSession();
                        Job job = jobSession.loadJob(jobIn.getId());

                        StringWriter memoryWriter = new StringWriter();
                        e.printStackTrace(new PrintWriter(memoryWriter));
                        job.setException(memoryWriter.toString());
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("attempting to update exception/retries: " + job);
                        }
                        int retries = 0;
                        if (isPersistenceException(e) ==false)
                        {
                            retries = job.getRetries() -1;
                        }
                        job.setRetries(retries);
                        if (logger.isInfoEnabled())
                        {
                            String msg = "updated job exception and set to " + job.getRetries() + " retries: " + jobIn;
                            logger.info(msg);
                        }
                    }
                    finally
                    {
                        jbpmContext.close();
                    }

                    return null;
                }
            });
        }
        catch (Exception e2)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("failed to update job exception/retries " + jobIn, e2);
            }
        }
    }

    private String getExecutorLock()
    {
        String lockToken = null;

        if (alfrescoJobExecutor.getJobExecutorLockEnabled())
        {
            try
            {
                lockToken = alfrescoJobExecutor.getJobLockService().getLock(LOCK_QNAME, jobLockTTL, 3000, 10);

                if (logger.isTraceEnabled())
                {
                    logger.trace(Thread.currentThread().getName() + " got lock token: " + lockToken);
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

        return lockToken;
    }

    private void refreshExecutorLock(String lockToken)
    {
        if (lockToken != null)
        {
            try
            {
                alfrescoJobExecutor.getJobLockService().refreshLock(lockToken, LOCK_QNAME, jobLockTTL);

                if (logger.isTraceEnabled())
                {
                    logger.trace(Thread.currentThread().getName() + " refreshed lock token: " + lockToken);
                }
            }
            catch (LockAcquisitionException e)
            {
                if (logger.isTraceEnabled())
                {
                    logger.trace("Failed to refresh Alfresco Job Executor lock  - may no longer exist (" + lockToken
                            + ")");
                }
                throw e;
            }
        }
    }

    private void releaseExecutorLock(String lockToken)
    {
        if (lockToken != null)
        {
            try
            {
                alfrescoJobExecutor.getJobLockService().releaseLock(lockToken, LOCK_QNAME);

                if (logger.isTraceEnabled())
                {
                    logger.trace(Thread.currentThread().getName() + " released lock token: " + lockToken);
                }
            }
            catch (LockAcquisitionException e)
            {
                if (logger.isTraceEnabled())
                {
                    logger.trace("Failed to release Alfresco Job Executor lock - may no longer exist (" + lockToken
                            + ")");
                }
                throw e;
            }
        }
    }

    private boolean isPersistenceException(Throwable throwable)
    {
        do
        {
            if (throwable instanceof HibernateException)
            {
                return true;
            }
            throwable = throwable.getCause();
        }
        while (throwable != null);
        
         return false;
    }
    
    public static class AlfrescoJbpmPersistenceException extends Exception implements DoNotRetryException
    {
        private static final long serialVersionUID = -2233119713831272158L;

        public AlfrescoJbpmPersistenceException(Throwable cause) {
            super(cause);
        }
    }
    
}
