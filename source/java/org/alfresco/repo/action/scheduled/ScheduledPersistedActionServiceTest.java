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
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Unit tests for the {@link ScheduledPersistedActionService}
 */
public class ScheduledPersistedActionServiceTest extends TestCase
{
    private static ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) ApplicationContextHelper
            .getApplicationContext();

    private ScheduledPersistedActionService service;
    private ScheduledPersistedActionServiceImpl serviceImpl;
    private Scheduler scheduler;

    private TransactionService transactionService;
    private RuntimeActionService runtimeActionService;
    private ActionService actionService;
    private NodeService nodeService;

    private Action testAction;
    private Action testAction2;

    @Override
    protected void setUp() throws Exception
    {
        actionService = (ActionService) ctx.getBean("actionService");
        nodeService = (NodeService) ctx.getBean("nodeService");
        transactionService = (TransactionService) ctx.getBean("transactionService");
        runtimeActionService = (RuntimeActionService) ctx.getBean("actionService");
        service = (ScheduledPersistedActionService) ctx.getBean("ScheduledPersistedActionService");
        serviceImpl = (ScheduledPersistedActionServiceImpl) ctx.getBean("scheduledPersistedActionService");
        scheduler = (Scheduler) ctx.getBean("schedulerFactory");

        // Set the current security context as admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();

        // Register the test executor, if needed
        SleepActionExecuter.registerIfNeeded(ctx);

        // Zap all test schedules
//        List<ScheduledPersistedAction> schedules = service.listSchedules();
//        for (ScheduledPersistedAction schedule : schedules)
//        {
//            service.deleteSchedule(schedule);
//        }

        // Persist an action that uses the test executor
        testAction = new TestAction(actionService.createAction(SleepActionExecuter.NAME));
        runtimeActionService.createActionNodeRef(
                //
                testAction, ScheduledPersistedActionServiceImpl.SCHEDULED_ACTION_ROOT_NODE_REF,
                ContentModel.ASSOC_CONTAINS, QName.createQName("TestAction"));

        testAction2 = new TestAction(actionService.createAction(SleepActionExecuter.NAME));
        runtimeActionService.createActionNodeRef(
                //
                testAction2, ScheduledPersistedActionServiceImpl.SCHEDULED_ACTION_ROOT_NODE_REF,
                ContentModel.ASSOC_CONTAINS, QName.createQName("TestAction2"));

        // Finish setup
        txn.commit();
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
    }

    /**
     * Test that the {@link ScheduledPersistedAction} implementation behaves
     * properly
     */
    public void testPersistedActionImpl() throws Exception
    {
        // TODO
    }

    /**
     * Tests that the to-trigger stuff works properly
     */

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
        schedule.setScheduleIntervalPeriod(ScheduledPersistedAction.IntervalPeriod.Day);
        assertEquals(ScheduledPersistedAction.IntervalPeriod.Day, schedule.getScheduleIntervalPeriod());
    }

    public void testCreateSaveLoad() throws Exception
    {
        // create and save schedule
        ScheduledPersistedAction schedule = service.createSchedule(testAction);
        assertNotNull(schedule);
        Date now = new Date();
        schedule.setScheduleStart(now);
        schedule.setScheduleIntervalCount(2);
        schedule.setScheduleIntervalPeriod(ScheduledPersistedAction.IntervalPeriod.Day);
        service.saveSchedule(schedule);

        // Load it again, should have the same details still
        ScheduledPersistedAction retrieved = serviceImpl.loadPersistentSchedule(((ScheduledPersistedActionImpl)schedule).getPersistedAtNodeRef());
        assertNotNull(retrieved);
        assertEquals(testAction.getNodeRef(), retrieved.getAction().getNodeRef());
        assertEquals(now, retrieved.getScheduleStart());
        assertEquals(new Integer(2), retrieved.getScheduleIntervalCount());
        assertEquals(ScheduledPersistedAction.IntervalPeriod.Day, retrieved.getScheduleIntervalPeriod());

        // Load a 2nd copy, won't be any changes
        ScheduledPersistedAction second = serviceImpl.loadPersistentSchedule(((ScheduledPersistedActionImpl)schedule).getPersistedAtNodeRef());
        assertNotNull(second);
        assertEquals(testAction.getNodeRef(), second.getAction().getNodeRef());
        assertEquals(now, second.getScheduleStart());
        assertEquals(new Integer(2), second.getScheduleIntervalCount());
        assertEquals(ScheduledPersistedAction.IntervalPeriod.Day, second.getScheduleIntervalPeriod());
    }
    
    /**
     * Ensures that we can create, save, edit, save
     *  load, edit, save, load etc, all without
     *  problems, and without creating duplicates
     */
    public void testEditing() throws Exception
    {
        // create and save schedule
        ScheduledPersistedAction schedule = service.createSchedule(testAction);
        assertNotNull(schedule);
        Date now = new Date();
        schedule.setScheduleStart(now);
        schedule.setScheduleIntervalCount(2);
        schedule.setScheduleIntervalPeriod(ScheduledPersistedAction.IntervalPeriod.Day);
        service.saveSchedule(schedule);

        // Load and check it hasn't changed
        ScheduledPersistedAction retrieved = serviceImpl.loadPersistentSchedule(((ScheduledPersistedActionImpl)schedule).getPersistedAtNodeRef());
        assertNotNull(retrieved);
        assertEquals(testAction.getNodeRef(), retrieved.getAction().getNodeRef());
        assertEquals(now, retrieved.getScheduleStart());
        assertEquals(new Integer(2), retrieved.getScheduleIntervalCount());
        assertEquals(ScheduledPersistedAction.IntervalPeriod.Day, retrieved.getScheduleIntervalPeriod());

        // Save and re-load without changes
        service.saveSchedule(schedule);
        retrieved = serviceImpl.loadPersistentSchedule(((ScheduledPersistedActionImpl)schedule).getPersistedAtNodeRef());
        assertNotNull(retrieved);
        assertEquals(testAction.getNodeRef(), retrieved.getAction().getNodeRef());
        assertEquals(now, retrieved.getScheduleStart());
        assertEquals(new Integer(2), retrieved.getScheduleIntervalCount());
        assertEquals(ScheduledPersistedAction.IntervalPeriod.Day, retrieved.getScheduleIntervalPeriod());

        // Make some small changes
        retrieved.setScheduleIntervalCount(3);
        service.saveSchedule(retrieved);
        retrieved = serviceImpl.loadPersistentSchedule(((ScheduledPersistedActionImpl)schedule).getPersistedAtNodeRef());
        assertNotNull(retrieved);
        assertEquals(testAction.getNodeRef(), retrieved.getAction().getNodeRef());
        assertEquals(now, retrieved.getScheduleStart());
        assertEquals(new Integer(3), retrieved.getScheduleIntervalCount());
        assertEquals(ScheduledPersistedAction.IntervalPeriod.Day, retrieved.getScheduleIntervalPeriod());
        
        // And some more changes
        retrieved.setScheduleIntervalPeriod(ScheduledPersistedAction.IntervalPeriod.Month);
        now = new Date(); 
        retrieved.setScheduleStart(now);
        service.saveSchedule(retrieved);
        retrieved = serviceImpl.loadPersistentSchedule(((ScheduledPersistedActionImpl)schedule).getPersistedAtNodeRef());
        assertNotNull(retrieved);
        assertEquals(testAction.getNodeRef(), retrieved.getAction().getNodeRef());
        assertEquals(now, retrieved.getScheduleStart());
        assertEquals(new Integer(3), retrieved.getScheduleIntervalCount());
        assertEquals(ScheduledPersistedAction.IntervalPeriod.Month, retrieved.getScheduleIntervalPeriod());
        
        // TODO: associated action
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
       assertEquals(testAction.getNodeRef(), service.listSchedules().get(0).getActionNodeRef());
       
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
       
       retrieved = service.getSchedule(testAction);
       assertNotNull(retrieved);
       assertEquals(testAction.getNodeRef(), retrieved.getActionNodeRef());
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
       NodeRef schedule1NodeRef = ((ScheduledPersistedActionImpl)schedule1).getPersistedAtNodeRef();
       NodeRef schedule2NodeRef = ((ScheduledPersistedActionImpl)schedule2).getPersistedAtNodeRef();

       // Delete one - the correct one goes!
       service.deleteSchedule(schedule2);
       assertEquals(1, service.listSchedules().size());
       assertEquals(testAction.getNodeRef(), service.listSchedules().get(0).getActionNodeRef());
       assertNotNull(serviceImpl.loadPersistentSchedule(schedule1NodeRef));
       assertNull(serviceImpl.loadPersistentSchedule(schedule2NodeRef));

       // Re-delete already deleted, no change
       service.deleteSchedule(schedule2);
       assertEquals(1, service.listSchedules().size());
       assertEquals(testAction.getNodeRef(), service.listSchedules().get(0).getActionNodeRef());
       assertNotNull(serviceImpl.loadPersistentSchedule(schedule1NodeRef));
       assertNull(serviceImpl.loadPersistentSchedule(schedule2NodeRef));

       // Delete the 2nd
       service.deleteSchedule(schedule1);
       assertEquals(0, service.listSchedules().size());
       assertNull(serviceImpl.loadPersistentSchedule(schedule1NodeRef));
       assertNull(serviceImpl.loadPersistentSchedule(schedule2NodeRef));
       
       // Can add back in again after being deleted
       service.saveSchedule(schedule1);
       assertEquals(1, service.listSchedules().size());
       assertEquals(testAction.getNodeRef(), service.listSchedules().get(0).getActionNodeRef());
    }
    
    /**
     * Tests that things get properly injected onto the job bean
     */
    public void testJobBeanInjection() throws Exception
    {
        // The job should run almost immediately
        Job job = new TestJob();
        JobDetail details = new JobDetail("ThisIsATest", null, job.getClass());
        Trigger now = new SimpleTrigger("TestTrigger", new Date(1));
        now.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);

        Scheduler scheduler = (Scheduler) ctx.getBean("schedulerFactory");
        scheduler.scheduleJob(details, now);

        // Allow it to run
        for (int i = 0; i < 20; i++)
        {
            if (!TestJob.ran)
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
        final SleepActionExecuter sleepActionExec = (SleepActionExecuter) ctx.getBean(SleepActionExecuter.NAME);
        sleepActionExec.resetTimesExecuted();
        sleepActionExec.setSleepMs(1);

        ScheduledPersistedAction schedule;

        // Until the schedule is persisted, nothing will happen
        schedule = service.createSchedule(testAction);
        assertEquals(0, scheduler.getJobNames(ScheduledPersistedActionServiceImpl.SCHEDULER_GROUP).length);

        // A job due to start in 1 second, and run once
        schedule = service.createSchedule(testAction);
        schedule.setScheduleStart(new Date(System.currentTimeMillis() + 1000));
        assertNull(schedule.getScheduleInterval());
        assertNull(schedule.getScheduleIntervalCount());
        assertNull(schedule.getScheduleIntervalPeriod());

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
        schedule.setScheduleStart(new Date(0));
        ((ScheduledPersistedActionImpl) schedule).setScheduleEnd(new Date(System.currentTimeMillis() + 3500));
        schedule.setScheduleIntervalCount(2);
        schedule.setScheduleIntervalPeriod(IntervalPeriod.Second);
        assertEquals("2Second", schedule.getScheduleInterval());

        // Reset count
        sleepActionExec.resetTimesExecuted();
        assertEquals(0, sleepActionExec.getTimesExecuted());

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
        // every second until we kill it
        schedule = service.createSchedule(testAction);
        schedule.setScheduleStart(new Date(System.currentTimeMillis() + 2000));
        schedule.setScheduleIntervalCount(1);
        schedule.setScheduleIntervalPeriod(IntervalPeriod.Second);
        assertEquals("1Second", schedule.getScheduleInterval());

        // Reset count
        sleepActionExec.resetTimesExecuted();
        assertEquals(0, sleepActionExec.getTimesExecuted());

        System.out.println("Job starts in 2s, repeats @ 1s");
        service.saveSchedule(schedule);

        // Let it run a few times
        Thread.sleep(5000);

        // Zap it - should still be live
        assertEquals(1, scheduler.getJobNames(ScheduledPersistedActionServiceImpl.SCHEDULER_GROUP).length);
        service.deleteSchedule(schedule);
        assertEquals(0, scheduler.getJobNames(ScheduledPersistedActionServiceImpl.SCHEDULER_GROUP).length);

        // Check it ran an appropriate number of times
        assertEquals("Didn't run enough - " + sleepActionExec.getTimesExecuted(), true, sleepActionExec
                .getTimesExecuted() >= 3);
        assertEquals("Ran too much - " + sleepActionExec.getTimesExecuted(), true,
                sleepActionExec.getTimesExecuted() < 5);

        // Ensure it finished shutting down
        Thread.sleep(500);
    }

    /**
     * Tests that when we have more than one schedule defined and active, then
     * the correct things run at the correct times, and we never get confused
     */
    public void DISABLEDtestMultipleExecutions() throws Exception
    {
        // Create one that starts running in 2 seconds, runs every 2 seconds
        // until 9 seconds are up (will run 4 times)

        // Create one that starts running now, every second until 9.5 seconds
        // are up (will run 9-10 times)

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
