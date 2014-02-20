package org.alfresco.util;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CronTriggerBeanTest {

	private ClassPathXmlApplicationContext ctx;
	private Scheduler scheduler;
	private static Map<String, ArrayList<Long>> dummyJobRuns;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		dummyJobRuns = new HashMap<String, ArrayList<Long>>();
		this.ctx = null;
		this.scheduler = null;
	}

	@After
	public void tearDown() throws Exception {
		try {
			this.scheduler.shutdown();
		} catch (Exception e) {
			// do nothing
		}
		
		try {
			ctx.close();
		} catch (Exception e) {
			// do nothing
		}
	}

	@Test
	public void testCodedCronTriggerBean() throws Exception {
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

		Thread.sleep(1000);
		int runs = jobRuns.size();
		assertTrue(runs > 0);

		scheduler.shutdown();
		Thread.sleep(1000);
		assertEquals(runs, jobRuns.size());
		Thread.sleep(1000);
		assertEquals(runs, jobRuns.size());
	}

	@Test
	public void testCodedDelayedCronTriggerBean() throws Exception {
		final String JOB_NAME = "codedDelayedCronJob";
		List<Long> jobRuns = this.getRunList(JOB_NAME);
		assertEquals(0, jobRuns.size());
		Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
		scheduler.start();
		CronTriggerBean ctBean = new CronTriggerBean();
		ctBean.setBeanName("Dummy");
		ctBean.setCronExpression("0/1 * * * * ? *");
		ctBean.setEnabled(true);
		JobDetail jobDetail = new JobDetail(JOB_NAME, "DefaultGroup", DummyJob.class);
		ctBean.setJobDetail(jobDetail);
		ctBean.setScheduler(scheduler);
		final long START_DELAY = 4000L;
		ctBean.setStartDelay(START_DELAY);
		ctBean.afterPropertiesSet();

		// It should not have run during the delay. Give a second precision leeway
		Thread.sleep(START_DELAY - 1000);
		assertEquals(0, jobRuns.size());
		// It should have had a chance to run after the delay
		Thread.sleep(1000);
		int runs = jobRuns.size();
		assertTrue(runs > 0);
		// It should not have run again after shutdown
		scheduler.shutdown();
		Thread.sleep(1000);
		assertEquals(runs, jobRuns.size());
		// Indeed after another second, it should not have changed.
		Thread.sleep(1000);
		assertEquals(runs, jobRuns.size());
	}

	@Test
	public void testConfiguredDelayedCronTriggerBean() throws BeansException, Exception {
		final String JOB_NAME = "configuredDelayedCronJob";
		List<Long> jobRuns = this.getRunList(JOB_NAME);
		assertEquals(0, jobRuns.size());
		ctx = new ClassPathXmlApplicationContext(
				"alfresco/scheduler-core-context.xml",
				"org/alfresco/util/test-scheduled-jobs-context.xml");
		CronTriggerBean ctBean = ctx.getBean("cronTriggerBeanDelayed", CronTriggerBean.class);
		scheduler = ctBean.getScheduler();
		scheduler.start();

		// It should not have run during the delay. Give a second precision leeway
		final long START_DELAY = ctBean.getStartDelay();
		Thread.sleep(START_DELAY - 1000);
		assertEquals(0, jobRuns.size());

		// After the interval, there should be at least one run
		final long INTERVAL = 1000L;
		Thread.sleep(INTERVAL);
		int runs = jobRuns.size();
		assertTrue(runs > 0);

		// When the context closes, the scheduler should close, thereby stopping the job
		ctx.close();
		Thread.sleep(INTERVAL);
		assertEquals(runs, jobRuns.size());
		Thread.sleep(INTERVAL);
		assertEquals(runs, jobRuns.size());
	}
	
	@Test
	public void testConfiguredCronTriggerBean() throws BeansException, Exception {
		final String JOB_NAME = "configuredCronJob";
		List<Long> jobRuns = this.getRunList(JOB_NAME);
		assertEquals(0, jobRuns.size());
		ctx = new ClassPathXmlApplicationContext(
				"alfresco/scheduler-core-context.xml",
				"org/alfresco/util/test-scheduled-jobs-context.xml");
		CronTriggerBean ctBean = ctx.getBean("cronTriggerBean", CronTriggerBean.class);
		scheduler = ctBean.getScheduler();
		scheduler.start();

		// After the interval, there should be at least one run
		final long INTERVAL = 1000L;
		Thread.sleep(INTERVAL);
		int runs = jobRuns.size();
		assertTrue(runs > 0);

		// When the context closes, the scheduler should close, thereby stopping the job
		ctx.close();
		Thread.sleep(INTERVAL);
		assertEquals(runs, jobRuns.size());
		Thread.sleep(INTERVAL);
		assertEquals(runs, jobRuns.size());
	}

	public static class DummyJob implements Job {
		public void execute(JobExecutionContext context) throws JobExecutionException {
			long now = System.currentTimeMillis();
			ArrayList<Long> runs = dummyJobRuns.get(context.getJobDetail().getName());
			if (runs == null) {
				runs = new ArrayList<Long>();
				dummyJobRuns.put(context.getJobDetail().getName(), runs);
			}
			runs.add(now);
		}
	}
	
	private List<Long> getRunList(String jobName) {
		ArrayList<Long> runs = dummyJobRuns.get(jobName);
		if (runs == null) {
			runs = new ArrayList<Long>();
			dummyJobRuns.put(jobName, runs);
		}
		return runs;
	}
}
