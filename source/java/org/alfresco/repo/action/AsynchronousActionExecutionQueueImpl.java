/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.action;

import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.alfresco.repo.rule.RuleServiceImpl;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionServiceException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;

/**
 * The asynchronous action execution queue implementation
 * 
 * @author Roy Wetherall
 */
public class AsynchronousActionExecutionQueueImpl extends ThreadPoolExecutor implements
        AsynchronousActionExecutionQueue
{
    /**
     * Default pool values
     */
    private static final int CORE_POOL_SIZE = 2;

    private static final int MAX_POOL_SIZE = 5;

    private static final long KEEP_ALIVE = 30;

    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    private static final int MAX_QUEUE_SIZE = 500;

    /**
     * The transaction service
     */
    private TransactionService transactionService;

    /**
     * The authentication component
     */
    private AuthenticationComponent authenticationComponent;

    /**
     * Default constructor
     */
    public AsynchronousActionExecutionQueueImpl()
    {
        super(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE, TIME_UNIT, new ArrayBlockingQueue<Runnable>(MAX_QUEUE_SIZE,
                true));
    }

    /**
     * Set the transaction service
     * 
     * @param transactionService
     *            the transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * Set the authentication component
     * 
     * @param authenticationComponent
     *            the authentication component
     */
    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    /**
     * @see org.alfresco.repo.action.AsynchronousActionExecutionQueue#executeAction(org.alfresco.service.cmr.repository.NodeRef,
     *      org.alfresco.service.cmr.action.Action, boolean)
     */
    public void executeAction(RuntimeActionService actionService, Action action, NodeRef actionedUponNodeRef,
            boolean checkConditions, Set<String> actionChain)
    {
        executeAction(actionService, action, actionedUponNodeRef, checkConditions, actionChain, null);
    }

    /**
     * @see org.alfresco.repo.action.AsynchronousActionExecutionQueue#executeAction(org.alfresco.service.cmr.repository.NodeRef,
     *      org.alfresco.service.cmr.action.Action, boolean,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    @SuppressWarnings("unchecked")
	public void executeAction(RuntimeActionService actionService, Action action, NodeRef actionedUponNodeRef,
            boolean checkConditions, Set<String> actionChain, NodeRef actionExecutionHistoryNodeRef)
    {
    	Set<RuleServiceImpl.ExecutedRuleData> executedRules =
            (Set<RuleServiceImpl.ExecutedRuleData>) AlfrescoTransactionSupport.getResource("RuleServiceImpl.ExecutedRules");    	
        execute(new ActionExecutionWrapper(actionService, transactionService, authenticationComponent, action,
                actionedUponNodeRef, checkConditions, actionExecutionHistoryNodeRef, actionChain, executedRules));
    }

    /**
     * @see java.util.concurrent.ThreadPoolExecutor#beforeExecute(java.lang.Thread,
     *      java.lang.Runnable)
     */
    @Override
    protected void beforeExecute(Thread thread, Runnable runnable)
    {
        super.beforeExecute(thread, runnable);
    }

    /**
     * @see java.util.concurrent.ThreadPoolExecutor#afterExecute(java.lang.Runnable,
     *      java.lang.Throwable)
     */
    @Override
    protected void afterExecute(Runnable thread, Throwable runnable)
    {
        super.afterExecute(thread, runnable);
    }

    /**
     * Runnable class to wrap the execution of the action.
     */
    private class ActionExecutionWrapper implements Runnable
    {
        /**
         * Runtime action service
         */
        private RuntimeActionService actionService;

        /**
         * The transaction service
         */
        private TransactionService transactionService;

        /**
         * The authentication component
         */
        private AuthenticationComponent authenticationComponent;

        /**
         * The action
         */
        private Action action;

        /**
         * The actioned upon node reference
         */
        private NodeRef actionedUponNodeRef;

        /**
         * The check conditions value
         */
        private boolean checkConditions;

        /**
         * The action execution history node reference
         */
        private NodeRef actionExecutionHistoryNodeRef;

        /**
         * The action chain
         */
        private Set<String> actionChain;
        
        /**
         * List of executed list, helps to prevent loop scenarios with async rules
         */
        private Set<RuleServiceImpl.ExecutedRuleData> executedRules;

        /**
         * Constructor
         * 
         * @param actionService
         * @param transactionService
         * @param authenticationComponent
         * @param action
         * @param actionedUponNodeRef
         * @param checkConditions
         * @param actionExecutionHistoryNodeRef
         */
        public ActionExecutionWrapper(RuntimeActionService actionService, TransactionService transactionService,
                AuthenticationComponent authenticationComponent, Action action, NodeRef actionedUponNodeRef,
                boolean checkConditions, NodeRef actionExecutionHistoryNodeRef, Set<String> actionChain, Set<RuleServiceImpl.ExecutedRuleData> executedRules)
        {
            this.actionService = actionService;
            this.transactionService = transactionService;
            this.authenticationComponent = authenticationComponent;
            this.actionedUponNodeRef = actionedUponNodeRef;
            this.action = action;
            this.checkConditions = checkConditions;
            this.actionExecutionHistoryNodeRef = actionExecutionHistoryNodeRef;
            this.actionChain = actionChain;
            this.executedRules = executedRules;
        }

        /**
         * Get the action
         * 
         * @return the action
         */
        public Action getAction()
        {
            return this.action;
        }

        /**
         * Get the actioned upon node reference
         * 
         * @return the actioned upon node reference
         */
        public NodeRef getActionedUponNodeRef()
        {
            return this.actionedUponNodeRef;
        }

        /**
         * Get the check conditions value
         * 
         * @return the check conditions value
         */
        public boolean getCheckCondtions()
        {
            return this.checkConditions;
        }

        /**
         * Get the action execution history node reference
         * 
         * @return the action execution history node reference
         */
        public NodeRef getActionExecutionHistoryNodeRef()
        {
            return this.actionExecutionHistoryNodeRef;
        }

        /**
         * Get the action chain
         * 
         * @return the action chain
         */
        public Set<String> getActionChain()
        {
            return actionChain;
        }

        /**
         * Executes the action via the action runtime service
         * 
         * @see java.lang.Runnable#run()
         */
        public void run()
        {
            try
            {
                // Get the run as user name
                final String userName = ((ActionImpl)ActionExecutionWrapper.this.action).getRunAsUser();
                if (userName == null)
                {
                    throw new ActionServiceException("Cannot execute action asynchronously since run as user is 'null'");              
                }
                
                ActionExecutionWrapper.this.authenticationComponent.setCurrentUser(userName);
                
                try
                {
                    TransactionUtil.executeInNonPropagatingUserTransaction(this.transactionService,
                            new TransactionUtil.TransactionWork<Object>()
                            {
                                public Object doWork()
                                {   
                                	if (ActionExecutionWrapper.this.executedRules != null)
                                	{
                                		AlfrescoTransactionSupport.bindResource("RuleServiceImpl.ExecutedRules", ActionExecutionWrapper.this.executedRules);
                                	}
                                	
                                    ActionExecutionWrapper.this.actionService.executeActionImpl(
                                        ActionExecutionWrapper.this.action,
                                        ActionExecutionWrapper.this.actionedUponNodeRef,
                                        ActionExecutionWrapper.this.checkConditions, true,
                                        ActionExecutionWrapper.this.actionChain);
    
                                    return null;
                                }
                            });
                }
                finally
                {
                    ActionExecutionWrapper.this.authenticationComponent.clearCurrentSecurityContext();
                }
            }
            catch (Throwable exception)
            {
                exception.printStackTrace();
            }
        }
    }
}
