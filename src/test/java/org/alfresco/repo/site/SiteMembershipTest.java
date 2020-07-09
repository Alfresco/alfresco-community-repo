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

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.junit.Before;
import org.junit.Test;

/**
 * Test Membership constructor logs. Based on REPO-2520
 * 
 * @author Alexandru Epure
 */
public class SiteMembershipTest
{
    SiteInfo siteInfo;
    SiteMembership siteMember;
    String personId = UUID.randomUUID().toString();
    String firstName = UUID.randomUUID().toString();
    String lastName = UUID.randomUUID().toString();
    String role = "Consumer";

    String idErrorMessage = "Id required building site membership";
    String firstNameErrorMessage = "FirstName required building site membership of ";
    String lastNameErrorMessage = "LastName required building site membership of ";
    String roleErrorMessage = "Role required building site membership";

    @Before
    public void createSite()
    {
        String sitePreset = "testSiteMembershipPreset";
        String shortName = "testSiteMembershipShortName";
        String title = "testSiteMembershipTile";
        String description = "testSiteMembershipDescription";
        siteInfo = new SiteInfoImpl(sitePreset, shortName, title, description,
                SiteVisibility.PUBLIC, null);
    }

    @Test
    public void testNullpersonInfo() throws Exception
    {
        try
        {
            siteMember = new SiteMembership(siteInfo, null, firstName, lastName, role);
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(idErrorMessage, e.getMessage());
        }

        try
        {
            siteMember = new SiteMembership(siteInfo, null, role);
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(idErrorMessage, e.getMessage());
        }
    }

    @Test
    public void testNullRole() throws Exception
    {
        try
        {
            siteMember = new SiteMembership(siteInfo, personId, firstName, lastName, null);
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(roleErrorMessage, e.getMessage());
        }

        try
        {
            siteMember = new SiteMembership(siteInfo, personId, null);
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(roleErrorMessage, e.getMessage());
        }
    }

    @Test
    public void testNullFirstName() throws Exception
    {
        try
        {
            siteMember = new SiteMembership(siteInfo, personId, null, lastName, role);
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(firstNameErrorMessage + siteInfo.getShortName(), e.getMessage());
        }
    }

    @Test
    public void testNullLastName() throws Exception
    {
        try
        {
            siteMember = new SiteMembership(siteInfo, personId, firstName, null, role);
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(lastNameErrorMessage + siteInfo.getShortName(), e.getMessage());
        }
    }
}
