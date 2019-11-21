package org.alfresco.rest.favorites;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.dataprep.SiteService;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestPersonFavoritesModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
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

public class GetFavoriteTests extends RestTest
{
    private UserModel adminUserModel;
    private SiteModel siteModel;
    private FolderModel folderModel;
    private FileModel fileModel;
    private ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUserModel = dataUser.getAdminUser();
        siteModel = dataSite.usingUser(adminUserModel).createPublicRandomSite();

        folderModel = dataContent.usingUser(adminUserModel).usingSite(siteModel).createFolder();
        fileModel = dataContent.usingUser(adminUserModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);
        usersWithRoles = dataUser.addUsersWithRolesToSite(siteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.SANITY,
            description = "Verify Manager user gets favorite site with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.SANITY })
    public void managerIsAbleToRetrieveFavoriteSite() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingAuthUser().addSiteToFavorites(siteModel);
        
        RestPersonFavoritesModel favSite = restClient.withCoreAPI().usingAuthUser().getFavorite(siteModel.getGuid());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        favSite.assertThat().field("targetGuid").is(siteModel.getGuid());

    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.SANITY,
            description = "Verify Manager user gets favorite folder with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.SANITY })
    public void managerIsAbleToRetrieveFavoriteFolder() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingAuthUser().addFolderToFavorites(folderModel);
        
        RestPersonFavoritesModel favoriteFolder = restClient.withCoreAPI().usingAuthUser().getFavorite(folderModel.getNodeRef());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        favoriteFolder.assertThat().field("targetGuid").is(folderModel.getNodeRef());

    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.SANITY,
            description = "Verify Manager user gets favorite file with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.SANITY })
    public void managerIsAbleToRetrieveFavoriteFile() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingAuthUser().addFileToFavorites(fileModel);
        
        RestPersonFavoritesModel favoriteFile = restClient.withCoreAPI().usingAuthUser().getFavorite(fileModel.getNodeRefWithoutVersion());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        favoriteFile.assertThat().field("targetGuid").is(fileModel.getNodeRefWithoutVersion());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.SANITY,
            description = "Verify user gets status code 401 if authentication call fails")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.SANITY })
//    @Bug(id = "MNT-16904", description = "fails only on environment with tenants")
    public void userIsNotAbleToRetrieveFavoriteSiteIfAuthenticationFails() throws Exception
    {
        UserModel siteManager = usersWithRoles.getOneUserWithRole(UserRole.SiteManager);
        siteManager.setPassword("wrongPassword");
        restClient.authenticateUser(siteManager).withCoreAPI()
                .usingUser(siteManager).getFavorite(siteModel.getGuid());
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Admin user gets favorite site with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void adminIsAbleToRetrieveFavoritesSite() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(adminUserModel).addSiteToFavorites(siteModel);

        RestPersonFavoritesModel favoriteSite = restClient.withCoreAPI().usingUser(adminUserModel).getFavorite(siteModel.getGuid());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        favoriteSite.assertThat().field("targetGuid").is(siteModel.getGuid());
        favoriteSite.getTarget().getSite()
                .assertThat().field("guid").is(siteModel.getGuid());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Admin user gets favorite folder with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void adminIsAbleToRetrieveFavoritesFolder() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(adminUserModel).addFolderToFavorites(folderModel);

        RestPersonFavoritesModel favoriteFolder = restClient.withCoreAPI().usingUser(adminUserModel).getFavorite(folderModel.getNodeRef());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        favoriteFolder.assertThat().field("targetGuid").is(folderModel.getNodeRef());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Admin user gets favorite file with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void adminIsAbleToRetrieveFavoritesFile() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(adminUserModel).addFileToFavorites(fileModel);

        RestPersonFavoritesModel favoriteFile = restClient.withCoreAPI().usingUser(adminUserModel).getFavorite(fileModel.getNodeRefWithoutVersion());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        favoriteFile.assertThat().field("targetGuid").is(fileModel.getNodeRefWithoutVersion());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Collaborator user gets favorite site with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void collaboratorIsAbleToRetrieveFavoriteSite() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                  .withCoreAPI().usingAuthUser().addSiteToFavorites(siteModel);
        
        RestPersonFavoritesModel favSite = restClient.withCoreAPI().usingAuthUser().getFavorite(siteModel.getGuid());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        favSite.assertThat().field("targetGuid").is(siteModel.getGuid());    
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify collaborator user gets favorite folder with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void collaboratorIsAbleToRetrieveFavoriteFolder() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));
        restClient.withCoreAPI().usingAuthUser().addFolderToFavorites(folderModel);
        
        RestPersonFavoritesModel favoriteFolder = restClient.withCoreAPI().usingAuthUser().getFavorite(folderModel.getNodeRef());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        favoriteFolder.assertThat().field("targetGuid").is(folderModel.getNodeRef());
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify collaborator user gets favorite file with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void collaboratorIsAbleToRetrieveFavoriteFile() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));
        restClient.withCoreAPI().usingAuthUser().addFileToFavorites(fileModel);
        
        RestPersonFavoritesModel favoriteFile = restClient.withCoreAPI().usingAuthUser().getFavorite(fileModel.getNodeRefWithoutVersion());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        favoriteFile.assertThat().field("targetGuid").is(fileModel.getNodeRefWithoutVersion());
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Contributor user gets favorite site with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void contributorIsAbleToRetrieveFavoriteSite() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        restClient.withCoreAPI().usingAuthUser().addSiteToFavorites(siteModel);
        
        RestPersonFavoritesModel favSite = restClient.withCoreAPI().usingAuthUser().getFavorite(siteModel.getGuid());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        favSite.assertThat().field("targetGuid").is(siteModel.getGuid());   ;
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify contributor user gets favorite folder with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void contributorIsAbleToRetrieveFavoriteFolder() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        restClient.withCoreAPI().usingAuthUser().addFolderToFavorites(folderModel);
        
        RestPersonFavoritesModel favoriteFolder = restClient.withCoreAPI().usingAuthUser().getFavorite(folderModel.getNodeRef());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        favoriteFolder.assertThat().field("targetGuid").is(folderModel.getNodeRef());
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify contributor user gets favorite file with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void contributorIsAbleToRetrieveFavoriteFile() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        restClient.withCoreAPI().usingAuthUser().addFileToFavorites(fileModel);
        
        RestPersonFavoritesModel favoriteFile = restClient.withCoreAPI().usingAuthUser().getFavorite(fileModel.getNodeRefWithoutVersion());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        favoriteFile.assertThat().field("targetGuid").is(fileModel.getNodeRefWithoutVersion());
    }

    
    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Consumer user gets favorite site with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void consumerIsAbleToRetrieveFavorites() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer));
        restClient.withCoreAPI().usingAuthUser().addSiteToFavorites(siteModel);
        
        RestPersonFavoritesModel favSite = restClient.withCoreAPI().usingAuthUser().getFavorite(siteModel.getGuid());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        favSite.assertThat().field("targetGuid").is(siteModel.getGuid());   
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify consumer user gets favorite folder with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void consumerIsAbleToRetrieveFavoriteFolder() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer));
        restClient.withCoreAPI().usingAuthUser().addFolderToFavorites(folderModel);
        
        RestPersonFavoritesModel favoriteFolder = restClient.withCoreAPI().usingAuthUser().getFavorite(folderModel.getNodeRef());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        favoriteFolder.assertThat().field("targetGuid").is(folderModel.getNodeRef());
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify consumer user gets favorite file with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void consumerIsAbleToRetrieveFavoriteFile() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer));
        restClient.withCoreAPI().usingAuthUser().addFileToFavorites(fileModel);
        
        RestPersonFavoritesModel favoriteFile = restClient.withCoreAPI().usingAuthUser().getFavorite(fileModel.getNodeRefWithoutVersion());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        favoriteFile.assertThat().field("targetGuid").is(fileModel.getNodeRefWithoutVersion());
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user doesn't get favorite site of another user with Rest API and status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void userIsNotAbleToRetrieveFavoriteSiteOfAnotherSiteMember() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer));
        restClient.withCoreAPI().usingUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                  .getFavorite(siteModel.getGuid());

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator).getUsername()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user doesn't get favorite site of admin user with Rest API and status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void userIsNotAbleToRetrieveFavoritesOfAdminUser() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                  .withCoreAPI().usingUser(adminUserModel).getFavorite(siteModel.getGuid());
        
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, adminUserModel.getUsername()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin user doesn't get favorite site of another user with Rest API and status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void adminIsNotAbleToRetrieveFavoritesOfAnotherUser() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI()
                  .usingUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).getFavorite(siteModel.getGuid());
        
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator).getUsername()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES },
            executionType = ExecutionType.REGRESSION, description = "Verify get favorite user doesn't have any favorite site, file or folder")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void getFavoriteSiteForUserWithoutAnyFavorites() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingAuthUser().getFavorite(siteModel.getGuid());
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, usersWithRoles.getOneUserWithRole(UserRole.SiteManager).getUsername(), siteModel.getGuid()))
                .statusCodeIs(HttpStatus.NOT_FOUND)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE)
                .containsErrorKey(RestErrorModel.RELATIONSHIP_NOT_FOUND_ERRORKEY);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION, description = "Verify get favorite site for -me-")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void getFavoriteSiteUsingMe()  throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI().usingAuthUser().addSiteToFavorites(siteModel);

        RestPersonFavoritesModel favoriteSite = restClient.withCoreAPI().usingMe().getFavorite(siteModel.getGuid());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        favoriteSite.assertThat().field("targetGuid").is(siteModel.getGuid());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION, description = "Verify get favorite site for -me-")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void getFavoriteFileUsingMe()  throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI().usingAuthUser().addFileToFavorites(fileModel);

        RestPersonFavoritesModel favoriteFile = restClient.withCoreAPI().usingMe().getFavorite(fileModel.getNodeRefWithoutVersion());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        favoriteFile.assertThat().field("targetGuid").is(fileModel.getNodeRefWithoutVersion());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES },
            executionType = ExecutionType.REGRESSION, description = "Verify get favorite site when person id does't exist")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void getFavoriteSiteWhenPersonIdNotExist() throws Exception {

        restClient.withCoreAPI().usingUser(adminUserModel).addSiteToFavorites(siteModel);
        UserModel someUser = new UserModel("invalidUser", DataUser.PASSWORD);

        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(someUser).getFavorite(siteModel.getGuid());
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, someUser.getUsername()))
                .statusCodeIs(HttpStatus.NOT_FOUND)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE)
                .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY);
    }

    @Bug(id = "ACE-2413")
    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES },
            executionType = ExecutionType.REGRESSION, description = "Verify get favorite site when person id is empty")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void getFavoriteSiteWithEmptyUserId() throws Exception {

        restClient.withCoreAPI().usingUser(adminUserModel).addSiteToFavorites(siteModel);
        UserModel someUser = new UserModel("", DataUser.PASSWORD);

        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(someUser).getFavorite(siteModel.getGuid());
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, someUser.getUsername()))
                .statusCodeIs(HttpStatus.BAD_REQUEST);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES },
            executionType = ExecutionType.REGRESSION, description = "Verify get favorite site when favorite id does't exist")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void getFavoriteSiteWhenFavoriteIdNotExist() throws Exception {

        restClient.withCoreAPI().usingUser(adminUserModel).addSiteToFavorites(siteModel);

        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(adminUserModel).getFavorite("invalidId");
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "invalidId"))
                .statusCodeIs(HttpStatus.NOT_FOUND)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE)
                .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Admin user gets favorite site with properties filter")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void verifyRequestForGetFavoriteSiteWithProperties() throws Exception
    {
        UserModel collaborator = usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator);
        restClient.authenticateUser(collaborator);
        restClient.withCoreAPI().usingUser(collaborator).addSiteToFavorites(siteModel);

        RestPersonFavoritesModel favoriteSite = restClient.authenticateUser(collaborator).withParams("properties=targetGuid")
                .withCoreAPI().usingAuthUser().getFavorite(siteModel.getGuid());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        favoriteSite.assertThat().field("targetGuid").is(siteModel.getGuid());
        favoriteSite.assertThat().fieldsCount().is(1);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Admin user gets favorite site with inccorect properties filter")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void verifyRequestForGetFavoriteSiteWithInvalidProperties() throws Exception
    {
        UserModel collaborator = usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator);
        restClient.authenticateUser(collaborator);
        restClient.withCoreAPI().usingUser(collaborator).addFileToFavorites(fileModel);

        RestPersonFavoritesModel favoriteFile = restClient.authenticateUser(collaborator).withParams("properties=tas")
                .withCoreAPI().usingAuthUser().getFavorite(fileModel.getNodeRefWithoutVersion());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        favoriteFile.assertThat().fieldsCount().is(0);
    }


    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify all values from get favorite rest api for a file")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void verifyRequestFieldsForGetFavoriteFile() throws Exception
    {
        UserModel contributor = usersWithRoles.getOneUserWithRole(UserRole.SiteContributor);
        restClient.authenticateUser(contributor);
        restClient.withCoreAPI().usingUser(contributor).addFileToFavorites(fileModel);

        RestPersonFavoritesModel favoriteFile = restClient.authenticateUser(contributor)
                .withCoreAPI().usingAuthUser().getFavorite(fileModel.getNodeRefWithoutVersion());
        restClient.assertStatusCodeIs(HttpStatus.OK);

        favoriteFile.assertThat().field("targetGuid").is(fileModel.getNodeRefWithoutVersion())
                .and().field("target.file.isFile").is("true")
                .and().field("target.file.mimeType").is("text/plain")
                .and().field("target.file.isFolder").is("false")
                .and().field("target.file.createdBy").is(adminUserModel.getUsername())
                .and().field("target.file.versionLabel").is("1.0")
                .and().field("target.file.name").is(fileModel.getName())
                .and().field("target.file.parentId").is(folderModel.getNodeRef())
                .and().field("target.file.guid").is(fileModel.getNodeRefWithoutVersion())
                .and().field("target.file.modifiedBy").is(adminUserModel.getUsername())
                .and().field("target.file.id").is(fileModel.getNodeRefWithoutVersion());
    }


    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify all values from get favorite rest api for a site")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void verifyRequestFieldsForGetFavoriteSite() throws Exception
    {
        UserModel contributor = usersWithRoles.getOneUserWithRole(UserRole.SiteContributor);
        restClient.authenticateUser(contributor);
        restClient.withCoreAPI().usingUser(contributor).addFavoriteSite(siteModel);

        RestPersonFavoritesModel favoriteSite = restClient.authenticateUser(contributor)
                .withCoreAPI().usingAuthUser().getFavorite(siteModel.getGuid());
        restClient.assertStatusCodeIs(HttpStatus.OK);

        favoriteSite.assertThat().field("targetGuid").is(siteModel.getGuid())
                .and().field("target.site.role").is(UserRole.SiteContributor.toString())
                .and().field("target.site.visibility").is(SiteService.Visibility.PUBLIC.toString())
                .and().field("target.site.guid").is(siteModel.getGuid())
                .and().field("target.site.description").is(siteModel.getDescription())
                .and().field("target.site.id").is(siteModel.getId())
                .and().field("target.site.preset").is("site-dashboard")
                .and().field("target.site.title").is(siteModel.getTitle());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify all values from get favorite rest api for a folder")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void verifyRequestFieldsForGetFavoriteFolder() throws Exception
    {
        UserModel contributor = usersWithRoles.getOneUserWithRole(UserRole.SiteContributor);
        restClient.authenticateUser(contributor);
        restClient.withCoreAPI().usingUser(contributor).addFolderToFavorites(folderModel);

        RestPersonFavoritesModel favoriteFolder = restClient.authenticateUser(contributor)
                .withCoreAPI().usingAuthUser().getFavorite(folderModel.getNodeRef());
        restClient.assertStatusCodeIs(HttpStatus.OK);

        favoriteFolder.assertThat().field("targetGuid").is(folderModel.getNodeRef())
                .and().field("target.folder.isFile").is("false")
                .and().field("target.folder.isFolder").is("true")
                .and().field("target.folder.createdBy").is(adminUserModel.getUsername())
                .and().field("target.folder.name").is(folderModel.getName())
                .and().field("target.folder.guid").is(folderModel.getNodeRef())
                .and().field("target.folder.modifiedBy").is(adminUserModel.getUsername())
                .and().field("target.folder.id").is(folderModel.getNodeRef());
    }
}