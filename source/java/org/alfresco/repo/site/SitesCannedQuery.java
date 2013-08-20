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

import org.alfresco.query.AbstractCannedQuery;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityService.AuthorityFilter;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteRole;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.Pair;

/**
 * A canned query to retrieve the sites for a user.
 * 
 * @author steveglover
 *
 */
// TODO currently have to read all sites into memory for sorting purposes. Find a way that doesn't involve doing this.
public class SitesCannedQuery extends AbstractCannedQuery<SiteMembership>
{
    private AuthorityService authorityService;
    private SiteService siteService;
    
    protected SitesCannedQuery(AuthorityService authorityService, SiteService siteService, CannedQueryParameters parameters)
    {
        super(parameters);
        this.authorityService = authorityService;
        this.siteService = siteService;
    }
    
    @Override
    protected List<SiteMembership> queryAndFilter(CannedQueryParameters parameters)
    {
        // get paramBean - note: this currently has both optional filter and optional sort param
        SitesCannedQueryParams paramBean = (SitesCannedQueryParams)parameters.getParameterBean();
        
        String userName = paramBean.getUsername();

		final int size = CannedQueryPageDetails.DEFAULT_PAGE_SIZE;

		CannedQuerySortDetails sortDetails = parameters.getSortDetails();
		List<Pair<? extends Object, SortOrder>> sortPairs = sortDetails.getSortPairs();

		CQAuthorityFilter filter = new CQAuthorityFilter(userName, sortPairs);
        authorityService.getContainingAuthoritiesInZone(AuthorityType.GROUP, userName, AuthorityService.ZONE_APP_SHARE, filter, size);

        return filter.getSiteMemberships();
    }
    
    @Override
    protected boolean isApplyPostQuerySorting()
    {
    	// already sorted as a side effect by CQAuthorityFilter
        return false;
    }

    private class CQAuthorityFilter implements AuthorityFilter
    {
    	private String userName;
    	private Set<SiteMembership> siteMembers;

    	CQAuthorityFilter(String userName, List<Pair<? extends Object, SortOrder>> sortPairs)
    	{
    		this.userName = userName;
    		this.siteMembers = sortPairs != null && sortPairs.size() > 0 ? new TreeSet<SiteMembership>(new SiteMembershipComparator(sortPairs)) : new HashSet<SiteMembership>();
    	}

    	@Override
        public boolean includeAuthority(String authority)
        {
            String siteName = siteService.resolveSite(authority);
            if(siteName != null)
            {
            	SiteInfo siteInfo = siteService.getSite(siteName);
            	if(siteInfo != null)
            	{
	        		String role = siteService.getMembersRole(siteName, userName);
	        		if (role != null)
	        		{
	        			siteMembers.add(new SiteMembership(siteInfo, authority, SiteRole.valueOf(role)));
	        		}
            	}
            }

            return true; // need to get all items so that the canned query can do the sort in memory
        }

    	List<SiteMembership> getSiteMemberships()
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
    		String siteTitle1 = siteInfo1.getTitle();
    		String siteTitle2 = siteInfo2.getTitle();

    		int personId = safeCompare(personId1, personId2);
    		int firstName = safeCompare(firstName1, firstName2);
    		int siteShortName = safeCompare(shortName1, shortName2);
    		int lastName = safeCompare(lastName1, lastName2);
    		int siteRole = safeCompare(siteRole1, siteRole2);
    		int siteTitle = safeCompare(siteTitle1, siteTitle2);
    		
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
    			if(name.equals(SiteService.SortFields.SiteShortName))
    			{
    				if(shortName1 == null || shortName2 == null)
    				{
    					continue;
    				}
    				ret = siteShortName * multiplier;
    			}
    			else if(name.equals(SiteService.SortFields.SiteTitle))
    			{
    				if(siteTitle1 == null || siteTitle2 == null)
    				{
    					continue;
    				}
    				ret = siteTitle * multiplier;
    			}
    			else if(name.equals(SiteService.SortFields.Role))
    			{
    				if(siteRole1 == null || siteRole2 == null)
    				{
    					continue;
    				}
    				ret = siteRole * multiplier;
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
