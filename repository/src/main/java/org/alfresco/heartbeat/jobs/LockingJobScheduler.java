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
package org.alfresco.heartbeat.jobs;

import org.alfresco.heartbeat.HBBaseDataCollector;
import org.alfresco.repo.lock.JobLockService;
import org.quartz.Job;
import org.quartz.JobDataMap;


/**
 *
 * The scheduler is responsible for the scheduling and unscheduling of locking jobs {@link LockingJob}.
 * Only one repository node in a cluster will collect data for collectors with this type of job.
 */
public class LockingJobScheduler extends QuartzJobScheduler
{
    /** Services needed to schedule and execute this job */
    private JobLockService jobLockService;

    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    @Override
    protected JobDataMap getJobDetailMap(HBBaseDataCollector collector)
    {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(LockingJob.COLLECTOR_KEY, collector);
        jobDataMap.put(LockingJob.DATA_SENDER_SERVICE_KEY, hbDataSenderService);
        jobDataMap.put(LockingJob.JOB_LOCK_SERVICE_KEY, jobLockService);
        return jobDataMap;
    }

    @Override
    protected Class<? extends Job> getHeartBeatJobClass()
    {
        return LockingJob.class;
    }
}
