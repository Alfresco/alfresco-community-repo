package org.alfresco.rest.sites.members;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
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

/**
 * @author iulia.cojocea
 */
public class RemoveSiteMemberTests extends RestTest
{
    private SiteModel publicSiteModel, moderatedSiteModel, privateSiteModel;
    private UserModel adminUserModel, siteCreator;
    private ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws DataPreparationException
    {
        adminUserModel = dataUser.getAdminUser();
        siteCreator = dataUser.createRandomTestUser();
        publicSiteModel = dataSite.usingUser(siteCreator).createPublicRandomSite();
        moderatedSiteModel = dataSite.usingUser(siteCreator).createModeratedRandomSite();
        privateSiteModel = dataSite.usingUser(siteCreator).createPrivateRandomSite();
        usersWithRoles = dataUser.addUsersWithRolesToSite(publicSiteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.SITES }, executionType = ExecutionType.SANITY, description = "Verify that site manager can delete site member and gets status code 204, 'No Content'")
    public void siteManagerIsAbleToDeleteSiteMemberWithConsumerRole() throws Exception
    {
        UserModel testUserModel = dataUser.createRandomTestUser();
        dataUser.addUserToSite(testUserModel, publicSiteModel, UserRole.SiteConsumer);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingSite(publicSiteModel).deleteSiteMember(testUserModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        Utility.sleep(300, 30000, () -> restClient.withCoreAPI().usingSite(publicSiteModel)
                .getSiteMembers().assertThat().entriesListDoesNotContain("id", testUserModel.getUsername()));
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.SITES }, executionType = ExecutionType.REGRESSION, description = "Verify that site collaborator cannot delete site member and gets status code 403, 'Forbidden'")
    public void siteCollaboratorIsNotAbleToDeleteSiteMemberWithConsumerRole() throws Exception
    {
        UserModel testUserModel = dataUser.createRandomTestUser();
        dataUser.addUserToSite(testUserModel, publicSiteModel, UserRole.SiteConsumer);
        
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));
        restClient.withCoreAPI().usingSite(publicSiteModel).deleteSiteMember(testUserModel);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY);

        Utility.sleep(300, 30000, () -> restClient.withCoreAPI()
                .usingSite(publicSiteModel).getSiteMembers()
                .assertThat().entriesListContains("id", testUserModel.getUsername()));
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.SITES }, executionType = ExecutionType.REGRESSION, description = "Verify that site contributor cannot delete site member and gets status code 403, 'Forbidden'")
    public void siteContributorIsNotAbleToDeleteSiteMemberWithConsumerRole() throws Exception
    {
        UserModel testUserModel = dataUser.createRandomTestUser();
        dataUser.addUserToSite(testUserModel, publicSiteModel, UserRole.SiteConsumer);
        
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        restClient.withCoreAPI().usingSite(publicSiteModel).deleteSiteMember(testUserModel);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY);

        Utility.sleep(300, 30000, () -> restClient.withCoreAPI().usingSite(publicSiteModel)
                .getSiteMembers().assertThat().entriesListContains("id", testUserModel.getUsername()));
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.SITES }, executionType = ExecutionType.REGRESSION, description = "Verify that site consumer cannot delete site member and gets status code 403, 'Forbidden'")
    public void siteConsumerIsNotAbleToDeleteSiteMemberWithConsumerRole() throws Exception
    {
        UserModel testUserModel = dataUser.createRandomTestUser();
        dataUser.addUserToSite(testUserModel, publicSiteModel, UserRole.SiteConsumer);
        
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).withCoreAPI().usingSite(publicSiteModel)
                .deleteSiteMember(testUserModel);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY);

        Utility.sleep(300, 30000, () -> restClient.withCoreAPI().usingSite(publicSiteModel)
                .getSiteMembers().assertThat().entriesListContains("id", testUserModel.getUsername()));
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.SITES }, executionType = ExecutionType.SANITY, description = "Verify that unauthenticated user is not able to delete site member")
//    @Bug(id = "MNT-16904", description = "It fails only on environment with tenants")
    public void unauthenticatedUserIsNotAuthorizedToDeleteSiteMember() throws Exception
    {
        UserModel testUserModel = dataUser.createRandomTestUser();
        dataUser.addUserToSite(testUserModel, publicSiteModel, UserRole.SiteConsumer);
        
        UserModel inexistentUser = new UserModel("inexistent user", "inexistent password");
        restClient.authenticateUser(inexistentUser).withCoreAPI().usingSite(publicSiteModel).deleteSiteMember(testUserModel);

        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that manager can NOT delete site member for an inexistent user and gets status code 404, 'Not Found'")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void managerIsNotAbleToDeleteInexistentSiteMember() throws Exception
    {
        UserModel inexistentUser = new UserModel("inexistentUser", "password");

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingSite(publicSiteModel).deleteSiteMember(inexistentUser);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, inexistentUser.getUsername()));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that manager can NOT delete site member for a non site member and gets status code 400, 'Bad Request'")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void managerIsNotAbleToDeleteNotSiteMember() throws Exception
    {
        UserModel nonMember = dataUser.createRandomTestUser();

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingSite(publicSiteModel).deleteSiteMember(nonMember);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.INVALID_ARGUMENT, "argument"));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that manager can NOT delete site member for an invalid site and gets status code 404, 'Not Found'")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void managerIsNotAbleToDeleteSiteMemberOfInvalidSite() throws Exception
    {
        SiteModel invalidSite = new SiteModel("invalidSite");
        UserModel testUserModel = dataUser.createRandomTestUser();
        dataUser.addUserToSite(testUserModel, publicSiteModel, UserRole.SiteConsumer);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingSite(invalidSite).deleteSiteMember(testUserModel);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, testUserModel.getUsername(), invalidSite.getId()));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that manager can delete a site member with manager role")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void siteManagerIsAbleToDeleteSiteMemberWithManagerRole() throws Exception
    {
        UserModel anothermanager = dataUser.createRandomTestUser();
        dataUser.addUserToSite(anothermanager, publicSiteModel, UserRole.SiteManager);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingSite(publicSiteModel).deleteSiteMember(anothermanager);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        Utility.sleep(300, 30000, () -> restClient.withCoreAPI().usingSite(publicSiteModel)
                .getSiteMembers().assertThat().entriesListDoesNotContain("id", anothermanager.getUsername()));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that manager can delete site member using \"-me-\" in place of personId")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void managerIsAbleToDeleteSiteMemberUsingMe() throws Exception
    {
        UserModel manager = dataUser.createRandomTestUser();
        dataUser.addUserToSite(manager, publicSiteModel, UserRole.SiteManager);
        UserModel meUser = new UserModel("-me-", "password");

        restClient.authenticateUser(manager);
        restClient.withCoreAPI().usingSite(publicSiteModel).deleteSiteMember(meUser);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        Utility.sleep(300, 30000, () -> restClient.withCoreAPI().usingSite(publicSiteModel)
                .getSiteMembers().assertThat().entriesListDoesNotContain("id", manager.getUsername()));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that site collaborator cannot delete a site member with Manager role and gets status code 422")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void siteCollaboratorIsNotAbleToDeleteSiteMemberWithManagerRole() throws Exception
    {
        UserModel managerForDelete = dataUser.createRandomTestUser();
        dataUser.addUserToSite(managerForDelete, publicSiteModel, UserRole.SiteManager);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));
        restClient.withCoreAPI().usingSite(publicSiteModel).deleteSiteMember(managerForDelete);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY)
                .assertLastError().containsSummary(String.format(RestErrorModel.NOT_SUFFICIENT_PERMISSIONS, publicSiteModel.getId()));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that site contributor cannot delete site member with Manager role and gets status code 422")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void siteContributorIsNotAbleToDeleteSiteMemberWithManagerRole() throws Exception
    {
        UserModel managerForDelete = dataUser.createRandomTestUser();
        dataUser.addUserToSite(managerForDelete, publicSiteModel, UserRole.SiteManager);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        restClient.withCoreAPI().usingSite(publicSiteModel).deleteSiteMember(managerForDelete);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY)
                .assertLastError().containsSummary(String.format(RestErrorModel.NOT_SUFFICIENT_PERMISSIONS, publicSiteModel.getId()));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that site consumer cannot delete site member with Manager role and gets status code 422")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void siteConsumerIsNotAbleToDeleteSiteMemberWithManagerRole() throws Exception
    {
        UserModel managerForDelete = dataUser.createRandomTestUser();
        dataUser.addUserToSite(managerForDelete, publicSiteModel, UserRole.SiteManager);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer));
        restClient.withCoreAPI().usingSite(publicSiteModel).deleteSiteMember(managerForDelete);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY)
                .assertLastError().containsSummary(String.format(RestErrorModel.NOT_SUFFICIENT_PERMISSIONS, publicSiteModel.getId()));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that site collaborator cannot delete a site member with Contributor role and gets status code 422")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void siteCollaboratorIsNotAbleToDeleteSiteMemberWithContributorRole() throws Exception
    {
        UserModel contributorForDelete = dataUser.createRandomTestUser();
        dataUser.addUserToSite(contributorForDelete, publicSiteModel, UserRole.SiteContributor);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));
        restClient.withCoreAPI().usingSite(publicSiteModel).deleteSiteMember(contributorForDelete);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY)
                .assertLastError().containsSummary(String.format(RestErrorModel.NOT_SUFFICIENT_PERMISSIONS, publicSiteModel.getId()));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that site contributor cannot delete site member with Contributor role and gets status code 422")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void siteContributorIsNotAbleToDeleteSiteMemberWithContributorRole() throws Exception
    {
        UserModel contributorForDelete = dataUser.createRandomTestUser();
        dataUser.addUserToSite(contributorForDelete, publicSiteModel, UserRole.SiteContributor);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        restClient.withCoreAPI().usingSite(publicSiteModel).deleteSiteMember(contributorForDelete);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY)
                .assertLastError().containsSummary(String.format(RestErrorModel.NOT_SUFFICIENT_PERMISSIONS, publicSiteModel.getId()));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that site consumer cannot delete site member with Contributor role and gets status code 422")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void siteConsumerIsNotAbleToDeleteSiteMemberWithContributorRole() throws Exception
    {
        UserModel contributorForDelete = dataUser.createRandomTestUser();
        dataUser.addUserToSite(contributorForDelete, publicSiteModel, UserRole.SiteContributor);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer));
        restClient.withCoreAPI().usingSite(publicSiteModel).deleteSiteMember(contributorForDelete);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY)
                .assertLastError().containsSummary(String.format(RestErrorModel.NOT_SUFFICIENT_PERMISSIONS, publicSiteModel.getId()));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that manager can delete a site member with Contributor role")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void siteManagerIsAbleToDeleteSiteMemberWithContributorRole() throws Exception
    {
        UserModel contributorForDelete = dataUser.createRandomTestUser();
        dataUser.addUserToSite(contributorForDelete, publicSiteModel, UserRole.SiteContributor);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingSite(publicSiteModel).deleteSiteMember(contributorForDelete);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        Utility.sleep(300, 30000, () ->  restClient.withCoreAPI().usingSite(publicSiteModel)
                .getSiteMembers().assertThat().entriesListDoesNotContain("id", contributorForDelete.getUsername()));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that site collaborator cannot delete a site member with Collaborator role and gets status code 422")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void siteCollaboratorIsNotAbleToDeleteSiteMemberWithCollaboratorRole() throws Exception
    {
        UserModel collaboratorForDelete = dataUser.createRandomTestUser();
        dataUser.addUserToSite(collaboratorForDelete, publicSiteModel, UserRole.SiteCollaborator);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));
        restClient.withCoreAPI().usingSite(publicSiteModel).deleteSiteMember(collaboratorForDelete);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY)
                .assertLastError().containsSummary(String.format(RestErrorModel.NOT_SUFFICIENT_PERMISSIONS, publicSiteModel.getId()));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that site contributor cannot delete site member with Collaborator role and gets status code 422")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void siteContributorIsNotAbleToDeleteSiteMemberWithCollaboratorRole() throws Exception
    {
        UserModel collaboratorForDelete = dataUser.createRandomTestUser();
        dataUser.addUserToSite(collaboratorForDelete, publicSiteModel, UserRole.SiteCollaborator);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        restClient.withCoreAPI().usingSite(publicSiteModel).deleteSiteMember(collaboratorForDelete);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY)
                .assertLastError().containsSummary(String.format(RestErrorModel.NOT_SUFFICIENT_PERMISSIONS, publicSiteModel.getId()));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that site consumer cannot delete site member with Collaborator role and gets status code 422")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void siteConsumerIsNotAbleToDeleteSiteMemberWithCollaboratorRole() throws Exception
    {
        UserModel collaboratorForDelete = dataUser.createRandomTestUser();
        dataUser.addUserToSite(collaboratorForDelete, publicSiteModel, UserRole.SiteCollaborator);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer));
        restClient.withCoreAPI().usingSite(publicSiteModel).deleteSiteMember(collaboratorForDelete);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY)
                .assertLastError().containsSummary(String.format(RestErrorModel.NOT_SUFFICIENT_PERMISSIONS, publicSiteModel.getId()));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that manager can delete a site member with Collaborator role")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void siteManagerIsAbleToDeleteSiteMemberWithCollaboratorRole() throws Exception
    {
        UserModel collaboratorForDelete = dataUser.createRandomTestUser();
        dataUser.addUserToSite(collaboratorForDelete, publicSiteModel, UserRole.SiteCollaborator);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingSite(publicSiteModel).deleteSiteMember(collaboratorForDelete);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        Utility.sleep(300, 30000, () -> restClient.withCoreAPI().usingSite(publicSiteModel)
                .getSiteMembers().assertThat().entriesListDoesNotContain("id", collaboratorForDelete.getUsername()));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that admin can delete a site member with Contributor role and gets status code 204")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void adminIsAbleToDeleteSiteMemberWithContributorRole() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(publicSiteModel)
                .deleteSiteMember(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        dataUser.addUserToSite(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor), publicSiteModel, UserRole.SiteContributor);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that admin can delete site member with Manager role and gets status code 204")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void aminIsAbleToDeleteSiteMemberWithManagerRole() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(publicSiteModel)
                .deleteSiteMember(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        dataUser.addUserToSite(usersWithRoles.getOneUserWithRole(UserRole.SiteManager), publicSiteModel, UserRole.SiteManager);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that admin can delete site member with Consumer role and gets status code 204")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void adminIsAbleToDeleteSiteMemberWithConsumerRole() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(publicSiteModel)
                .deleteSiteMember(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer));
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        dataUser.addUserToSite(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer), publicSiteModel, UserRole.SiteConsumer);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that admin can delete a site member with Collaborator role and gets status code 204")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void adminIsAbleToDeleteSiteMemberWithCollaboratorRole() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(publicSiteModel)
                .deleteSiteMember(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        dataUser.addUserToSite(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator), publicSiteModel, UserRole.SiteCollaborator);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that admin can delete a site member of moderated site and gets status code 204")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void adminIsAbleToDeleteModeratedSiteMember() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        newMember.setUserRole(UserRole.SiteManager);
        dataUser.addUserToSite(newMember, moderatedSiteModel, UserRole.SiteManager);

        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(moderatedSiteModel).deleteSiteMember(newMember);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that admin can delete a site member of private site and gets status code 204")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void adminIsAbleToDeletePrivateSiteMember() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        newMember.setUserRole(UserRole.SiteManager);
        dataUser.addUserToSite(newMember, privateSiteModel, UserRole.SiteManager);

        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(privateSiteModel).deleteSiteMember(newMember);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that regular user can not delete admin and gets status code 422")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void regularUserIsNotAbleToDeleteASiteMember() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI().usingSite(publicSiteModel)
                .deleteSiteMember(siteCreator);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY).assertLastError()
                .containsSummary(String.format(RestErrorModel.NOT_SUFFICIENT_PERMISSIONS, publicSiteModel.getTitle()))
                .containsErrorKey(RestErrorModel.API_DEFAULT_ERRORKEY)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that admin can not delete a site member twice and gets status code 404 for the second attempt")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @Bug(id="ACE-5447")
    public void adminIsNotAbleToRemoveSiteMemberTwice() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(publicSiteModel)
                .deleteSiteMember(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(publicSiteModel)
                .deleteSiteMember(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(RestErrorModel.ENTITY_NOT_FOUND);
        dataUser.addUserToSite(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor), publicSiteModel, UserRole.SiteContributor);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin is not able to remove from site a user that created a member request that was not accepted yet")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @Bug(id="ACE-5447")
    public void adminIsNotAbleToRemoveFromSiteANonExistingMember() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(publicSiteModel);

        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(moderatedSiteModel).deleteSiteMember(newMember);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(RestErrorModel.ENTITY_NOT_FOUND);
    }
}
