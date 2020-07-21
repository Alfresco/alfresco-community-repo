package org.alfresco.rest.sites.membershipRequests;

import org.alfresco.dataprep.SiteService;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestSiteEntry;
import org.alfresco.rest.model.RestSiteMembershipRequestModel;
import org.alfresco.rest.model.RestSiteMembershipRequestModelsCollection;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.exception.DataPreparationException;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AddSiteMembershipRequestTests extends RestTest
{
    private SiteModel publicSite, anotherPublicSite, moderatedSite, privateSite;
    private DataUser.ListUserWithRoles usersWithRoles;
    private UserModel adminUser, siteManager, regularUser, newMember;
    private RestSiteMembershipRequestModel siteMembershipRequest;
    private RestSiteMembershipRequestModelsCollection siteMembershipRequests;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws DataPreparationException
    {
        adminUser = dataUser.getAdminUser();
        publicSite = dataSite.usingUser(adminUser).createPublicRandomSite();
        usersWithRoles = dataUser.addUsersWithRolesToSite(publicSite, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);

        siteManager = dataUser.createRandomTestUser();
        anotherPublicSite = dataSite.usingUser(siteManager).createPublicRandomSite();

        moderatedSite = dataSite.usingUser(adminUser).createModeratedRandomSite();
        privateSite = dataSite.usingUser(adminUser).createPrivateRandomSite();

        newMember = dataUser.createRandomTestUser();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY,
            description = "Verify site manager is able to create new site membership request for himself and status code is 201")
    public void managerCreatesNewSiteMembershipRequestForSelf() throws Exception
    {
        siteMembershipRequest = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).withCoreAPI().usingAuthUser().addSiteMembershipRequest(anotherPublicSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        siteMembershipRequest.assertThat().field("id").is(anotherPublicSite.getId())
                    .assertThat().field("site").isNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify site contributor is able to create new site membership request for himself and status code is 201")
    public void contributorCreatesNewSiteMembershipRequestForSelf() throws Exception
    {
        siteMembershipRequest = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).withCoreAPI().usingAuthUser().addSiteMembershipRequest(anotherPublicSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        siteMembershipRequest.assertThat().field("id").is(anotherPublicSite.getId())
                    .assertThat().field("site").isNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to create new site membership request for himself and status code is 201")
    public void collaboratorCreatesNewSiteMembershipRequestForSelf() throws Exception
    {
        siteMembershipRequest = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI().usingAuthUser().addSiteMembershipRequest(anotherPublicSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        siteMembershipRequest.assertThat().field("id").is(anotherPublicSite.getId())
                    .assertThat().field("site").isNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify site consumer is able to create new site membership request for himself and status code is 201")
    public void consumerCreatesNewSiteMembershipRequestForSelf() throws Exception
    {
        siteMembershipRequest = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).withCoreAPI().usingAuthUser().addSiteMembershipRequest(anotherPublicSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        siteMembershipRequest.assertThat().field("id").is(anotherPublicSite.getId())
                    .assertThat().field("site").isNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin is able to create new site membership request for himself and status code is 201")
    public void adminCreatesNewSiteMembershipRequestForSelf() throws Exception
    {
        siteMembershipRequest = restClient.authenticateUser(adminUser).withCoreAPI().usingAuthUser().addSiteMembershipRequest(anotherPublicSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        siteMembershipRequest.assertThat().field("id").is(anotherPublicSite.getId())
                    .assertThat().field("site").isNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY,
            description = "Verify unauthenticated user is not able to create new site membership request")
    public void unauthenticatedUserIsNotAbleToCreateSiteMembershipRequest() throws Exception
    {
        restClient.authenticateUser(new UserModel("random user", "random password")).withCoreAPI().usingAuthUser().addSiteMembershipRequest(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify addSiteMembershipRequest Rest API status code is 400 for a user that has already been invited")
    public void addSiteMembershipRequestStatusCodeIs400ReceivedForAUserThatIsAlreadyInvited() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        dataUser.addUserToSite(newMember, moderatedSite, UserRole.SiteContributor);
        restClient.authenticateUser(newMember).withCoreAPI().usingMe().addSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
        restClient.assertLastError().containsSummary(String.format(RestErrorModel.ALREADY_Site_MEMBER, newMember.getUsername(), moderatedSite.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify addSiteMembershipRequest Rest API status code is 404 for a user that does not exist")
    public void addSiteMembershipRequestStatusCodeIs404ReceivedForAUserThatDoesNotExist() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(newMember).withCoreAPI().usingUser(new UserModel("invalidUser", "password")).addSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
        restClient.assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "invalidUser"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify addSiteMembershipRequest Rest API status code is 404 for a site that does not exist")
    public void addSiteMembershipRequestStatusCodeIs404ReceivedForASiteThatDoesNotExist() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(newMember).withCoreAPI().usingMe().addSiteMembershipRequest(new SiteModel("invalidSiteID"));
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
        restClient.assertLastError().containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, newMember.getUsername(), "invalidSiteID"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify addSiteMembershipRequest Rest API status code is 400 for empty request body")
    public void addSiteMembershipRequestStatusCodeIs400ForEmptyRequestBody() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(newMember).withCoreAPI().usingMe().addSiteMembershipRequest("");
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.NO_CONTENT, "No content to map due to end-of-input"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify addSiteMembershipRequest Rest API status code is 201 for request with empty message")
    public void addSiteMembershipRequestStatusCodeIs201ForRequestWithEmptyMessage() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        siteMembershipRequest = restClient.authenticateUser(newMember).withCoreAPI().usingMe().addSiteMembershipRequest("", moderatedSite, "New request");
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        siteMembershipRequest.assertThat().field("id").is(moderatedSite.getId()).assertThat().field("site").isNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify addSiteMembershipRequest Rest API status code is 201 for request with empty title")
    public void addSiteMembershipRequestStatusCodeIs201ForRequestWithEmptyTitle() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        siteMembershipRequest = restClient.authenticateUser(newMember).withCoreAPI().usingMe().addSiteMembershipRequest("Please accept me", moderatedSite, "");
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        siteMembershipRequest.assertThat().field("id").is(moderatedSite.getId()).assertThat().field("site").isNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify site manager is not able to create new site membership request for other user")
    public void managerIsNotAbleToCreateSiteMembershipRequestForOtherUser() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).withCoreAPI().usingUser(newMember)
                .addSiteMembershipRequest(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, newMember.getUsername()))
                .statusCodeIs(HttpStatus.NOT_FOUND)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE)
                .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify site collaborator is not able to create new site membership request for other user")
    public void collaboratorIsNotAbleToCreateSiteMembershipRequestForOtherUser() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI().usingUser(newMember)
                .addSiteMembershipRequest(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, newMember.getUsername()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify site contributor is not able to create new site membership request for other user")
    public void contributorIsNotAbleToCreateSiteMembershipRequestForOtherUser() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).withCoreAPI().usingUser(newMember)
                .addSiteMembershipRequest(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, newMember.getUsername()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify site consumer is not able to create new site membership request for other user")
    public void consumerIsNotAbleToCreateSiteMembershipRequestForOtherUser() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).withCoreAPI().usingUser(newMember)
                .addSiteMembershipRequest(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, newMember.getUsername()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify admin user is not able to create new site membership request for other user")
    public void adminIsNotAbleToCreateSiteMembershipRequestForOtherUser() throws Exception
    {
        restClient.authenticateUser(adminUser).withCoreAPI().usingUser(newMember).addSiteMembershipRequest(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, newMember.getUsername()));

        restClient.withCoreAPI().usingUser(newMember).addSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, newMember.getUsername()));

        restClient.withCoreAPI().usingUser(newMember).addSiteMembershipRequest(privateSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, newMember.getUsername()));
    }

    @Bug(id="ACE-2413")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify addSiteMembershipRequest Rest API status code is 400 for an invalid user id")
    public void addSiteMembershipRequestReturns400ForEmptyUserId() throws Exception
    {
        restClient.authenticateUser(adminUser).withCoreAPI()
                .usingUser(new UserModel("", "password")).addSiteMembershipRequest(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify regular user is able to create new public site membership request for himself. Check that user joins immediately as consumer.")
    public void userIsAbleToRequestMembershipOfPublicSite() throws Exception
    {
        regularUser = dataUser.createRandomTestUser();
        siteMembershipRequest = restClient.authenticateUser(regularUser).withCoreAPI().usingAuthUser().addSiteMembershipRequest(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        siteMembershipRequest.assertThat().field("id").is(publicSite.getId())
                .assertThat().field("site").isNotEmpty();
        RestSiteEntry siteEntry = restClient.withCoreAPI().usingAuthUser().getSiteMembership(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteEntry.assertThat().field("role").is(UserRole.SiteConsumer)
                .and().field("id").is(publicSite.getId());
        siteMembershipRequests = restClient.withCoreAPI().usingAuthUser().getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembershipRequests.assertThat().entriesListIsEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify regular user is able to create new moderated site membership request for himself. Check that the request is added to the site membership request list.")
    public void userIsAbleToRequestMembershipOfModeratedSite() throws Exception
    {
        regularUser = dataUser.createRandomTestUser();
        siteMembershipRequest = restClient.authenticateUser(regularUser).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        siteMembershipRequest.assertThat().field("id").is(moderatedSite.getId())
                .assertThat().field("message").is("Please accept me")
                .assertThat().field("site").isNotEmpty();
        siteMembershipRequest.getSite()
                .assertThat().field("visibility").is(moderatedSite.getVisibility())
                .assertThat().field("guid").is(moderatedSite.getGuid())
                .assertThat().field("description").is(moderatedSite.getDescription())
                .assertThat().field("id").is(moderatedSite.getId())
                .assertThat().field("title").is(moderatedSite.getTitle());

        siteMembershipRequests = restClient.withCoreAPI().usingAuthUser().getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembershipRequests.assertThat().entriesListContains("id", moderatedSite.getId())
                .assertThat().entriesListCountIs(1);

    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify regular user is not able to request membership of a private site.")
    public void userIsNotAbleToRequestMembershipOfPrivateSite() throws Exception
    {
        regularUser = dataUser.createRandomTestUser();
        restClient.authenticateUser(regularUser).withCoreAPI().usingAuthUser().addSiteMembershipRequest(privateSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, regularUser.getUsername(), privateSite.getId()));
        siteMembershipRequests = restClient.withCoreAPI().usingAuthUser().getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembershipRequests.assertThat().entriesListIsEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify regular user is not able to create new site membership request for other user")
    public void regularUserIsNotAbleToCreateSiteMembershipRequestForOtherUser() throws Exception
    {
        regularUser = dataUser.createRandomTestUser();
        restClient.authenticateUser(regularUser).withCoreAPI().usingUser(newMember)
                .addSiteMembershipRequest(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, newMember.getUsername()));

        restClient.withCoreAPI().usingUser(newMember).addSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, newMember.getUsername()));

        restClient.withCoreAPI().usingUser(newMember).addSiteMembershipRequest(privateSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, newMember.getUsername()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify create public site membership request returns status code 400 when the request is made twice")
    public void userRequestsTwiceMembershipOfPublicSite() throws Exception
    {
        regularUser = dataUser.createRandomTestUser();
        siteMembershipRequest = restClient.authenticateUser(regularUser).withCoreAPI().usingAuthUser().addSiteMembershipRequest(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        siteMembershipRequest.assertThat().field("id").is(publicSite.getId())
                .assertThat().field("site").isNotEmpty();
        restClient.withCoreAPI().usingAuthUser().addSiteMembershipRequest(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
        restClient.assertLastError().containsSummary(String.format(RestErrorModel.ALREADY_Site_MEMBER, regularUser.getUsername(), publicSite.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify create moderated site membership request returns status code 400 when the request is made twice")
    public void userRequestsTwiceMembershipOfModeratedSite() throws Exception
    {
        regularUser = dataUser.createRandomTestUser();
        siteMembershipRequest = restClient.authenticateUser(regularUser).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        siteMembershipRequest.assertThat().field("id").is(moderatedSite.getId())
                .assertThat().field("site").isNotEmpty();
        restClient.withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
        restClient.assertLastError().containsSummary(String.format(RestErrorModel.ALREADY_INVITED, regularUser.getUsername(), moderatedSite.getId()));

        siteMembershipRequests = restClient.withCoreAPI().usingAuthUser().getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembershipRequests.assertThat().entriesListContains("id", moderatedSite.getId())
                .assertThat().entriesListCountIs(1);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify create private site membership request returns status code 404 when request is made with the user who created the site")
    public void siteCreatorRequestsMembershipOfHisPrivateSite() throws Exception
    {
        restClient.authenticateUser(adminUser).withCoreAPI().usingAuthUser().addSiteMembershipRequest(privateSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, adminUser.getUsername(), privateSite.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify create moderated site membership request returns status code 400 when request is made with the user who created the site")
    public void siteCreatorRequestsMembershipOfHisModeratedSite() throws Exception
    {
        restClient.authenticateUser(adminUser).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.ALREADY_Site_MEMBER, adminUser.getUsername(), moderatedSite.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin is able to create new moderated site membership request for himself. Check that the request is added to the site membership request list.")
    public void adminIsAbleToRequestMembershipOfModeratedSite() throws Exception
    {
        regularUser = dataUser.createRandomTestUser();
        SiteModel anotherModeratedSite = dataSite.usingUser(regularUser).createModeratedRandomSite();
        siteMembershipRequest = restClient.authenticateUser(adminUser).withCoreAPI().usingAuthUser().addSiteMembershipRequest(anotherModeratedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        siteMembershipRequest.assertThat().field("id").is(anotherModeratedSite.getId())
                .assertThat().field("site").isNotEmpty();
        siteMembershipRequests = restClient.withCoreAPI().usingAuthUser().getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembershipRequests.assertThat().entriesListContains("id", anotherModeratedSite.getId());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify site membership request is automatically rejected when a site is switched from moderated to private")
    public void siteMembershipRequestIsRejectedWhenSiteIsSwitchedFromModeratedToPrivate() throws Exception
    {
        regularUser = dataUser.createRandomTestUser();
        SiteModel moderatedThenPrivateSite = dataSite.usingUser(adminUser).createModeratedRandomSite();
        siteMembershipRequest = restClient.authenticateUser(regularUser).withCoreAPI().usingMe().addSiteMembershipRequest(moderatedThenPrivateSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        siteMembershipRequest.assertThat().field("id").is(moderatedThenPrivateSite.getId())
                .assertThat().field("site").isNotEmpty();

        dataSite.usingUser(adminUser).updateSiteVisibility(moderatedThenPrivateSite, SiteService.Visibility.PRIVATE);

        siteMembershipRequests = restClient.withCoreAPI().usingAuthUser().getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembershipRequests.assertThat().entriesListIsEmpty();
    }
}