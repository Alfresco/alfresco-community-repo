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
package org.alfresco.repo.action.scheduled;

import java.util.Date;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.action.RuntimeActionService;
import org.alfresco.repo.action.ActionServiceImplTest.SleepActionExecuter;
import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.scheduled.ScheduledPersistedAction;
import org.alfresco.service.cmr.action.scheduled.ScheduledPersistedActionService;
import org.alfresco.service.cmr.action.scheduled.ScheduledPersistedAction.IntervalPeriod;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.core.jmx.JobDetailSupport;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Unit tests for the {@link ScheduledPersistedActionService}
 */
public class ScheduledPersistedActionServiceTest extends TestCase 
{
   private static ConfigurableApplicationContext ctx = 
      (ConfigurableApplicationContext)ApplicationContextHelper.getApplicationContext();

   private ScheduledPersistedActionService service;
   private Scheduler scheduler;
   
   private TransactionService transactionService;
   private RuntimeActionService runtimeActionService;
   private ActionService actionService;
   private NodeService nodeService;
   private Repository repositoryHelper;
   
   private Action testAction;
   private NodeRef scheduledRoot;
   
   @Override
   protected void setUp() throws Exception
   {
      actionService = (ActionService) ctx.getBean("actionService");
      nodeService = (NodeService) ctx.getBean("nodeService");
      repositoryHelper = (Repository) ctx.getBean("repositoryHelper");
      transactionService = (TransactionService) ctx.getBean("transactionService");
      runtimeActionService = (RuntimeActionService) ctx.getBean("actionService");
      service = (ScheduledPersistedActionService) ctx.getBean("scheduledPersistedActionService");
      scheduler = (Scheduler) ctx.getBean("schedulerFactory");
      
      
      // Set the current security context as admin
      AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
      
      UserTransaction txn = transactionService.getUserTransaction();
      txn.begin();
      
      // Zap any existing persisted entries
      scheduledRoot = ScheduledPersistedActionServiceImpl.SCHEDULED_ACTION_ROOT_NODE_REF;
      for(ChildAssociationRef child : nodeService.getChildAssocs(scheduledRoot))
      {
         QName type = nodeService.getType( child.getChildRef() );
         if(ScheduledPersistedActionServiceImpl.ACTION_TYPES.contains(type))
         {
            nodeService.deleteNode(child.getChildRef());
         }
      }
      
      // Register the test executor, if needed
      SleepActionExecuter.registerIfNeeded(ctx);
      
      // Persist an action that uses the test executor
      testAction = new TestAction(actionService.createAction(SleepActionExecuter.NAME));
      NodeRef actionNodeRef = runtimeActionService.createActionNodeRef(//
            testAction,
            ScheduledPersistedActionServiceImpl.SCHEDULED_ACTION_ROOT_NODE_REF,
            ContentModel.ASSOC_CONTAINS,
            QName.createQName("TestAction")
      );
      
      // Finish setup
      txn.commit();
   }
   
   @Override
   protected void tearDown() throws Exception {
      UserTransaction txn = transactionService.getUserTransaction();
      txn.begin();
      
      
      txn.commit();
   }
   
   
   /**
    * Test that the {@link ScheduledPersistedAction} implementation
    *  behaves properly
    */
   public void testPersistedActionImpl() throws Exception
   {
      // TODO
   }
   
   /**
    * Tests that the to-trigger stuff works properly
    */
   
   /**
    * Tests that we can create, save, edit, delete etc the
    *  scheduled persisted actions
    */
   
   /**
    * Tests that the listings work, both of all scheduled,
    *  and from an action
    */
   
   /**
    * Tests that things get properly injected onto the job bean
    */
   public void testJobBeanInjection() throws Exception
   {
      // The job should run almost immediately
      Job job = new TestJob();
      JobDetail details = new JobDetail(
            "ThisIsATest", null, job.getClass()
      );
      Trigger now = new SimpleTrigger(
            "TestTrigger", new Date(1)
      );
      now.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
      
      Scheduler scheduler = (Scheduler)ctx.getBean("schedulerFactory");
      scheduler.scheduleJob(details, now);

      // Allow it to run
      for(int i=0; i<20; i++)
      {
         if(! TestJob.ran) 
            Thread.sleep(50);
      }
      
      // Ensure it ran, and it got a copy of the context
      assertEquals(true, TestJob.ran);
      assertEquals(true, TestJob.gotContext);
   }
   
   /**
    * Tests that things actually get run correctly
    */
   public void testExecution() throws Exception
   {
      final SleepActionExecuter sleepActionExec = 
         (SleepActionExecuter)ctx.getBean(SleepActionExecuter.NAME);
      sleepActionExec.resetTimesExecuted();
      sleepActionExec.setSleepMs(1);
      
      ScheduledPersistedAction schedule;
      
      
      // Until the schedule is persisted, nothing will happen
      schedule = service.createSchedule(testAction);
      assertEquals(0, scheduler.getJobNames(ScheduledPersistedActionServiceImpl.SCHEDULER_GROUP).length);
      
      
      // A job due to start in 1 second, and run once
      schedule = service.createSchedule(testAction);
      schedule.setScheduleStart(
            new Date(System.currentTimeMillis()+1000)
      );
      assertNull(schedule.getScheduleInterval());
      assertNull(schedule.getScheduleIntervalCount());
      assertNull(schedule.getScheduleIntervalPeriod());
      
      // TODO - Remove this hacky workaround when real persistence is in
      ((ScheduledPersistedActionImpl)schedule).setPersistedAtNodeRef(
            testAction.getNodeRef()
      );
      System.out.println("Job starts in 1 second, no repeat...");
      service.saveSchedule(schedule);
      
      // Check it went in
      assertEquals(1, scheduler.getJobNames(ScheduledPersistedActionServiceImpl.SCHEDULER_GROUP).length);
      
      // Let it run
      Thread.sleep(2000);
      
      // Ensure it did properly run the once
      assertEquals(1, sleepActionExec.getTimesExecuted());
      
      // Should have removed itself now the schedule is over
      assertEquals(0, scheduler.getJobNames(ScheduledPersistedActionServiceImpl.SCHEDULER_GROUP).length);
      
      // Zap it
      service.deleteSchedule(schedule);
      assertEquals(0, scheduler.getJobNames(ScheduledPersistedActionServiceImpl.SCHEDULER_GROUP).length);

      
      // ==========================
      
      
      // A job that runs every 2 seconds, for the next 3.5 seconds
      // (Should get to run twice, now and @2 secs)
      schedule = service.createSchedule(testAction);
      schedule.setScheduleStart(
            new Date(0)
      );
      ((ScheduledPersistedActionImpl)schedule).setScheduleEnd(
            new Date(System.currentTimeMillis()+3500)
      );
      schedule.setScheduleIntervalCount(2);
      schedule.setScheduleIntervalPeriod(IntervalPeriod.Second);
      assertEquals("2s", schedule.getScheduleInterval());
      
      // Reset count
      sleepActionExec.resetTimesExecuted();
      assertEquals(0, sleepActionExec.getTimesExecuted());
      
      // TODO - Remove this hacky workaround when real persistence is in
      ((ScheduledPersistedActionImpl)schedule).setPersistedAtNodeRef(
            testAction.getNodeRef()
      );
      System.out.println("Job starts now, repeats twice @ 2s");
      service.saveSchedule(schedule);
      
      Thread.sleep(4000);
      
      // Ensure it did properly run twice times
      assertEquals(2, sleepActionExec.getTimesExecuted());
      
      // Zap it
      service.deleteSchedule(schedule);
      assertEquals(0, scheduler.getJobNames(ScheduledPersistedActionServiceImpl.SCHEDULER_GROUP).length);
      
      
      // ==========================
      
      
      // A job that starts in 2 seconds time, and runs
      //  every second until we kill it
      schedule = service.createSchedule(testAction);
      schedule.setScheduleStart(
            new Date(System.currentTimeMillis()+2000)
      );
      schedule.setScheduleIntervalCount(1);
      schedule.setScheduleIntervalPeriod(IntervalPeriod.Second);
      assertEquals("1s", schedule.getScheduleInterval());
      
      // Reset count
      sleepActionExec.resetTimesExecuted();
      assertEquals(0, sleepActionExec.getTimesExecuted());
      
      // TODO - Remove this hacky workaround when real persistence is in
      ((ScheduledPersistedActionImpl)schedule).setPersistedAtNodeRef(
            testAction.getNodeRef()
      );
      System.out.println("Job starts in 2s, repeats @ 1s");
      service.saveSchedule(schedule);
      
      // Let it run a few times
      Thread.sleep(5000);
      
      // Zap it - should still be live
      assertEquals(1, scheduler.getJobNames(ScheduledPersistedActionServiceImpl.SCHEDULER_GROUP).length);
      service.deleteSchedule(schedule);
      assertEquals(0, scheduler.getJobNames(ScheduledPersistedActionServiceImpl.SCHEDULER_GROUP).length);
      
      // Check it ran an appropriate number of times
      assertEquals(
            "Didn't run enough - " + sleepActionExec.getTimesExecuted(),
            true,
            sleepActionExec.getTimesExecuted() >= 3
      );
      assertEquals(
            "Ran too much - " + sleepActionExec.getTimesExecuted(),
            true,
            sleepActionExec.getTimesExecuted() < 5
      );
      
      // Ensure it finished shutting down
      Thread.sleep(500);
   }
   
   /**
    * Tests that when we have more than one schedule
    *  defined and active, then the correct things run
    *  at the correct times, and we never get confused
    */
   public void DISABLEDtestMultipleExecutions() throws Exception
   {
      // Create one that starts running in 2 seconds, runs every 2 seconds
      //  until 9 seconds are up (will run 4 times) 
      
      // Create one that starts running now, every second until 9.5 seconds
      //  are up (will run 9-10 times)
      
      // Set them going
      
      // Wait
      
      // Check that they really did run properly
      // TODO
   }
   
   // ============================================================================

  
   /**
    * For unit testing only - not thread safe!
    */
   public static class TestJob implements Job, ApplicationContextAware
   {
      private static boolean gotContext = false;
      private static boolean ran = false;
      
      public TestJob() {
         gotContext = false;
         ran = false;
      }
      
      public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
         gotContext = true;
      }
      public void execute(JobExecutionContext paramJobExecutionContext)
            throws JobExecutionException {
         ran = true;
      }
   }
   
   protected static class TestAction extends ActionImpl
   {
      protected TestAction(Action action)
      {
         super(action);
      }
   }
   
}
