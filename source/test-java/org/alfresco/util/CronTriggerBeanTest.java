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
package org.alfresco.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * This class tests that the CronTriggerBean correctly delays jobs when specified.
 * This test runs in about 25 seconds.
 * 
 * @author Ahmed Owian
 */
public class CronTriggerBeanTest
{
    // One second - an arbitrarily small amount of time to allow for the
    // scheduler to start the jobs
    final long PRECISION_LEEWAY = 1000L;
    final long INTERVAL = 1000L;// One run every second

    private ClassPathXmlApplicationContext context;
    private Scheduler scheduler;

    private static Map<String, ArrayList<Long>> dummyJobRuns;
    private static Object lockToken = new Object();

    @Before
    public void setUp() throws Exception
    {
        dummyJobRuns = new HashMap<String, ArrayList<Long>>();
        this.context = null;
        this.scheduler = null;
    }

    @After
    public void tearDown() throws Exception
    {
        try
        {
            this.scheduler.shutdown();
        }
        catch (Exception e)
        {
            // do nothing
        }

        try
        {
            context.close();
        }
        catch (Exception e)
        {
            // do nothing
        }
    }

    /**
     * Ensures that jobs that are coded without a delay run without delay.
     * @throws Exception
     */
    @Test
    public void testCodedCronTriggerBean() throws Exception
    {
        final String JOB_NAME = "codedCronJob";
        List<Long> jobRuns = this.getRunList(JOB_NAME);
        assertEquals(0, jobRuns.size());
        scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        CronTriggerBean ctBean = new CronTriggerBean();
        ctBean.setBeanName("Dummy");
        ctBean.setCronExpression("0/1 * * * * ? *");
        ctBean.setEnabled(true);
        JobDetail jobDetail = new JobDetail(JOB_NAME, "DefaultGroup", DummyJob.class);
        ctBean.setJobDetail(jobDetail);
        ctBean.setScheduler(scheduler);
        ctBean.afterPropertiesSet();

        assertJobRunsAfterInterval(jobRuns);
        scheduler.shutdown();
        this.assertJobStopsAfterShutdown(jobRuns);
    }

    /**
     * Ensures that jobs that are configured without a delay run without delay.
     * @throws BeansException
     * @throws Exception
     */
    @Test
    public void testConfiguredCronTriggerBean() throws BeansException, Exception
    {
        final String JOB_NAME = "configuredCronJob";
        List<Long> jobRuns = this.getRunList(JOB_NAME);
        assertEquals(0, jobRuns.size());
        context = new ClassPathXmlApplicationContext("alfresco/scheduler-core-context.xml",
                    "org/alfresco/util/test-scheduled-jobs-context.xml");
        CronTriggerBean ctBean = context.getBean("cronTriggerBean", CronTriggerBean.class);
        scheduler = ctBean.getScheduler();
        scheduler.start();

        assertJobRunsAfterInterval(jobRuns);
        context.close(); // When the context closes, the scheduler should close,
                         // thereby stopping the job
        assertJobStopsAfterShutdown(jobRuns);
    }

    /**
     * Ensures that jobs that are coded with a delay run after the delay.
     * @throws Exception
     */
    @Test
    public void testCodedDelayedCronTriggerBean() throws Exception
    {
        final String JOB_NAME = "codedDelayedCronJob";
        List<Long> jobRuns = this.getRunList(JOB_NAME);
        assertEquals(0, jobRuns.size());
        CronTriggerBean ctBean = new CronTriggerBean();
        ctBean.setBeanName("Dummy");
        ctBean.setCronExpression("0/1 * * * * ? *");
        ctBean.setEnabled(true);
        JobDetail jobDetail = new JobDetail(JOB_NAME, "DefaultGroup", DummyJob.class);
        ctBean.setJobDetail(jobDetail);
        scheduler = StdSchedulerFactory.getDefaultScheduler();
        ctBean.setScheduler(scheduler);
        final long START_DELAY = 4000L;
        ctBean.setStartDelay(START_DELAY);

        final long PRE_SCHEDULING = System.currentTimeMillis();
        ctBean.afterPropertiesSet(); // This is when the trigger is actually
                                     // scheduled
        long startTime = ctBean.getTrigger().getStartTime().getTime();
        assertTrue("The startTime should be the time when the trigger is scheduled plus the START_DELAY.",
                    startTime - PRE_SCHEDULING - START_DELAY <= PRECISION_LEEWAY);
        assertEquals(0, jobRuns.size());

        scheduler.start();
        assertJobDoesNotRunBeforeStartTime(jobRuns, startTime);
        assertJobRunsAfterInterval(jobRuns);
        scheduler.shutdown();
        assertJobStopsAfterShutdown(jobRuns);
    }

    /**
     * Ensures that jobs that are configured with a delay run after the delay.
     * @throws BeansException
     * @throws Exception
     */
    @Test
    public void testConfiguredDelayedCronTriggerBean() throws BeansException, Exception
    {
        final String JOB_NAME = "configuredDelayedCronJob";
        List<Long> jobRuns = this.getRunList(JOB_NAME);
        assertEquals(0, jobRuns.size());

        // Captures the system time before the Spring context is initialized and
        // the triggers are scheduled
        final long PRE_INITIALIZATION = System.currentTimeMillis();
        context = new ClassPathXmlApplicationContext("alfresco/scheduler-core-context.xml",
                    "org/alfresco/util/test-scheduled-jobs-context.xml");
        CronTriggerBean ctBean = context.getBean("cronTriggerBeanDelayed", CronTriggerBean.class);
        final long START_DELAY = ctBean.getStartDelay();
        long startTime = ctBean.getTrigger().getStartTime().getTime();
        assertTrue("The startTime should be the time when the Spring context is initialized plus the START_DELAY.",
                    startTime - PRE_INITIALIZATION - START_DELAY <= PRECISION_LEEWAY);
        assertEquals(0, jobRuns.size());

        scheduler = ctBean.getScheduler();
        scheduler.start();
        assertJobDoesNotRunBeforeStartTime(jobRuns, startTime);
        assertJobRunsAfterInterval(jobRuns);
        context.close(); // When the context closes, the scheduler should close,
                         // thereby stopping the job
        assertJobStopsAfterShutdown(jobRuns);
    }

    private void assertJobStopsAfterShutdown(List<Long> jobRuns) throws InterruptedException
    {
        // Gives the job one final interval to stop, but after that, there
        // should be no more runs
        Thread.sleep(INTERVAL);
        int runs = jobRuns.size();
        Thread.sleep(INTERVAL);
        assertEquals(runs, jobRuns.size());
        Thread.sleep(INTERVAL);
        assertEquals(runs, jobRuns.size());
    }

    private void assertJobRunsAfterInterval(List<Long> jobRuns) throws InterruptedException
    {
        // After the interval, there should be at least one run
        Thread.sleep(INTERVAL);
        assertTrue(jobRuns.size() > 0);
    }

    private void assertJobDoesNotRunBeforeStartTime(List<Long> jobRuns, long startTime)
                throws InterruptedException
    {
        // Synchronizing on an object prevents jobs from running while checking
        synchronized (lockToken)
        {
            // It should not run before the start time
            while (System.currentTimeMillis() < startTime)
            {
                assertEquals(0, jobRuns.size());
                Thread.sleep(20); // Sleeps so as to not take up all the CPU
            }
        }
    }

    public static class DummyJob implements Job
    {
        public void execute(JobExecutionContext context) throws JobExecutionException
        {
            synchronized (lockToken)
            {
                long now = System.currentTimeMillis();
                ArrayList<Long> runs = dummyJobRuns.get(context.getJobDetail().getName());
                if (runs == null)
                {
                    runs = new ArrayList<Long>();
                    dummyJobRuns.put(context.getJobDetail().getName(), runs);
                }
                runs.add(now);
            }
        }
    }

    private List<Long> getRunList(String jobName)
    {
        ArrayList<Long> runs = dummyJobRuns.get(jobName);
        if (runs == null)
        {
            runs = new ArrayList<Long>();
            dummyJobRuns.put(jobName, runs);
        }
        return runs;
    }
}
