package org.alfresco.rest.sites.membershipRequests;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestSiteMembershipRequestModel;
import org.alfresco.rest.model.RestTaskModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.exception.DataPreparationException;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class UpdateSiteMembershipRequestTests extends RestTest
{
    private SiteModel moderatedSite;
    private ListUserWithRoles usersWithRoles;
    private UserModel adminUser, managerUser;
    private String updatedMessage, moderatedSiteId;
    private RestSiteMembershipRequestModel requestUpdateModel, requestGetModel;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws DataPreparationException
    {
        adminUser = dataUser.getAdminUser();
        managerUser = dataUser.createRandomTestUser();
        moderatedSite = dataSite.usingUser(managerUser).createModeratedRandomSite();

        usersWithRoles = dataUser.addUsersWithRolesToSite(moderatedSite, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);
        updatedMessage = "Please review my request";
        moderatedSiteId = moderatedSite.getId();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify user is able to update its own site membership request")
    public void userIsAbleToUpdateItsOwnSiteMembershipRequest() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);
        requestUpdateModel = restClient.withCoreAPI().usingUser(newMember).updateSiteMembershipRequest(moderatedSite, updatedMessage);
        restClient.assertStatusCodeIs(HttpStatus.OK);

        requestUpdateModel.assertThat().field("id").is(moderatedSite.getId())
                .assertThat().field("message").is(updatedMessage)
                .assertThat().field("site").isNotEmpty()
                .assertThat().field("modifiedAt").isNotEmpty()
                .assertThat().field("createdAt").isNotEmpty();
        requestUpdateModel.getSite().assertThat().field("visibility").is(moderatedSite.getVisibility())
                .assertThat().field("guid").is(moderatedSite.getGuid())
                .assertThat().field("description").is(moderatedSite.getDescription())
                .assertThat().field("id").is(moderatedSite.getId())
                .assertThat().field("preset").is("site-dashboard")
                .assertThat().field("title").is(moderatedSite.getTitle());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify site manager is not able to update membership request of another user")
//    @Bug(id = "MNT-16919", description = "Not a bug, The presence of the personId in the URL does not mean it should be possible to act on any other users behalf.")
    public void siteManagerIsNotAbleToUpdateSiteMembershipRequestOfAnotherUser() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();

        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).withCoreAPI().usingUser(newMember)
                .updateSiteMembershipRequest(moderatedSite, updatedMessage);

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, newMember.getUsername()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify site collaborator is not able to update membership request of another user")
    //    @Bug(id = "MNT-16919", description = "Not a bug, The presence of the personId in the URL does not mean it should be possible to act on any other users behalf.")
    public void siteCollaboratorIsNotAbleToUpdateSiteMembershipRequestOfAnotherUser() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();

        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI().usingUser(newMember)
                .updateSiteMembershipRequest(moderatedSite, updatedMessage);

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, newMember.getUsername()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify site contributor is not able to update membership request of another user")
    //    @Bug(id = "MNT-16919", description = "Not a bug, The presence of the personId in the URL does not mean it should be possible to act on any other users behalf.")
    public void siteContributorIsNotAbleToUpdateSiteMembershipRequestOfAnotherUser() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();

        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).withCoreAPI().usingUser(newMember)
                .updateSiteMembershipRequest(moderatedSite, updatedMessage);

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, newMember.getUsername()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify site consumer is not able to update membership request of another user")
    //    @Bug(id = "MNT-16919", description = "Not a bug, The presence of the personId in the URL does not mean it should be possible to act on any other users behalf.")
    public void siteConsumerIsNotAbleToUpdateSiteMembershipRequestOfAnotherUser() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();

        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).withCoreAPI().usingUser(newMember)
                .updateSiteMembershipRequest(moderatedSite, updatedMessage);

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, newMember.getUsername()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify one user is not able to update membership request of another user")
    //    @Bug(id = "MNT-16919", description = "Not a bug, The presence of the personId in the URL does not mean it should be possible to act on any other users behalf.")
    public void regularUserIsNotAbleToUpdateSiteMembershipRequestOfAnotherUser() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        UserModel otherMember = dataUser.createRandomTestUser();

        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);

        restClient.authenticateUser(otherMember).withCoreAPI().usingUser(newMember).updateSiteMembershipRequest(moderatedSite, updatedMessage);

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, newMember.getUsername()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify admin is not able to update membership request of another user")
    //    @Bug(id = "MNT-16919", description = "Not a bug, The presence of the personId in the URL does not mean it should be possible to act on any other users behalf.")
    public void adminIsNotAbleToUpdateSiteMembershipRequestOfAnotherUser() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();

        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);

        restClient.authenticateUser(adminUser).withCoreAPI().usingUser(newMember).updateSiteMembershipRequest(moderatedSite, updatedMessage);

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, newMember.getUsername()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify unauthorized user is not able to update user site membership request")
    public void unauthorizedUserIsNotAbleToUpdateSiteMembershipRequest() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);
        newMember.setPassword("fakePass");
        restClient.withCoreAPI().usingUser(newMember).updateSiteMembershipRequest(moderatedSite, updatedMessage);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify user is able to update its own site membership request using -me-")
    public void usingMeUpdateSiteMembershipRequest() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);
        requestUpdateModel = restClient.withCoreAPI().usingMe().updateSiteMembershipRequest(moderatedSite, updatedMessage);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        requestUpdateModel.assertMembershipRequestMessageIs(updatedMessage).and().field("id").is(moderatedSite.getId()).and().field("modifiedAt").isNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify inexistent user is not able to update its own site membership request")
    public void inexistentUserCannotUpdateSiteMembershipRequest() throws Exception
    {
        UserModel inexistentUser = UserModel.getRandomUserModel();
        restClient.authenticateUser(managerUser).withCoreAPI().usingUser(inexistentUser).updateSiteMembershipRequest(moderatedSite, updatedMessage);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, inexistentUser.getUsername()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify user is not able to update its own site membership request for inexistent site")
    public void userCannotUpdateSiteMembershipRequestForInexistentSite() throws Exception
    {
        SiteModel randomSite = SiteModel.getRandomSiteModel();
        UserModel newMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(newMember).withCoreAPI().usingMe().updateSiteMembershipRequest(randomSite, updatedMessage);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, newMember.getUsername(), randomSite.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify user is able not to update its own site membership request for public site")
    public void userCannotUpdateSiteMembershipRequestForPublicSite() throws Exception
    {
        SiteModel publicSite = dataSite.usingUser(managerUser).createPublicRandomSite();
        UserModel newMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(publicSite);
        restClient.withCoreAPI().usingMe().updateSiteMembershipRequest(publicSite, updatedMessage);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, newMember.getUsername(), publicSite.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify user is not able to update its own site membership request for private site")
    public void userCannotUpdateSiteMembershipRequestForPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingUser(managerUser).createPrivateRandomSite();
        UserModel newMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(privateSite);
        restClient.withCoreAPI().usingMe().updateSiteMembershipRequest(privateSite, updatedMessage);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, newMember.getUsername(), privateSite.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify user is able to update its own site membership request with initial message")
    public void userCanUpdateSiteMembershipRequestWithInitialMessage() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(updatedMessage, moderatedSite, "Accept me");
        requestUpdateModel = restClient.withCoreAPI().usingMe().updateSiteMembershipRequest(moderatedSite, updatedMessage);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        requestUpdateModel.assertMembershipRequestMessageIs(updatedMessage).and().field("id").is(moderatedSite.getId()).and().field("modifiedAt").isNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify user is able to update its own site membership request with different message")
    public void userCanUpdateSiteMembershipRequestWithDifferentMessage() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest("", moderatedSite, "Accept me");
        requestUpdateModel = restClient.withCoreAPI().usingMe().updateSiteMembershipRequest(moderatedSite, updatedMessage);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        requestUpdateModel.assertMembershipRequestMessageIs(updatedMessage).and().field("id").is(moderatedSite.getId()).and().field("modifiedAt").isNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify modifiedAt field for update siteMembership request call")
    public void verifyModifiedAtForUpdateSiteMembershipRequest() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);
        String firstModifiedAt = restClient.withCoreAPI().usingMe().updateSiteMembershipRequest(moderatedSite, "first message").getModifiedAt();
        String secondModifiedAt = restClient.withCoreAPI().usingMe().updateSiteMembershipRequest(moderatedSite, "second message").getModifiedAt();
        Assert.assertNotEquals(firstModifiedAt, secondModifiedAt);
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify user is not able to update membership request of admin")
    public void userIsNotAbleToUpdateSiteMembershipRequestOfAdmin() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(adminUser).withCoreAPI().usingAuthUser()
                .addSiteMembershipRequest(moderatedSite);

        restClient.authenticateUser(newMember).withCoreAPI().usingUser(adminUser)
                .updateSiteMembershipRequest(moderatedSite, updatedMessage);

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsSummary(String.format("The entity with id: " + RestErrorModel.ENTITY_NOT_FOUND, adminUser.getUsername()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is able to update site membership request")
    public void updateSiteMembershipRequestAfterUserIsAddedAsMember() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser()
                .addSiteMembershipRequest(moderatedSite);
        dataUser.usingUser(managerUser).addUserToSite(newMember, moderatedSite, UserRole.SiteManager);
        requestUpdateModel = restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser()
                .updateSiteMembershipRequest(moderatedSite, updatedMessage);

        restClient.assertStatusCodeIs(HttpStatus.OK);
        requestUpdateModel.assertThat().field("id").is(moderatedSite.getId())
                .assertThat().field("message").is(updatedMessage)
                .assertThat().field("modifiedAt").isNotEmpty()
                .assertThat().field("createdAt").isNotEmpty();
        requestUpdateModel.getSite().assertThat().field("role").is(UserRole.SiteManager)
                .assertThat().field("visibility").is(moderatedSite.getVisibility())
                .assertThat().field("guid").is(moderatedSite.getGuid())
                .assertThat().field("description").is(moderatedSite.getDescription())
                .assertThat().field("id").is(moderatedSite.getId())
                .assertThat().field("preset").is("site-dashboard")
                .assertThat().field("title").is(moderatedSite.getTitle());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin is able to update site membership request")
    public void adminIsAbleToUpdateHisSiteMembershipRequest() throws Exception
    {
        UserModel regularUser = dataUser.createRandomTestUser();
        SiteModel anotherModeratedSite = dataSite.usingUser(regularUser).createModeratedRandomSite();
        requestUpdateModel = restClient.authenticateUser(adminUser).withCoreAPI().usingAuthUser()
                .addSiteMembershipRequest(anotherModeratedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        requestUpdateModel = restClient.withCoreAPI().usingAuthUser()
                .updateSiteMembershipRequest(anotherModeratedSite, updatedMessage);

        restClient.assertStatusCodeIs(HttpStatus.OK);
        requestUpdateModel.assertThat().field("id").is(anotherModeratedSite.getId())
                .assertThat().field("message").is("Please review my request")
                .assertThat().field("modifiedAt").isNotEmpty()
                .assertThat().field("createdAt").isNotEmpty();
        requestUpdateModel.getSite().assertThat().field("visibility").is(anotherModeratedSite.getVisibility())
                .assertThat().field("guid").is(anotherModeratedSite.getGuid())
                .assertThat().field("description").is(anotherModeratedSite.getDescription())
                .assertThat().field("id").is(anotherModeratedSite.getId())
                .assertThat().field("preset").is("site-dashboard")
                .assertThat().field("title").is(anotherModeratedSite.getTitle());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify user update site membership request with no message then adds a new message")
    public void userUpdateSiteMembershipRequestWithNoMessageThenAddsNewMessage() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        requestUpdateModel = restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser()
                .addSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        requestUpdateModel.assertThat().field("id").is(moderatedSite.getId())
                .assertThat().field("message").is("Please accept me");

        requestUpdateModel = restClient.withCoreAPI().usingMe().updateSiteMembershipRequest(moderatedSite, "");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        requestUpdateModel.assertThat().field("id").is(moderatedSite.getId())
                .and().field("modifiedAt").isNotEmpty()
                .assertThat().field("message").isNull();

        requestUpdateModel = restClient.withCoreAPI().usingMe().updateSiteMembershipRequest(moderatedSite, updatedMessage);
        requestUpdateModel.assertThat().field("id").is(moderatedSite.getId())
                .assertThat().field("message").is(updatedMessage)
                .assertThat().field("modifiedAt").isNotEmpty()
                .assertThat().field("createdAt").isNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify user updates site membership request and verifies 'ModifiedAt' field using GET")
    public void userUpdateSiteMembershipRequestAndCheckModifiedAtWithGet() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        requestUpdateModel = restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser()
                .addSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        requestUpdateModel.assertThat().field("id").is(moderatedSite.getId())
                .assertThat().field("message").is("Please accept me");

        requestUpdateModel = restClient.withCoreAPI().usingAuthUser().updateSiteMembershipRequest(moderatedSite, updatedMessage);
        requestGetModel = restClient.withCoreAPI().usingAuthUser().getSiteMembershipRequest(moderatedSite);

        restClient.assertStatusCodeIs(HttpStatus.OK);
        requestGetModel.assertThat().field("id").is(requestUpdateModel.getId())
                .and().field("modifiedAt").is(requestUpdateModel.getModifiedAt())
                .assertThat().field("message").is(requestUpdateModel.getMessage());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Update membership request - empty body")
    public void emptyBodyUpdateSiteMembershipRequest() throws Exception
    {
        restClient.authenticateUser(adminUser).withCoreAPI();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, "{\"\": \"\",\"\": \"\", \"\": \"\"}",
                "people/{personId}/site-membership-requests/{siteId}", adminUser.getUsername(), moderatedSite.getId());
        restClient.processModel(RestSiteMembershipRequestModel.class, request);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.NO_CONTENT, "Unrecognized field " + "\"\""));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Update membership request -  missing 'Title' field from Json")
    public void missingTitleFieldBodyUpdateSiteMembershipRequest() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingUser(managerUser).createModeratedRandomSite();
        UserModel newMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser()
                .addSiteMembershipRequest(moderatedSite);

        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT,
                "{\"message\":\"Please review my request\",\"id\":\"" + moderatedSite.getId() + "\"}",
                "people/{personId}/site-membership-requests/{siteId}",
                newMember.getUsername(), moderatedSite.getId());
        requestUpdateModel = restClient.processModel(RestSiteMembershipRequestModel.class, request);

        restClient.assertStatusCodeIs(HttpStatus.OK);
        requestUpdateModel.assertThat().field("id").is(moderatedSite.getId())
                .assertThat().field("message").is(updatedMessage)
                .assertThat().field("site").isNotEmpty()
                .assertThat().field("modifiedAt").isNotEmpty()
                .assertThat().field("createdAt").isNotEmpty();
        requestUpdateModel.getSite().assertThat().field("visibility").is(moderatedSite.getVisibility())
                .assertThat().field("guid").is(moderatedSite.getGuid())
                .assertThat().field("description").is(moderatedSite.getDescription())
                .assertThat().field("id").is(moderatedSite.getId())
                .assertThat().field("preset").is("site-dashboard")
                .assertThat().field("title").is(moderatedSite.getTitle());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Update site membership request - missing JSON body")
    public void missingJsonFieldBodyUpdateSiteMembershipRequest() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingUser(managerUser).createModeratedRandomSite();
        UserModel newMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser()
                .addSiteMembershipRequest(moderatedSite);

        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, "",
                "people/{personId}/site-membership-requests/{siteId}", newMember.getUsername(),
                moderatedSite.getId());
        requestUpdateModel = restClient.processModel(RestSiteMembershipRequestModel.class, request);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.NO_CONTENT, "No content to map due to end-of-input"))
                .containsErrorKey(String.format(RestErrorModel.NO_CONTENT, "No content to map due to end-of-input"))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Update site membership request - empty JSON body")
    public void emptyJsonFieldBodyUpdateSiteMembershipRequest() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingUser(managerUser).createModeratedRandomSite();
        UserModel newMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser()
                .addSiteMembershipRequest(moderatedSite);

        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, "{}",
                "people/{personId}/site-membership-requests/{siteId}", newMember.getUsername(),
                moderatedSite.getId());
        requestUpdateModel = restClient.processModel(RestSiteMembershipRequestModel.class, request);

        restClient.assertStatusCodeIs(HttpStatus.OK);
        requestUpdateModel.assertThat().field("id").is(moderatedSite.getId())
                .assertThat().field("message").isNull()
                .assertThat().field("site").isNotEmpty()
                .assertThat().field("modifiedAt").isNotEmpty()
                .assertThat().field("createdAt").isNotEmpty();
        requestUpdateModel.getSite().assertThat().field("visibility").is(moderatedSite.getVisibility())
                .assertThat().field("guid").is(moderatedSite.getGuid())
                .assertThat().field("description").is(moderatedSite.getDescription())
                .assertThat().field("id").is(moderatedSite.getId())
                .assertThat().field("preset").is("site-dashboard")
                .assertThat().field("title").is(moderatedSite.getTitle());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Update site membership request - invalid SiteId")
    public void invalidSiteIdUpdateSiteMembershipRequest() throws Exception
    {
        moderatedSite.setId("invalidSiteId");
        restClient.authenticateUser(managerUser).withCoreAPI().usingAuthUser()
                .updateSiteMembershipRequest(moderatedSite, updatedMessage);

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, managerUser.getUsername(), moderatedSite.getId()));

        moderatedSite.setId(moderatedSiteId);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Update site membership request - empty SiteId")
    public void emptySiteIdUpdateSiteMembershipRequest() throws Exception
    {
        moderatedSite.setId("");
        restClient.authenticateUser(managerUser).withCoreAPI().usingAuthUser()
                .updateSiteMembershipRequest(moderatedSite, updatedMessage);

        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED)
                .assertLastError()
                .containsSummary(RestErrorModel.PUT_EMPTY_ARGUMENT)
                .containsErrorKey(RestErrorModel.PUT_EMPTY_ARGUMENT)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);

        moderatedSite.setId(moderatedSiteId);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify inexistent user is not able to update site membership request")
    public void emptyUserCannotUpdateSiteMembershipRequest() throws Exception
    {
        UserModel emptyUser = new UserModel("", "password");
        restClient.authenticateUser(emptyUser).withCoreAPI().usingAuthUser()
                .updateSiteMembershipRequest(moderatedSite, updatedMessage);

        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Update site membership request - rejected Request")
    public void rejectRequestThenUpdateSiteMembershipRequest() throws Exception
    {
        UserModel userWithRejectedRequests = dataUser.createRandomTestUser();
        restClient.authenticateUser(userWithRejectedRequests).withCoreAPI().usingAuthUser()
                .addSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        RestTaskModel taskModel = restClient.authenticateUser(userWithRejectedRequests).withWorkflowAPI().getTasks()
                .getTaskModelByDescription(moderatedSite);
        workflow.approveSiteMembershipRequest(managerUser.getUsername(), managerUser.getPassword(), taskModel.getId(), false, "Rejected");

        requestUpdateModel = restClient.authenticateUser(userWithRejectedRequests).withCoreAPI().usingMe()
                .updateSiteMembershipRequest(moderatedSite, updatedMessage);

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, userWithRejectedRequests.getUsername(), moderatedSite.getId()))
                .containsErrorKey(RestErrorModel.RELATIONSHIP_NOT_FOUND_ERRORKEY)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Update site membership request - approved Request")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void approveRequestThenUpdateSiteMembershipRequest() throws Exception
    {
        UserModel userWithApprovedRequests = dataUser.createRandomTestUser();
        restClient.authenticateUser(userWithApprovedRequests).withCoreAPI().usingAuthUser()
                .addSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        RestTaskModel taskModel = restClient.authenticateUser(userWithApprovedRequests).withWorkflowAPI().getTasks().getTaskModelByDescription(moderatedSite);
        workflow.approveSiteMembershipRequest(managerUser.getUsername(), managerUser.getPassword(), taskModel.getId(), true, "Approve");

        requestUpdateModel = restClient.authenticateUser(userWithApprovedRequests).withCoreAPI().usingMe()
                .updateSiteMembershipRequest(moderatedSite, updatedMessage);

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, userWithApprovedRequests.getUsername(), moderatedSite.getId()))
                .containsErrorKey(RestErrorModel.RELATIONSHIP_NOT_FOUND_ERRORKEY)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE}, executionType = ExecutionType.REGRESSION,
            description = "Update site membership request using invalid network")
    public void updateSiteMembershipRequestUsingInvalidNetwork() throws Exception
    {
        UserModel invalidUserNetwork = dataUser.createRandomTestUser();
        SiteModel site = dataSite.usingUser(invalidUserNetwork).createModeratedRandomSite();
        dataSite.usingUser(invalidUserNetwork).usingSite(site).addSiteToFavorites();
        invalidUserNetwork.setDomain("invalidNetwork");

        requestUpdateModel = restClient.authenticateUser(invalidUserNetwork).withCoreAPI().usingMe()
                .updateSiteMembershipRequest(site, updatedMessage);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE}, executionType = ExecutionType.REGRESSION,
            description = "Verify user is not able to update a deleted site membership request")
    public void userCantUpdateSiteMembershipRequestForDeletedRequest() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingUser(managerUser).createModeratedRandomSite();
        UserModel newMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser()
                .addSiteMembershipRequest(moderatedSite);

        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser()
                .deleteSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        requestUpdateModel = restClient.withCoreAPI().usingAuthUser()
                .updateSiteMembershipRequest(moderatedSite, updatedMessage);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, newMember.getUsername(), moderatedSite.getId()));
    }
}