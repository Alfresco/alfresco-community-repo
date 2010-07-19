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
package org.alfresco.repo.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.alfresco.repo.cache.EhCacheAdapter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionStatus;
import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.action.CancellableAction;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Action execution tracking service implementation
 * 
 * @author Nick Burch
 */
public class ActionTrackingServiceImpl implements ActionTrackingService 
{
   /**
    * The logger
    */
   private static Log logger = LogFactory.getLog(ActionTrackingServiceImpl.class);

   // TODO - Fix types
   private EhCacheAdapter<String, Void> executingActionsCache;
   
   private TransactionService transactionService;
   private RuntimeActionService runtimeActionService;
   
   /**
    * Doesn't need to be cluster unique, is just used
    *  to try to reduce the chance of clashes in the 
    *  quickest and easiest way.
    */
   private short nextExecutionId = 1;
   
   /**
    * Set the transaction service
    * 
    * @param transactionService the transaction service
    */
   public void setTransactionService(TransactionService transactionService)
   {
       this.transactionService = transactionService;
   }

   /**
    * Set the runtime action service
    * 
    * @param runtimeActionService the runtime action service
    */
   public void setRuntimeActionService(RuntimeActionService runtimeActionService)
   {
       this.runtimeActionService = runtimeActionService;
   }
   
   /**
    * Sets the cache used to store details of
    *  currently executing actions, cluster wide.
    * TODO Fix types
    */
   public void setExecutingActionsCache(EhCacheAdapter<String, Void> executingActionsCache)
   {
       this.executingActionsCache = executingActionsCache;
   }

   
   
   public void recordActionPending(Action action) 
   {
      ((ActionImpl)action).setExecutionStatus(ActionStatus.Pending);
   }
   
   public void recordActionComplete(Action action) 
   {
      // Mark it as having worked
      ((ActionImpl)action).setExecutionEndDate(new Date());
      ((ActionImpl)action).setExecutionStatus(ActionStatus.Completed);
      ((ActionImpl)action).setExecutionFailureMessage(null);
      if(action.getNodeRef() != null)
      {
         runtimeActionService.saveActionImpl(action.getNodeRef(), action);
      }
   }

   public void recordActionExecuting(Action action) 
   {
      // Mark the action as starting
      ((ActionImpl)action).setExecutionStartDate(new Date());
      ((ActionImpl)action).setExecutionStatus(ActionStatus.Running);
   }

   /**
    * Schedule the recording of the action failure to occur
    *  in another transaction
    */
   public void recordActionFailure(Action action, Throwable exception)
   {
      if (logger.isDebugEnabled() == true)
      {
         logger.debug("Will shortly record failure of action " + action + " due to " + exception.getMessage());
      }
      
      ((ActionImpl)action).setExecutionEndDate(new Date());
      ((ActionImpl)action).setExecutionStatus(ActionStatus.Failed);
      ((ActionImpl)action).setExecutionFailureMessage(exception.getMessage());
      
      if(action.getNodeRef() != null)
      {
         // Take a local copy of the details
         // (That way, if someone has a reference to the
         //  action and plays with it, we still save the
         //  correct information)
         final String actionId = action.getId();
         final Date startedAt = action.getExecutionStartDate();
         final Date endedAt = action.getExecutionEndDate();
         final String message = action.getExecutionFailureMessage();
         final NodeRef actionNode = action.getNodeRef();
         
         // Have the details updated on the action as soon
         //  as the transaction has finished rolling back
         AlfrescoTransactionSupport.bindListener(
            new TransactionListenerAdapter() {
               public void afterRollback()
               {
                  transactionService.getRetryingTransactionHelper().doInTransaction(
                      new RetryingTransactionCallback<Object>()
                      {
                         public Object execute() throws Throwable
                         {
                            // Update the action as the system user
                            return AuthenticationUtil.runAs(new RunAsWork<Action>() {
                               public Action doWork() throws Exception
                               {
                                  // Grab the latest version of the action
                                  ActionImpl action = (ActionImpl)
                                     runtimeActionService.createAction(actionNode);
                                  
                                  // Update it
                                  action.setExecutionStartDate(startedAt);
                                  action.setExecutionEndDate(endedAt);
                                  action.setExecutionStatus(ActionStatus.Failed);
                                  action.setExecutionFailureMessage(message);
                                  runtimeActionService.saveActionImpl(actionNode, action);
                                  
                                  if (logger.isDebugEnabled() == true)
                                  {
                                     logger.debug("Recorded failure of action " + actionId + ", node " + actionNode + " due to " + message);
                                  }
                                  
                                  // All done
                                  return action;
                               }
                            }, AuthenticationUtil.SYSTEM_USER_NAME);
                         }
                      }, false, true
                  );
               }
            }
         );
      }
   }

   public boolean isCancellationRequested(CancellableAction action) 
   {
      // If the action isn't in the cache, but is of
      //  status executing, then put it back into the
      //  cache and warn
      // (Probably means the cache is too small)
      
      // Retrieve from the cache, and see if cancellation
      //  has been requested
      
      // TODO
      return false;
   }

   public void requestActionCancellation(CancellableAction action) 
   {
      // See if the action is in the cache
      // If it isn't, nothing to do
      // If it is, update the cancelled flag on it
      // TODO
   }
   
   public List<Void> getAllExecutingActions() {
      Collection<String> actions = executingActionsCache.getKeys();
      // TODO fix types
      List<Void> details = new ArrayList<Void>(actions.size());
      for(String key : actions) {
         //details.add( buildExecutionSummary(key) );
      }
      return details;
   }

   public List<Void> getExecutingActions(Action action) {
      Collection<String> actions = executingActionsCache.getKeys();
      // TODO fix types
      List<Void> details = new ArrayList<Void>();
      String match = action.getActionDefinitionName() + "-" + action.getId();
      for(String key : actions) {
         if(key.startsWith(match)) {
            //details.add( buildExecutionSummary(key) );
         }
      }
      return details;
   }

   public List<Void> getExecutingActions(String type) {
      Collection<String> actions = executingActionsCache.getKeys();
      // TODO fix types
      List<Void> details = new ArrayList<Void>();
      for(String key : actions) {
         if(key.startsWith(type)) {
            //details.add( buildExecutionSummary(key) );
         }
      }
      return details;
   }

   public void getExecutionDetails(Void executionSummary) {
      // TODO Auto-generated method stub
      
   }

   public void requestActionCancellation(Void executionSummary) {
      // TODO Auto-generated method stub
      
   }

   /**
    * Generates the cache key for the specified action.
    */
   protected String generateCacheKey(Action action)
   {
      // TODO
      return null;
   }

   /**
    * Turns a cache key back into its constituent
    *  parts, for easier access.
    * TODO Fix types
    */
   protected void buildExecutionSummary(String key)
   {
      
   }
   
}
