package org.alfresco.rest.api.tests;

import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Public API tests.
 * 
 * @author steveglover
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestSites.class,
    TestNodeComments.class,
    TestCMIS.class,
    TestFavouriteSites.class,
    TestSiteContainers.class,
    TestNodeRatings.class,
    TestUserPreferences.class,
    TestTags.class,
    TestNetworks.class,
    TestActivities.class,
    TestPeople.class,
    TestSiteMembers.class,
    TestPersonSites.class,
    TestSiteMembershipRequests.class,
    TestFavourites.class,
    TestPublicApi128.class
})
public class ApiTest
{
    @AfterClass
    public static void after() throws Exception
    {
//        EnterprisePublicApiTestFixture.cleanup();
    }
}
