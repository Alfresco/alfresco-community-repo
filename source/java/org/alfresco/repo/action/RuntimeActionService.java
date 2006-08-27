/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.action;

import java.util.List;
import java.util.Set;

import org.alfresco.repo.action.ActionServiceImpl.PendingAction;
import org.alfresco.repo.action.evaluator.ActionConditionEvaluator;
import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * @author Roy Wetherall
 */
public interface RuntimeActionService
{
    /**
     * Get the asynchronous action queue.
     * 
     * @return  the asynchronous action queue
     */
	AsynchronousActionExecutionQueue getAsynchronousActionExecutionQueue();
	
    /**
     * Register an action condition evaluator
     * 
     * @param actionConditionEvaluator  action condition evaluator
     */
	void registerActionConditionEvaluator(ActionConditionEvaluator actionConditionEvaluator);
	
    /**
     * Register an action executer
     * 
     * @param actionExecuter    action executer
     */
	void registerActionExecuter(ActionExecuter actionExecuter);
	        
    Action createAction(NodeRef actionNodeRef);
    
    NodeRef createActionNodeRef(Action action, NodeRef parentNodeRef, QName assocTypeName, QName assocName);
	
	/**
	 * Save action, used internally to store the details of an action on the aciton node.
	 * 
	 * @param actionNodeRef	the action node reference
	 * @param action		the action 
	 */
	void saveActionImpl(NodeRef actionNodeRef, Action action);
	
	/**
	 * 
	 * @param action
	 * @param actionedUponNodeRef
	 * @param checkConditions
	 */
	public void executeActionImpl(
			Action action, 
			NodeRef actionedUponNodeRef, 
			boolean checkConditions, 
			boolean executedAsynchronously,
            Set<String> actionChain);
	
    /**
     * Execute an action directly
     * 
     * @param action                the action 
     * @param actionedUponNodeRef   the actioned upon node reference
     */
	public void directActionExecution(Action action, NodeRef actionedUponNodeRef);
	
    /**
     * Gets a list of the actions that are pending post transaction
     * 
     * @return  list of pending actions
     */
	public List<PendingAction> getPostTransactionPendingActions();
}
