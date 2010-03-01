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
		int firstRank = getRolePrecedence().get(first);
		int secondRank = getRolePrecedence().get(second);
					
		return secondRank - firstRank;			
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


