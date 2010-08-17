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

import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.scheduled.ScheduledPersistedAction;
import org.alfresco.service.cmr.action.scheduled.ScheduledPersistedActionService;
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

   private TransactionService transactionService;
   private ActionService actionService;
   private NodeService nodeService;
   private Repository repositoryHelper;
   
   private NodeRef scheduledRoot;
   
   @Override
   protected void setUp() throws Exception
   {
      actionService = (ActionService) ctx.getBean("actionService");
      nodeService = (NodeService) ctx.getBean("nodeService");
      repositoryHelper = (Repository) ctx.getBean("repositoryHelper");
      transactionService = (TransactionService) ctx.getBean("transactionService");
      
      
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
      // A job due to start in 2 seconds
      // TODO
      
      // A job that runs every 2 seconds
      // TODO
      
      // A job that starts in 2 seconds time, and runs
      //  every second
      // TODO
   }
   
   // ============================================================================

   /**
    * An action that updates a static count, so we
    *  can tell how often it is run.
    * We have one of these persisted in the repository during
    *  the tests
    * TODO
    */
   
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
}
