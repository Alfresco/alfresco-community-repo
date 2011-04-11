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
package org.alfresco.repo.usage;

import java.util.Date;

import org.alfresco.error.AlfrescoRuntimeException;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;

/**
 * This component monitors the repository usages, issuing warnings and errors
 * as necessary.
 * 
 * @author Derek Hulley
 * @since 3.5
 */
public class RepoUsageMonitor implements RepoUsageComponent.RestrictionObserver
{
    private static Log logger = LogFactory.getLog(RepoUsageMonitor.class);
    
    private Scheduler scheduler;
    private TransactionServiceImpl transactionService;
    private RepoUsageComponent repoUsageComponent;
    private final QName vetoName = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "RepoUsageMonitor");
    
    /**
     * @param scheduler                 Timed updates
     */
    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    /**
     * @param transactionService        service that tells if the server is read-only or not
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
     * @param repoUsageComponent        provides data on usages
     */
    public void setRepoUsageComponent(RepoUsageComponent repoUsageComponent)
    {
        this.repoUsageComponent = repoUsageComponent;
    }

    /**
     * Check that all properties are properly set
     */
    public void init() throws SchedulerException
    {
        PropertyCheck.mandatory(this, "scheduler", scheduler);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "repoUsageComponent", repoUsageComponent);
        
        // Trigger the scheduled updates
        final JobDetail jobDetail = new JobDetail("rmj", Scheduler.DEFAULT_GROUP, RepoUsageMonitorJob.class);
        jobDetail.getJobDataMap().put("RepoUsageMonitor", this);
        final Trigger trigger = TriggerUtils.makeHourlyTrigger(12);                         // every 12 hours
        trigger.setStartTime(new Date(System.currentTimeMillis() + 60L * 60L * 1000L));     // one hour from now
        trigger.setName("rmt");
        trigger.setGroup(Scheduler.DEFAULT_GROUP);
        
        repoUsageComponent.observeRestrictions(this);
        
        // Unschedule in case it was scheduled in an earlier retry of the transaction
        scheduler.unscheduleJob("rmt", Scheduler.DEFAULT_GROUP);
        scheduler.scheduleJob(jobDetail, trigger);
    }
    
    /**
     * Performs the physical checking of usages.
     */
    public void checkUsages()
    {
        final RetryingTransactionCallback<Void> checkWork = new RetryingTransactionCallback<Void>()
        {
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
        RunAsWork<Void> runAs = new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                transactionService.getRetryingTransactionHelper().doInTransaction(checkWork, false);
                return null;
            }
        };
        AuthenticationUtil.runAs(runAs, AuthenticationUtil.getSystemUserName());
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
}
