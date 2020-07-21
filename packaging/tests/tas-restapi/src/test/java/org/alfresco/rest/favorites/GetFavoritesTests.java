package org.alfresco.rest.favorites;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestPersonFavoritesModel;
import org.alfresco.rest.model.RestPersonFavoritesModelsCollection;
import org.alfresco.rest.model.RestSiteModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetFavoritesTests extends RestTest
{
    private UserModel adminUserModel, userModel;
    private SiteModel firstSiteModel;
    private SiteModel secondSiteModel;
    private SiteModel thirdSiteModel;
    private FileModel firstFileModel;
    private FileModel secondFileModel;
    private FolderModel firstFolderModel;
    private FolderModel secondFolderModel;
    private ListUserWithRoles firstSiteUsers, secondSiteUsers;
    private RestPersonFavoritesModelsCollection userFavorites;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUserModel = dataUser.getAdminUser();
        userModel = dataUser.createRandomTestUser();

        firstSiteModel = dataSite.usingUser(adminUserModel).createPublicRandomSite();
        secondSiteModel = dataSite.usingUser(adminUserModel).createPublicRandomSite();
        thirdSiteModel = dataSite.usingUser(adminUserModel).createPublicRandomSite();
        firstFolderModel = dataContent.usingUser(adminUserModel).usingSite(firstSiteModel).createFolder();
        secondFolderModel = dataContent.usingUser(adminUserModel).usingSite(firstSiteModel).createFolder();
        firstFileModel = dataContent.usingUser(adminUserModel).usingResource(firstFolderModel).createContent(DocumentType.TEXT_PLAIN);
        secondFileModel = dataContent.usingUser(adminUserModel).usingResource(firstFolderModel).createContent(DocumentType.TEXT_PLAIN);

        firstSiteUsers = dataUser.addUsersWithRolesToSite(firstSiteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);
        secondSiteUsers = dataUser.addUsersWithRolesToSite(secondSiteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
            UserRole.SiteContributor);

        restClient.authenticateUser(userModel);
        restClient.withCoreAPI().usingUser(userModel).addSiteToFavorites(firstSiteModel);
        restClient.withCoreAPI().usingUser(userModel).addSiteToFavorites(secondSiteModel);
        restClient.withCoreAPI().usingUser(userModel).addSiteToFavorites(thirdSiteModel);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.SANITY,
            description = "Verify Manager user gets favorites with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.SANITY })
    public void managerIsAbleToRetrieveFavorites() throws Exception
    {
        restClient.authenticateUser(firstSiteUsers.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingAuthUser().addSiteToFavorites(firstSiteModel);
        restClient.withCoreAPI().usingAuthUser().addSiteToFavorites(secondSiteModel);

        userFavorites = restClient.withCoreAPI().usingAuthUser().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        userFavorites.assertThat().entriesListContains("targetGuid", firstSiteModel.getGuid())
                .assertThat().entriesListContains("targetGuid", secondSiteModel.getGuid());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.SANITY,
            description = "Verify user gets status code 401 if authentication call fails")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.SANITY })
//    @Bug(id = "MNT-16904", description = "It fails only on environment with tenants")
    public void userIsNotAbleToRetrieveFavoritesIfAuthenticationFails() throws Exception
    {
        UserModel siteManager = firstSiteUsers.getOneUserWithRole(UserRole.SiteManager);
        siteManager.setPassword("wrongPassword");
        restClient.authenticateUser(siteManager).withCoreAPI().usingAuthUser().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Admin user gets favorites sites with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void adminIsAbleToRetrieveFavoritesSites() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(adminUserModel).addSiteToFavorites(firstSiteModel);
        restClient.withCoreAPI().usingUser(adminUserModel).addSiteToFavorites(secondSiteModel);
        
        userFavorites = restClient.withCoreAPI()
                .usingAuthUser().where().targetSiteExist().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        userFavorites.assertThat().entriesListContains("targetGuid", firstSiteModel.getGuid())
                  .and().entriesListContains("targetGuid", secondSiteModel.getGuid());    
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Admin user gets favorites folders with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void adminIsAbleToRetrieveFavoritesFolders() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(adminUserModel).addFolderToFavorites(firstFolderModel);
        restClient.withCoreAPI().usingUser(adminUserModel).addFolderToFavorites(secondFolderModel);
        
        userFavorites = restClient.withCoreAPI()
                .usingAuthUser().where().targetFolderExist().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        userFavorites.assertThat().entriesListContains("targetGuid", firstFolderModel.getNodeRef())
                  .and().entriesListContains("targetGuid", secondFolderModel.getNodeRef());    
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Admin user gets favorites files with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void adminIsAbleToRetrieveFavoritesFiles() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(adminUserModel).addFileToFavorites(firstFileModel);
        restClient.withCoreAPI().usingUser(adminUserModel).addFileToFavorites(secondFileModel);
        
        userFavorites = restClient.withCoreAPI()
                .usingAuthUser().where().targetFileExist().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        userFavorites.assertThat().entriesListContains("targetGuid", firstFileModel.getNodeRefWithoutVersion())
                .and().entriesListContains("targetGuid", secondFileModel.getNodeRefWithoutVersion());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Collaborator user gets favorites with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void collaboratorIsAbleToRetrieveFavorites() throws Exception
    {
        restClient.authenticateUser(firstSiteUsers.getOneUserWithRole(UserRole.SiteCollaborator))
                  .withCoreAPI().usingAuthUser().addSiteToFavorites(firstSiteModel);
        restClient.withCoreAPI().usingAuthUser().addSiteToFavorites(secondSiteModel);
        
        userFavorites = restClient.withCoreAPI().usingAuthUser().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        userFavorites.assertThat().entriesListContains("targetGuid", firstSiteModel.getGuid())
                  .and().entriesListContains("targetGuid", secondSiteModel.getGuid());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Contributor user gets favorites with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void contributorIsAbleToRetrieveFavorites() throws Exception
    {
        restClient.authenticateUser(firstSiteUsers.getOneUserWithRole(UserRole.SiteContributor))
                  .withCoreAPI().usingAuthUser().addSiteToFavorites(firstSiteModel);
        restClient.withCoreAPI().usingAuthUser().addSiteToFavorites(secondSiteModel);
        
        userFavorites = restClient.withCoreAPI().usingAuthUser().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        userFavorites.assertThat().entriesListContains("targetGuid", firstSiteModel.getGuid()).and()
                  .entriesListContains("targetGuid", secondSiteModel.getGuid());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Consumer user gets favorites with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void consumerIsAbleToRetrieveFavorites() throws Exception
    {
        restClient.authenticateUser(firstSiteUsers.getOneUserWithRole(UserRole.SiteConsumer))
                  .withCoreAPI().usingAuthUser().addSiteToFavorites(firstSiteModel);
        restClient.withCoreAPI().usingAuthUser().addSiteToFavorites(secondSiteModel);
        
        userFavorites = restClient.withCoreAPI().usingAuthUser().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        userFavorites.assertThat().entriesListContains("targetGuid", firstSiteModel.getGuid())
                  .assertThat().entriesListContains("targetGuid", secondSiteModel.getGuid());    
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user doesn't have permission to get favorites of another user with Rest API and status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void userIsNotAbleToRetrieveFavoritesOfAnotherUser() throws Exception
    {
        restClient.authenticateUser(firstSiteUsers.getOneUserWithRole(UserRole.SiteConsumer))
                  .withCoreAPI().usingUser(firstSiteUsers.getOneUserWithRole(UserRole.SiteCollaborator)).getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, firstSiteUsers.getOneUserWithRole(UserRole.SiteCollaborator).getUsername()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user doesn't have permission to get favorites of admin user with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION  })
    public void userIsNotAbleToRetrieveFavoritesOfAdminUser() throws Exception
    {
        restClient.authenticateUser(firstSiteUsers.getOneUserWithRole(UserRole.SiteConsumer)).withCoreAPI()
                  .usingUser(adminUserModel).getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                  .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, adminUserModel.getUsername()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin user doesn't have permission to get favorites of another user with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void adminIsNotAbleToRetrieveFavoritesOfAnotherUser() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(firstSiteUsers.getOneUserWithRole(UserRole.SiteCollaborator))
                  .getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                  .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, firstSiteUsers.getOneUserWithRole(UserRole.SiteCollaborator).getUsername()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify User gets only favorites sites with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void userIsAbleToRetrieveOnlyFavoritesSites() throws Exception
    {
        restClient.authenticateUser(secondSiteUsers.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingAuthUser().addSiteToFavorites(firstSiteModel);
        restClient.withCoreAPI().usingAuthUser().addFileToFavorites(firstFileModel);
        restClient.withCoreAPI().usingAuthUser().addFolderToFavorites(firstFolderModel);

        userFavorites = restClient.withCoreAPI().usingAuthUser().where().targetSiteExist().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        userFavorites.assertThat().entriesListContains("targetGuid", firstSiteModel.getGuid())
                .and().entriesListDoesNotContain("targetGuid", secondSiteModel.getGuid())
                .and().entriesListDoesNotContain("targetGuid", firstFileModel.getNodeRefWithoutVersion())
                .and().entriesListDoesNotContain("targetGuid", firstFolderModel.getNodeRef());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify User gets only favorites files with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void userIsAbleToRetrieveOnlyFavoritesFiles() throws Exception
    {
        restClient.authenticateUser(firstSiteUsers.getOneUserWithRole(UserRole.SiteCollaborator));
        restClient.withCoreAPI().usingAuthUser().addSiteToFavorites(firstSiteModel);
        restClient.withCoreAPI().usingAuthUser().addFileToFavorites(firstFileModel);
        restClient.withCoreAPI().usingAuthUser().addFolderToFavorites(firstFolderModel);

        userFavorites = restClient.withCoreAPI().usingAuthUser().where().targetFileExist().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        userFavorites.assertThat().entriesListContains("targetGuid", firstFileModel.getNodeRefWithoutVersion())
                .and().entriesListDoesNotContain("targetGuid", secondFileModel.getNodeRefWithoutVersion())
                .and().entriesListDoesNotContain("targetGuid", firstSiteModel.getGuid())
                .and().entriesListDoesNotContain("targetGuid", firstFolderModel.getNodeRef());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify User gets only favorites folders with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void userIsAbleToRetrieveOnlyFavoritesFolders() throws Exception
    {
        restClient.authenticateUser(firstSiteUsers.getOneUserWithRole(UserRole.SiteCollaborator));
        restClient.withCoreAPI().usingAuthUser().addSiteToFavorites(firstSiteModel);
        restClient.withCoreAPI().usingAuthUser().addFileToFavorites(firstFileModel);
        restClient.withCoreAPI().usingAuthUser().addFolderToFavorites(firstFolderModel);

        userFavorites = restClient.withCoreAPI().usingAuthUser().where().targetFolderExist().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        userFavorites.assertThat().entriesListContains("targetGuid", firstFolderModel.getNodeRef())
                .and().entriesListDoesNotContain("targetGuid", secondFolderModel.getNodeRef())
                .and().entriesListDoesNotContain("targetGuid", firstSiteModel.getGuid())
                .and().entriesListDoesNotContain("targetGuid", firstFileModel.getNodeRefWithoutVersion());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify User gets only favorites files or folders with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void userIsAbleToRetrieveFavoritesFilesOrFolders() throws Exception
    {
        restClient.authenticateUser(firstSiteUsers.getOneUserWithRole(UserRole.SiteConsumer));
        restClient.withCoreAPI().usingAuthUser().addSiteToFavorites(firstSiteModel);
        restClient.withCoreAPI().usingAuthUser().addFileToFavorites(firstFileModel);
        restClient.withCoreAPI().usingAuthUser().addFolderToFavorites(firstFolderModel);

        userFavorites = restClient.withCoreAPI().usingAuthUser()
                .where().targetFolderExist().or().targetFileExist()
                .getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        userFavorites.assertThat().entriesListContains("targetGuid", firstFolderModel.getNodeRef())
                .and().entriesListContains("targetGuid", firstFileModel.getNodeRefWithoutVersion())
                .and().entriesListDoesNotContain("targetGuid", secondFolderModel.getNodeRef())
                .and().entriesListDoesNotContain("targetGuid", secondFileModel.getNodeRefWithoutVersion())
                .and().entriesListDoesNotContain("targetGuid", firstSiteModel.getGuid());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify User gets only favorites files or sites with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void userIsAbleToRetrieveFavoritesFilesOrSites() throws Exception
    {
        restClient.authenticateUser(secondSiteUsers.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingAuthUser().addSiteToFavorites(firstSiteModel);
        restClient.withCoreAPI().usingAuthUser().addFileToFavorites(firstFileModel);
        restClient.withCoreAPI().usingAuthUser().addFolderToFavorites(firstFolderModel);

        userFavorites = restClient.withCoreAPI().usingAuthUser()
                .where().targetSiteExist().or().targetFileExist()
                .getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        userFavorites.assertThat().entriesListContains("targetGuid", firstSiteModel.getGuid())
                .and().entriesListContains("targetGuid", firstFileModel.getNodeRefWithoutVersion())
                .and().entriesListDoesNotContain("targetGuid", secondSiteModel.getGuid())
                .and().entriesListDoesNotContain("targetGuid", secondFileModel.getNodeRefWithoutVersion())
                .and().entriesListDoesNotContain("targetGuid", firstFolderModel.getNodeRef());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify User gets only favorites folders or sites with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void userIsAbleToRetrieveFavoritesFoldersOrSites() throws Exception
    {
        restClient.authenticateUser(secondSiteUsers.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingAuthUser().addSiteToFavorites(firstSiteModel);
        restClient.withCoreAPI().usingAuthUser().addFileToFavorites(firstFileModel);
        restClient.withCoreAPI().usingAuthUser().addFolderToFavorites(firstFolderModel);

        userFavorites = restClient.withCoreAPI().usingAuthUser()
                .where().targetSiteExist().or().targetFolderExist()
                .getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        userFavorites.assertThat().entriesListContains("targetGuid", firstSiteModel.getGuid())
                .and().entriesListContains("targetGuid", firstFolderModel.getNodeRef())
                .and().entriesListDoesNotContain("targetGuid", secondSiteModel.getGuid())
                .and().entriesListDoesNotContain("targetGuid", secondFolderModel.getNodeRef())
                .and().entriesListDoesNotContain("targetGuid", firstFileModel.getNodeRefWithoutVersion());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify User gets all favorites with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void userIsAbleToRetrieveAllFavorites() throws Exception
    {
        restClient.authenticateUser(secondSiteUsers.getOneUserWithRole(UserRole.SiteCollaborator));
        restClient.withCoreAPI().usingAuthUser().addSiteToFavorites(firstSiteModel);
        restClient.withCoreAPI().usingAuthUser().addFileToFavorites(firstFileModel);
        restClient.withCoreAPI().usingAuthUser().addFolderToFavorites(firstFolderModel);

        userFavorites = restClient.withCoreAPI().usingAuthUser()
                .where().targetSiteExist().or().targetFolderExist().or().targetFileExist()
                .getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        userFavorites.assertThat().entriesListContains("targetGuid", firstSiteModel.getGuid())
                .and().entriesListContains("targetGuid", firstFolderModel.getNodeRef())
                .and().entriesListContains("targetGuid", firstFileModel.getNodeRefWithoutVersion())
                .and().entriesListDoesNotContain("targetGuid", secondSiteModel.getGuid())
                .and().entriesListDoesNotContain("targetGuid", secondFolderModel.getNodeRef())
                .and().entriesListDoesNotContain("targetGuid", secondFileModel.getNodeRefWithoutVersion());

        userFavorites = restClient.withCoreAPI().usingAuthUser().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        userFavorites.assertThat().entriesListContains("targetGuid", firstSiteModel.getGuid())
                .and().entriesListContains("targetGuid", firstFolderModel.getNodeRef())
                .and().entriesListContains("targetGuid", firstFileModel.getNodeRefWithoutVersion())
                .and().entriesListDoesNotContain("targetGuid", secondSiteModel.getGuid())
                .and().entriesListDoesNotContain("targetGuid", secondFolderModel.getNodeRef())
                .and().entriesListDoesNotContain("targetGuid", secondFileModel.getNodeRefWithoutVersion());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify request for a user with no favorites returns status 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void userHasNoFavorites() throws Exception
    {
        restClient.authenticateUser(secondSiteUsers.getOneUserWithRole(UserRole.SiteContributor));

        userFavorites = restClient.withCoreAPI().usingAuthUser().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        userFavorites.assertThat().entriesListIsEmpty().and().paginationField("totalItems").is("0");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify request using invalid where parameter returns status 400")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void getFavoritesUsingInvalidWhereParameter() throws Exception
    {
        restClient.authenticateUser(firstSiteUsers.getOneUserWithRole(UserRole.SiteConsumer));
        restClient.withCoreAPI().usingAuthUser().addSiteToFavorites(firstSiteModel);
        restClient.withCoreAPI().usingAuthUser().addFileToFavorites(firstFileModel);
        restClient.withCoreAPI().usingAuthUser().addFolderToFavorites(firstFolderModel);

        userFavorites = restClient.withCoreAPI().usingAuthUser()
                .where().or().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.INVALID_ARGUMENT, "WHERE query"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify User gets correct favorites after deleting a favorite")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void checkFavoriteFolderIsRemoved() throws Exception
    {
        restClient.authenticateUser(firstSiteUsers.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingAuthUser().addSiteToFavorites(firstSiteModel);
        restClient.withCoreAPI().usingAuthUser().addFileToFavorites(firstFileModel);
        restClient.withCoreAPI().usingAuthUser().addFolderToFavorites(firstFolderModel);

        restClient.withCoreAPI().usingAuthUser().deleteFolderFromFavorites(firstFolderModel);

        userFavorites = restClient.withCoreAPI().usingAuthUser().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        userFavorites.assertThat().entriesListDoesNotContain("targetGuid", firstFolderModel.getNodeRef())
                .and().paginationField("totalItems").is("2");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify get favorites specifying -me- string in place of <personid> for request")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void userIsAbleToGetFavoritesWhenUsingMeAsUsername() throws Exception
    {
        userFavorites = restClient.authenticateUser(userModel).withCoreAPI().usingMe().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        userFavorites.assertThat().entriesListContains("targetGuid", firstSiteModel.getGuid()).and().entriesListContains("targetGuid", secondSiteModel.getGuid());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify get favorites using empty for where parameter for request")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void userIsAbleToGetFavoritesWhenUsingEmptyWhereParameter() throws Exception
    {
        userFavorites = restClient.authenticateUser(adminUserModel).withCoreAPI().usingAuthUser().where().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.INVALID_ARGUMENT, "WHERE query"));
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that for invalid maxItems parameter status code returned is 400.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void checkInvalidMaxItemsStatusCode() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withParams("maxItems=AB").withCoreAPI().usingUser(adminUserModel).getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary("Invalid paging parameter");
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that for invalid skipCount parameter status code returned is 400.")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void checkInvalidSkipCountStatusCode() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withParams("skipCount=AB").withCoreAPI().usingUser(adminUserModel).getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary("Invalid paging parameter");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify get favorites when using invalid network id for non-tenant user")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void getFavoritesWhenNetworkIdIsInvalid() throws Exception
    {
        UserModel networkUserModel = dataUser.createRandomTestUser();
        networkUserModel.setDomain("invalidNetwork");
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(networkUserModel).where().targetSiteExist().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, networkUserModel.getUsername()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify get favorites using AND instead of OR in where parameter for request")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void userIsNotAbleToGetFavoritesWhenUsingANDInWhereParameter() throws Exception
    {
        userFavorites = restClient.withCoreAPI().usingAuthUser().where().targetFolderExist().invalidWhereParameter("AND").targetFileExist().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.INVALID_ARGUMENT, "WHERE query"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify get favorites using wrong name instead of EXISTS in where parameter for request")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void userIsNotAbleToGetFavoritesWhenUsingWrongWhereParameter() throws Exception
    {
        userFavorites = restClient.withCoreAPI().usingAuthUser().where().invalidWhereParameter("EXIST((target/site))").targetFileExist().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.INVALID_ARGUMENT, "WHERE query"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify get favorites except the first one for request")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void userIsAbleToGetFavoritesExceptTheFirstOne() throws Exception
    {
        userFavorites = restClient.authenticateUser(userModel).withParams("skipCount=1").withCoreAPI().usingUser(userModel).getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);

        userFavorites.assertThat().entriesListContains("targetGuid", firstSiteModel.getGuid())
                .and().entriesListContains("targetGuid", secondSiteModel.getGuid())
                .and().entriesListDoesNotContain("targetGuid", thirdSiteModel.getGuid());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify get first two favorites sites")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void userIsAbleToGetFirstTwoFavorites() throws Exception
    {
        userFavorites = restClient.authenticateUser(userModel).withParams("maxItems=2").withCoreAPI().usingUser(userModel).getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);

        userFavorites.assertThat().entriesListContains("targetGuid", thirdSiteModel.getGuid())
                .and().entriesListContains("targetGuid", secondSiteModel.getGuid())
                .and().entriesListDoesNotContain("targetGuid", firstSiteModel.getGuid())
                .getPagination().assertThat().field("maxItems").is("2")
                .and().field("hasMoreItems").is("true")
                .and().field("count").is("2");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify get favorites sites when using empty values for skipCount and maxItems")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void userIsAbleToGetFavoritesWhenSkipCountAndMaxItemsAreEmpty() throws Exception
    {
        restClient.authenticateUser(userModel).withParams("skipCount= ").withCoreAPI().usingUser(userModel).getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary(String.format(RestErrorModel.INVALID_SKIPCOUNT, " "));

        restClient.authenticateUser(userModel).withParams("maxItems= ").withCoreAPI().usingUser(userModel).getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary(String.format(RestErrorModel.INVALID_MAXITEMS, " "));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify the get favorites request for a high value for skipCount parameter")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void userIsAbleToGetFavoritesWithHighSkipCount() throws Exception
    {
        userFavorites = restClient.authenticateUser(userModel).withParams("skipCount=999999999").withCoreAPI().usingUser(userModel).getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);

        userFavorites.assertThat().entriesListIsEmpty().assertThat().paginationField("skipCount").is("999999999");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify the get favorites request with properties parameter applied")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void userIsAbleToGetFavoritesWithPropertiesParamApplied() throws Exception
    {
        userFavorites = restClient.authenticateUser(userModel).withParams("properties=targetGuid").withCoreAPI().usingUser(userModel).getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);

        RestPersonFavoritesModel restPersonFavoritesModel = userFavorites.getEntries().get(0).onModel();
        restPersonFavoritesModel.assertThat().field("targetGuid").is(thirdSiteModel.getGuid()).and().field("createdAt").isNull();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION, description = "Verify entry details for get favorites response with Rest API")
    public void checkResponseSchemaForGetFavorites() throws Exception
    {
        userFavorites = restClient.authenticateUser(userModel).withCoreAPI().usingAuthUser().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);

        RestPersonFavoritesModel restPersonFavoritesModel = userFavorites.getEntries().get(0).onModel();
        restPersonFavoritesModel.assertThat().field("targetGuid").is(thirdSiteModel.getGuid());

        RestSiteModel restSiteModel = restPersonFavoritesModel.getTarget().getSite();
        restSiteModel.assertThat().field("visibility").is(thirdSiteModel.getVisibility()).and()
                .field("guid").is(thirdSiteModel.getGuid()).and()
                .field("description").is(thirdSiteModel.getDescription()).and()
                .field("id").is(thirdSiteModel.getId()).and()
                .field("title").is(thirdSiteModel.getTitle());
    }
}
