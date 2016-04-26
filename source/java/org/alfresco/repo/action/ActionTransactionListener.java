package org.alfresco.repo.action;

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
		this.actionService.postCommit();
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
