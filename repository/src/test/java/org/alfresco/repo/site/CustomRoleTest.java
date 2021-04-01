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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.testing.category.NeverRunsTests;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test custom role for a Site. Based on MNT-12873
 * 
 * @author Alex Bykov
 */
@Transactional
@ContextConfiguration({"classpath:alfresco/application-context.xml",
        "classpath:org/alfresco/repo/site/site-custom-context.xml"})
@Category(NeverRunsTests.class)
public class CustomRoleTest extends BaseAlfrescoSpringTest
{
    private static final String USER_ONE = "UserOne_CustomRoleTest";
    private static final String USER_TWO = "UserTwo_CustomRoleTest";

    private SiteService siteService;
    private PersonService personService;

    @Before
    public void before() throws Exception
    {
        super.before();

        // Get the required services
        this.authenticationComponent = (AuthenticationComponent) this.applicationContext.getBean("authenticationComponent");
        this.siteService = (SiteService) this.applicationContext.getBean("SiteService");
        this.personService = (PersonService) this.applicationContext.getBean("PersonService");

        // Create the test users
        createUser(USER_ONE, "UserOne");
        createUser(USER_TWO, "UserTwo");

        // Set the current authentication
        this.authenticationComponent.setCurrentUser(USER_ONE);
    }

    // MNT-12873
    public void testCustomRole()
    {
        try
        {
            // Create a site
            siteService.createSite("customrolessite", "customrolessite", "Test custom role", "Test custom role", SiteVisibility.PUBLIC);

            this.siteService.setMembership("customrolessite", USER_TWO, "SiteCoordinator");

            // Get the members of the site
            final List<Pair<SiteService.SortFields, Boolean>> sort = new ArrayList<Pair<SiteService.SortFields, Boolean>>();
            sort.add(new Pair<SiteService.SortFields, Boolean>(SiteService.SortFields.SiteTitle, Boolean.TRUE));
            sort.add(new Pair<SiteService.SortFields, Boolean>(SiteService.SortFields.Role, Boolean.TRUE));

            PagingResults<SiteMembership> sites = siteService.listSitesPaged(USER_TWO, sort, new PagingRequest(0, 100));
            assertNotNull(sites);
            assertEquals(sites.getPage().size(), 1);
        }
        catch (Exception ex)
        {
            fail("Custom role breaks sites API. Take a look on MNT-12873\n" + ex.getMessage());
        }
    }
}
