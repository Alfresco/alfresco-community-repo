/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.schedule;

import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.VmShutdownListener.VmShutdownException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * This class encapsulates the {@link org.alfresco.repo.lock.JobLockService JobLockService}
 * usage in order to guarantee that a job is not executed
 * simultaneously in more than one node in a cluster. After instantiated passing
 * in constructor {@link org.alfresco.schedule.AbstractScheduledLockedJob job}
 * to be executed, as well as the name of the to be locked job and the
 * {@link org.alfresco.repo.lock.JobLockService JobLockService}, the execute
 * method of this class will execute the job taking care of all cluster aware lockings.
 * <p/>
 * This code is based on original code by Derek Hulley on
 * {@link org.alfresco.repo.content.cleanup.ContentStoreCleaner ContentStoreCleaner},
 * extracting the generic locking code in order to be reused and avoid code duplication.
 * 
 * @author Rui Fernandes
 * @since 4.1.5
 */
public class ScheduledJobLockExecuter
{
    private static final long LOCK_TTL = 30000L;

    private static Log logger = LogFactory.getLog(ScheduledJobLockExecuter.class.getName());
    private static ThreadLocal<Pair<Long, String>> lockThreadLocal = new ThreadLocal<Pair<Long, String>>();

    private final JobLockService jobLockService;
    private final QName lockQName;
    private final AbstractScheduledLockedJob job;

    /**
     * @param jobLockService the {@link JobLockService JobLockService}
     * @param name the name of the job to be used for the lock registry
     * @param job the {@link AbstractScheduledLockedJob job} to be executed
     */
    public ScheduledJobLockExecuter(JobLockService jobLockService, String name, AbstractScheduledLockedJob job)
    {
        this.jobLockService = jobLockService;
        this.lockQName = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, name);
        this.job = job;
    }

    /**
     * It will execute the {@link AbstractScheduledLockedJob job} passed on
     * instantiation taking care of all cluster aware lockings.
     * 
     * @param jobContext the usual quartz job context
     * @throws JobExecutionException        thrown if the job fails to execute
     */
    public void execute(JobExecutionContext jobContext) throws JobExecutionException
    {
        try
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(String.format("   Job %s started.", lockQName.getLocalName()));
            }
            refreshLock();
            job.executeJob(jobContext);
            if (logger.isDebugEnabled())
            {
                logger.debug(String.format("   Job %s completed.", lockQName.getLocalName()));
            }
        }
        catch (LockAcquisitionException e)
        {
            // Job being done by another process
            if (logger.isDebugEnabled())
            {
                logger.debug(String.format("   Job %s already underway.", lockQName.getLocalName()));
            }
        }
        catch (VmShutdownException e)
        {
            // Aborted
            if (logger.isDebugEnabled())
            {
                logger.debug(String.format("   Job %s aborted.", lockQName.getLocalName()));
            }
        }
        finally
        {
            releaseLock();
        }
    }

    /**
     * Lazily update the job lock
     */
    private void refreshLock()
    {
        Pair<Long, String> lockPair = lockThreadLocal.get();
        if (lockPair == null)
        {
            String lockToken = jobLockService.getLock(lockQName, LOCK_TTL);
            Long lastLock = new Long(System.currentTimeMillis());
            // We have not locked before
            lockPair = new Pair<Long, String>(lastLock, lockToken);
            lockThreadLocal.set(lockPair);
        }
        else
        {
            long now = System.currentTimeMillis();
            long lastLock = lockPair.getFirst().longValue();
            String lockToken = lockPair.getSecond();
            // Only refresh the lock if we are past a threshold
            if (now - lastLock > (long) (LOCK_TTL / 2L))
            {
                jobLockService.refreshLock(lockToken, lockQName, LOCK_TTL);
                lastLock = System.currentTimeMillis();
                lockPair = new Pair<Long, String>(lastLock, lockToken);
                lockThreadLocal.set(lockPair);
            }
        }
    }

    /**
     * Release the lock after the job completes
     */
    private void releaseLock()
    {
        Pair<Long, String> lockPair = lockThreadLocal.get();
        if (lockPair != null)
        {
            // We can't release without a token
            try
            {
                jobLockService.releaseLock(lockPair.getSecond(), lockQName);
            }
            finally
            {
                // Reset
                lockThreadLocal.set(null);
            }
        }
        // else: We can't release without a token
    }
}
