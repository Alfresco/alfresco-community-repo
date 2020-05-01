/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.util.Date;

/**
 * Used to schedule reading of config. The config is assumed to change from time to time.
 * Initially or on error the reading frequency is high but slower once no problems are reported.
 * If the normal cron schedule is not set or is in the past, the config is read only once when
 * {@link #run(boolean, Log, CronExpression, CronExpression)} is called.
 *
 * @author adavis
 */
public abstract class ConfigScheduler<Data>
{
    public static class ConfigSchedulerJob implements Job
    {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException
        {
            JobDataMap dataMap = context.getJobDetail().getJobDataMap();
            ConfigScheduler configScheduler = (ConfigScheduler)dataMap.get(CONFIG_SCHEDULER);
            boolean successReadingConfig = configScheduler.readConfigAndReplace(true);
            configScheduler.changeScheduleOnStateChange(successReadingConfig);
        }
    }

    public static final String CONFIG_SCHEDULER = "configScheduler";

    private static final Log defaultLog = LogFactory.getLog(ConfigScheduler.class);
    private static StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();

    private final String jobName;
    private Log log;
    private CronExpression cronExpression;
    private CronExpression initialAndOnErrorCronExpression;

    private Scheduler scheduler;
    private JobKey jobKey;
    private boolean normalCronSchedule;

    protected Data data;
    private ThreadLocal<Data> threadData = ThreadLocal.withInitial(() -> data);

    private ShutdownIndicator shutdownIndicator;

    public ConfigScheduler(Object client)
    {
        jobName = client.getClass().getName()+"Job@"+Integer.toHexString(System.identityHashCode(client));
    }

    public void setShutdownIndicator(ShutdownIndicator shutdownIndicator)
    {
        this.shutdownIndicator = shutdownIndicator;
    }

    private boolean shuttingDown()
    {
        return shutdownIndicator != null && shutdownIndicator.isShuttingDown();
    }

    public abstract boolean readConfig() throws IOException;

    public abstract Data createData();

    public synchronized Data getData()
    {
        // Only the first thread should see a null at the very start.
        Data data = threadData.get();
        if (data == null)
        {
            data = createData();
            setData(data);
        }
        return data;
    }

    private synchronized void setData(Data data)
    {
        this.data = data;
        // Reset what all other Threads see as the data.
        threadData = ThreadLocal.withInitial(() -> data);
    }

    private synchronized void clearData()
    {
        this.data = null;    // as run() should only be called multiple times in testing, it is okay to discard the
                             // previous data, as there should be no other Threads trying to read it, unless they are
                             // left over from previous tests.
        threadData.remove(); // we need to pick up the initial value next time (whatever the data value is at that point)
    }

    /**
     * This method should only be called once in production on startup generally from Spring afterPropertiesSet methods.
     * In testing it is allowed to call this method multiple times, but in that case it is recommended to pass in a
     * null cronExpression (or a cronExpression such as a date in the past) so the scheduler is not started. If this is
     * done, the config is still read, but before the method returns.
     */
    public void run(boolean enabled, Log log, CronExpression cronExpression, CronExpression initialAndOnErrorCronExpression)
    {
        clearPreviousSchedule();
        clearData();
        if (enabled)
        {
            this.log = log == null ? ConfigScheduler.defaultLog : log;
            Date now = new Date();
            if (cronExpression != null &&
                initialAndOnErrorCronExpression != null &&
                cronExpression.getNextValidTimeAfter(now) != null &&
                initialAndOnErrorCronExpression.getNextValidTimeAfter(now) != null)
            {
                this.cronExpression = cronExpression;
                this.initialAndOnErrorCronExpression = initialAndOnErrorCronExpression;
                schedule();
            }
            else
            {
                readConfigAndReplace(false);
            }
        }
    }

    private synchronized void schedule()
    {
        try
        {
            scheduler = schedulerFactory.getScheduler();

            JobDetail job = JobBuilder.newJob()
                    .withIdentity(jobName)
                    .ofType(ConfigSchedulerJob.class)
                    .build();
            jobKey = job.getKey();
            job.getJobDataMap().put(CONFIG_SCHEDULER, this);
            CronExpression cronExpression = normalCronSchedule ? this.cronExpression : initialAndOnErrorCronExpression;
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName+"Trigger", Scheduler.DEFAULT_GROUP)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();
            scheduler.startDelayed(0);
            scheduler.scheduleJob(job, trigger);
            log.debug("Schedule set "+cronExpression);
        }
        catch (Exception e)
        {
            log.error("Error scheduling "+e.getMessage());
        }
    }

    private void clearPreviousSchedule()
    {
        if (scheduler != null)
        {
            try
            {
                scheduler.deleteJob(jobKey);
                scheduler = null;
                jobKey = null;
            }
            catch (Exception e)
            {
                log.error("Error clearing previous schedule " + e.getMessage());
            }
        }
    }

    /**
     * Should only be called directly from test code.
     */
    public boolean readConfigAndReplace(boolean scheduledRead)
    {
        // Config replacement is not done during shutdown. We cannot even log it without generating an INFO message.

        // If shutting down, we return true indicating there were not problems, as that will result in the next
        // scheduled job taking place later where as false would switch to a more frequent retry sequence.
        boolean successReadingConfig = true;
        if (!shuttingDown())
        {
            log.debug((scheduledRead ? "Scheduled" : "Unscheduled") + " config read started");
            Data data = getData();
            try
            {
                Data newData = createData();
                threadData.set(newData);
                successReadingConfig = readConfig();
                data = newData;
                log.debug("Config read finished " + data +
                        (successReadingConfig ? "" : ". Config replaced but there were problems") + "\n");
            }
            catch (Exception e)
            {
                successReadingConfig = false;
                log.error("Config read failed. " + e.getMessage(), e);
            }
            setData(data);
        }
        return successReadingConfig;
    }

    private void changeScheduleOnStateChange(boolean successReadingConfig)
    {
        // Switch schedule sequence if we were on the normal schedule and we now have problems or if
        // we are on the initial/error schedule and there were no errors.
        if ( normalCronSchedule && !successReadingConfig ||
            !normalCronSchedule &&  successReadingConfig)
        {
            normalCronSchedule = !normalCronSchedule;
            clearPreviousSchedule();
            schedule();
        }
    }
}
