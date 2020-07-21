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

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.util.Pair;

import java.util.Comparator;
import java.util.List;

@AlfrescoPublicApi
public class SiteGroupMembership extends AbstractSiteMembership
{
    private String displayName;

    public SiteGroupMembership(SiteInfo siteInfo, String id, String role, String displayName)
    {
        super(siteInfo, id, role);
        this.displayName = displayName;
    }

    public String getDisplayName()
    {
        return displayName;
    }


    static int compareTo(List<Pair<? extends Object, CannedQuerySortDetails.SortOrder>> sortPairs, SiteGroupMembership o1, SiteGroupMembership o2)
    {
        String displayName1 = o1.getDisplayName();
        String displayName2 = o2.getDisplayName();

        String siteRole1 = o1.getRole();
        String siteRole2 = o2.getRole();

        String shortName1 = o1.getSiteInfo().getShortName();
        String shortName2 = o2.getSiteInfo().getShortName();

        int groupName = SiteMembershipComparator.safeCompare(displayName1, displayName2);
        int siteRole = SiteMembershipComparator.safeCompare(siteRole1, siteRole2);
        int siteShortName = SiteMembershipComparator.safeCompare(shortName1, shortName2);

        if (siteRole == 0 && siteShortName == 0 && groupName == 0)
            return 0;

        return SiteMembershipComparator.compareSiteGroupsBody(sortPairs, displayName1, displayName2,  siteRole1, siteRole2, groupName, siteRole , 0);
    }

    static Comparator<SiteGroupMembership> getComparator(List<Pair<?, CannedQuerySortDetails.SortOrder>> sortPairs)
    {
        return (SiteGroupMembership o1, SiteGroupMembership o2) -> compareTo(sortPairs, o1, o2);
    }
}
