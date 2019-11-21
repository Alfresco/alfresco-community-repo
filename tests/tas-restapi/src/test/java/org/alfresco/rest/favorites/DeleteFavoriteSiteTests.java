package org.alfresco.rest.favorites;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
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
 * @author Cristina Axinte
 */

public class DeleteFavoriteSiteTests extends RestTest
{
    private UserModel userModel, adminUserModel;
    private SiteModel siteModel1, siteModel2;
    private DataUser.ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        userModel = dataUser.createRandomTestUser();
        siteModel1 = dataSite.usingUser(userModel).createPublicRandomSite();
        siteModel2 = dataSite.usingUser(userModel).createPublicRandomSite();
        adminUserModel = dataUser.getAdminUser();

        usersWithRoles = dataUser.addUsersWithRolesToSite(siteModel1, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, 
    description = "Verify manager user removes a site from its favorite sites list with Rest API and response is successful (204)")
    public void managerUserRemovesFavoriteSiteWithSuccess() throws Exception
    {
        UserModel managerUser = usersWithRoles.getOneUserWithRole(UserRole.SiteManager);
        dataSite.usingUser(managerUser).usingSite(siteModel1).addSiteToFavorites();
        dataSite.usingUser(managerUser).usingSite(siteModel2).addSiteToFavorites();

        restClient.authenticateUser(managerUser).withCoreAPI().usingAuthUser().removeFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        
        restClient.withCoreAPI().usingAuthUser().getFavorites()
                  .assertThat()
                  .entriesListDoesNotContain("targetGuid", siteModel1.getGuid())
                  .and().entriesListDoesNotContain("id", siteModel1.getId())
                  .and().entriesListDoesNotContain("description", siteModel1.getDescription())
                  .and().entriesListDoesNotContain("visibility", siteModel1.getVisibility().toString())
                  .and().entriesListDoesNotContain("title", siteModel1.getTitle());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY,
            description = "Verify manager user is NOT Authorized to remove a site from its favorite sites list with Rest API when authentication fails (401)")
    public void managerUserNotAuthorizedFailsToRemoveFavoriteSite() throws Exception
    {
        UserModel managerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(managerUser, siteModel1, UserRole.SiteManager);
        dataSite.usingUser(managerUser).usingSite(siteModel1).addSiteToFavorites();
        dataSite.usingUser(managerUser).usingSite(siteModel2).addSiteToFavorites();
        managerUser.setPassword("newpassword");

        restClient.authenticateUser(managerUser).withCoreAPI().usingAuthUser().removeFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
    description = "Verify collaborator user removes a site from its favorite sites list with Rest API and response is successful (204)")
    public void collaboratorUserRemovesFavoriteSiteWithSuccess() throws Exception
    {
        UserModel collaboratorUser = usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator);
        dataSite.usingUser(collaboratorUser).usingSite(siteModel1).addSiteToFavorites();
        dataSite.usingUser(collaboratorUser).usingSite(siteModel2).addSiteToFavorites();

        restClient.authenticateUser(collaboratorUser).withCoreAPI().usingAuthUser().removeFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        
        restClient.withCoreAPI().usingAuthUser().getFavorites()
                  .assertThat()
                  .entriesListDoesNotContain("targetGuid", siteModel1.getGuid())
                  .and().entriesListDoesNotContain("id", siteModel1.getId())
                  .and().entriesListDoesNotContain("description", siteModel1.getDescription())
                  .and().entriesListDoesNotContain("visibility", siteModel1.getVisibility().toString())
                  .and().entriesListDoesNotContain("title", siteModel1.getTitle());        
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
    description = "Verify contributor user removes a site from its favorite sites list with Rest API and response is successful (204)")
    public void contributorUserRemovesFavoriteSiteWithSuccess() throws Exception
    {
        UserModel contributorUser = usersWithRoles.getOneUserWithRole(UserRole.SiteContributor);
        dataSite.usingUser(contributorUser).usingSite(siteModel1).addSiteToFavorites();
        dataSite.usingUser(contributorUser).usingSite(siteModel2).addSiteToFavorites();

        restClient.authenticateUser(contributorUser).withCoreAPI().usingAuthUser().removeFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        
        restClient.withCoreAPI().usingAuthUser().getFavorites()
                  .assertThat()
                  .entriesListDoesNotContain("targetGuid", siteModel1.getGuid())
                  .and().entriesListDoesNotContain("id", siteModel1.getId())
                  .and().entriesListDoesNotContain("description", siteModel1.getDescription())
                  .and().entriesListDoesNotContain("visibility", siteModel1.getVisibility().toString())
                  .and().entriesListDoesNotContain("title", siteModel1.getTitle());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
    description = "Verify consumer user removes a site from its favorite sites list with Rest API and response is successful (204)")
    public void consumerUserRemovesFavoriteSiteWithSuccess() throws Exception
    {
        UserModel consumerUser = usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer);
        dataSite.usingUser(consumerUser).usingSite(siteModel1).addSiteToFavorites();
        dataSite.usingUser(consumerUser).usingSite(siteModel2).addSiteToFavorites();

        restClient.authenticateUser(consumerUser).withCoreAPI().usingAuthUser().removeFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        
        restClient.withCoreAPI().usingAuthUser().getFavorites()
                  .assertThat()
                  .entriesListDoesNotContain("targetGuid", siteModel1.getGuid())
                  .and().entriesListDoesNotContain("id", siteModel1.getId())
                  .and().entriesListDoesNotContain("description", siteModel1.getDescription())
                  .and().entriesListDoesNotContain("visibility", siteModel1.getVisibility().toString())
                  .and().entriesListDoesNotContain("title", siteModel1.getTitle());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
    description = "Verify admin user removes a site from any user's favorite sites list with Rest API and response is successful (204)")
    public void adminUserRemovesAnyFavoriteSiteWithSuccess() throws Exception
    {
        UserModel anyUser = dataUser.usingAdmin().createRandomTestUser();
        dataSite.usingUser(anyUser).usingSite(siteModel1).addSiteToFavorites();
        dataSite.usingUser(anyUser).usingSite(siteModel2).addSiteToFavorites();

        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingUser(anyUser).removeFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE , TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
    description = "Verify a user removes a site from another user's favorite sites list with Rest API and response is permission denied (403)")
    public void userCannotRemoveAnotherUserFavoriteSite() throws Exception
    {
        UserModel userAuth = dataUser.usingAdmin().createRandomTestUser();
        UserModel anotherUser = dataUser.usingAdmin().createRandomTestUser();
        dataSite.usingUser(anotherUser).usingSite(siteModel1).addSiteToFavorites();
        dataSite.usingUser(anotherUser).usingSite(siteModel2).addSiteToFavorites();

        restClient.authenticateUser(userAuth).withCoreAPI().usingUser(anotherUser).removeFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
                  .assertLastError()
                  .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE)
                  .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY);  
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify uninvited user can delete favorite public site and response is 204")
    public void uninvitedUserCanDeleteFavoritePublicSite() throws Exception
    {
        SiteModel publicSiteModel = dataSite.usingAdmin().createPublicRandomSite();
        restClient.authenticateUser(userModel).withCoreAPI().usingUser(userModel).addFavoriteSite(publicSiteModel);

        restClient.withCoreAPI().usingAuthUser()
                .removeFavoriteSite(publicSiteModel);

        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withCoreAPI().usingAuthUser().getFavoriteSites()
                .assertThat().entriesListDoesNotContain("id", publicSiteModel.getId());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify uninvited user can delete favorite moderated site and response is 204")
    public void uninvitedUserCanDeleteFavoriteModeratedSite() throws Exception
    {
        SiteModel moderatedSiteModel = dataSite.usingAdmin().createModeratedRandomSite();
        restClient.authenticateUser(userModel).withCoreAPI().usingUser(userModel).addFavoriteSite(moderatedSiteModel);

        restClient.withCoreAPI().usingAuthUser()
                .removeFavoriteSite(moderatedSiteModel);

        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withCoreAPI().usingAuthUser().getFavoriteSites()
                .assertThat().entriesListDoesNotContain("id", moderatedSiteModel.getId());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify user can delete favorite private site and response is 204")
    public void userCanDeleteFavoritePrivateSite() throws Exception
    {
        SiteModel privateSiteModel = dataSite.usingUser(userModel).createPrivateRandomSite();
        restClient.authenticateUser(userModel).withCoreAPI().usingUser(userModel).addFavoriteSite(privateSiteModel);

        restClient.withCoreAPI().usingAuthUser()
                .removeFavoriteSite(privateSiteModel);

        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withCoreAPI().usingAuthUser().getFavoriteSites()
                .assertThat().entriesListDoesNotContain("id", privateSiteModel.getId());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify manager user removes a site from its favorites and adds it again and response is successful (204)")
    public void managerUserRemovesFavoriteSiteAndAddItAgain() throws Exception
    {
        UserModel managerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(managerUser, siteModel1, UserRole.SiteManager);
        dataSite.usingUser(managerUser).usingSite(siteModel1).addSiteToFavorites();

        restClient.authenticateUser(managerUser).withCoreAPI().usingAuthUser()
                .removeFavoriteSite(siteModel1);

        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withCoreAPI().usingUser(managerUser).addFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify user removes a site from favorites using '-me-' in place of personId with Rest API and response is successful (204)")
    public void removeFavoriteSiteWithSuccessUsingMeAsPersonId() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        dataSite.usingUser(adminUserModel).usingSite(siteModel1).addSiteToFavorites();

        restClient.withCoreAPI().usingMe().removeFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
//    @Bug(id = "REPO-1642", description = "reproduced on 5.2.1 only, it works on 5.2.0")
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify inexistent user is not able to remove a site from favorites and response is 404")
    public void inexistentUserIsNotAbleToRemoveFavoriteSite() throws Exception
    {
        UserModel inexistentUser = new UserModel("inexistenUser", "password");
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(inexistentUser)
                .removeFavoriteSite(siteModel1);

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "inexistenUser"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify user is not able to remove from favorites a site with inexistent id and response is 404")
    public void userIsNotAbleToRemoveFavoriteSiteWithInexistentId() throws Exception
    {
        SiteModel inexistentSite = new SiteModel("inexistentSite");
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(adminUserModel)
                .removeFavoriteSite(inexistentSite);

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND,
                        adminUserModel.getUsername(), inexistentSite.getTitle()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE)
                .containsErrorKey(RestErrorModel.RELATIONSHIP_NOT_FOUND_ERRORKEY);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "User is not able to remove a favorite site of admin user")
    public void userIsNotAbleToDeleteFavoritesOfAdmin() throws Exception
    {
        UserModel contributor = usersWithRoles.getOneUserWithRole(UserRole.SiteContributor);
        dataSite.usingUser(adminUserModel).usingSite(siteModel1).addSiteToFavorites();

        restClient.authenticateUser(contributor)
                .withCoreAPI().usingUser(adminUserModel).removeFavoriteSite(siteModel1);

        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
                .assertLastError()
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Users removed twice from favorites same site.")
    public void managerUserRemovesDeletedFavoriteSite() throws Exception
    {
        UserModel managerUser = usersWithRoles.getOneUserWithRole(UserRole.SiteManager);
        dataSite.usingUser(managerUser).usingSite(siteModel1).addSiteToFavorites();

        restClient.authenticateUser(managerUser).withCoreAPI().usingAuthUser().removeFavoriteSite(siteModel1);
        restClient.authenticateUser(managerUser).withCoreAPI().usingAuthUser().removeFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.NOT_FAVOURITE_SITE, siteModel1.getTitle()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Users removes from favorite a site that is already deleted.")
    public void consumerUserRemovesDeletedFavoriteSite() throws Exception
    {
        SiteModel siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        UserModel consumerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(consumerUser, siteModel, UserRole.SiteConsumer);
        dataSite.usingUser(consumerUser).usingSite(siteModel).addSiteToFavorites();

        dataSite.usingAdmin().deleteSite(siteModel);
        restClient.authenticateUser(consumerUser).withCoreAPI().usingAuthUser().removeFavoriteSite(siteModel);

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND,
                        consumerUser.getUsername(), siteModel.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Delete  favorite site that is NOT favorite.")
    public void adminUserRemovesDeletedFavoriteSiteThatIsNotFavorite() throws Exception
    {
        SiteModel siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingAuthUser().removeFavoriteSite(siteModel);

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.NOT_FAVOURITE_SITE, siteModel.getTitle()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify user doesn't have permission to delete favorites of another user with Rest API and status code is 403")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void siteMemberIsNotAbleToDeleteFavoritesOfAnotherSiteMember() throws Exception
    {
        UserModel siteCollaborator = usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator);
        dataSite.usingUser(siteCollaborator).usingSite(siteModel1).addSiteToFavorites();

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                .withCoreAPI()
                .usingUser(siteCollaborator)
                .removeFavoriteSite(siteModel1);

        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
                .assertLastError()
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }
}
