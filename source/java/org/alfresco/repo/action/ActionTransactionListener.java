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

import org.alfresco.repo.action.ActionServiceImpl.PendingAction;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.util.GUID;

/**
 * The action service transaction listener
 * 
 * @author Roy Wetherall
 */
public class ActionTransactionListener implements TransactionListener
{
	/**
	 * Id used in equals and hash
	 */
	private String id = GUID.generate();
	
	/**
	 * The action service (runtime interface)
	 */
	private RuntimeActionService actionService;
	
	/**
	 * Constructor
	 * 
	 * @param actionService		the action service
	 */
	public ActionTransactionListener(RuntimeActionService actionService)
	{
		this.actionService = actionService;
	}
	
	/**
	 * @see org.alfresco.repo.transaction.TransactionListener#flush()
	 */
	public void flush()
	{
	}

	/**
	 * @see org.alfresco.repo.transaction.TransactionListener#beforeCommit(boolean)
	 */
	public void beforeCommit(boolean readOnly)
	{
	}

	/**
	 * @see org.alfresco.repo.transaction.TransactionListener#beforeCompletion()
	 */
	public void beforeCompletion()
	{
	}

	/**
	 * @see org.alfresco.repo.transaction.TransactionListener#afterCommit()
	 */
	public void afterCommit()
	{
		for (PendingAction pendingAction : this.actionService.getPostTransactionPendingActions())
		{
			this.actionService.getAsynchronousActionExecutionQueue().executeAction(
					actionService,
					pendingAction.getAction(),
					pendingAction.getActionedUponNodeRef(),
					pendingAction.getCheckConditions(),
                    pendingAction.getActionChain());
		} 
	}

	/**
	 * @see org.alfresco.repo.transaction.TransactionListener#afterRollback()
	 */
	public void afterRollback()
	{
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return this.id.hashCode();
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
        {
            return true;
        }
        if (obj instanceof ActionTransactionListener)
        {
        	ActionTransactionListener that = (ActionTransactionListener) obj;
            return (this.id.equals(that.id));
        }
        else
        {
            return false;
        }
	}

}
