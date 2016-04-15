/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
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


