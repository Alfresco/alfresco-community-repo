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
package org.alfresco.heartbeat.jobs;

import org.alfresco.heartbeat.HBBaseDataCollector;
import org.alfresco.heartbeat.datasender.HBData;
import org.alfresco.heartbeat.datasender.HBDataSenderService;
import org.alfresco.heartbeat.jobs.LockingJob;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by mmuller on 27/10/2017.
 */
public class LockingJobTest
{

    private HBDataSenderService mockDataSenderService;
    private JobLockService mockJobLockService;
    private HeartBeatJobScheduler mockScheduler;

    @Before
    public void setUp()
    {
        mockDataSenderService = mock(HBDataSenderService.class);
        mockJobLockService = mock(JobLockService.class);
        mockScheduler = mock(HeartBeatJobScheduler.class);
    }

    private class SimpleHBDataCollector extends HBBaseDataCollector
    {

        public SimpleHBDataCollector(String collectorId)
        {
            super(collectorId,"1.0","0 0 0 ? * *", mockScheduler);
        }

        public List<HBData> collectData()
        {
            List<HBData> result = new LinkedList<>();
            result.add(new HBData("systemId2", this.getCollectorId(), "1", new Date()));
            return result;
        }
    }

    @Test
    public void testJobInClusterNotLocked() throws Exception
    {
        // mock the job context
        JobExecutionContext mockJobExecutionContext = mock(JobExecutionContext.class);
        // create the hb collector
        SimpleHBDataCollector simpleCollector = spy(new SimpleHBDataCollector("simpleCollector"));
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("collector", simpleCollector);
        jobDataMap.put("hbDataSenderService", mockDataSenderService);
        jobDataMap.put("jobLockService", mockJobLockService);
        JobDetail jobDetail = JobBuilder.newJob()
                .setJobData(jobDataMap)
                .ofType(LockingJob.class)
                .build();
        when(mockJobExecutionContext.getJobDetail()).thenReturn(jobDetail);


        // collector job is not locked from an other collector
        String lockToken = "locked";

        Runnable r1 = () ->
        {
            // if a second job tries to get the lock before we finished that will raise the exception
            when(mockJobLockService.getLock(isA(QName.class), anyLong())).thenReturn(lockToken).thenThrow(new LockAcquisitionException("", ""));
            try
            {
                new LockingJob().execute(mockJobExecutionContext);
            }
            catch (JobExecutionException e)
            {
                //
            }
            finally
            {
                // when we are finished an other job can have the lock
                when(mockJobLockService.getLock(isA(QName.class), anyLong())).thenReturn(lockToken);
            }
        };
        Runnable r2 = () ->
        {
            try
            {
                new LockingJob().execute(mockJobExecutionContext);
            }
            catch (JobExecutionException e)
            {
                //
            }
        };

        Thread t1 = new Thread(r1);
        Thread t2 = new Thread(r2);

        t1.start();
        Thread.sleep(500);
        t2.start();

        // Wait for threads to finish before testing
        Thread.sleep(1000);

        // verify that we collected and send data but just one time
        verify(simpleCollector, Mockito.times(2)).collectData();
        verify(mockDataSenderService, Mockito.times(2)).sendData(any(List.class));
        verify(mockDataSenderService, Mockito.times(0)).sendData(any(HBData.class));
        verify(mockJobLockService, Mockito.times(2)).getLock(any(QName.class), anyLong());
        verify(mockJobLockService, Mockito.times(2)).refreshLock(eq(lockToken), any(QName.class), anyLong(), any(
                JobLockService.JobLockRefreshCallback.class));
    }


    @Test
    public void testJobLocking() throws Exception
    {
        HBBaseDataCollector simpleCollector = mock(HBBaseDataCollector.class);
        when(simpleCollector.getCollectorId()).thenReturn("c1");
        when(simpleCollector.getCronExpression()).thenReturn("0 0 0 ? * *");

        // mock the job context
        JobExecutionContext mockJobExecutionContext = mock(JobExecutionContext.class);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("collector", simpleCollector);
        jobDataMap.put("hbDataSenderService", mockDataSenderService);
        jobDataMap.put("jobLockService", mockJobLockService);
        JobDetail jobDetail = JobBuilder.newJob()
                .setJobData(jobDataMap)
                .ofType(LockingJob.class)
                .build();
        when(mockJobExecutionContext.getJobDetail()).thenReturn(jobDetail);

        // Simulate job lock service
        String lockToken = "token";
        when(mockJobLockService.getLock(isA(QName.class), anyLong()))
                .thenReturn(lockToken)                                    // first job gets the lock
                .thenThrow(new LockAcquisitionException("", ""));         // second job doesn't get the lock

        // Run two heart beat jobs
        new LockingJob().execute(mockJobExecutionContext);
        new LockingJob().execute(mockJobExecutionContext);

        // Verify that the collector only collects data once, since only one job got the lock
        verify(simpleCollector, Mockito.times(1)).collectData();
        // Verify that data was passed to data sender
        verify(mockDataSenderService, Mockito.times(1)).sendData(any(List.class));
        verify(mockDataSenderService, Mockito.times(0)).sendData(any(HBData.class));
        // Verify that both jobs tried to get the lock
        verify(mockJobLockService, Mockito.times(2)).getLock(any(QName.class), anyLong());
        // Verify that a callback was registered once
        verify(mockJobLockService, Mockito.times(1)).refreshLock(eq(lockToken), any(QName.class),
                anyLong(),
                any(JobLockService.JobLockRefreshCallback.class));
    }
}
