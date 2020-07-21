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

import org.alfresco.query.AbstractCannedQuery;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteService.SiteMembersCallback;
import org.alfresco.util.Pair;

import java.util.Set;
import java.util.List;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.ArrayList;

// TODO currently have to read all sites into memory for sorting purposes. Find a way that doesn't
public class SiteGroupCannedQuery extends AbstractCannedQuery<SiteGroupMembership>
{
  private AuthorityService authorityService;
  private SiteService siteService;

  protected SiteGroupCannedQuery(SiteService siteService, AuthorityService authorityService, CannedQueryParameters parameters)
  {
    super(parameters);
    this.authorityService = authorityService;
    this.siteService = siteService;
  }

  @Override
  protected List<SiteGroupMembership> queryAndFilter(CannedQueryParameters parameters)
  {
    SiteMembersCannedQueryParams paramBean = (SiteMembersCannedQueryParams) parameters.getParameterBean();

    String siteShortName = paramBean.getShortName();
    CannedQuerySortDetails sortDetails = parameters.getSortDetails();

    final CQSiteGroupsCallback callback = new CQSiteGroupsCallback(siteShortName, sortDetails.getSortPairs());
    siteService.listMembers(siteShortName, null, null, false, true, paramBean.isExpandGroups(), callback);
    callback.done();

    return callback.getSiteMembers();
  }

  @Override
  protected boolean isApplyPostQuerySorting()
  {
    // already sorted as a side effect
    return false;
  }

  private class CQSiteGroupsCallback implements SiteMembersCallback
  {
    private SiteInfo siteInfo;
    private Set<SiteGroupMembership> siteGroups;

    CQSiteGroupsCallback(String siteShortName, List<Pair<? extends Object, SortOrder>> sortPairs)
    {
      this.siteInfo = siteService.getSite(siteShortName);
      this.siteGroups = sortPairs != null && sortPairs.size() > 0
              ? new TreeSet<>(SiteGroupMembership.getComparator(sortPairs))
              : new HashSet<>();
    }

    @Override
    public void siteMember(String authority, String role)
    {
		if(authorityService.authorityExists(authority))
		{
		  String displayName = authorityService.getAuthorityDisplayName(authority);
		  siteGroups.add(new SiteGroupMembership(siteInfo, authority, role, displayName));
		}
    }

    @Override
    public boolean isDone()
    {
      // need to read in all site members for sort
      return false;
    }

    List<SiteGroupMembership> getSiteMembers()
    {
      return new ArrayList<>(siteGroups);
    }

    void done() {}
  }
}
