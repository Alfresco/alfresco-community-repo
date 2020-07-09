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

/**
 * Conveys information for a member of a site.
 *
 * @author steveglover
 *
 */
@AlfrescoPublicApi
public class SiteMembership extends AbstractSiteMembership
{
    private String firstName;
    private String lastName;
    private boolean isMemberOfGroup;

    /**
     * @deprecated from 7.0.0
     */
    public SiteMembership(SiteInfo siteInfo, String id, String firstName, String lastName,
            String role)
    {
        super(siteInfo,id, role);
        if (firstName == null)
        {
            throw new java.lang.IllegalArgumentException(
                    "FirstName required building site membership of " + siteInfo.getShortName());
        }
        if (lastName == null)
        {
            throw new java.lang.IllegalArgumentException(
                    "LastName required building site membership of " + siteInfo.getShortName());
        }
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public SiteMembership(SiteInfo siteInfo, String id, String firstName, String lastName,
            String role, boolean isMemberOfGroup)
    {
        super(siteInfo, id, role);
        if (firstName == null)
        {
            throw new java.lang.IllegalArgumentException(
                    "FirstName required building site membership of " + siteInfo.getShortName());
        }
        if (lastName == null)
        {
            throw new java.lang.IllegalArgumentException(
                    "LastName required building site membership of " + siteInfo.getShortName());
        }
        this.firstName = firstName;
        this.lastName = lastName;
        this.isMemberOfGroup = isMemberOfGroup;
    }

    public SiteMembership(SiteInfo siteInfo, String id, String role)
    {
        super(siteInfo, id, role);
    }

    /** @deprecated from 7.0.0 use getId instead */
    public String getPersonId()
    {
        return id;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public boolean isMemberOfGroup()
    {
        return isMemberOfGroup;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((role == null) ? 0 : role.hashCode());
        result = prime * result + ((getSiteInfo() == null) ? 0 : getSiteInfo().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SiteMembership other = (SiteMembership) obj;
        if (id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!id.equals(other.id))
            return false;
        if (role != other.role)
            return false;

        if (isMemberOfGroup != other.isMemberOfGroup)
            return false;

        if (getSiteInfo() == null)
        {
            if (other.getSiteInfo() != null)
                return false;
        }
        else if (!getSiteInfo().equals(other.getSiteInfo()))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "SiteMembership [siteInfo=" + getSiteInfo() + ", id=" + id
                + ", firstName=" + firstName + ", lastName=" + lastName + ", role=" + role +
                ", isMemberOfGroup = " + isMemberOfGroup + "]";
    }


    static int compareTo(List<Pair<? extends Object, CannedQuerySortDetails.SortOrder>> sortPairs, SiteMembership o1, SiteMembership o2)
    {
        String personId1 = o1.getPersonId();
        String personId2 = o2.getPersonId();
        String firstName1 = o1.getFirstName();
        String firstName2 = o2.getFirstName();
        String lastName1 = o1.getLastName();
        String lastName2 = o2.getLastName();
        String siteRole1 = o1.getRole();
        String siteRole2 = o2.getRole();
        String shortName1 = o1.getSiteInfo().getShortName();
        String shortName2 = o2.getSiteInfo().getShortName();

        int personId = SiteMembershipComparator.safeCompare(personId1, personId2);
        int firstName = SiteMembershipComparator.safeCompare(firstName1, firstName2);
        int siteShortName = SiteMembershipComparator.safeCompare(shortName1, shortName2);
        int lastName = SiteMembershipComparator.safeCompare(lastName1, lastName2);
        int siteRole = SiteMembershipComparator.safeCompare(siteRole1, siteRole2);

        if (siteRole == 0 && siteShortName == 0 && personId == 0)
        {
            // equals contract
            return 0;
        }

        return SiteMembershipComparator.compareSiteMembersBody(sortPairs, personId1, personId2, lastName1, lastName2, siteRole1, siteRole2, personId, firstName, lastName, siteRole, 0);
    }

    static Comparator<SiteMembership> getComparator(List<Pair<?, CannedQuerySortDetails.SortOrder>> sortPairs)
    {
        return (SiteMembership o1, SiteMembership o2) -> compareTo(sortPairs, o1, o2);
    }
}
