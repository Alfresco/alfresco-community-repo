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
    		this.siteMembers = sortPairs != null && sortPairs.size() > 0 ? new TreeSet<SiteMembership>(new SiteMembershipComparator(sortPairs, SiteMembershipComparator.Type.MEMBERS)) : new HashSet<SiteMembership>();
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

			SiteMembership siteMember = new SiteMembership(siteInfo, authority, firstName, lastName, permission);
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
}
