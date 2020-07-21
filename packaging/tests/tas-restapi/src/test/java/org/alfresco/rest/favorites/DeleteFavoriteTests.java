package org.alfresco.rest.favorites;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DeleteFavoriteTests extends RestTest
{
    private UserModel adminUserModel;
    private SiteModel siteModel;
    private ListUserWithRoles usersWithRoles;
    private FileModel fileModel;
    private FolderModel folderModel;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUserModel = dataUser.getAdminUser();
        
        siteModel = dataSite.usingUser(adminUserModel).createPublicRandomSite();
        siteModel.setGuid(restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(siteModel).getSite().getGuid());

        fileModel = dataContent.usingUser(adminUserModel).usingSite(siteModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        folderModel = dataContent.usingSite(siteModel).usingUser(adminUserModel).createFolder();
        
        usersWithRoles = dataUser.addUsersWithRolesToSite(siteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.SANITY,
            description = "Verify Manager user deletes site from favorites with Rest API and status code is 204")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.SANITY })
    public void managerIsAbleToDeleteFavorites() throws Exception
    {
        UserModel siteManager = usersWithRoles.getOneUserWithRole(UserRole.SiteManager);
        restClient.authenticateUser(siteManager).withCoreAPI().usingAuthUser().addSiteToFavorites(siteModel);
        
        restClient.withCoreAPI().usingAuthUser().deleteSiteFromFavorites(siteModel).assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withCoreAPI().usingAuthUser().getFavorites()
                  .assertThat()
                  .entriesListDoesNotContain("targetGuid", siteModel.getGuid())
                  .and().entriesListDoesNotContain("id", siteModel.getId())
                  .and().entriesListDoesNotContain("description", siteModel.getDescription())
                  .and().entriesListDoesNotContain("visibility", siteModel.getVisibility().toString())
                  .and().entriesListDoesNotContain("title", siteModel.getTitle());                
    }

    //    @Bug(id = "MNT-16904", description = "It fails only on environment with tenants")
    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.SANITY,
            description = "Verify user gets status code 401 if authentication call fails")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.SANITY })
    public void userIsNotAbleToDeleteFavoritesIfAuthenticationFails() throws Exception
    {
        UserModel siteManager = usersWithRoles.getOneUserWithRole(UserRole.SiteManager);
        siteManager.setPassword("wrongPassword");
        restClient.authenticateUser(siteManager).withCoreAPI().usingAuthUser()
                .deleteSiteFromFavorites(siteModel).assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Collaborator user deletes site from favorites with Rest API and status code is 204")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void collaboratorIsAbleToDeleteFavorites() throws Exception
    {
        UserModel siteCollaborator = usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator);
        restClient.authenticateUser(siteCollaborator).withCoreAPI().usingAuthUser().addSiteToFavorites(siteModel);
        
        restClient.withCoreAPI().usingAuthUser().deleteSiteFromFavorites(siteModel).assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withCoreAPI().usingAuthUser().getFavorites()
                  .assertThat()
                  .entriesListDoesNotContain("targetGuid", siteModel.getGuid())
                  .and().entriesListDoesNotContain("id", siteModel.getId())
                  .and().entriesListDoesNotContain("description", siteModel.getDescription())
                  .and().entriesListDoesNotContain("visibility", siteModel.getVisibility().toString())
                  .and().entriesListDoesNotContain("title", siteModel.getTitle());                
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Contributor user deletes site from favorites with Rest API and status code is 204")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void contributorIsAbleToDeleteFavorites() throws Exception
    {
        UserModel siteContributor = usersWithRoles.getOneUserWithRole(UserRole.SiteContributor);
        restClient.authenticateUser(siteContributor).withCoreAPI().usingAuthUser().addSiteToFavorites(siteModel);
        
        restClient.withCoreAPI().usingAuthUser().deleteSiteFromFavorites(siteModel).assertStatusCodeIs(HttpStatus.NO_CONTENT); 
        restClient.withCoreAPI().usingAuthUser().getFavorites()
                  .assertThat()
                  .entriesListDoesNotContain("targetGuid", siteModel.getGuid())
                  .and().entriesListDoesNotContain("id", siteModel.getId())
                  .and().entriesListDoesNotContain("description", siteModel.getDescription())
                  .and().entriesListDoesNotContain("visibility", siteModel.getVisibility().toString())
                  .and().entriesListDoesNotContain("title", siteModel.getTitle());                
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Consumer user delets site from favorites with Rest API and status code is 204")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void consumerIsAbleToDeleteFavorites() throws Exception
    {
        UserModel siteConsumer = usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer);
        restClient.authenticateUser(siteConsumer).withCoreAPI().usingAuthUser().addSiteToFavorites(siteModel);
        
        restClient.withCoreAPI().usingAuthUser().deleteSiteFromFavorites(siteModel).assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withCoreAPI().usingAuthUser().getFavorites()
                  .assertThat()
                  .entriesListDoesNotContain("targetGuid", siteModel.getGuid())
                  .and().entriesListDoesNotContain("id", siteModel.getId())
                  .and().entriesListDoesNotContain("description", siteModel.getDescription())
                  .and().entriesListDoesNotContain("visibility", siteModel.getVisibility().toString())
                  .and().entriesListDoesNotContain("title", siteModel.getTitle());                
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Admin user deletes site from favorites with Rest API and status code is 204")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void adminIsAbleToDeleteFavorites() throws Exception
    {
        restClient.withCoreAPI().usingUser(adminUserModel).addSiteToFavorites(siteModel);

        restClient.withCoreAPI().usingAuthUser().deleteSiteFromFavorites(siteModel).assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withCoreAPI().usingAuthUser().getFavorites().assertThat().entriesListDoesNotContain("targetGuid", siteModel.getGuid());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin deletes files from favorites with Rest API and status code is (204)")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void adminIsAbleToDeleteFavoriteFile() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingAuthUser().addFileToFavorites(fileModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.withCoreAPI().usingAuthUser().deleteFileFromFavorites(fileModel).assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withCoreAPI().usingAuthUser().getFavorites()
                .assertThat()
                .entriesListDoesNotContain("targetGuid", fileModel.getNodeRefWithoutVersion());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin deletes folder from favorites with Rest API and status code is (204)")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void adminIsAbleToDeleteFavoriteFolder() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingAuthUser().addFolderToFavorites(folderModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.withCoreAPI().usingAuthUser().deleteFolderFromFavorites(folderModel)
                .assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withCoreAPI().usingAuthUser().getFavorites()
                .assertThat()
                .entriesListDoesNotContain("targetGuid", fileModel.getNodeRefWithoutVersion());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user removes a site from favorites using '-me-' in place of personId with Rest API and response is successful (204)")
    public void deleteFavoriteSiteWithSuccessUsingMeAsPersonId() throws Exception
    {
        siteModel.setGuid(restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(siteModel).getSite().getGuid());
        restClient.withCoreAPI().usingUser(adminUserModel).addSiteToFavorites(siteModel);

        restClient.withCoreAPI().usingMe().deleteSiteFromFavorites(siteModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }

    @Bug(id="ACE-5588")
    @TestRail(section = { TestGroup.REST_API,TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user doesn't have permission to delete favorites of admin user with Rest API and status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void userIsNotAbleToDeleteFavoritesOfAdminUser() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingAuthUser().addSiteToFavorites(siteModel);
        
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                  .withCoreAPI().usingAuthUser().deleteSiteFromFavorites(siteModel)
                  .assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Bug(id="ACE-5588")
    @TestRail(section = { TestGroup.REST_API,TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin user doesn't have permission to delete favorites of another user with Rest API and status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void adminIsNotAbleToDeleteFavoritesOfASiteMember() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .withCoreAPI().usingAuthUser().addSiteToFavorites(siteModel);

        restClient.authenticateUser(adminUserModel)
                .withCoreAPI().usingAuthUser().deleteSiteFromFavorites(siteModel)
                .assertStatusCodeIs(HttpStatus.FORBIDDEN)
                .assertLastError()
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Bug(id="ACE-5588")
    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user doesn't have permission to delete favorites of another user with Rest API and status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void consumerIsNotAbleToDeleteFavoritesOfAnotherUser() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI()
                .usingAuthUser().addSiteToFavorites(siteModel);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).withCoreAPI()
                .usingAuthUser().deleteSiteFromFavorites(siteModel)
                .assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Bug(id="ACE-5588")
    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify consumer user doesn't have permission to delete favorites of another user with Rest API and status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void collaboratorIsNotAbleToDeleteFavoriteOfAnotherUser() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).withCoreAPI()
                .usingAuthUser().addSiteToFavorites(siteModel);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI()
                .usingAuthUser().deleteSiteFromFavorites(siteModel)
                .assertStatusCodeIs(HttpStatus.FORBIDDEN)
                .assertLastError()
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Bug(id="ACE-5588")
    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify contributor user doesn't have permission to delete favorites of another user with Rest API and status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void contributorIsNotAbleToDeleteFavoriteOfAnotherUser() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI()
                .usingAuthUser().addSiteToFavorites(siteModel);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).withCoreAPI()
                .usingAuthUser().deleteSiteFromFavorites(siteModel)
                .assertStatusCodeIs(HttpStatus.FORBIDDEN)
                .assertLastError()
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Bug(id="ACE-5588")
    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify contributor user doesn't have permission to delete favorites of another user with Rest API and status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void adminIsNotAbleToDeleteFavoriteOfAnotherUser() throws Exception
    {
        UserModel user = dataUser.createRandomTestUser();
        restClient.authenticateUser(user).withCoreAPI().usingAuthUser().addSiteToFavorites(siteModel);

        restClient.authenticateUser(adminUserModel).withCoreAPI()
                .usingAuthUser().deleteSiteFromFavorites(siteModel)
                .assertStatusCodeIs(HttpStatus.FORBIDDEN)
                .assertLastError()
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that status code is 404 if PersonID is incorrect - favorite file.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION})
    public void deleteFavoriteIfPersonIdNotExists() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(adminUserModel).addSiteToFavorites(siteModel);
        restClient.withCoreAPI().usingAuthUser().addFileToFavorites(fileModel);

        restClient.withCoreAPI().usingUser(new UserModel("inexistent", "inexistent"))
                .deleteFileFromFavorites(fileModel).assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "inexistent"));
    }

    @Bug(id="ACE-2413")
    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that status code is 404 if PersonID is empty - favorite file.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION})
    public void deleteFavoriteIfPersonIdIsEmpty() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(adminUserModel).addSiteToFavorites(siteModel);
        restClient.withCoreAPI().usingAuthUser().addFileToFavorites(fileModel);

        restClient.withCoreAPI().usingUser(new UserModel ("", ""))
                .deleteFileFromFavorites(fileModel).assertStatusCodeIs(HttpStatus.BAD_REQUEST);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that status code is 404 if FavoriteID is incorrect - favorite file.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION})
    public void deleteFavoriteFileIfFavoriteIdIsIncorrect() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingAuthUser().addFileToFavorites(fileModel);

        fileModel.setNodeRef("wrongFavoriteId");
        restClient.withCoreAPI().usingAuthUser()
                .deleteFileFromFavorites(fileModel).assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "wrongFavoriteId"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that status code is 404 if FavoriteID is incorrect - favorite file.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION})
    public void deleteFavoriteFileIfFavoriteIdNotExists() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingAuthUser().addFileToFavorites(fileModel);
        FileModel inexistentDocument = new FileModel();
        inexistentDocument.setNodeRef("inexistent");

        restClient.withCoreAPI().usingAuthUser()
                .deleteFileFromFavorites(inexistentDocument).assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "inexistent"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that status code is 404 if FavoriteID is incorrect - favorite site.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION})
    public void deleteFavoriteSiteIfFavoriteIdNotExists() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(adminUserModel).addSiteToFavorites(siteModel);
        siteModel.setGuid("wrongFavoriteId");

        restClient.withCoreAPI().usingAuthUser()
                .deleteSiteFromFavorites(siteModel).assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "wrongFavoriteId"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that status code is 404 if FavoriteID is incorrect - favorite folder.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION})
    public void deleteFavoriteFolderIfFavoriteIdNotExists() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(adminUserModel).addSiteToFavorites(siteModel);
        restClient.withCoreAPI().usingAuthUser().addFolderToFavorites(folderModel);

        folderModel.setNodeRef("wrongFavoriteId");
        restClient.withCoreAPI().usingAuthUser()
                .deleteFolderFromFavorites(folderModel).assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "wrongFavoriteId"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin is not able to deletes files from favorites that were already deleted with Rest API and status code is (404)")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void adminIsNotAbleToDeleteADeletedFavoriteFile() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingAuthUser().addFileToFavorites(fileModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.withCoreAPI().usingAuthUser().deleteFileFromFavorites(fileModel).assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withCoreAPI().usingAuthUser().getFavorites()
                .assertThat()
                .entriesListDoesNotContain("targetGuid", fileModel.getNodeRefWithoutVersion());

        restClient.withCoreAPI().usingAuthUser().deleteFileFromFavorites(fileModel)
                .assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND,
                        adminUserModel.getUsername(), fileModel.getNodeRef()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user can't delete favorites using an invalid network ID.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void userIsNotAbleToDeleteFavoriteSiteWithInvalidNetworkID() throws Exception
    {
        UserModel networkUserModel = dataUser.createRandomTestUser();
        restClient.authenticateUser(networkUserModel).withCoreAPI().usingAuthUser().addSiteToFavorites(siteModel);
        networkUserModel.setDomain("invalidNetwork");

        restClient.withCoreAPI()
                .usingAuthUser().deleteSiteFromFavorites(siteModel)
                .assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify contributor user doesn't have permission to delete favorites of another user with Rest API and status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void adminDeletesFavoriteForNotFavouriteFile() throws Exception
    {
        fileModel = dataContent.usingSite(siteModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        restClient.authenticateUser(adminUserModel).withCoreAPI().usingAuthUser()
                .deleteFileFromFavorites(fileModel)
                .assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND,
                        adminUserModel.getUsername(), fileModel.getNodeRef()))
                .containsErrorKey(RestErrorModel.RELATIONSHIP_NOT_FOUND_ERRORKEY)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }
}
