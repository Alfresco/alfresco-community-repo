package org.alfresco.repo.action;

import java.util.Set;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Asynchronous action execution queue
 * 
 * @author Roy Wetherall
 */
public interface AsynchronousActionExecutionQueue
{
	/**
	 * @param actionService RuntimeActionService
	 * @param action Action
	 * @param actionedUponNodeRef NodeRef
	 * @param checkConditions boolean
	 * @param actionChain Set<String>
	 */
	void executeAction(
			RuntimeActionService actionService,
			Action action,
			NodeRef actionedUponNodeRef, 			 
			boolean checkConditions,
            Set<String> actionChain);
	
}
