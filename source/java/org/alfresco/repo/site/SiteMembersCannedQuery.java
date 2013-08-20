/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.model.ContentModel;
import org.alfresco.query.AbstractCannedQuery;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteRole;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteService.SiteMembersCallback;
import org.alfresco.util.Pair;

/**
 * A canned query for retrieving the members of a site.
 * 
 * @author steveglover
 *
 */
//TODO currently have to read all sites into memory for sorting purposes. Find a way that doesn't involve doing this.
public class SiteMembersCannedQuery extends AbstractCannedQuery<SiteMembership>
{
	private NodeService nodeService;
	private PersonService personService;
    private SiteService siteService;
    
    protected SiteMembersCannedQuery(SiteService siteService, PersonService personService, NodeService nodeService, CannedQueryParameters parameters)
    {
        super(parameters);
        this.personService = personService;
        this.nodeService = nodeService;
        this.siteService = siteService;
    }
    
    @Override
    protected List<SiteMembership> queryAndFilter(CannedQueryParameters parameters)
    {
        SiteMembersCannedQueryParams paramBean = (SiteMembersCannedQueryParams)parameters.getParameterBean();
        
        String siteShortName = paramBean.getShortName();
        boolean collapseGroups = paramBean.isCollapseGroups();

		CannedQuerySortDetails sortDetails = parameters.getSortDetails();
		List<Pair<? extends Object, SortOrder>> sortPairs = sortDetails.getSortPairs();
		
    	final CQSiteMembersCallback callback = new CQSiteMembersCallback(siteShortName, sortPairs);
    	siteService.listMembers(siteShortName, null, null, collapseGroups, callback);
    	callback.done();

        return callback.getSiteMembers();
    }
    
    @Override
    protected boolean isApplyPostQuerySorting()
    {
    	// already sorted as a side effect by CQSiteMembersCallback
        return false;
    }

    private class CQSiteMembersCallback implements SiteMembersCallback
    {
    	private String siteShortName;
    	private SiteInfo siteInfo;
    	private Set<SiteMembership> siteMembers;

    	CQSiteMembersCallback(String siteShortName, List<Pair<? extends Object, SortOrder>> sortPairs)
    	{
    		this.siteShortName = siteShortName;
			this.siteInfo = siteService.getSite(siteShortName);
    		this.siteMembers = sortPairs != null && sortPairs.size() > 0 ? new TreeSet<SiteMembership>(new SiteMembershipComparator(sortPairs)) : new HashSet<SiteMembership>();
    	}

		@Override
		public void siteMember(String authority, String permission)
		{
			String firstName = null;
			String lastName = null;

			if(personService.personExists(authority))
			{
				NodeRef nodeRef = personService.getPerson(authority);
				firstName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_FIRSTNAME);
				lastName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_LASTNAME);
			}

			SiteMembership siteMember = new SiteMembership(siteInfo, authority, firstName, lastName, SiteRole.valueOf(permission));
    		siteMembers.add(siteMember);
		}

		@Override
		public boolean isDone()
		{
			return false; // need to read in all site members for sort
		}
		
		List<SiteMembership> getSiteMembers()
		{
    		// "drain" the site memberships into a (sorted) list

    		List<SiteMembership> siteMemberships = new ArrayList<SiteMembership>(siteMembers.size());
    		Iterator<SiteMembership> it = siteMembers.iterator();
    		while(it.hasNext())
    		{
    			siteMemberships.add(it.next());
    			it.remove();
    		}
    		return siteMemberships;
		}

		void done()
		{
		}
    }
    
    private static class SiteMembershipComparator implements Comparator<SiteMembership>
    {
    	private List<Pair<? extends Object, SortOrder>> sortPairs;
		private static Collator collator = Collator.getInstance();

    	public SiteMembershipComparator(List<Pair<? extends Object, SortOrder>> sortPairs)
    	{
    		if(sortPairs.size() < 1)
    		{
    			throw new IllegalArgumentException("Must provide at least one sort criterion");
    		}
    		this.sortPairs = sortPairs;
    	}

    	private <T extends Object> int safeCompare(Comparable<T> o1, T o2)
    	{
    		int ret = 0;

    		if(o1 == null)
    		{
    			if(o2 == null)
    			{
    				ret = 0;
    			}
    			else
    			{
    				ret = -1;
    			}
    		}
    		else
    		{
    			if(o2 == null)
    			{
    				ret = 1;
    			}
    			else
    			{
    				ret = o1.compareTo(o2);
    			}
    		}

    		return ret;
    	}

    	private int safeCompare(String s1, String s2)
    	{
    		int ret = 0;

    		if(s1 == null)
    		{
    			if(s2 == null)
    			{
    				ret = 0;
    			}
    			else
    			{
    				ret = -1;
    			}
    		}
    		else
    		{
    			if(s2 == null)
    			{
    				ret = 1;
    			}
    			else
    			{
    				ret = collator.compare(s1, s2);
    			}
    		}

    		return ret;
    	}
    	
    	@Override
    	public int compare(SiteMembership o1, SiteMembership o2)
    	{
    		String personId1 = o1.getPersonId();
    		String personId2 = o2.getPersonId();
    		SiteInfo siteInfo1 = o1.getSiteInfo();
    		SiteInfo siteInfo2 = o2.getSiteInfo();
    		String shortName1 = siteInfo1.getShortName();
    		String shortName2 = siteInfo2.getShortName();
    		String firstName1 = o1.getFirstName();
    		String firstName2 = o2.getFirstName();
    		String lastName1 = o1.getLastName();
    		String lastName2 = o2.getLastName();
    		SiteRole siteRole1 = o1.getRole();
    		SiteRole siteRole2 = o2.getRole();

    		int personId = safeCompare(personId1, personId2);
    		int firstName = safeCompare(firstName1, firstName2);
    		int siteShortName = safeCompare(shortName1, shortName2);
    		int lastName = safeCompare(lastName1, lastName2);
    		int siteRole = safeCompare(siteRole1, siteRole2);
    		
    		if(siteRole == 0 && siteShortName == 0 && personId == 0)
    		{
    			// equals contract
    			return 0;
    		}

    		int ret = 0;

    		for(Pair<? extends Object, SortOrder> pair : sortPairs)
    		{
    			Object name = pair.getFirst();
    			SortOrder sortOrder = pair.getSecond();

    			int multiplier = sortOrder.equals(SortOrder.ASCENDING) ? 1 : -1;
    			if(name.equals(SiteService.SortFields.FirstName))
    			{
    				ret = firstName * multiplier;
    			}
    			else if(name.equals(SiteService.SortFields.LastName))
    			{
    				if(lastName1 == null || lastName2 == null)
    				{
    					continue;
    				}
    				ret = lastName * multiplier;
    			}
    			else if(name.equals(SiteService.SortFields.Role))
    			{
    				if(siteRole1 == null || siteRole2 == null)
    				{
    					continue;
    				}
    				ret = siteRole * multiplier;
    			}
    			else if(name.equals(SiteService.SortFields.Username))
    			{
    				if(personId1 == null || personId2 == null)
    				{
    					continue;
    				}
    				ret = personId * multiplier;
    			}

    			if(ret != 0)
    			{
    				break;
    			}
    		}

    		return ret;
    	}
    }
}
