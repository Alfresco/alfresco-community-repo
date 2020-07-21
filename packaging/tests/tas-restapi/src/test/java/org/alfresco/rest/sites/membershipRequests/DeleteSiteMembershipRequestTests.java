package org.alfresco.rest.sites.membershipRequests;

import org.alfresco.dataprep.SiteService.Visibility;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestSiteMembershipRequestModelsCollection;
import org.alfresco.rest.model.RestTaskModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author iulia.cojocea
 */

public class DeleteSiteMembershipRequestTests extends RestTest
{
    UserModel siteCreator, adminUserModel, secondSiteCreator, siteMember;
    SiteModel moderatedSite, secondModeratedSite, publicSite;
    private ListUserWithRoles usersWithRoles;
    RestSiteMembershipRequestModelsCollection siteMembershipRequests;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUserModel = dataUser.getAdminUser();
        siteCreator = dataUser.createRandomTestUser();
        secondSiteCreator = dataUser.createRandomTestUser();
        String siteId = RandomData.getRandomName("site");
        moderatedSite = dataSite.usingUser(siteCreator).createSite(new SiteModel(Visibility.MODERATED, siteId, siteId, siteId, siteId));
        usersWithRoles = dataUser.addUsersWithRolesToSite(moderatedSite, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);

        dataUser.addUserToSite(adminUserModel, moderatedSite, UserRole.SiteManager);
        secondModeratedSite = dataSite.usingUser(secondSiteCreator).createModeratedRandomSite();
        publicSite = dataSite.usingUser(siteCreator).createPublicRandomSite();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify one user is able to delete his one site memebership request")
    public void userCanDeleteHisOwnSiteMembershipRequest() throws  Exception
    {
        restClient.authenticateUser(secondSiteCreator).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.withCoreAPI().usingUser(secondSiteCreator).deleteSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify site manager is able to delete site membership request")
    public void managerCanDeleteSiteMembershipRequest() throws  Exception
    {
        siteMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(siteMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).withCoreAPI().usingUser(siteMember)
                .deleteSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        restClient.withCoreAPI().usingUser(siteMember).getSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsErrorKey(RestErrorModel.RELATIONSHIP_NOT_FOUND_ERRORKEY)
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, siteMember.getUsername(), moderatedSite.getTitle()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify admin user is able to delete site memebership request")
    public void adminUserCanDeleteSiteMembershipRequest() throws  Exception
    {
        siteMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(siteMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(siteMember).deleteSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @Bug(id = "REPO-1946")
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify collaborator user is not able to delete site memebership request")
    public void collaboratorCannotDeleteSiteMembershipRequest() throws  Exception
    {
        siteMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(siteMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);
        
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI().usingUser(siteMember)
                .deleteSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @Bug(id = "REPO-1946")
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify contributor user is not able to delete site memebership request")
    public void contributorCannotDeleteSiteMembershipRequest() throws  Exception
    {
        siteMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(siteMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).withCoreAPI().usingUser(siteMember)
                .deleteSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @Bug(id = "REPO-1946")
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify consumer user is not able to delete site memebership request")
    public void consumerCannotDeleteSiteMembershipRequest() throws  Exception
    {
        siteMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(siteMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).withCoreAPI().usingUser(siteMember)
                .deleteSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @Bug(id = "REPO-1946")
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify random user is not able to delete site memebership request")
    public void randomUserCanNotDeleteSiteMembershipRequest() throws  Exception
    {
        siteMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(siteMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);

        restClient.authenticateUser(dataUser.createRandomTestUser()).withCoreAPI().usingUser(siteMember).deleteSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })    
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY, description = "Failed authentication get site member call returns status code 401")
//    @Bug(id = "MNT-16904")
    public void unauthenticatedUserIsNotAuthorizedToDeleteSiteMmebershipRequest() throws Exception
    {
        restClient.authenticateUser(dataUser.createRandomTestUser()).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);
        UserModel inexistentUser = new UserModel("inexistent user", "inexistent password");
        restClient.authenticateUser(inexistentUser).withCoreAPI().usingAuthUser().deleteSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify user is able to remove his own site memebership request using '-me-' in place of personId and response is successful (204)")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void userCanDeleteHisOwnSiteMembershipRequestUsingMeAsPersonId() throws  Exception
    {
        restClient.authenticateUser(secondSiteCreator).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);
        restClient.withCoreAPI().usingMe().deleteSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify user is not able to remove a site memebership request of an inexistent user and response is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void adminIsNotAbleToDeleteSiteMembershipRequestOfAnInexistentUser() throws  Exception
    {
        UserModel inexistentUser = new UserModel("inexistenUser", "password");
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(inexistentUser).deleteSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, inexistentUser.getUsername()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify user is not able to remove a site memebership request to an inexistent site and response is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void adminIsNotAbleToDeleteSiteMembershipRequestToAnInexistentSite() throws  Exception
    {
        SiteModel inexistentSite = new SiteModel("inexistentSite");
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(adminUserModel).deleteSiteMembershipRequest(inexistentSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, adminUserModel.getUsername(), inexistentSite.getId()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify user is not able to remove a site memebership request to an empty site id and response is 405")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void adminIsNotAbleToDeleteSiteMembershipRequestWithEmptySiteId() throws  Exception
    {
        SiteModel inexistentSite = new SiteModel("");
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(adminUserModel).deleteSiteMembershipRequest(inexistentSite);
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED).assertLastError().containsSummary(RestErrorModel.DELETE_EMPTY_ARGUMENT);
    }

    @Bug(id = "REPO-1946")
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify contributor user is not able to remove a site memebership request of admin to a moderated site and response is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void contributorIsNotAbleToDeleteSiteMembershipRequestOfAdminToAModeratedSite() throws  Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).withCoreAPI().usingUser(adminUserModel).deleteSiteMembershipRequest(secondModeratedSite);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify manager is able to remove a site memebership request of admin to a moderated site and response is 204")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void managerUserIsAbleToDeleteSiteMembershipRequestOfAdminToAModeratedSite() throws  Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingAuthUser().addSiteMembershipRequest(secondModeratedSite);
        restClient.authenticateUser(secondSiteCreator).withCoreAPI().usingUser(adminUserModel).deleteSiteMembershipRequest(secondModeratedSite);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }

    @TestRail(section = { TestGroup.REST_API,
            TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify manager is not able to remove a site memebership request if it was already approved and response is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void managerUserIsNotAbleToDeleteASiteMembershipRequestIfItWasAlreadyApproved() throws  Exception
    {
        siteMember.setUserRole(UserRole.SiteCollaborator);
        restClient.authenticateUser(siteMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(secondModeratedSite);
        RestTaskModel taskModel = restClient.authenticateUser(siteMember).withWorkflowAPI().getTasks().getTaskModelByDescription(secondModeratedSite);
        workflow.approveSiteMembershipRequest(secondSiteCreator.getUsername(), secondSiteCreator.getPassword(), taskModel.getId(), true, "Approve");

        restClient.authenticateUser(secondSiteCreator).withCoreAPI().usingUser(siteMember).deleteSiteMembershipRequest(secondModeratedSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, siteMember.getUsername(), secondModeratedSite.getId()));
    }

    @TestRail(section = { TestGroup.REST_API,
            TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify manager is not able to remove an inexitent site memebership request and response is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void managerUserIsNotAbleToDeleteAnInexistentSiteMembershipRequest() throws  Exception
    {
        restClient.authenticateUser(secondSiteCreator).withCoreAPI().usingUser(adminUserModel).deleteSiteMembershipRequest(secondModeratedSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, adminUserModel.getUsername(), secondModeratedSite.getId()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify collaborator user is not able to remove a site memebership request of admin to a public site and response is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void collaboratorIsNotAbleToDeleteSiteMembershipRequestOfAdminToAPublicSite() throws Exception
    {
        UserModel userCollaborator = usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator);
        restClient.authenticateUser(siteMember).withCoreAPI().usingSite(publicSite).addPerson(userCollaborator);
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingAuthUser().addSiteMembershipRequest(publicSite);

        restClient.authenticateUser(userCollaborator).withCoreAPI().usingUser(adminUserModel).deleteSiteMembershipRequest(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsErrorKey(RestErrorModel.RELATIONSHIP_NOT_FOUND_ERRORKEY)
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, adminUserModel.getUsername(), publicSite.getId()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Delete a rejected site membership request.")
    public void deleteARejectedSiteMembershipRequest() throws Exception
    {
        UserModel userWithRejectedRequests = dataUser.createRandomTestUser();
        restClient.authenticateUser(userWithRejectedRequests).withCoreAPI().usingAuthUser().addSiteMembershipRequest(secondModeratedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        RestTaskModel taskModel = restClient.authenticateUser(userWithRejectedRequests).withWorkflowAPI().getTasks()
                .getTaskModelByDescription(secondModeratedSite);
        workflow.approveSiteMembershipRequest(secondSiteCreator.getUsername(), secondSiteCreator.getPassword(), taskModel.getId(), false, "Rejected");

        siteMembershipRequests = restClient.authenticateUser(userWithRejectedRequests).withCoreAPI().usingMe().getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembershipRequests.assertThat().entriesListDoesNotContain("id", secondModeratedSite.getId());

        restClient.authenticateUser(secondSiteCreator).withCoreAPI().usingUser(userWithRejectedRequests).deleteSiteMembershipRequest(secondModeratedSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsErrorKey(RestErrorModel.RELATIONSHIP_NOT_FOUND_ERRORKEY)
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, userWithRejectedRequests.getUsername(), secondModeratedSite.getId()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Bug(id="ACE-2413")
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify user is not able to remove a site memebership request-  empty person id and response is 400")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void adminIsNotAbleToDeleteSiteMembershipRequestEmptyPersonID() throws Exception
    {
        UserModel emptyPersonId = new UserModel("", "password");
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(emptyPersonId).deleteSiteMembershipRequest(moderatedSite);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError()
                .containsErrorKey(RestErrorModel.API_DEFAULT_ERRORKEY)
                .containsSummary(RestErrorModel.ENTITY_NOT_FOUND)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify manager removes/cancel twice same site memebership request and response is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void managerCancelsTwiceSiteMembershipRequestModeratedSite() throws Exception
    {
        siteMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(siteMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite);

        restClient.authenticateUser(siteCreator).withCoreAPI().usingUser(siteMember).deleteSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withCoreAPI().usingUser(siteMember).deleteSiteMembershipRequest(moderatedSite);

        restClient.withCoreAPI().usingUser(siteMember).getSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsErrorKey(RestErrorModel.RELATIONSHIP_NOT_FOUND_ERRORKEY)
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, siteMember.getUsername(), moderatedSite.getTitle()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }
}
