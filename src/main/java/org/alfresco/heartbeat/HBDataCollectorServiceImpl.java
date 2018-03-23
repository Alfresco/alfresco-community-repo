/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import org.alfresco.heartbeat.datasender.HBDataSenderService;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.service.cmr.repository.HBDataCollectorService;
import org.alfresco.service.license.LicenseDescriptor;
import org.alfresco.service.license.LicenseService.LicenseChangeHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

/**
 * HBDataCollectorService implementation. This service manages multiple collectors. The collectors containing cron expression
 * which will be used to create time scheduled jobs for executing the tasks from the collector
 *
 */
public class HBDataCollectorServiceImpl implements HBDataCollectorService, LicenseChangeHandler
{
    /** The logger. */
    private static final Log logger = LogFactory.getLog(HBDataCollectorServiceImpl.class);

    /** List of collectors registered with this service */
    private List<HBBaseDataCollector> collectors = new LinkedList<>();

    /** The service responsible for sending the collected data */
    private HBDataSenderService hbDataSenderService;
    private JobLockService jobLockService;

    /** The default enable state */
    private final boolean defaultHbState;

    private Scheduler scheduler;

    /** schedule set for all collectors if testMode is on */
    private boolean testMode = false;
    private final String testCronExpression = "0 0/1 * * * ?";

    /** Current enabled state */
    private boolean enabled = false;

    /**
     *
     * @param defaultHeartBeatState
     *          the default enabled state of heartbeat
     *
     */
    public HBDataCollectorServiceImpl (boolean defaultHeartBeatState)
    {
        this.defaultHbState = defaultHeartBeatState;
        this.enabled = defaultHeartBeatState;
    }

    public void setHbDataSenderService(HBDataSenderService hbDataSenderService)
    {
        this.hbDataSenderService = hbDataSenderService;
    }

    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public synchronized boolean isEnabled()
    {
        return this.enabled;
    }

    public void setTestMode(boolean testMode)
    {
        this.testMode = testMode;
    }

    /**
     *
     * Register data collector with this service and start the schedule.
     * The registered collector will be called to provide heartbeat data at the scheduled interval.
     * Each collector registered via this method must have a unique collector id.
     *
     * @param collector collector to register
     */
    @Override
    public synchronized void registerCollector(final HBBaseDataCollector collector)
    {
        // Check collector with the same ID does't already exist
        for (HBBaseDataCollector col : collectors)
        {
            if(collector.getCollectorId().equals(col.getCollectorId()))
            {
                throw new IllegalArgumentException("HeartBeat did not registered collector, ID must be unique. ID: "
                        + collector.getCollectorId());
            }
        }

        // Schedule collector job and add collector to list of registered collectors
        try
        {
            scheduleCollector(collector);
            collectors.add(collector);

            if (logger.isDebugEnabled())
            {
                logger.debug("HeartBeat registered collector: " + collectorInfo(collector));
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("HeartBeat did not registered collector: "
                    + collectorInfo(collector), e);
        }
    }

    /**
     * Deregister data collector. Before the collector will be removed the collector job will be unscheduled
     *
     * @param collector
     */
    public synchronized void deregisterCollector(final HBBaseDataCollector collector)
    {
        if (collectors.remove(collector))
        {
            try
            {
                final String jobName = "heartbeat-" + collector.getCollectorId();
                final String triggerName = jobName + "-Trigger";
                unscheduleJob(triggerName, collector);

                if (logger.isDebugEnabled())
                {
                    logger.debug("HeartBeat deregistered collector: " + collectorInfo(collector));
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException("HeartBeat did not deregister collector: "
                        + collectorInfo(collector), e);
            }
        }
    }

    @Override
    public boolean isEnabledByDefault()
    {
        return defaultHbState;
    }

    /**
     * Start or stop the HertBeat jobs for all registered collectors
     * depending on whether the heartbeat is enabled or not
     */
    private void scheduleCollector(final HBBaseDataCollector collector) throws ParseException, SchedulerException
    {
        final String jobName = "heartbeat-" + collector.getCollectorId();
        final String triggerName = jobName + "-Trigger";

        if (this.enabled)
        {
            scheduleJob(jobName, triggerName, collector);
        }
        else
        {
            unscheduleJob(triggerName, collector);
        }

    }

    private void scheduleJob(final String jobName, final String triggerName, final HBBaseDataCollector collector) throws SchedulerException
    {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(HeartBeatJob.COLLECTOR_KEY, collector);
        jobDataMap.put(HeartBeatJob.DATA_SENDER_SERVICE_KEY, hbDataSenderService);
        jobDataMap.put(HeartBeatJob.JOB_LOCK_SERVICE_KEY, jobLockService);
        final JobDetail jobDetail = JobBuilder.newJob()
                .withIdentity(jobName)
                .usingJobData(jobDataMap)
                .ofType(HeartBeatJob.class)
                .build();

        final String cronExpression = testMode ? testCronExpression : collector.getCronExpression();
        // Schedule job
        final CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerName)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();
        // Ensure the job wasn't already scheduled in an earlier retry of this transaction
        scheduler.unscheduleJob(cronTrigger.getKey());
        scheduler.scheduleJob(jobDetail, cronTrigger);

        if (logger.isDebugEnabled())
        {
            logger.debug("HeartBeat job scheduled for collector: " + collectorInfo(collector));
        }
    }

    private void unscheduleJob(final String triggerName, final HBBaseDataCollector collector) throws SchedulerException
    {
        scheduler.unscheduleJob(new TriggerKey(triggerName));

        if (logger.isDebugEnabled())
        {
            logger.debug("HeartBeat unscheduled job for collector: " + collectorInfo(collector));
        }
    }

    /**
     * Listens for license changes.  If a license is change or removed, the heartbeat job is rescheduled.
     */
    @Override
    public synchronized void onLicenseChange(final LicenseDescriptor licenseDescriptor)
    {
        final boolean newEnabled = !licenseDescriptor.isHeartBeatDisabled();

        if (newEnabled != this.enabled)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("HeartBeat enabled state change. Enabled=" + newEnabled);
            }
            setEnable(newEnabled);
            restartAllCollectorSchedules();
        }
    }

    /**
     * License load failure resets the heartbeat back to the default state
     */
    @Override
    public synchronized void onLicenseFail()
    {
        final boolean newEnabled = isEnabledByDefault();

        if (newEnabled != this.enabled)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("HeartBeat enabled state change. Enabled=" + newEnabled);
            }
            setEnable(newEnabled);
            restartAllCollectorSchedules();
        }
    }

    private void restartAllCollectorSchedules()
    {
        for(HBBaseDataCollector collector : collectors)
        {
            try
            {
                scheduleCollector(collector);
            }
            catch (Exception e)
            {
                // Log and ignore
                logger.error("HeartBeat failed to restart collector: " + collector.getCollectorId() ,e);
            }
        }
    }

    private void setEnable(boolean enable)
    {
        this.enabled = enable;
        if (hbDataSenderService != null)
        {
            hbDataSenderService.enable(enable);
        }
    }

    private String collectorInfo(HBBaseDataCollector collector)
    {
        return collector.getCollectorId() + " " + collector.getCollectorVersion();
    }
}
