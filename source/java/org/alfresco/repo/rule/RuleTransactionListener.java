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
	 * @param ruleService RuntimeRuleService
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
