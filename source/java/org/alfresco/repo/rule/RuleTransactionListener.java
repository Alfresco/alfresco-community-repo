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
package org.alfresco.repo.rule;

import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.util.GUID;

/**
 * The rule service transaction listener
 * 
 * @author Roy Wetherall
 */
public class RuleTransactionListener extends TransactionListenerAdapter
{
	/**
	 * Id used in equals and hash
	 */
	private String id = GUID.generate();
	
	/**
	 * The rule service (runtime interface) 
	 */
	private RuntimeRuleService ruleService;
	
	/**
	 * Constructor
	 * 
	 * @param 
	 */
	public RuleTransactionListener(RuntimeRuleService ruleService)
	{
		this.ruleService = ruleService;
	}
	
	/**
	 * @see org.alfresco.repo.transaction.TransactionListener#beforeCommit(boolean)
	 */
	@Override
	public void beforeCommit(boolean readOnly)
	{
		this.ruleService.executePendingRules();
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
        if (obj instanceof RuleTransactionListener)
        {
        	RuleTransactionListener that = (RuleTransactionListener) obj;
            return (this.id.equals(that.id));
        }
        else
        {
            return false;
        }
	}

}
