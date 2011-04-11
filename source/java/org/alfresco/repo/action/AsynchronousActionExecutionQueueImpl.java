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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.error.StackTraceUtil;
import org.alfresco.repo.action.AsynchronousActionExecutionQueuePolicies.OnAsyncActionExecute;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.rule.RuleServiceImpl;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionServiceException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The asynchronous action execution queue implementation
 * 
 * @author Roy Wetherall
 */
public class AsynchronousActionExecutionQueueImpl implements AsynchronousActionExecutionQueue
{
    private static Log logger = LogFactory.getLog(AsynchronousActionExecutionQueueImpl.class);
    
    /** Services */
    private ThreadPoolExecutor threadPoolExecutor;
    private TransactionService transactionService;
    private PolicyComponent policyComponent;
    private Map<String, AbstractAsynchronousActionFilter>
            actionFilters = new ConcurrentHashMap<String, AbstractAsynchronousActionFilter>();

	/**
	 * We keep a record of ongoing asynchronous actions (this includes those being executed and
	 * those that are in the queue).
	 * This needs to be thread-safe - hence the Vector.
	 */
    List<OngoingAsyncAction> ongoingActions = new Vector<OngoingAsyncAction>();
    
    // Policy delegates
    private ClassPolicyDelegate<OnAsyncActionExecute> onAsyncActionExecuteDelegate; 

    /**
     * Default constructor
     */
    public AsynchronousActionExecutionQueueImpl()
    {
    }
    
    /**
     * Init method.  Registers the policies.
     */
    public void init()
    {
        // Register the policies
        onAsyncActionExecuteDelegate = policyComponent.registerClassPolicy(OnAsyncActionExecute.class);
    }

    /**
     * Set the thread pool, which may be shared with other components, that will be used
     * to run the actions.
     * 
     * @param threadPoolExecutor            the thread pool
     */
    public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor)
    {
        this.threadPoolExecutor = threadPoolExecutor;
    }

    /**
     * Set the transaction service
     * 
     * @param transactionService            the transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * @deprecated Not used since 3.4
     */
    public void setAuthenticationContext(AuthenticationContext authenticationContext)
    {
        logger.warn("Property 'authenticationContext' is no longer required.");
    }

    /**
     * Set the policy component
     * 
     * @param policyComponent   policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    private void invokeOnAsyncActionExecutePolicy(Action action, NodeRef actionedUponNodeRef)
    {
        // Execute the policy, passing it all details, firing as a general action case
        AsynchronousActionExecutionQueuePolicies.OnAsyncActionExecute policy = 
           onAsyncActionExecuteDelegate.get(actionedUponNodeRef, ActionModel.TYPE_ACTION);
        policy.onAsyncActionExecute(action, actionedUponNodeRef);
    }
    
    /**
     * This method registers an action filter, which can be used to prevent unwanted or unnecessary
     * asynchronous actions from being scheduled for execution.
     * 
     * @param filter the filter implementation.
     */
    public void registerActionFilter(AbstractAsynchronousActionFilter filter)
    {
    	String filterName = filter.getName();
    	
    	if (logger.isDebugEnabled())
    	{
    		StringBuilder msg = new StringBuilder();
    		msg.append("Registered asynchronous action filter ")
    		    .append(filter.getName()).append(" for action ")
    		    .append(filter.getActionDefinitionName());
    		logger.debug(msg.toString());
    	}
    	
    	AbstractAsynchronousActionFilter existingFilter = actionFilters.get(filterName);
		if (logger.isDebugEnabled() && existingFilter != null)
    	{
    		StringBuilder msg = new StringBuilder();
			msg.append("This replaces previous filter ")
    		    .append(existingFilter.getName());
    		logger.debug(msg.toString());
    	}
		
    	this.actionFilters.put(filter.getName(), filter);
    }
    
    /**
     * {@inheritDoc}
     */
    public void executeAction(RuntimeActionService actionService, Action action, NodeRef actionedUponNodeRef,
            boolean checkConditions, Set<String> actionChain)
    {
        executeAction(actionService, action, actionedUponNodeRef, checkConditions, actionChain, null);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
	public void executeAction(RuntimeActionService actionService, Action action, NodeRef actionedUponNodeRef,
            boolean checkConditions, Set<String> actionChain, NodeRef actionExecutionHistoryNodeRef)
    {
    	if (logger.isDebugEnabled())
    	{
    	    StringBuilder msg = new StringBuilder();
    	    msg.append("Received request to execute async action ").append(action.getActionDefinitionName())
    	        .append(" on ").append(actionedUponNodeRef);
    	    logger.debug(msg.toString());

    	    msg = new StringBuilder();
    	    msg.append("ThreadPool's active count = ").append(this.threadPoolExecutor.getActiveCount());
    	    logger.debug(msg.toString());

    	    msg = new StringBuilder();
    	    msg.append("ThreadPool's queue size = ").append(this.threadPoolExecutor.getQueue().size());
    	    logger.debug(msg.toString());
    	}

    	Set<RuleServiceImpl.ExecutedRuleData> executedRules =
            (Set<RuleServiceImpl.ExecutedRuleData>) AlfrescoTransactionSupport.getResource("RuleServiceImpl.ExecutedRules");
        Runnable runnable = new ActionExecutionWrapper(
                actionService,
                action,
                actionedUponNodeRef,
                checkConditions,
                actionChain,
                executedRules);
        
        // Consider whether this action should be filtered out by one of the registered filters.
        boolean newActionShouldBeFilteredOut = false;
        OngoingAsyncAction nodeBeingNewlyActioned = new OngoingAsyncAction(actionedUponNodeRef, action);
        
        for (Entry<String, AbstractAsynchronousActionFilter> entry : actionFilters.entrySet())
        {
        	AbstractAsynchronousActionFilter comparator = entry.getValue();
        	String actionDefinitionName = comparator.getActionDefinitionName();
        	
        	if (actionDefinitionName.equals(action.getActionDefinitionName()) == false)
        	{
        		// We're only interested in registered actions with the same name as this one.
        		continue;
        	}
        	else
        	{
        		// Now we've found a registered action that matches the current one.
        		// So we'll go through the actions that are ongoing and consider them for matches with this one.
        		// Need to synchronize to prevent changes to ongoingActions whilst iterating. Assume that ongoingActions
        		// is not going to be too big and the loop will execute quite quickly, so that the synchronization 
        		// will not impact concurrency too much.
        		synchronized(this.ongoingActions)
        		{
	        		for (OngoingAsyncAction ongoingAction : this.ongoingActions)
	        		{
	        			if (comparator.compare(ongoingAction, nodeBeingNewlyActioned) == 0)
	        			{
	        				newActionShouldBeFilteredOut = true;
	        				break;
	        			}
	        		}
        		}
        	}
        }
        if (newActionShouldBeFilteredOut)
        {
        	if (logger.isDebugEnabled())
        	{
        		StringBuilder msg = new StringBuilder();
        		msg.append("Dropping action ").append(action).append(" as equivalent is ongoing.");
        		logger.debug(msg.toString());
        	}
        	return;
        }
        else
        {
        	if (logger.isDebugEnabled())
        	{
        		StringBuilder msg = new StringBuilder();
        		msg.append("Executing action ").append(action);
        		logger.debug(msg.toString());
        	}
        	
        	// Queue it and do it.
        	ongoingActions.add(nodeBeingNewlyActioned);
        	threadPoolExecutor.execute(runnable);
        }

        // Done
        if (logger.isDebugEnabled())
        {
            // get the stack trace
            Exception e = new Exception();
            e.fillInStackTrace();
            StackTraceElement[] trace = e.getStackTrace();
            StringBuilder sb = new StringBuilder();
            sb.append("\n")
              .append("Placed action on execution queue: \n")
              .append("   Action:     " + action);
            String msg = sb.toString();
            sb = new StringBuilder();
            StackTraceUtil.buildStackTrace(msg, trace, sb, -1);
            logger.debug(sb);
        }
    }
    
    private void handleAsyncActionIsCompleted(NodeRef n, Action action) {
    	if (logger.isDebugEnabled())
    	{
    		StringBuilder msg = new StringBuilder();
    		msg.append("Completed action ").append(action);
    		logger.debug(msg.toString());
    	}
    	OngoingAsyncAction ongoing = new OngoingAsyncAction(n, action);
    	ongoingActions.remove(ongoing);
    }
    
    /**
     * Transaction listener used to invoke callback policies
     */
    public class CallbackTransactionListener extends TransactionListenerAdapter
    {
        private Action action;
        private NodeRef actionedUponNodeRef;
        
        /**
         * Constructor
         * 
         * @param action                    action
         * @param actionedUponNodeRef       actioned upon node reference
         */
        public CallbackTransactionListener(Action action, NodeRef actionedUponNodeRef)
        {
            this.action = action;
            this.actionedUponNodeRef = actionedUponNodeRef;
        }

        /**
         * @see org.alfresco.repo.transaction.TransactionListenerAdapter#afterCommit()
         */
        @Override
        public void afterCommit()
        {
            // Invoke the execute complete policy
            invokeOnAsyncActionExecutePolicy(action, actionedUponNodeRef);           
        }        
    }

    /**
     * Runnable class to wrap the execution of the action.
     */
    private class ActionExecutionWrapper implements Runnable
    {
        private RuntimeActionService actionService;

        private Action action;
        private NodeRef actionedUponNodeRef;
        private boolean checkConditions;
        private Set<String> actionChain;
        private Set<RuleServiceImpl.ExecutedRuleData> executedRules;

        /**
         * @param actionService                     the action service
         * @param action                            the action to perform
         * @param actionedUponNodeRef               the node to perform the action on
         * @param checkConditions                   the check conditions
         * @param actionChain                       the action chain
         * @param executedRules                     list of executions done to helps to prevent loop scenarios with async rules
         */
        public ActionExecutionWrapper(
                RuntimeActionService actionService,
                Action action,
                NodeRef actionedUponNodeRef,
                boolean checkConditions,
                Set<String> actionChain,
                Set<RuleServiceImpl.ExecutedRuleData> executedRules)
        {
            this.actionService = actionService;
            this.actionedUponNodeRef = actionedUponNodeRef;
            this.action = action;
            this.checkConditions = checkConditions;
            this.actionChain = actionChain;
            this.executedRules = executedRules;
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
                
                // import the content
                RunAsWork<Object> actionRunAs = new RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        RetryingTransactionCallback<Object> actionCallback = new RetryingTransactionCallback<Object>()
                        {
                            public Object execute()
                            {   
                                // If we have rules, apply them
                                if (ActionExecutionWrapper.this.executedRules != null)
                                {
                                    AlfrescoTransactionSupport.bindResource("RuleServiceImpl.ExecutedRules", ActionExecutionWrapper.this.executedRules);
                                }
                                
                                // Allow other classes to know when this action completes
                                AlfrescoTransactionSupport.bindListener(new CallbackTransactionListener(
                                      ActionExecutionWrapper.this.action,
                                      ActionExecutionWrapper.this.actionedUponNodeRef
                                ));
                                
                                // Have the action run
                                ActionExecutionWrapper.this.actionService.executeActionImpl(
                                        ActionExecutionWrapper.this.action,
                                        ActionExecutionWrapper.this.actionedUponNodeRef,
                                        ActionExecutionWrapper.this.checkConditions, true,
                                        ActionExecutionWrapper.this.actionChain);

                                return null;
                            }
                        };
                        return transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback);
                    }
                };
                AuthenticationUtil.runAs(actionRunAs, userName);
            }
            catch (Throwable exception)
            {
                logger.error("Failed to execute asynchronous action: " + action, exception);
            }
            handleAsyncActionIsCompleted(actionedUponNodeRef, action);
        }
    }
}