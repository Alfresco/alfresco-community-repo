package org.alfresco.repo.site;

import java.util.Comparator;
import java.util.Map;

/* package scope */class RoleComparatorImpl implements Comparator<String>
{
    private Map<String, Integer> rolePrecedence;
	
	public RoleComparatorImpl() 
	{	
	}
	
    public RoleComparatorImpl(Map<String, Integer> rolePrecedence)
    {
    		this.setRolePrecedence(rolePrecedence);
    }

    public int compare(String first, String second) 
    {
        int firstRank = 0;
        int secondRank = 0;
        if (getRolePrecedence().containsKey(first))
        {
            firstRank = getRolePrecedence().get(first);
        }
        if (getRolePrecedence().containsKey(second))
        {
            secondRank = getRolePrecedence().get(second);
        }

        return secondRank > firstRank ? 1 : secondRank < firstRank ? -1 : 0;
    }
		
	public void init() 
	{
	    	
	}
	public void setRolePrecedence(Map<String, Integer> rolePrecedence) 
	{
		this.rolePrecedence = rolePrecedence;
	}
	public Map<String, Integer> getRolePrecedence() 
	{
		return rolePrecedence;
	}
}


