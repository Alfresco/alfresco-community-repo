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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.TestCase;


public class RoleComparatorImplTest extends TestCase 
{
	public void testRoleComparator()
	{
		Map<String, Integer> rolePrecedence = new HashMap<String, Integer>();
		rolePrecedence.put("SiteManager", 4);
		rolePrecedence.put("SiteCollaborator", 3);
		rolePrecedence.put("SiteContributor", 2);
		rolePrecedence.put("SiteConsumer", 1);
		Comparator<String> roleComparator = new RoleComparatorImpl(rolePrecedence);
		
		
        List<String> roles = new ArrayList<String>();
        roles.add("SiteConsumer");       
        assertEquals("SiteConsumer", sort(roles, roleComparator));
        roles.add("SiteConsumer");       
        assertEquals("SiteConsumer", sort(roles, roleComparator));
        roles.add("SiteContributor");       
        assertEquals("SiteContributor", sort(roles, roleComparator));
        roles.add("SiteConsumer"); 
        assertEquals("SiteContributor", sort(roles, roleComparator));
        roles.add("SiteManager"); 
        assertEquals("SiteManager", sort(roles, roleComparator));
        roles.add("SiteCollaborator"); 
        assertEquals("SiteManager", sort(roles, roleComparator));
	}
		
	
	
    private String sort(List<String> list, Comparator comparator)
    {
        	SortedSet<String> sortedRoles = new TreeSet<String>(comparator);
        	for (String role : list)
        	{
        		sortedRoles.add(role);
        	}
        	return sortedRoles.first();
    }
}
