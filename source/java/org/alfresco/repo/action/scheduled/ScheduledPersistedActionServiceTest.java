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
}
