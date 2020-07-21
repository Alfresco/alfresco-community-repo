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
import org.quartz.Job;
import org.quartz.JobDataMap;

/**
 * This scheduler is responsible for the scheduling and unscheduling of non locking jobs {@link NonLockingJob}.
 * All repository nodes in a cluster will send data for collectors which have jobs scheduled by this scheduler.
 *
 * @author eknizat
 *
 */
public class NonLockingJobScheduler extends QuartzJobScheduler
{
    @Override
    protected JobDataMap getJobDetailMap(HBBaseDataCollector collector)
    {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(NonLockingJob.COLLECTOR_KEY, collector);
        jobDataMap.put(NonLockingJob.DATA_SENDER_SERVICE_KEY, hbDataSenderService);
        return jobDataMap;
    }

    @Override
    protected Class<? extends Job> getHeartBeatJobClass()
    {
        return NonLockingJob.class;
    }
}