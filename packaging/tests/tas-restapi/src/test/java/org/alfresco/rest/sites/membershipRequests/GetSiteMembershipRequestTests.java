package org.alfresco.rest.sites.membershipRequests;

import org.alfresco.dataprep.SiteService;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestSiteMembershipRequestModel;
import org.alfresco.rest.model.RestTaskModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetSiteMembershipRequestTests extends RestTest
{
    private SiteModel publicSite, moderatedSite;
    private ListUserWithRoles usersWithRoles;
    private UserModel adminUser, siteCreator, newMember;
    private RestSiteMembershipRequestModel returnedModel;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        publicSite = dataSite.usingUser(adminUser).createPublicRandomSite();

        siteCreator = dataUser.createRandomTestUser();
        moderatedSite = dataSite.usingUser(siteCreator).createModeratedRandomSite();

        usersWithRoles = dataUser.addUsersWithRolesToSite(moderatedSite, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);

        newMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify site manager is able to retrieve site membership request")
    public void siteManagerIsAbleToRetrieveSiteMembershipRequest() throws Exception
    {
        returnedModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).withCoreAPI().usingUser(newMember).getSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedModel.assertThat().field("id").is(moderatedSite.getId())
                .and().field("message").is("Please accept me")
                .and().field("site.title").is(moderatedSite.getTitle())
                .and().field("site.visibility").is(SiteService.Visibility.MODERATED.toString())
                .and().field("site.guid").isNotEmpty()
                .and().field("site.description").is(moderatedSite.getDescription())
                .and().field("site.preset").is("site-dashboard");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify site collaborator is able to retrieve site membership request")
    public void siteCollaboratorIsNotAbleToRetrieveSiteMembershipRequest() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI().usingUser(newMember).getSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify site contributor is able to retrieve site membership request")
    public void siteContributorIsNotAbleToRetrieveSiteMembershipRequest() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).withCoreAPI().usingUser(newMember).getSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify site consumer is able to retrieve site membership request")
    public void siteConsumerIsNotAbleToRetrieveSiteMembershipRequest() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).withCoreAPI().usingUser(newMember).getSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify admin user is able to retrieve site membership request")
    public void adminIsAbleToRetrieveSiteMembershipRequest() throws Exception
    {
        returnedModel = restClient.authenticateUser(adminUser).withCoreAPI().usingUser(newMember).getSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedModel.assertThat().field("id").is(moderatedSite.getId())
                .and().field("message").is("Please accept me")
                .and().field("site.title").is(moderatedSite.getTitle())
                .and().field("site.visibility").is(SiteService.Visibility.MODERATED.toString())
                .and().field("site.guid").isNotEmpty()
                .and().field("site.description").is(moderatedSite.getDescription())
                .and().field("site.preset").is("site-dashboard");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY,
            description = "Verify user fails to get all site membership requests of a specific person with Rest API when the authentication fails (401)")
    public void unauthorizedUserFailsToGetSiteMembershipRequests() throws Exception
    {
        UserModel manager = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(siteCreator).addUserToSite(manager, moderatedSite, UserRole.SiteManager);
        manager.setPassword("newpassword");
        restClient.authenticateUser(manager).withCoreAPI().usingUser(newMember).getSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify a user gets all its own site membership requests using '-me-' with Rest API and response is successful (200)")
    public void usingMeGetSiteMembershipRequestsWithSuccess() throws Exception
    {
        restClient.authenticateUser(newMember).withCoreAPI().usingMe().getSiteMembershipRequest(moderatedSite).assertThat().field("id").is(moderatedSite.getId());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify site manager can't get site membership requests for inexistent user and response is not found (404)")
    public void siteManagerCantGetSiteMembershipRequestsInexistentUser() throws Exception
    {
        restClient.authenticateUser(newMember).withCoreAPI().usingUser(UserModel.getRandomUserModel()).getSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, ""));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify user can get site membership requests on site with no requests and response is successful (200)")
    public void userWithNoRequestsCantGetSiteMembershipRequests() throws Exception
    {
        UserModel noRequestUser = dataUser.createRandomTestUser();
        restClient.authenticateUser(noRequestUser).withCoreAPI().usingMe().getSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, noRequestUser.getUsername(), moderatedSite.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify site manager can't get site membership requests on public site and response is not found (404)")
    public void siteManagerCantGetPublicSiteMembershipRequests() throws Exception
    {
        UserModel publicUser = dataUser.createRandomTestUser();
        restClient.authenticateUser(publicUser).withCoreAPI().usingAuthUser().addSiteMembershipRequest(publicSite);
        restClient.authenticateUser(adminUser).withCoreAPI()
                .usingUser(publicUser).getSiteMembershipRequest(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, publicUser.getUsername(), publicSite.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify site manager can't get site membership requests on private site and response is not found (404)")
    public void siteManagerCantGetPrivateSiteMembershipRequests() throws Exception
    {
        UserModel privateUser = dataUser.createRandomTestUser();
        SiteModel privateSite = dataSite.usingUser(siteCreator).createPrivateRandomSite();
        restClient.authenticateUser(privateUser).withCoreAPI().usingAuthUser().addSiteMembershipRequest(privateSite);
        restClient.authenticateUser(siteCreator).withCoreAPI().usingUser(privateUser).getSiteMembershipRequest(privateSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, privateUser.getUsername(), privateSite.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify site manager can't get site membership requests  for inexistent site and response is not found (404)")
    public void siteManagerCantGetSiteMembershipRequestsForInexistentSite() throws Exception
    {
        SiteModel inexistentSite = SiteModel.getRandomSiteModel();
        restClient.authenticateUser(siteCreator).withCoreAPI().usingMe().getSiteMembershipRequest(inexistentSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, siteCreator.getUsername(), inexistentSite.getId()));
    }

    @Bug(id = "ACE-2413")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify if person ID field is empty user can't get site membership requests on moderated site and response is not found (400)")
    public void emptyPersonIdCantGetModeratedSiteMembershipRequests() throws Exception
    {
        UserModel emptyUser = new UserModel("", "password");
        restClient.authenticateUser(siteCreator).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);
        restClient.authenticateUser(siteCreator).withCoreAPI().usingUser(emptyUser).getSiteMembershipRequest(moderatedSite);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError()
                .containsErrorKey(RestErrorModel.API_DEFAULT_ERRORKEY)
                .containsSummary(String.format("The entity with id: personId is null. was not found"))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Approve site membership request then verify get site membership requests - response is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void approveRequestThenGetSiteMembershipRequest() throws Exception
    {
        UserModel userWithApprovedRequests = dataUser.createRandomTestUser();
        restClient.authenticateUser(userWithApprovedRequests).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        RestTaskModel taskModel = restClient.authenticateUser(userWithApprovedRequests).withWorkflowAPI().getTasks().getTaskModelByDescription(moderatedSite);
        workflow.approveSiteMembershipRequest(siteCreator.getUsername(), siteCreator.getPassword(), taskModel.getId(), true, "Approve");

        returnedModel = restClient.authenticateUser(userWithApprovedRequests).withCoreAPI().usingMe().getSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, userWithApprovedRequests.getUsername(), moderatedSite.getId()))
                .containsErrorKey(RestErrorModel.RELATIONSHIP_NOT_FOUND_ERRORKEY)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Reject site membership request then verify get site membership requests - response is 404")
    public void rejectRequestThenGetSiteMembershipRequest() throws Exception
    {
        UserModel userWithRejectedRequests = dataUser.createRandomTestUser();
        restClient.authenticateUser(userWithRejectedRequests).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        RestTaskModel taskModel = restClient.authenticateUser(userWithRejectedRequests).withWorkflowAPI().getTasks().getTaskModelByDescription(moderatedSite);
        workflow.approveSiteMembershipRequest(siteCreator.getUsername(), siteCreator.getPassword(), taskModel.getId(), false, "Rejected");

        returnedModel = restClient.authenticateUser(userWithRejectedRequests).withCoreAPI().usingMe().getSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, userWithRejectedRequests.getUsername(), moderatedSite.getId()))
                .containsErrorKey(RestErrorModel.RELATIONSHIP_NOT_FOUND_ERRORKEY)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify entry details for get site favorite response with Rest API")
    public void checkResponseSchemaForGetSiteMembershipRequest() throws Exception
    {
        dataUser.usingUser(siteCreator).addUserToSite(newMember, moderatedSite, UserRole.SiteContributor);
        returnedModel = restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser().getSiteMembershipRequest(moderatedSite);

        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedModel.assertThat().field("id").is(moderatedSite.getId())
                .and().field("message").is("Please accept me")
                .and().field("site.title").is(moderatedSite.getTitle())
                .and().field("site.visibility").is(SiteService.Visibility.MODERATED.toString())
                .and().field("site.guid").isNotEmpty()
                .and().field("site.description").is(moderatedSite.getDescription())
                .and().field("site.preset").is("site-dashboard")
                .and().field("site.role").is("SiteContributor");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify that getFavoriteSites request applies valid properties param")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void getSiteMembershipRequestWithValidPropertiesParam() throws Exception
    {
        RestSiteMembershipRequestModel returnedModel = restClient.authenticateUser(newMember).withParams("properties=message").withCoreAPI().usingAuthUser()
                .getSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedModel.assertThat().fieldsCount().is(1).assertThat().field("message").isNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify that getFavoriteSites request returns status 200 when using valid parameters")
    public void getSiteMembershipRequestUsingParameters() throws Exception
    {
        RestSiteMembershipRequestModel returnedModel = restClient.withParams("message=Please accept me")
                .authenticateUser(newMember).withCoreAPI().usingMe()
                .getSiteMembershipRequest(moderatedSite);

        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedModel.assertThat().field("id").is(moderatedSite.getId()).and().field("site.title").is(moderatedSite.getTitle());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify user doesn't have permission to get site membership request of admin without membership request with Rest API and status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void userIsNotAbleToGetSiteMembershipRequestOfAdminWithoutRequest() throws Exception
    {
        returnedModel = restClient.authenticateUser(newMember).withCoreAPI().usingUser(adminUser).getSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsErrorKey(RestErrorModel.RELATIONSHIP_NOT_FOUND_ERRORKEY)
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, adminUser.getUsername(), moderatedSite.getId()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER).stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify user doesn't have permission to get site membership request of admin with requests with Rest API and status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void userIsNotAbleToGetSiteMembershipRequestOfAdminWithRequest() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(adminUser).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);

        returnedModel = restClient.authenticateUser(newMember).withCoreAPI().usingUser(adminUser).getSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsErrorKey(RestErrorModel.RELATIONSHIP_NOT_FOUND_ERRORKEY)
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, adminUser.getUsername(), moderatedSite.getId()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin user doesn't have permission to get site membership request of another user with Rest API and status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void userIsNotAbleToGetSiteMembershipRequestOfAnotherUser() throws Exception
    {
        UserModel userCollaborator = dataUser.createRandomTestUser();
        UserModel userConsumer = dataUser.createRandomTestUser();
        restClient.authenticateUser(userConsumer).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);
        dataUser.usingUser(siteCreator).addUserToSite(userConsumer, moderatedSite, UserRole.SiteConsumer);
        restClient.authenticateUser(userCollaborator).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);
        dataUser.usingUser(siteCreator).addUserToSite(userCollaborator, moderatedSite, UserRole.SiteCollaborator);

        returnedModel = restClient.authenticateUser(userCollaborator).withCoreAPI().usingUser(userConsumer).getSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsErrorKey(RestErrorModel.RELATIONSHIP_NOT_FOUND_ERRORKEY)
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, userConsumer.getUsername(), moderatedSite.getId()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

}