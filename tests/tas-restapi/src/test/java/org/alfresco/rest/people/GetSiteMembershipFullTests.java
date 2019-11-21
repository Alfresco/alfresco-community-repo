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

public class GetSiteMembershipFullTests extends RestTest
{
    UserModel regularUser, adminUser;
    SiteModel publicSite, moderatedSite;
    private DataUser.ListUserWithRoles publicSiteUsers;
    private RestSiteEntry restSiteEntry;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        regularUser = dataUser.createRandomTestUser();
        publicSite = dataSite.usingAdmin().createPublicRandomSite();
        moderatedSite = dataSite.usingAdmin().createModeratedRandomSite();

        publicSiteUsers = dataUser.addUsersWithRolesToSite(publicSite, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify that properties parameter is applied.")
    public void checkThatPropertiesParameterIsApplied() throws Exception
    {
        restSiteEntry = restClient.authenticateUser(regularUser).withCoreAPI().usingParams("properties=site,role,id")
            .usingUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteManager)).getSiteMembership(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteEntry.assertThat().field("role").is(UserRole.SiteManager)
            .and().field("id").is(publicSite.getId())
            .and().field("site").isNotEmpty()
            .and().field("guid").isNull();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify that regular user "
            + "is able to retrieve site membership information of admin.")
    public void regularUserGetsSiteMembershipForAdmin() throws Exception
    {
        restSiteEntry = restClient.authenticateUser(regularUser).withCoreAPI().usingUser(adminUser).getSiteMembership(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteEntry.assertThat().field("role").is(UserRole.SiteManager)
            .and().field("id").is(publicSite.getId())
            .and().field("site.id").is(publicSite.getId());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify that regular user "
            + "is able to retrieve site membership details for a Collaborator role.")
    public void getSiteMembershipDetailsForACollaboratorRole() throws Exception
    {
        restSiteEntry = restClient.authenticateUser(regularUser).withCoreAPI()
            .usingUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteCollaborator)).getSiteMembership(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteEntry.assertThat().field("role").is(UserRole.SiteCollaborator)
            .and().field("guid").is(publicSite.getGuid())
            .and().field("id").is(publicSite.getId())
            .and().field("site.visibility").is(publicSite.getVisibility().toString())
            .and().field("site.guid").is(publicSite.getGuid())
            .and().field("site.description").is(publicSite.getDescription())
            .and().field("site.id").is(publicSite.getId())
            .and().field("site.preset").is("site-dashboard")
            .and().field("site.title").is(publicSite.getTitle());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify that regular user "
            + "is able to retrieve site membership details for a Contributor role.")
    public void getSiteMembershipDetailsForAContributorRole() throws Exception
    {
        restSiteEntry = restClient.authenticateUser(regularUser).withCoreAPI()
            .usingUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteContributor)).getSiteMembership(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteEntry.assertThat().field("role").is(UserRole.SiteContributor)
            .and().field("id").is(publicSite.getId())
            .and().field("site").isNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify that regular user "
            + "is able to retrieve site membership details for a Consumer role.")
    public void getSiteMembershipDetailsForAConsumerRole() throws Exception
    {
        restSiteEntry = restClient.authenticateUser(regularUser).withCoreAPI()
            .usingUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteConsumer)).getSiteMembership(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteEntry.assertThat().field("role").is(UserRole.SiteConsumer)
            .and().field("id").is(publicSite.getId())
            .and().field("site").isNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "As regular user make a request to join a moderated site."
            + " The request is pending. Check membership details.")
    public void pendingRequestToASiteCheckMembershipDetails() throws Exception
    {
        restClient.authenticateUser(regularUser).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        restSiteEntry = restClient.authenticateUser(adminUser).withCoreAPI()
            .usingUser(regularUser).getSiteMembership(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
            .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, regularUser.getUsername(), moderatedSite.getTitle()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "As Collaborator user leave site and perform get call."
            + "Check default error model schema.")
    public void leaveSiteAndPerformGetCallCheckDefaultErrorModelSchema() throws Exception
    {
        UserModel leaveSiteUser = dataUser.createRandomTestUser();
        dataUser.addUserToSite(leaveSiteUser, publicSite, UserRole.SiteCollaborator);
        restClient.authenticateUser(leaveSiteUser).withCoreAPI().usingAuthUser().deleteSiteMember(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        
        restSiteEntry = restClient.authenticateUser(adminUser).withCoreAPI()
            .usingUser(leaveSiteUser).getSiteMembership(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
            .containsErrorKey(RestErrorModel.RELATIONSHIP_NOT_FOUND_ERRORKEY)
            .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, leaveSiteUser.getUsername(), publicSite.getTitle()))
            .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
            .stackTraceIs(RestErrorModel.STACKTRACE);
    }
}
