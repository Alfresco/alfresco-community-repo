package org.alfresco.rest.api.tests;

import org.alfresco.rest.DeletedNodesTest;
import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Public API tests.
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
    QueriesApiTest.class,
    RenditionsTest.class,
    SharedLinkApiTest.class,
    ActivitiesPostingTest.class,
    DeletedNodesTest.class,
    AuthenticationsTest.class,
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
    TestRemovePermissions.class,
    TestPublicApi128.class,
    TestPublicApiCaching.class,
    ModulePackagesApiTest.class
})
public class ApiTest
{
    @AfterClass
    public static void after() throws Exception
    {
//        EnterprisePublicApiTestFixture.cleanup();
    }
}
