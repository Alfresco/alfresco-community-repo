/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api.tests;

import org.alfresco.rest.DeletedNodesTest;
import org.alfresco.rest.api.search.BasicSearchApiIntegrationTest;
import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Public V1 REST API tests
 * 
 * @author steveglover
 * @author janv
 * @author Jamal Kaabi-Mofrad
 * @author Gethin James
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        
    NodeApiTest.class,
    NodeAssociationsApiTest.class,
    NodeVersionsApiTest.class,
    BasicSearchApiIntegrationTest.class,
    QueriesNodesApiTest.class,
    QueriesPeopleApiTest.class,
    QueriesSitesApiTest.class,
    RenditionsTest.class,
    SharedLinkApiTest.class,
    ActivitiesPostingTest.class,
    DeletedNodesTest.class,
    AuthenticationsTest.class,
    ModulePackagesApiTest.class,
    WherePredicateApiTest.class,
    DiscoveryApiTest.class,
    TestSites.class,
    TestNodeComments.class,
    TestFavouriteSites.class,
    TestSiteContainers.class,
    TestNodeRatings.class,
    TestUserPreferences.class,
    TestTags.class,
    TestNetworks.class,
    TestActivities.class,
    GroupsTest.class,
    TestPeople.class,
    TestSiteMembers.class,
    TestPersonSites.class,
    TestSiteMembershipRequests.class,
    TestFavourites.class,
    TestPublicApi128.class,
    TestPublicApiCaching.class
})
public class ApiTest
{
    @AfterClass
    public static void after() throws Exception
    {
//        EnterprisePublicApiTestFixture.cleanup();
    }
}
