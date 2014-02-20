package org.alfresco.util;

import static org.junit.Assert.*;

import java.text.ParseException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CronTriggerBeanTest {

    private ClassPathXmlApplicationContext ctx;
	private static int dummyJobRuns; 
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		dummyJobRuns = 0;
	}

	@After
	public void tearDown() throws Exception {
		try {
			ctx.close();
		} catch(Exception e) { 
			// do nothing
		}
	}

	@Test
	public void testCodedCronTriggerBean() throws Exception {
		assertEquals(0, dummyJobRuns);
	    Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
    	scheduler.start();
    	CronTriggerBean ctBean = new CronTriggerBean();
    	ctBean.setBeanName("Dummy");
    	ctBean.setCronExpression("0/1 * * * * ? *");
    	ctBean.setEnabled(true);
    	JobDetail jobDetail = new JobDetail("DummyJob", "DefaultGroup", DummyJob.class);
		ctBean.setJobDetail(jobDetail );
		ctBean.setScheduler(scheduler);
		ctBean.afterPropertiesSet();

		Thread.sleep(1000);
        int runs = dummyJobRuns;
        assertTrue(runs > 0);

    	scheduler.shutdown();
		Thread.sleep(1000);
		assertEquals(runs, dummyJobRuns);
		Thread.sleep(1000);
		assertEquals(runs, dummyJobRuns);
	}


	@Ignore
	@Test
	public void testCodedDelayedCronTriggerBean() throws Exception {
		assertEquals(0, dummyJobRuns);
	    Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
    	scheduler.start();
    	CronTriggerBean ctBean = new CronTriggerBean();
    	ctBean.setBeanName("Dummy");
    	ctBean.setCronExpression("0/1 * * * * ? *");
    	ctBean.setEnabled(true);
    	JobDetail jobDetail = new JobDetail("DummyJob", "DefaultGroup", DummyJob.class);
		ctBean.setJobDetail(jobDetail );
		ctBean.setScheduler(scheduler);
		ctBean.afterPropertiesSet();
    	
		// Validate delayed runs
	}
	
	@Test
	public void testConfiguredCronTriggerBean() throws BeansException, Exception {
        assertEquals(0, dummyJobRuns);
        ctx = new ClassPathXmlApplicationContext("alfresco/scheduler-core-context.xml",
        		"org/alfresco/util/test-scheduled-jobs-context.xml");
        
        CronTriggerBean ctBean = ctx.getBean(CronTriggerBean.class);
        Scheduler scheduler = ctBean.getScheduler();
        scheduler.start();

		Thread.sleep(1000);
        int runs = dummyJobRuns;
        assertTrue(runs > 0);

    	ctx.close();
		Thread.sleep(1000);
		assertEquals(runs, dummyJobRuns);
		Thread.sleep(1000);
		assertEquals(runs, dummyJobRuns);
	}

    public static class DummyJob implements Job
    {
    	public void execute(JobExecutionContext context) throws JobExecutionException
        {
    		dummyJobRuns++;
        }
    }	
}
