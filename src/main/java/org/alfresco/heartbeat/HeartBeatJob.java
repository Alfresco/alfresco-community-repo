/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.heartbeat;

import org.alfresco.heartbeat.datasender.HBData;
import org.alfresco.heartbeat.datasender.HBDataSenderService;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.JobLockServiceImpl;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The scheduler job responsible for triggering a heartbeat on a regular basis.
 */

public class HeartBeatJob implements Job
{
    /** The logger. */
    private static final Log logger = LogFactory.getLog(HeartBeatJob.class);

    /** Time to live 5 seconds */
    private static final long LOCK_TTL = 5000L;

    public static final String COLLECTOR_KEY = "collector";
    public static final String DATA_SENDER_SERVICE_KEY = "hbDataSenderService";
    public static final String JOB_LOCK_SERVICE_KEY = "jobLockService";


    public void execute(final JobExecutionContext jobexecutioncontext) throws JobExecutionException
    {
        final JobDataMap dataMap = jobexecutioncontext.getJobDetail().getJobDataMap();
        final HBBaseDataCollector collector = (HBBaseDataCollector) dataMap.get(COLLECTOR_KEY);
        final HBDataSenderService hbDataSenderService = (HBDataSenderService) dataMap.get(DATA_SENDER_SERVICE_KEY);
        final JobLockService jobLockService = (JobLockService) dataMap.get(JOB_LOCK_SERVICE_KEY);

        if(collector == null)
        {
            logger.error("Exit HeartBeatJob because there is no assigned HB collector");
            return;
        }
        if(hbDataSenderService == null)
        {
            logger.error("Exit HeartBeatJob because there is no HBDataSenderService");
            return;
        }
        if(jobLockService == null)
        {
            logger.error("Exit HeartBeatJob because there is no JobLockService");
            return;
        }
        QName qName = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, collector.getCollectorId());
        String lockToken = null;
        LockCallback lockCallback = new LockCallback(qName);
        try
        {
            // Get a dynamic lock
            lockToken = acquireLock(lockCallback, qName, jobLockService);
            collectAndSendDataLocked(collector, hbDataSenderService);
        }
        catch (LockAcquisitionException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Skipping collect and send data (could not get lock): " + e.getMessage());
            }
        }
        finally
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Finished collector job. ID:" + collector.getCollectorId());
            }
            lockCallback.running.set(false);
        }
    }

    private void collectAndSendDataLocked(final HBBaseDataCollector collector, final HBDataSenderService hbDataSenderService) throws JobExecutionException
    {
        try
        {
            List<HBData> data = collector.collectData();
            hbDataSenderService.sendData(data);
        }
        catch (final Exception e)
        {
            // Log the error but don't rethrow, collector errors are non fatal
            logger.error("Heartbeat failed to collect data for collector ID: " + collector.getCollectorId(), e);
        }
    }

    private String acquireLock(JobLockService.JobLockRefreshCallback lockCallback, QName lockQname, JobLockService jobLockService)
    {
        // Get lock
        String lockToken = jobLockService.getLock(lockQname, LOCK_TTL);

        // Register the refresh callback which will keep the lock alive
        jobLockService.refreshLock(lockToken, lockQname, LOCK_TTL, lockCallback);

        if (logger.isDebugEnabled())
        {
            logger.debug("Lock acquired: " + lockQname + ": " + lockToken);
        }

        return lockToken;
    }

    private class LockCallback implements JobLockService.JobLockRefreshCallback
    {
        final AtomicBoolean running = new AtomicBoolean(true);
        private QName lockQname;

        public LockCallback(QName lockQname)
        {
            this.lockQname = lockQname;
        }

        @Override
        public boolean isActive()
        {
            return running.get();
        }

        @Override
        public void lockReleased()
        {
            running.set(false);
            if (logger.isDebugEnabled())
            {
                logger.debug("Lock release notification: " + lockQname);
            }
        }
    }
}
