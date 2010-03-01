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
