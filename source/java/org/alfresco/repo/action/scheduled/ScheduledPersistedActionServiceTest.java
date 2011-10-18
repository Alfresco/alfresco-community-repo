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
import java.util.List;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.action.RuntimeActionService;
import org.alfresco.repo.action.ActionServiceImplTest.SleepActionExecuter;
import org.alfresco.repo.action.scheduled.ScheduledPersistedActionServiceImpl.ScheduledPersistedActionServiceBootstrap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.scheduled.ScheduledPersistedAction;
import org.alfresco.service.cmr.action.scheduled.ScheduledPersistedActionService;
import org.alfresco.service.cmr.action.scheduled.SchedulableAction.IntervalPeriod;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.util.ApplicationContextHelper;
import org.quartz.DateIntervalTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.DateIntervalTrigger.IntervalUnit;
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
        (ConfigurableApplicationContext) ApplicationContextHelper.getApplicationContext();

    private ScheduledPersistedActionServiceBootstrap bootstrap;
    private ScheduledPersistedActionService service;
    private ScheduledPersistedActionServiceImpl serviceImpl;
    private Scheduler scheduler;

    private TransactionService transactionService;
    private RuntimeActionService runtimeActionService;
    private ActionService actionService;
    private NodeService nodeService;

    private Action testAction;
    private Action testAction2;
    private Action testAction3;

    @Override
    protected void setUp() throws Exception 
    {
      actionService = (ActionService) ctx.getBean("actionService");
      nodeService = (NodeService) ctx.getBean("nodeService");
      transactionService = (TransactionService) ctx
            .getBean("transactionService");
      runtimeActionService = (RuntimeActionService) ctx
            .getBean("actionService");
      service = (ScheduledPersistedActionService) ctx
            .getBean("ScheduledPersistedActionService");
      serviceImpl = (ScheduledPersistedActionServiceImpl) ctx
            .getBean("scheduledPersistedActionService");
      scheduler = (Scheduler) ctx.getBean("schedulerFactory");
      bootstrap = (ScheduledPersistedActionServiceBootstrap) ctx
            .getBean("scheduledPersistedActionServiceBootstrap");

      // Set the current security context as admin
      AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil
            .getAdminUserName());

      UserTransaction txn = transactionService.getUserTransaction();
      txn.begin();

      // Register the test executor, if needed
      SleepActionExecuter.registerIfNeeded(ctx);

      // Zap all test schedules
      List<ScheduledPersistedAction> schedules = service.listSchedules();
      for (ScheduledPersistedAction schedule : schedules) 
      {
         service.deleteSchedule(schedule);
      }

      // Persist an action that uses the test executor
      testAction = new TestAction(actionService
            .createAction(SleepActionExecuter.NAME));
      runtimeActionService.createActionNodeRef(
            testAction, serviceImpl.SCHEDULED_ACTION_ROOT_NODE_REF,
            ContentModel.ASSOC_CONTAINS, QName.createQName("TestAction"));

      testAction2 = new TestAction(actionService
            .createAction(SleepActionExecuter.NAME));
      runtimeActionService.createActionNodeRef(
            testAction2, serviceImpl.SCHEDULED_ACTION_ROOT_NODE_REF,
            ContentModel.ASSOC_CONTAINS, QName.createQName("TestAction2"));

      testAction3 = new TestAction(actionService
            .createAction(SleepActionExecuter.NAME));

      // Finish setup
      txn.commit();

      // By default, we don't want the scheduler to fire while the tests run
      // Certain tests will enable it as required
      scheduler.standby();
    }

    @Override
    protected void tearDown() throws Exception 
    {
      UserTransaction txn = transactionService.getUserTransaction();
      txn.begin();

      // Zap all test schedules
      List<ScheduledPersistedAction> schedules = service.listSchedules();
      for (ScheduledPersistedAction schedule : schedules) 
      {
         service.deleteSchedule(schedule);
      }
      txn.commit();

      // Re-enable the scheduler again
      scheduler.start();
   }

   /**
    * Test that the {@link ScheduledPersistedAction} implementation behaves
    * properly
    */
   public void testPersistedActionImpl() throws Exception 
   {
      ScheduledPersistedActionImpl schedule = 
         new ScheduledPersistedActionImpl(testAction);
      ScheduledPersistedActionImpl schedule3 = 
         new ScheduledPersistedActionImpl(testAction3);

      // Check the core bits
      assertEquals(null, schedule.getPersistedAtNodeRef());
      assertEquals(testAction, schedule.getAction());
      assertEquals(testAction.getNodeRef(), schedule.getActionNodeRef());

      assertEquals(null, schedule3.getPersistedAtNodeRef());
      assertEquals(testAction3, schedule3.getAction());
      assertEquals(null, schedule3.getActionNodeRef());

      // Persist the 3rd action
      runtimeActionService.createActionNodeRef(
            //
            testAction3, serviceImpl.SCHEDULED_ACTION_ROOT_NODE_REF,
            ContentModel.ASSOC_CONTAINS, QName.createQName("TestAction3"));

      assertEquals(null, schedule.getPersistedAtNodeRef());
      assertEquals(testAction, schedule.getAction());
      assertEquals(testAction.getNodeRef(), schedule.getActionNodeRef());

      assertEquals(null, schedule3.getPersistedAtNodeRef());
      assertEquals(testAction3, schedule3.getAction());
      assertEquals(testAction3.getNodeRef(), schedule3.getActionNodeRef());
      assertNotNull(schedule3.getAction().getNodeRef());

      // Check the start/end date bits
      assertEquals(null, schedule.getScheduleStart());
      assertEquals(null, schedule.getScheduleEnd());

      schedule.setScheduleStart(new Date(1234));
      assertEquals(1234, schedule.getScheduleStart().getTime());
      assertEquals(null, schedule.getScheduleEnd());

      schedule.setScheduleEnd(new Date(4321));
      assertEquals(1234, schedule.getScheduleStart().getTime());
      assertEquals(4321, schedule.getScheduleEnd().getTime());

      assertEquals(null, schedule3.getScheduleStart());
      assertEquals(null, schedule3.getScheduleEnd());

      schedule.setScheduleStart(null);
      assertEquals(null, schedule.getScheduleStart());
      assertEquals(4321, schedule.getScheduleEnd().getTime());

      schedule.setScheduleEnd(null);
      assertEquals(null, schedule.getScheduleStart());
      assertEquals(null, schedule.getScheduleEnd());

      // Check the interval parts
      assertEquals(null, schedule.getScheduleInterval());
      assertEquals(null, schedule.getScheduleIntervalCount());
      assertEquals(null, schedule.getScheduleIntervalPeriod());

      schedule.setScheduleIntervalCount(3);
      assertEquals(null, schedule.getScheduleInterval());
      assertEquals(3, schedule.getScheduleIntervalCount().intValue());
      assertEquals(null, schedule.getScheduleIntervalPeriod());

      schedule.setScheduleIntervalPeriod(IntervalPeriod.Hour);
      assertEquals("3Hour", schedule.getScheduleInterval());
      assertEquals(3, schedule.getScheduleIntervalCount().intValue());
      assertEquals(IntervalPeriod.Hour, schedule.getScheduleIntervalPeriod());

      schedule.setScheduleIntervalCount(8);
      schedule.setScheduleIntervalPeriod(IntervalPeriod.Month);
      assertEquals("8Month", schedule.getScheduleInterval());
      assertEquals(8, schedule.getScheduleIntervalCount().intValue());
      assertEquals(IntervalPeriod.Month, schedule.getScheduleIntervalPeriod());

      schedule.setScheduleIntervalCount(null);
      assertEquals(null, schedule.getScheduleInterval());
      assertEquals(null, schedule.getScheduleIntervalCount());
      assertEquals(IntervalPeriod.Month, schedule.getScheduleIntervalPeriod());

      schedule.setScheduleIntervalPeriod(null);
      assertEquals(null, schedule.getScheduleInterval());
      assertEquals(null, schedule.getScheduleIntervalCount());
      assertEquals(null, schedule.getScheduleIntervalPeriod());

      // Trigger parts happen in another test
   }

   /**
    * Tests that the to-trigger stuff works properly
    */
   public void testActionToTrigger() throws Exception 
   {
      // Can't get a trigger until persisted
      ScheduledPersistedActionImpl schedule = 
         (ScheduledPersistedActionImpl) service.createSchedule(testAction);
      Trigger t;
      try {
         schedule.asTrigger();
         fail("Should require persistence first");
      } catch (IllegalStateException e) {
      }

      service.saveSchedule(schedule);
      schedule.asTrigger();

      // No schedule, no trigger
      assertEquals(null, schedule.getScheduleInterval());
      assertEquals(null, schedule.getScheduleIntervalCount());
      assertEquals(null, schedule.getScheduleIntervalPeriod());
      assertEquals(null, schedule.asTrigger());

      // Only start date
      schedule.setScheduleStart(new Date(12345));

      t = schedule.asTrigger();
      assertNotNull(t);
      assertEquals(12345, t.getStartTime().getTime());
      assertEquals(null, t.getEndTime());
      assertEquals(SimpleTrigger.class, t.getClass());

      // Only end date
      // (End date + no repeat = never schedule)
      schedule.setScheduleStart(null);
      schedule.setScheduleEnd(new Date(12345));

      t = schedule.asTrigger();
      assertEquals(null, t);

      // Only interval
      schedule.setScheduleStart(null);
      schedule.setScheduleEnd(null);
      schedule.setScheduleIntervalCount(2);
      schedule.setScheduleIntervalPeriod(IntervalPeriod.Second);

      t = schedule.asTrigger();
      assertNotNull(t);
      assertEquals((double) System.currentTimeMillis(), (double) t
            .getStartTime().getTime(), 10); // Within 10ms
      assertEquals(null, t.getEndTime());
      assertEquals(DateIntervalTrigger.class, t.getClass());
      assertEquals(2, ((DateIntervalTrigger) t).getRepeatInterval());
      assertEquals(IntervalUnit.SECOND, ((DateIntervalTrigger) t)
            .getRepeatIntervalUnit());

      // Start+interval
      schedule.setScheduleStart(new Date(12345));
      schedule.setScheduleEnd(null);
      schedule.setScheduleIntervalCount(3);
      schedule.setScheduleIntervalPeriod(IntervalPeriod.Month);

      t = schedule.asTrigger();
      assertNotNull(t);
      assertEquals(12345, t.getStartTime().getTime());
      assertEquals(null, t.getEndTime());
      assertEquals(DateIntervalTrigger.class, t.getClass());
      assertEquals(3, ((DateIntervalTrigger) t).getRepeatInterval());
      assertEquals(IntervalUnit.MONTH, ((DateIntervalTrigger) t)
            .getRepeatIntervalUnit());

      // Start+interval+end-in-the-past
      schedule.setScheduleStart(new Date(12345));
      schedule.setScheduleEnd(new Date(54321));
      schedule.setScheduleIntervalCount(12);
      schedule.setScheduleIntervalPeriod(IntervalPeriod.Week);

      t = schedule.asTrigger();
      assertEquals(null, t);

      // Start+interval+end-in-the-future
      long future = System.currentTimeMillis() + 1234567;
      schedule.setScheduleStart(new Date(12345));
      schedule.setScheduleEnd(new Date(future));
      schedule.setScheduleIntervalCount(12);
      schedule.setScheduleIntervalPeriod(IntervalPeriod.Week);

      t = schedule.asTrigger();
      assertNotNull(t);
      assertEquals(12345, t.getStartTime().getTime());
      assertEquals(future, t.getEndTime().getTime());
      assertEquals(DateIntervalTrigger.class, t.getClass());
      assertEquals(12, ((DateIntervalTrigger) t).getRepeatInterval());
      assertEquals(IntervalUnit.WEEK, ((DateIntervalTrigger) t)
            .getRepeatIntervalUnit());

      // interval+end
      schedule.setScheduleStart(null);
      schedule.setScheduleEnd(new Date(future));
      schedule.setScheduleIntervalCount(6);
      schedule.setScheduleIntervalPeriod(IntervalPeriod.Hour);

      t = schedule.asTrigger();
      assertNotNull(t);
      assertEquals((double) System.currentTimeMillis(), (double) t
            .getStartTime().getTime(), 2); // Within 2ms
      assertEquals(future, t.getEndTime().getTime());
      assertEquals(DateIntervalTrigger.class, t.getClass());
      assertEquals(6, ((DateIntervalTrigger) t).getRepeatInterval());
      assertEquals(IntervalUnit.HOUR, ((DateIntervalTrigger) t)
            .getRepeatIntervalUnit());

      // Start+end-in-the-past
      // (Ignored as the end has passed)
      schedule.setScheduleStart(new Date(12345));
      schedule.setScheduleEnd(new Date(54321));
      schedule.setScheduleIntervalCount(null);
      schedule.setScheduleIntervalPeriod(null);

      t = schedule.asTrigger();
      assertEquals(null, t);

      // Start+end-in-the-future
      // Start is used to decide when to run
      // End is used to decide if we missed it!
      // (No interval so no repeats so end not needed on trigger)
      schedule.setScheduleStart(new Date(12345));
      schedule.setScheduleEnd(new Date(future));
      schedule.setScheduleIntervalCount(null);
      schedule.setScheduleIntervalPeriod(null);

      t = schedule.asTrigger();
      assertNotNull(t);
      assertEquals(12345, t.getStartTime().getTime());
      assertEquals(null, t.getEndTime());
      assertEquals(SimpleTrigger.class, t.getClass());
   }

   /**
    * Tests that the triggers are suitably tweaked based on when the last run
    * occured
    */
   public void testAsTriggerLastRun() throws Exception 
   {
      long future = System.currentTimeMillis() + 1234567;
      long future90mins = System.currentTimeMillis() + 90*60*1000;
      long past30mins = System.currentTimeMillis() - 30*60*1000;
      long past90mins = System.currentTimeMillis() - 90*60*1000;
      long past150mins = System.currentTimeMillis() - 150*60*1000;
      
      ScheduledPersistedActionImpl schedule = (ScheduledPersistedActionImpl) service
            .createSchedule(testAction);
      service.saveSchedule(schedule);
      Trigger t;
      
      
      // No start date, repeats set, never run
      //  Will be started ASAP
      schedule.setScheduleStart(null);
      schedule.setScheduleEnd(null);
      schedule.setScheduleLastExecutedAt(null);
      schedule.setScheduleIntervalPeriod(IntervalPeriod.Hour);
      schedule.setScheduleIntervalCount(2);

      t = schedule.asTrigger();
      assertNotNull(t);
      
      assertEquals(
            (double) System.currentTimeMillis(), 
            (double) t.getStartTime().getTime(), 10); // Within 10ms
      assertEquals(null, t.getEndTime());
      

      // No start date, repeats set, previously run
      //  Will be started ASAP
      schedule.setScheduleStart(null);
      schedule.setScheduleEnd(null);
      schedule.setScheduleLastExecutedAt(new Date(past30mins));
      schedule.setScheduleIntervalPeriod(IntervalPeriod.Hour);
      schedule.setScheduleIntervalCount(2);

      t = schedule.asTrigger();
      assertNotNull(t);
      
      assertEquals(
            (double) System.currentTimeMillis(), 
            (double) t.getStartTime().getTime(), 10); // Within 10ms
      assertEquals(null, t.getEndTime());

      
      // Start date in the past, no repeats, never run
      //  Will be started ASAP
      schedule.setScheduleStart(new Date(past30mins));
      schedule.setScheduleEnd(null);
      schedule.setScheduleLastExecutedAt(null);
      schedule.setScheduleIntervalPeriod(null);
      schedule.setScheduleIntervalCount(null);

      t = schedule.asTrigger();
      assertNotNull(t);
      
      assertEquals(past30mins, t.getStartTime().getTime());
      assertEquals(null, t.getEndTime());

      
      // Start date in the future, no repeats, never run
      //  Will be started at the requested time
      schedule.setScheduleStart(new Date(future));
      schedule.setScheduleEnd(null);
      schedule.setScheduleLastExecutedAt(null);
      schedule.setScheduleIntervalPeriod(null);
      schedule.setScheduleIntervalCount(null);

      t = schedule.asTrigger();
      assertNotNull(t);
      
      assertEquals(future, t.getStartTime().getTime());
      assertEquals(null, t.getEndTime());

      
      // Start date in the past, no repeats, has run since the start date
      //  Won't be started, as deemed to have already fired
      schedule.setScheduleStart(new Date(past90mins));
      schedule.setScheduleEnd(null);
      schedule.setScheduleLastExecutedAt(new Date(past30mins));
      schedule.setScheduleIntervalPeriod(null);
      schedule.setScheduleIntervalCount(null);

      t = schedule.asTrigger();
      assertEquals(null, t);
      
      
      // Start date in the past, no repeats, previously run but before the start date
      //  Will be run ASAP, previous run details will be ignored
      schedule.setScheduleStart(new Date(past30mins));
      schedule.setScheduleEnd(null);
      schedule.setScheduleLastExecutedAt(new Date(past90mins));
      schedule.setScheduleIntervalPeriod(null);
      schedule.setScheduleIntervalCount(null);

      t = schedule.asTrigger();
      assertNotNull(t);
      
      assertEquals(past30mins, t.getStartTime().getTime());
      assertEquals(null, t.getEndTime());

      
      // Start date in the future, no repeats, previously run
      //  Will be started at the requested time
      schedule.setScheduleStart(new Date(future));
      schedule.setScheduleEnd(null);
      schedule.setScheduleLastExecutedAt(new Date(past30mins));
      schedule.setScheduleIntervalPeriod(IntervalPeriod.Hour);
      schedule.setScheduleIntervalCount(2);

      t = schedule.asTrigger();
      assertNotNull(t);
      
      assertEquals(future, t.getStartTime().getTime());
      assertEquals(null, t.getEndTime());

      
      // Start date in the past, has repeats, never run
      //  Will be run ASAP 
      schedule.setScheduleStart(new Date(past30mins));
      schedule.setScheduleEnd(null);
      schedule.setScheduleLastExecutedAt(null);
      schedule.setScheduleIntervalPeriod(IntervalPeriod.Hour);
      schedule.setScheduleIntervalCount(2);

      t = schedule.asTrigger();
      assertNotNull(t);
      
      assertEquals(past30mins, t.getStartTime().getTime());
      assertEquals(null, t.getEndTime());

      
      // Start date in the future, has repeats, never run
      //  Will be started at the requested time 
      schedule.setScheduleStart(new Date(future));
      schedule.setScheduleEnd(null);
      schedule.setScheduleLastExecutedAt(null);
      schedule.setScheduleIntervalPeriod(IntervalPeriod.Hour);
      schedule.setScheduleIntervalCount(2);

      t = schedule.asTrigger();
      assertNotNull(t);
      
      assertEquals(future, t.getStartTime().getTime());
      assertEquals(null, t.getEndTime());

      
      // Start date in the past, has repeats, last run was before
      //  the start date
      //  Will be run ASAP, previous run details will be ignored
      schedule.setScheduleStart(new Date(past30mins));
      schedule.setScheduleEnd(null);
      schedule.setScheduleLastExecutedAt(new Date(past90mins));
      schedule.setScheduleIntervalPeriod(IntervalPeriod.Hour);
      schedule.setScheduleIntervalCount(2);

      t = schedule.asTrigger();
      assertNotNull(t);
      
      assertEquals(past30mins, t.getStartTime().getTime());
      assertEquals(null, t.getEndTime());

      
      // Start date in the past, has repeats, run since the start date,
      //  last run within the repeat interval
      // Will run at the next interval, based off the start time
      schedule.setScheduleStart(new Date(past150mins)); // 2.5 hours ago
      schedule.setScheduleEnd(null);
      schedule.setScheduleLastExecutedAt(new Date(past30mins)); // 0.5 hours ago, schedule worked
      schedule.setScheduleIntervalPeriod(IntervalPeriod.Hour);
      schedule.setScheduleIntervalCount(2);

      t = schedule.asTrigger();
      assertNotNull(t);
      
      assertEquals(future90mins, t.getStartTime().getTime()); // 4 hours from start time
      assertEquals(null, t.getEndTime());

      
      // Start date in the past, has repeats, run since the start date,
      //  last run over a repeat interval ago
      // Will be run ASAP, start date based on real start so that
      //  the interval is done right
      schedule.setScheduleStart(new Date(past150mins)); // 2.5 hours ago
      schedule.setScheduleEnd(null);
      schedule.setScheduleLastExecutedAt(new Date(past90mins));
      schedule.setScheduleIntervalPeriod(IntervalPeriod.Hour);
      schedule.setScheduleIntervalCount(2);

      t = schedule.asTrigger();
      assertNotNull(t);
      
      assertEquals(past150mins, t.getStartTime().getTime()); // Real start used
      assertEquals(future90mins, t.getFireTimeAfter(new Date()).getTime()); // After this, fire 4 hours from start 
      assertEquals(null, t.getEndTime());
   }

   /**
    * Tests that we can create, save, edit, delete etc the scheduled persisted
    * actions
    */
   public void testCreation() 
   {
      ScheduledPersistedAction schedule = service.createSchedule(testAction);
      assertNotNull(schedule);
      assertTrue(testAction == schedule.getAction());
      assertEquals(testAction.getNodeRef(), schedule.getAction().getNodeRef());

      assertNull(schedule.getScheduleStart());
      assertNull(schedule.getScheduleInterval());
      assertNull(schedule.getScheduleIntervalCount());
      assertNull(schedule.getScheduleIntervalPeriod());

      Date now = new Date();
      schedule.setScheduleStart(now);
      assertEquals(now, schedule.getScheduleStart());
      schedule.setScheduleIntervalCount(2);
      assertEquals(new Integer(2), schedule.getScheduleIntervalCount());
      schedule
            .setScheduleIntervalPeriod(ScheduledPersistedAction.IntervalPeriod.Day);
      assertEquals(ScheduledPersistedAction.IntervalPeriod.Day, schedule
            .getScheduleIntervalPeriod());
   }

   public void testCreateSaveLoad() throws Exception 
   {
      // create and save schedule
      ScheduledPersistedAction schedule = service.createSchedule(testAction);
      assertNotNull(schedule);
      Date now = new Date();
      schedule.setScheduleStart(now);
      schedule.setScheduleIntervalCount(2);
      schedule
            .setScheduleIntervalPeriod(ScheduledPersistedAction.IntervalPeriod.Day);

      assertNull(
            ((ScheduledPersistedActionImpl) schedule).getPersistedAtNodeRef()
      );
      service.saveSchedule(schedule);
      assertNotNull(((ScheduledPersistedActionImpl) schedule)
            .getPersistedAtNodeRef());

      // Load it again, should have the same details still
      ScheduledPersistedAction retrieved = serviceImpl
            .loadPersistentSchedule(((ScheduledPersistedActionImpl) schedule)
                  .getPersistedAtNodeRef());
      assertNotNull(retrieved);
      assertEquals(testAction.getNodeRef(), retrieved.getAction().getNodeRef());
      assertEquals(now, retrieved.getScheduleStart());
      assertEquals(new Integer(2), retrieved.getScheduleIntervalCount());
      assertEquals(ScheduledPersistedAction.IntervalPeriod.Day, retrieved
            .getScheduleIntervalPeriod());
      assertNotNull(((ScheduledPersistedActionImpl) schedule)
            .getPersistedAtNodeRef());

      // Load a 2nd copy, won't be any changes
      ScheduledPersistedAction second = serviceImpl
            .loadPersistentSchedule(((ScheduledPersistedActionImpl) schedule)
                  .getPersistedAtNodeRef());
      assertNotNull(second);
      assertEquals(testAction.getNodeRef(), second.getAction().getNodeRef());
      assertEquals(now, second.getScheduleStart());
      assertEquals(new Integer(2), second.getScheduleIntervalCount());
      assertEquals(ScheduledPersistedAction.IntervalPeriod.Day, second
            .getScheduleIntervalPeriod());

      // Now ensure we can create for an action that didn't have a noderef
      // when we started
      schedule = service.createSchedule(testAction3);

      assertNull(schedule.getActionNodeRef());
      assertNull(((ScheduledPersistedActionImpl) schedule)
            .getPersistedAtNodeRef());

      runtimeActionService.createActionNodeRef(
            testAction3, serviceImpl.SCHEDULED_ACTION_ROOT_NODE_REF,
            ContentModel.ASSOC_CONTAINS, QName.createQName("TestAction3"));

      assertNotNull(schedule.getActionNodeRef());
      assertNull(((ScheduledPersistedActionImpl) schedule)
            .getPersistedAtNodeRef());

      service.saveSchedule(schedule);

      assertNotNull(schedule.getActionNodeRef());
      assertNotNull(((ScheduledPersistedActionImpl) schedule)
            .getPersistedAtNodeRef());
   }

   /**
    * Ensures that we can create, save, edit, save load, edit, save, load etc,
    * all without problems, and without creating duplicates
    */
   public void testEditing() throws Exception 
   {
      // create and save schedule
      ScheduledPersistedAction schedule = service.createSchedule(testAction);
      assertNotNull(schedule);
      Date now = new Date();
      schedule.setScheduleStart(now);
      schedule.setScheduleIntervalCount(2);
      schedule
            .setScheduleIntervalPeriod(ScheduledPersistedAction.IntervalPeriod.Day);

      UserTransaction txn = transactionService.getUserTransaction();
      txn.begin();
      service.saveSchedule(schedule);
      txn.commit();

      // Load and check it hasn't changed
      txn = transactionService.getUserTransaction();
      txn.begin();
      ScheduledPersistedAction retrieved = serviceImpl
            .loadPersistentSchedule(((ScheduledPersistedActionImpl) schedule)
                  .getPersistedAtNodeRef());
      txn.commit();

      assertNotNull(retrieved);
      assertEquals(testAction.getNodeRef(), retrieved.getAction().getNodeRef());
      assertEquals(now, retrieved.getScheduleStart());
      assertEquals(new Integer(2), retrieved.getScheduleIntervalCount());
      assertEquals(ScheduledPersistedAction.IntervalPeriod.Day, retrieved
            .getScheduleIntervalPeriod());

      // Save and re-load without changes
      txn = transactionService.getUserTransaction();
      txn.begin();
      service.saveSchedule(schedule);
      retrieved = serviceImpl
            .loadPersistentSchedule(((ScheduledPersistedActionImpl) schedule)
                  .getPersistedAtNodeRef());
      txn.commit();

      assertNotNull(retrieved);
      assertEquals(testAction.getNodeRef(), retrieved.getAction().getNodeRef());
      assertEquals(now, retrieved.getScheduleStart());
      assertEquals(new Integer(2), retrieved.getScheduleIntervalCount());
      assertEquals(ScheduledPersistedAction.IntervalPeriod.Day, retrieved
            .getScheduleIntervalPeriod());

      // Make some small changes
      txn = transactionService.getUserTransaction();
      txn.begin();
      retrieved = serviceImpl
            .loadPersistentSchedule(((ScheduledPersistedActionImpl) schedule)
                  .getPersistedAtNodeRef());
      retrieved.setScheduleIntervalCount(3);
      service.saveSchedule(retrieved);
      retrieved = serviceImpl
            .loadPersistentSchedule(((ScheduledPersistedActionImpl) schedule)
                  .getPersistedAtNodeRef());
      txn.commit();

      assertNotNull(retrieved);
      assertEquals(testAction.getNodeRef(), retrieved.getAction().getNodeRef());
      assertEquals(now, retrieved.getScheduleStart());
      assertEquals(new Integer(3), retrieved.getScheduleIntervalCount());
      assertEquals(ScheduledPersistedAction.IntervalPeriod.Day, retrieved
            .getScheduleIntervalPeriod());

      // And some more changes
      retrieved
            .setScheduleIntervalPeriod(ScheduledPersistedAction.IntervalPeriod.Month);
      now = new Date();
      retrieved.setScheduleStart(now);

      txn = transactionService.getUserTransaction();
      txn.begin();
      service.saveSchedule(retrieved);
      retrieved = serviceImpl
            .loadPersistentSchedule(((ScheduledPersistedActionImpl) schedule)
                  .getPersistedAtNodeRef());
      txn.commit();

      assertNotNull(retrieved);
      assertEquals(testAction.getNodeRef(), retrieved.getAction().getNodeRef());
      assertEquals(now, retrieved.getScheduleStart());
      assertEquals(new Integer(3), retrieved.getScheduleIntervalCount());
      assertEquals(ScheduledPersistedAction.IntervalPeriod.Month, retrieved
            .getScheduleIntervalPeriod());
   }

   /**
    * Tests that the listings work, both of all scheduled, and from an action
    */
   public void testLoadList() throws Exception 
   {
      assertEquals(0, service.listSchedules().size());

      // Create
      ScheduledPersistedAction schedule1 = service.createSchedule(testAction);
      assertNotNull(schedule1);
      ScheduledPersistedAction schedule2 = service.createSchedule(testAction2);
      assertNotNull(schedule2);

      assertEquals(0, service.listSchedules().size());

      service.saveSchedule(schedule1);

      assertEquals(1, service.listSchedules().size());
      assertEquals(testAction.getNodeRef(), service.listSchedules().get(0)
            .getActionNodeRef());

      service.saveSchedule(schedule2);
      assertEquals(2, service.listSchedules().size());
   }

   public void testLoadFromAction() throws Exception 
   {
      // Create schedule
      ScheduledPersistedAction schedule1 = service.createSchedule(testAction);
      assertNotNull(schedule1);
      service.saveSchedule(schedule1);

      // retrieve schedule for action which doesn't have schedule
      ScheduledPersistedAction retrieved = service.getSchedule(testAction2);
      assertNull(retrieved);

      // and from one which does
      retrieved = service.getSchedule(testAction);
      assertNotNull(retrieved);
      assertEquals(testAction.getNodeRef(), retrieved.getActionNodeRef());
   }

   /**
    * Tests that the startup registering works properly
    */
   public void testStartup() throws Exception
   {
       // Startup with none there, nothing happens
       assertEquals(
            0,
            scheduler.getJobNames(ScheduledPersistedActionServiceImpl.SCHEDULER_GROUP).length
       );
       assertEquals(
             0,
             service.listSchedules().size()
       );
       
       bootstrap.onBootstrap(null);
       
       assertEquals(
             0,
             scheduler.getJobNames(ScheduledPersistedActionServiceImpl.SCHEDULER_GROUP).length
       );
       assertEquals(
              0,
              service.listSchedules().size()
       );
        
        
       // Manually add a scheduled action
       // Does a bit of faffing to have it not in Quartz initially
       long future = System.currentTimeMillis() + 1234567;
       ScheduledPersistedAction schedule = service.createSchedule(testAction);
       schedule.setScheduleStart(new Date(future));
       service.saveSchedule(schedule);
        
       ((ScheduledPersistedActionServiceImpl)ctx.getBean("scheduledPersistedActionService"))
           .removeFromScheduler( (ScheduledPersistedActionImpl)schedule );
        
       assertEquals(
              0,
              scheduler.getJobNames(ScheduledPersistedActionServiceImpl.SCHEDULER_GROUP).length
       );
       assertEquals(
              1,
              service.listSchedules().size()
       );
         
       // Now do the bootstrap, and see it get registered
       bootstrap.onBootstrap(null);
       
       assertEquals(
             1,
             scheduler.getJobNames(ScheduledPersistedActionServiceImpl.SCHEDULER_GROUP).length
       );
       assertEquals(
             1,
             service.listSchedules().size()
       );
    }

   /**
    * Ensures that deletion works correctly
    */
   public void testDeletion() throws Exception 
   {
      // Delete does nothing if not persisted
      assertEquals(0, service.listSchedules().size());
      ScheduledPersistedAction schedule1 = service.createSchedule(testAction);
      assertEquals(0, service.listSchedules().size());

      service.deleteSchedule(schedule1);
      assertEquals(0, service.listSchedules().size());

      // Create and save two
      ScheduledPersistedAction schedule2 = service.createSchedule(testAction2);
      service.saveSchedule(schedule1);
      service.saveSchedule(schedule2);
      assertEquals(2, service.listSchedules().size());
      NodeRef schedule1NodeRef = ((ScheduledPersistedActionImpl) schedule1)
            .getPersistedAtNodeRef();
      NodeRef schedule2NodeRef = ((ScheduledPersistedActionImpl) schedule2)
            .getPersistedAtNodeRef();

      // Both should have the relationship
      assertEquals(1, nodeService.getTargetAssocs(schedule1NodeRef,
            RegexQNamePattern.MATCH_ALL).size());
      assertEquals(1, nodeService.getTargetAssocs(schedule2NodeRef,
            RegexQNamePattern.MATCH_ALL).size());
      assertEquals(1, nodeService.getSourceAssocs(testAction.getNodeRef(),
            RegexQNamePattern.MATCH_ALL).size());
      assertEquals(1, nodeService.getSourceAssocs(testAction2.getNodeRef(),
            RegexQNamePattern.MATCH_ALL).size());

      // Delete one - the correct one goes!
      service.deleteSchedule(schedule2);
      assertEquals(1, service.listSchedules().size());
      assertEquals(testAction.getNodeRef(), service.listSchedules().get(0)
            .getActionNodeRef());
      assertNotNull(serviceImpl.loadPersistentSchedule(schedule1NodeRef));
      assertNull(serviceImpl.loadPersistentSchedule(schedule2NodeRef));
      assertNotNull(service.getSchedule(testAction));
      assertNull(service.getSchedule(testAction2));

      // Ensure that the relationship went
      assertEquals(1, nodeService.getTargetAssocs(schedule1NodeRef,
            RegexQNamePattern.MATCH_ALL).size());
      assertEquals(1, nodeService.getSourceAssocs(testAction.getNodeRef(),
            RegexQNamePattern.MATCH_ALL).size());
      assertEquals(0, nodeService.getSourceAssocs(testAction2.getNodeRef(),
            RegexQNamePattern.MATCH_ALL).size());

      // Re-delete already deleted, no change
      service.deleteSchedule(schedule2);
      assertEquals(1, service.listSchedules().size());
      assertEquals(testAction.getNodeRef(), service.listSchedules().get(0)
            .getActionNodeRef());
      assertNotNull(serviceImpl.loadPersistentSchedule(schedule1NodeRef));
      assertNull(serviceImpl.loadPersistentSchedule(schedule2NodeRef));
      assertNotNull(service.getSchedule(testAction));
      assertNull(service.getSchedule(testAction2));

      // Delete the 2nd
      service.deleteSchedule(schedule1);
      assertEquals(0, service.listSchedules().size());
      assertNull(serviceImpl.loadPersistentSchedule(schedule1NodeRef));
      assertNull(serviceImpl.loadPersistentSchedule(schedule2NodeRef));
      assertNull(service.getSchedule(testAction));
      assertNull(service.getSchedule(testAction2));

      // Ensure that the relationship went
      assertEquals(0, nodeService.getSourceAssocs(testAction.getNodeRef(),
            RegexQNamePattern.MATCH_ALL).size());
      assertEquals(0, nodeService.getSourceAssocs(testAction2.getNodeRef(),
            RegexQNamePattern.MATCH_ALL).size());

      // Can add back in again after being deleted
      service.saveSchedule(schedule1);
      assertEquals(1, service.listSchedules().size());
      assertEquals(testAction.getNodeRef(), service.listSchedules().get(0)
            .getActionNodeRef());
      assertNotNull(service.getSchedule(testAction));
      assertNull(service.getSchedule(testAction2));

      // If we delete the action, then we have an orphaned schedule
      UserTransaction txn = transactionService.getUserTransaction();
      txn.begin();
      nodeService.deleteNode(testAction.getNodeRef());
      txn.commit();

      assertEquals(1, service.listSchedules().size());
      assertEquals(1, service.listSchedules().size());
      assertEquals(null, service.listSchedules().get(0).getAction());
      assertEquals(null, service.listSchedules().get(0).getActionNodeRef());
   }

   /**
    * Tests that things get properly injected onto the job bean
    */
   public void testJobBeanInjection() throws Exception 
   {
      // This test needs the scheduler running properly
      scheduler.start();

      // The job should run almost immediately
      Job job = new TestJob();
      JobDetail details = new JobDetail("ThisIsATest", null, job.getClass());
      Trigger now = new SimpleTrigger("TestTrigger", new Date(1));
      now.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);

      Scheduler scheduler = (Scheduler) ctx.getBean("schedulerFactory");
      scheduler.scheduleJob(details, now);

      // Allow it to run
      for (int i = 0; i < 20; i++) {
         if (!TestJob.ran)
            Thread.sleep(50);
      }

      // Ensure it ran, and it got a copy of the context
      assertEquals(true, TestJob.ran);
      assertEquals(true, TestJob.gotContext);
   }

   /**
    * Tests that things actually get run correctly. Each sub-test runs in its
    * own transaction
    */
   public void testExecution() throws Exception 
   {
      final SleepActionExecuter sleepActionExec = (SleepActionExecuter) ctx
            .getBean(SleepActionExecuter.NAME);
      sleepActionExec.resetTimesExecuted();
      sleepActionExec.setSleepMs(1);

      // This test needs the scheduler running properly
      scheduler.start();

      // Until the schedule is persisted, nothing will happen
      ScheduledPersistedAction schedule = service.createSchedule(testAction);
      assertEquals(
            0,
            scheduler.getJobNames(ScheduledPersistedActionServiceImpl.SCHEDULER_GROUP).length
      );

      transactionService.getRetryingTransactionHelper().doInTransaction(
            new RetryingTransactionCallback<Void>() {
               public Void execute() throws Throwable {

                  // A job due to start in 1 second, and run once
                  ScheduledPersistedAction schedule = service
                        .createSchedule(testAction);
                  schedule.setScheduleStart(new Date(
                        System.currentTimeMillis() + 1000));
                  assertNull(schedule.getScheduleInterval());
                  assertNull(schedule.getScheduleIntervalCount());
                  assertNull(schedule.getScheduleIntervalPeriod());
                  assertNull(schedule.getScheduleLastExecutedAt());

                  System.out.println("Job starts in 1 second, no repeat...");
                  service.saveSchedule(schedule);

                  return null;
               }
            }, false, true);

      // Check it went in
      assertEquals(
            1,
            scheduler
                  .getJobNames(ScheduledPersistedActionServiceImpl.SCHEDULER_GROUP).length);

      // Let it run
      Thread.sleep(2000);

      // Ensure it did properly run the once
      assertEquals(1, sleepActionExec.getTimesExecuted());

      // Should have removed itself now the schedule is over
      assertEquals(
            0,
            scheduler
                  .getJobNames(ScheduledPersistedActionServiceImpl.SCHEDULER_GROUP).length);

      transactionService.getRetryingTransactionHelper().doInTransaction(
            new RetryingTransactionCallback<Void>() {
               public Void execute() throws Throwable {

                  // Ensure it was tagged with when it ran
                  ScheduledPersistedAction schedule = service
                        .getSchedule(testAction);
                  assertEquals(
                        (double) System.currentTimeMillis(),
                        (double) schedule.getScheduleLastExecutedAt().getTime(),
                        2500); // Within 2.5 secs

                  // Zap it
                  service.deleteSchedule(schedule);
                  assertEquals(
                        0,
                        scheduler
                              .getJobNames(ScheduledPersistedActionServiceImpl.SCHEDULER_GROUP).length);

                  return null;
               }
            }, false, true);

      // ==========================

      transactionService.getRetryingTransactionHelper().doInTransaction(
            new RetryingTransactionCallback<Void>() {
               public Void execute() throws Throwable {

                  // A job that runs every 2 seconds, for the next 3.5 seconds
                  // (Should get to run twice, now and @2 secs)
                  ScheduledPersistedAction schedule = service
                        .createSchedule(testAction);
                  schedule.setScheduleStart(new Date(System.currentTimeMillis()-50));
                  ((ScheduledPersistedActionImpl) schedule)
                        .setScheduleEnd(new Date(
                              System.currentTimeMillis() + 3500));
                  schedule.setScheduleIntervalCount(2);
                  schedule.setScheduleIntervalPeriod(IntervalPeriod.Second);
                  assertEquals("2Second", schedule.getScheduleInterval());

                  // Reset count
                  sleepActionExec.resetTimesExecuted();
                  assertEquals(0, sleepActionExec.getTimesExecuted());

                  service.saveSchedule(schedule);
                  System.out.println(
                        "Job " + 
                        ((ScheduledPersistedActionImpl)schedule).getPersistedAtNodeRef() +
                        " starts now, repeats twice @ 2s"
                  );

                  return null;
               }
            }, false, true);
      transactionService.getRetryingTransactionHelper().doInTransaction(
            new RetryingTransactionCallback<Void>() {
               public Void execute() throws Throwable {
                  Thread.sleep(4250);

                  ScheduledPersistedAction schedule = service
                        .getSchedule(testAction);

                  // Ensure it did properly run two times
                  // (Depending on timing of tests, might actually slip in 3
                  // runs)
                  if (sleepActionExec.getTimesExecuted() == 3) {
                     assertEquals(3, sleepActionExec.getTimesExecuted());
                  } else {
                     assertEquals(2, sleepActionExec.getTimesExecuted());
                  }

                  // Zap it
                  service.deleteSchedule(schedule);
                  assertEquals(
                        0,
                        scheduler.getJobNames(ScheduledPersistedActionServiceImpl.SCHEDULER_GROUP).length
                  );

                  return null;
               }
            }, false, true);

      // ==========================

      transactionService.getRetryingTransactionHelper().doInTransaction(
            new RetryingTransactionCallback<Void>() {
               public Void execute() throws Throwable {

                  // A job that starts in 2 seconds time, and runs
                  // every second until we kill it
                  ScheduledPersistedAction schedule = service
                        .createSchedule(testAction);
                  schedule.setScheduleStart(new Date(
                        System.currentTimeMillis() + 2000));
                  schedule.setScheduleIntervalCount(1);
                  schedule.setScheduleIntervalPeriod(IntervalPeriod.Second);
                  assertEquals("1Second", schedule.getScheduleInterval());

                  // Reset count
                  sleepActionExec.resetTimesExecuted();
                  assertEquals(0, sleepActionExec.getTimesExecuted());

                  System.out.println("Job starts in 2s, repeats @ 1s");
                  service.saveSchedule(schedule);

                  return null;
               }
            }, false, true);
      transactionService.getRetryingTransactionHelper().doInTransaction(
            new RetryingTransactionCallback<Void>() {
               public Void execute() throws Throwable {

                  // Let it run a few times
                  Thread.sleep(5000);

                  // Zap it - should still be live
                  ScheduledPersistedAction schedule = service
                        .getSchedule(testAction);
                  assertEquals(
                        1,
                        scheduler
                              .getJobNames(ScheduledPersistedActionServiceImpl.SCHEDULER_GROUP).length);
                  service.deleteSchedule(schedule);
                  assertEquals(
                        0,
                        scheduler
                              .getJobNames(ScheduledPersistedActionServiceImpl.SCHEDULER_GROUP).length);

                  // Check it ran an appropriate number of times
                  assertEquals("Didn't run enough - "
                        + sleepActionExec.getTimesExecuted(), true,
                        sleepActionExec.getTimesExecuted() >= 3);
                  assertEquals("Ran too much - "
                        + sleepActionExec.getTimesExecuted(), true,
                        sleepActionExec.getTimesExecuted() < 5);

                  // Ensure it finished shutting down
                  Thread.sleep(500);

                  return null;
               }
            }, false, true);
   }

   /**
    * Tests that when we have more than one schedule defined and active, then
    * the correct things run at the correct times, and we never get confused
    */
   public void testMultipleExecutions() throws Exception 
   {
      final SleepActionExecuter sleepActionExec = (SleepActionExecuter) ctx
            .getBean(SleepActionExecuter.NAME);
      sleepActionExec.resetTimesExecuted();
      sleepActionExec.setSleepMs(1);

      transactionService.getRetryingTransactionHelper().doInTransaction(
            new RetryingTransactionCallback<Void>() {
               public Void execute() throws Throwable {

                  // Create one that starts running in 2 seconds, runs every 2
                  // seconds
                  // until 9 seconds are up (will run 4 times)
                  ScheduledPersistedActionImpl scheduleA = (ScheduledPersistedActionImpl) service
                        .createSchedule(testAction);
                  scheduleA.setScheduleStart(new Date(System
                        .currentTimeMillis() + 2000));
                  scheduleA.setScheduleEnd(new Date(
                        System.currentTimeMillis() + 9000));
                  scheduleA.setScheduleIntervalCount(2);
                  scheduleA.setScheduleIntervalPeriod(IntervalPeriod.Second);
                  service.saveSchedule(scheduleA);

                  // Create one that starts running now, every second until 9.5
                  // seconds
                  // are up (will run 9-10 times)
                  ScheduledPersistedActionImpl scheduleB = (ScheduledPersistedActionImpl) service
                        .createSchedule(testAction2);
                  scheduleB.setScheduleStart(new Date(System
                        .currentTimeMillis()));
                  scheduleB.setScheduleEnd(new Date(
                        System.currentTimeMillis() + 9500));
                  scheduleB.setScheduleIntervalCount(1);
                  scheduleB.setScheduleIntervalPeriod(IntervalPeriod.Second);
                  service.saveSchedule(scheduleB);

                  return null;
               }
            }, false, true);

      // Set them going
      scheduler.start();

      // Wait for 10 seconds for them to run and finish
      Thread.sleep(10 * 1000);

      // Check that they really did run properly
      transactionService.getRetryingTransactionHelper().doInTransaction(
            new RetryingTransactionCallback<Void>() {
               public Void execute() throws Throwable {

                  ScheduledPersistedAction scheduleA = service
                        .getSchedule(testAction);
                  ScheduledPersistedAction scheduleB = service
                        .getSchedule(testAction2);

                  // Both should have last run at some point in the last second
                  // or two
                  assertEquals((double) System.currentTimeMillis(),
                        (double) scheduleA.getScheduleLastExecutedAt()
                              .getTime(), 2500);
                  assertEquals((double) System.currentTimeMillis(),
                        (double) scheduleB.getScheduleLastExecutedAt()
                              .getTime(), 2500);

                  // A should have run ~4 times
                  // B should have run ~9 times
                  assertEquals("Didn't run enough - "
                        + sleepActionExec.getTimesExecuted(), true,
                        sleepActionExec.getTimesExecuted() >= 11);
                  assertEquals("Ran too much - "
                        + sleepActionExec.getTimesExecuted(), true,
                        sleepActionExec.getTimesExecuted() < 16);

                  return null;
               }
            }, false, true);
   }

   // ============================================================================

   /**
    * For unit testing only - not thread safe!
    */
   public static class TestJob implements Job, ApplicationContextAware 
   {
      private static boolean gotContext = false;
      private static boolean ran = false;

      public TestJob() 
      {
         gotContext = false;
         ran = false;
      }

      public void setApplicationContext(ApplicationContext applicationContext) throws BeansException 
      {
         gotContext = true;
      }

      public void execute(JobExecutionContext paramJobExecutionContext) throws JobExecutionException 
      {
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
