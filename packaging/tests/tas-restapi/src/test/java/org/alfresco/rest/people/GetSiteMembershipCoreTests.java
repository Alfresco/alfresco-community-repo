package org.alfresco.rest.people;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestSiteEntry;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 11/28/2016.
 */

public class GetSiteMembershipCoreTests extends RestTest
{
    UserModel regularUser, publicSiteManager, privateSiteManager, moderatedSiteManager, adminUser;
    SiteModel publicSite, privateSite, moderatedSite;
    private DataUser.ListUserWithRoles publicSiteUsers;
    private RestSiteEntry restSiteEntry;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        regularUser = dataUser.createRandomTestUser();
        privateSiteManager = dataUser.createRandomTestUser();
        moderatedSiteManager = dataUser.createRandomTestUser();
        publicSite = dataSite.usingAdmin().createPublicRandomSite();
        privateSite = dataSite.usingAdmin().createPrivateRandomSite();
        moderatedSite = dataSite.usingAdmin().createModeratedRandomSite();

        publicSiteUsers = dataUser.addUsersWithRolesToSite(publicSite, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);
        publicSiteManager = publicSiteUsers.getOneUserWithRole(UserRole.SiteManager);
        dataUser.usingAdmin().addUserToSite(privateSiteManager, privateSite, UserRole.SiteManager);
        dataUser.usingAdmin().addUserToSite(moderatedSiteManager, moderatedSite, UserRole.SiteManager);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
//    @Bug(id = "REPO-1642", description = "reproduced on 5.2.1 only, it works on 5.2.0")
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify get site membership for a site returns status 404 when personId does not exist.")
    public void getSiteMembershipUsingNonExistentPersonId() throws Exception
    {
        UserModel someUser = new UserModel("someUser", DataUser.PASSWORD);

        restClient.authenticateUser(regularUser).withCoreAPI().usingUser(someUser).getSiteMembership(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "someUser"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify get site membership for a site returns status 404 when siteId does not exist.")
    public void getSiteMembershipUsingNonExistentSiteId() throws Exception
    {
        SiteModel someSite = new SiteModel("someSite");

        restClient.authenticateUser(regularUser).withCoreAPI().usingAuthUser().getSiteMembership(someSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, regularUser.getUsername(), "someSite"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify get site membership for a site returns status 404 when personId is not a site member.")
    public void getSiteMembershipForPersonThatIsNotSiteMember() throws Exception
    {
        restClient.authenticateUser(publicSiteManager).withCoreAPI().usingUser(regularUser).getSiteMembership(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, regularUser.getUsername(), publicSite.getTitle()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify regular user is not able to retrieve site membership information of a private site member.")
    public void regularUserIsNotAbleToRetrieveSiteMembershipForPrivateSiteManager() throws Exception
    {
        restClient.authenticateUser(regularUser).withCoreAPI().usingUser(privateSiteManager).getSiteMembership(privateSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, privateSiteManager.getUsername(), privateSite.getTitle()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify regular user is able to retrieve site membership information of a moderated site member.")
    public void regularUserGetsSiteMembershipForModeratedSiteMember() throws Exception
    {
        restSiteEntry = restClient.authenticateUser(regularUser).withCoreAPI().usingUser(moderatedSiteManager).getSiteMembership(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteEntry.assertThat().field("role").is(UserRole.SiteManager).and().field("id").is(moderatedSite.getId()).and().field("site").isNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify if Admin user is able to retrieve site membership information of him.")
    public void adminGetsSiteMembershipForHim() throws Exception
    {
        restSiteEntry = restClient.authenticateUser(adminUser).withCoreAPI().usingUser(adminUser).getSiteMembership(privateSite);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteEntry.assertThat().field("role").is(UserRole.SiteManager).and().field("id").is(privateSite.getId()).and().field("site").isNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify if Admin user is able to retrieve site membership information of him.")
    public void getSiteMembershipAfterRemovingASiteMember() throws Exception
    {
        UserModel publicSiteConsumer = publicSiteUsers.getOneUserWithRole(UserRole.SiteConsumer);
        restClient.authenticateUser(publicSiteManager).withCoreAPI().usingSite(publicSite).deleteSiteMember(publicSiteConsumer);
        restClient.authenticateUser(publicSiteManager).withCoreAPI().usingUser(publicSiteConsumer).getSiteMembership(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, publicSiteConsumer.getUsername(), publicSite.getTitle()));
    }
}
