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
import java.util.Comparator;
import java.util.List;

import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.Pair;

public class SiteMembershipComparator implements Comparator<SiteMembership>
{
    public enum Type
    {
        SITES, MEMBERS
    }

    private List<Pair<? extends Object, SortOrder>> sortPairs;
    private static Collator collator = Collator.getInstance();
    private Type comparatorType;

    public SiteMembershipComparator(List<Pair<? extends Object, SortOrder>> sortPairs, Type comparatorType)
    {
        if (sortPairs.size() < 1)
        {
            throw new IllegalArgumentException("Must provide at least one sort criterion");
        }
        this.sortPairs = sortPairs;
        this.comparatorType = comparatorType;
    }

    private <T extends Object> int safeCompare(Comparable<T> o1, T o2)
    {
        int ret = 0;

        if (o1 == null)
        {
            if (o2 == null)
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
            if (o2 == null)
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

        if (s1 == null)
        {
            if (s2 == null)
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
            if (s2 == null)
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

    private int compareMembersBody(String personId1, String personId2, String lastName1, String lastName2, String siteRole1, String siteRole2, int personId, int firstName,
            int lastName, int siteRole, int ret)
    {
        for (Pair<? extends Object, SortOrder> pair : sortPairs)
        {
            Object name = pair.getFirst();
            SortOrder sortOrder = pair.getSecond();

            int multiplier = sortOrder.equals(SortOrder.ASCENDING) ? 1 : -1;
            if (name.equals(SiteService.SortFields.FirstName))
            {
                ret = firstName * multiplier;
            }
            else if (name.equals(SiteService.SortFields.LastName))
            {
                if (lastName1 == null || lastName2 == null)
                {
                    continue;
                }
                ret = lastName * multiplier;
            }
            else if (name.equals(SiteService.SortFields.Role))
            {
                if (siteRole1 == null || siteRole2 == null)
                {
                    continue;
                }
                ret = siteRole * multiplier;
            }
            else if (name.equals(SiteService.SortFields.Username))
            {
                if (personId1 == null || personId2 == null)
                {
                    continue;
                }
                ret = personId * multiplier;
            }

            if (ret != 0)
            {
                break;
            }
        }
        return ret;
    }

    private int compareSitesBody(String shortName1, String shortName2, String siteRole1, String siteRole2, String siteTitle1, String siteTitle2, int siteShortName, int siteRole,
            int siteTitle, int ret)
    {
        for (Pair<? extends Object, SortOrder> pair : sortPairs)
        {
            Object name = pair.getFirst();
            SortOrder sortOrder = pair.getSecond();

            int multiplier = sortOrder.equals(SortOrder.ASCENDING) ? 1 : -1;
            if (name.equals(SiteService.SortFields.SiteShortName))
            {
                if (shortName1 == null || shortName2 == null)
                {
                    continue;
                }
                ret = siteShortName * multiplier;
            }
            else if (name.equals(SiteService.SortFields.SiteTitle))
            {
                if (siteTitle1 == null || siteTitle2 == null)
                {
                    continue;
                }
                ret = siteTitle * multiplier;
            }
            else if (name.equals(SiteService.SortFields.Role))
            {
                if (siteRole1 == null || siteRole2 == null)
                {
                    continue;
                }
                ret = siteRole * multiplier;
            }

            if (ret != 0)
            {
                break;
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
        String siteRole1 = o1.getRole();
        String siteRole2 = o2.getRole();
        String siteTitle1 = siteInfo1.getTitle();
        String siteTitle2 = siteInfo2.getTitle();

        int personId = safeCompare(personId1, personId2);
        int firstName = safeCompare(firstName1, firstName2);
        int siteShortName = safeCompare(shortName1, shortName2);
        int lastName = safeCompare(lastName1, lastName2);
        int siteRole = safeCompare(siteRole1, siteRole2);
        int siteTitle = safeCompare(siteTitle1, siteTitle2);

        if (siteRole == 0 && siteShortName == 0 && personId == 0)
        {
            // equals contract
            return 0;
        }

        int ret = 0;

        switch (comparatorType)
        {
            case SITES:
            {
                ret = compareSitesBody(shortName1, shortName2, siteRole1, siteRole2, siteTitle1, siteTitle2, siteShortName, siteRole, siteTitle, ret);
                break;
            }
            case MEMBERS:
            {
                ret = compareMembersBody(personId1, personId2, lastName1, lastName2, siteRole1, siteRole2, personId, firstName, lastName, siteRole, ret);
                break;
            }
        }
        
        return ret;
    }
}
