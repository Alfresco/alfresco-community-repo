/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
