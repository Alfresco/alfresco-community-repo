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
