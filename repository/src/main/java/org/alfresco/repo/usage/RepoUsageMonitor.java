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
package org.alfresco.repo.usage;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionServiceImpl;
import org.alfresco.service.cmr.admin.RepoUsage;
import org.alfresco.service.cmr.admin.RepoUsage.UsageType;
import org.alfresco.service.cmr.admin.RepoUsageStatus;
import org.alfresco.service.cmr.admin.RepoUsageStatus.RepoUsageLevel;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;

/**
 * This component monitors the repository usages, issuing warnings and errors as necessary.
 * 
 * @author Derek Hulley
 * @since 3.5
 */
public class RepoUsageMonitor implements RepoUsageComponent.RestrictionObserver
{
    private static Log logger = LogFactory.getLog(RepoUsageMonitor.class);

    public static final Long LOCK_TTL = 60000L;
    public static final QName LOCK_USAGE = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "RepoUsageMonitor");

    private Scheduler scheduler;
    private TransactionServiceImpl transactionService;
    private RepoUsageComponent repoUsageComponent;
    private JobLockService jobLockService;
    private final QName vetoName = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "RepoUsageMonitor");

    /**
     * @param scheduler
     *            Timed updates
     */
    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    /**
     * @param transactionService
     *            service that tells if the server is read-only or not
     */
    public void setTransactionService(TransactionService transactionService)
    {
        try
        {
            this.transactionService = (TransactionServiceImpl) transactionService;
        }
        catch (ClassCastException e)
        {
            throw new AlfrescoRuntimeException("The RepoUsageMonitor needs direct access to the TransactionServiceImpl");
        }
    }

    /**
     * @param repoUsageComponent
     *            provides data on usages
     */
    public void setRepoUsageComponent(RepoUsageComponent repoUsageComponent)
    {
        this.repoUsageComponent = repoUsageComponent;
    }

    /**
     * @param jobLockService
     *            service to prevent duplicate work when updating usages
     */
    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    /**
     * Check that all properties are properly set
     */
    public void init() throws SchedulerException
    {
        PropertyCheck.mandatory(this, "scheduler", scheduler);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "repoUsageComponent", repoUsageComponent);
        PropertyCheck.mandatory(this, "jobLockService", jobLockService);

        // Trigger the scheduled updates
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("RepoUsageMonitor", this);
        final JobDetail jobDetail = JobBuilder.newJob()
                .withIdentity("rmj")
                .usingJobData(jobDataMap)
                .ofType(RepoUsageMonitorJob.class)
                .build();

        final Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("rmt")
                .withSchedule(SimpleScheduleBuilder.repeatHourlyForever().withIntervalInHours(12))
                .startAt(new Date(System.currentTimeMillis() + 60L * 60L * 1000L))
                .build();

        repoUsageComponent.observeRestrictions(this);

        // Unschedule in case it was scheduled in an earlier retry of the transaction
        scheduler.unscheduleJob(trigger.getKey());
        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * Performs the physical checking of usages.
     */
    public void checkUsages()
    {
        final RetryingTransactionCallback<Void> checkWork = new RetryingTransactionCallback<Void>() {
            @Override
            public Void execute() throws Throwable
            {
                RepoUsage restrictions = repoUsageComponent.getRestrictions();
                // Bypass if there are no restrictions
                if (restrictions.getUsers() == null && restrictions.getDocuments() == null)
                {
                    transactionService.setAllowWrite(true, vetoName);
                    return null;
                }

                // Update user count, if required
                if (restrictions.getUsers() != null)
                {
                    repoUsageComponent.updateUsage(UsageType.USAGE_USERS);
                }
                // Update document count, if required
                if (restrictions.getDocuments() != null)
                {
                    repoUsageComponent.updateUsage(UsageType.USAGE_DOCUMENTS);
                }

                // Same as if restrictions have been changed
                onChangeRestriction(restrictions);

                return null;
            }
        };
        RunAsWork<Void> runAs = new RunAsWork<Void>() {
            @Override
            public Void doWork() throws Exception
            {
                transactionService.getRetryingTransactionHelper().doInTransaction(checkWork, false);
                return null;
            }
        };
        String lockToken = null;
        TrackerJobLockRefreshCallback callback = new TrackerJobLockRefreshCallback();
        try
        {
            // Lock to prevent concurrent queries
            lockToken = jobLockService.getLock(LOCK_USAGE, LOCK_TTL);
            jobLockService.refreshLock(lockToken, LOCK_USAGE, LOCK_TTL / 2, callback);
            AuthenticationUtil.runAs(runAs, AuthenticationUtil.getSystemUserName());
        }
        catch (LockAcquisitionException e)
        {
            logger.debug("Failed to get lock for usage monitor: " + e.getMessage());
        }
        finally
        {
            if (lockToken != null)
            {
                try
                {
                    callback.isActive = false;
                    jobLockService.releaseLock(lockToken, LOCK_USAGE);
                }
                catch (LockAcquisitionException e)
                {
                    logger.debug("Failed to release lock for usage monitor: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Checks the current status, logs messages and sets a read-write veto, if necessary
     */
    @Override
    public void onChangeRestriction(RepoUsage restrictions)
    {
        RepoUsageStatus status = repoUsageComponent.getUsageStatus();
        if (logger.isDebugEnabled())
        {
            logger.debug("Current status is " + status);
        }

        status.logMessages(logger);

        if (status.getLevel() == RepoUsageLevel.LOCKED_DOWN)
        {
            transactionService.setAllowWrite(false, vetoName);
        }
        else
        {
            transactionService.setAllowWrite(true, vetoName);
        }
    }

    /**
     * The job that kicks off the usage monitoring.
     * 
     * @author Derek Hulley
     * @since V3.4 Team
     */
    public static class RepoUsageMonitorJob implements Job
    {
        public void execute(final JobExecutionContext context) throws JobExecutionException
        {
            final JobDataMap jdm = context.getJobDetail().getJobDataMap();
            final RepoUsageMonitor repoUsageMonitor = (RepoUsageMonitor) jdm.get("RepoUsageMonitor");
            repoUsageMonitor.checkUsages();
        }
    }

    private class TrackerJobLockRefreshCallback implements JobLockService.JobLockRefreshCallback
    {
        public boolean isActive = true;

        @Override
        public boolean isActive()
        {
            return isActive;
        }

        @Override
        public void lockReleased()
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("lock released");
            }
        }
    }
}
