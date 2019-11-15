package org.alfresco.rest.demo.workshop;

import org.alfresco.rest.RestTest;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.Test;

public class RestApiDemoTests extends RestTest
{    
    /*
     * Test steps:
     * 1. create a user
     * 2. create a site
     * 3. create a second user
     * 4. add the second user to site with a user role
     * 5. call rest api call " GET sites/{siteId}/members" with first user authenticated
     * Expected: the response contains the user added as a member to the site
     */
    @Test(groups = { "demo" })
    public void verifyGetSiteMembersRestApiCall() throws Exception
    {
        UserModel user = dataUser.createRandomTestUser();
        SiteModel site = dataSite.usingUser(user).createPublicRandomSite();
        UserModel member = dataUser.createRandomTestUser();
        dataUser.usingUser(user).addUserToSite(member, site, UserRole.SiteCollaborator);
        
        //add here code for step 5 

    }

    /*
     * Test steps:
     * 1. create a user
     * 2. create a site
     * 3. create a second user
     * 4. add the second user to site with a user role
     * 5. call rest api call " GET sites/{siteId}/members/{personId}" with first user authenticated
     * Expected: the response contains the user added as a member to the site
     */

    @Test(groups = { "demo" })
    public void verifyGetASiteMemberApiCall() throws Exception
    {
        UserModel user = dataUser.createRandomTestUser();
        SiteModel site = dataSite.usingUser(user).createPublicRandomSite();
        UserModel member = dataUser.createRandomTestUser();
        dataUser.usingUser(user).addUserToSite(member, site, UserRole.SiteCollaborator);

        //add here code for step 5
    }
}
