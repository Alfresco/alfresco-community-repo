package org.alfresco.rest.favorites;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.dataprep.SiteService;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestSiteModel;
import org.alfresco.rest.model.RestSiteModelsCollection;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for Get Favorite Sites (/people/{personId}/preferences) RestAPI call
 * 
 * @author Cristina Axinte
 */

public class GetFavoriteSiteTests extends RestTest
{
    private UserModel userModel;
    private SiteModel siteModel1;
    private SiteModel siteModel2;
    private RestSiteModel restSiteModel;
    private DataUser.ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        userModel = dataUser.createRandomTestUser();
        siteModel1 = dataSite.usingUser(userModel).createPublicRandomSite();
        siteModel2 = dataSite.usingUser(userModel).createPublicRandomSite();

        usersWithRoles = dataUser.addUsersWithRolesToSite(siteModel1, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);

        dataSite.usingUser(userModel).usingSite(siteModel1).addSiteToFavorites();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify manager user gets its specific favorite site with Rest API and response is successful (200)")
    public void managerUserGetsFavoriteSiteWithSuccess() throws Exception
    {
        UserModel managerUser = usersWithRoles.getOneUserWithRole(UserRole.SiteManager);

        dataSite.usingUser(managerUser).usingSite(siteModel1).addSiteToFavorites();
        dataSite.usingUser(managerUser).usingSite(siteModel2).addSiteToFavorites();

        restSiteModel = restClient.authenticateUser(managerUser).withCoreAPI().usingAuthUser().getFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteModel.assertThat().field("id").is(siteModel1.getId()).and().field("title").isNotNull();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify collaborator user gets its specific favorite site with Rest API and response is successful (200)")
    public void collaboratorUserGetsFavoriteSiteWithSuccess() throws Exception
    {
        UserModel collaboratorUser = usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator);
        
        dataSite.usingUser(collaboratorUser).usingSite(siteModel1).addSiteToFavorites();
        dataSite.usingUser(collaboratorUser).usingSite(siteModel2).addSiteToFavorites();

        restSiteModel = restClient.authenticateUser(collaboratorUser).withCoreAPI().usingAuthUser().getFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteModel.assertThat().field("id").is(siteModel1.getId()).and().field("title").isNotNull();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify contributor user gets its specific favorite site with Rest API and response is successful (200)")
    public void contributorUserGetsFavoriteSiteWithSuccess() throws Exception
    {
        UserModel contributorUser = usersWithRoles.getOneUserWithRole(UserRole.SiteContributor);
        
        dataSite.usingUser(contributorUser).usingSite(siteModel1).addSiteToFavorites();
        dataSite.usingUser(contributorUser).usingSite(siteModel2).addSiteToFavorites();

        restSiteModel = restClient.authenticateUser(contributorUser).withCoreAPI().usingAuthUser().getFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteModel.assertThat().field("id").is(siteModel1.getId()).and().field("title").isNotNull();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify consumer user gets its specific favorite site with Rest API and response is successful (200)")
    public void consumerUserGetsFavoriteSiteWithSuccess() throws Exception
    {
        UserModel consumerUser = usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer);
        
        dataSite.usingUser(consumerUser).usingSite(siteModel1).addSiteToFavorites();
        dataSite.usingUser(consumerUser).usingSite(siteModel2).addSiteToFavorites();

        restSiteModel = restClient.authenticateUser(consumerUser).withCoreAPI().usingAuthUser().getFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteModel.assertThat().field("id").is(siteModel1.getId()).and().field("title").isNotNull();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify admin user gets specific favorite site of any user with Rest API and response is successful (200)")
    public void adminUserGetsAnyFavoriteSiteWithSuccess() throws Exception
    {
        UserModel adminUser = dataUser.getAdminUser();
        UserModel anyUser = dataUser.usingAdmin().createRandomTestUser();
        dataSite.usingUser(anyUser).usingSite(siteModel1).addSiteToFavorites();
        dataSite.usingUser(anyUser).usingSite(siteModel2).addSiteToFavorites();

        restSiteModel = restClient.authenticateUser(adminUser).withCoreAPI().usingUser(anyUser).getFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteModel.assertThat().field("id").is(siteModel1.getId()).and().field("title").isNotNull();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify manager user fails to get specific favorite site of another user with Rest API and response is successful (403)")
    public void managerUserFailsToGetFavoriteSiteOfAnotherUser() throws Exception
    {
        UserModel managerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(managerUser, siteModel1, UserRole.SiteManager);
        dataSite.usingUser(managerUser).usingSite(siteModel1).addSiteToFavorites();
        dataSite.usingUser(managerUser).usingSite(siteModel2).addSiteToFavorites();

        restClient.authenticateUser(managerUser).withCoreAPI().usingUser(userModel).getFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify manager user is NOT Authorized gets its specific favorite site with Rest API when authentication fails (401)")
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void managerUserNotAuthorizedFailsToGetFavoriteSite() throws Exception
    {
        UserModel managerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(managerUser, siteModel1, UserRole.SiteManager);
        dataSite.usingUser(managerUser).usingSite(siteModel1).addSiteToFavorites();
        dataSite.usingUser(managerUser).usingSite(siteModel2).addSiteToFavorites();
        managerUser.setPassword("newpassword");

        restClient.authenticateUser(managerUser).withCoreAPI().usingAuthUser().getFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify invalid request returns status 404 when personId does not exist")
    public void getFavoriteSiteWithNonExistentPersonId() throws Exception
    {
        UserModel someUser = new UserModel("someUser", DataUser.PASSWORD);

        restClient.authenticateUser(userModel).withCoreAPI().usingUser(someUser).getFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "someUser"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify invalid request returns status 404 when siteId does not exist")
    public void getFavoriteSiteWithNonExistentSiteId() throws Exception
    {
        SiteModel nonExistentSite = new SiteModel("nonExistentSite");

        restClient.authenticateUser(userModel).withCoreAPI().usingAuthUser().getFavoriteSite(nonExistentSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, userModel.getUsername(), nonExistentSite.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify User fails to get specific favorite site of admin with Rest API and response is 403")
    public void userFailsToGetFavoriteSiteOfAdmin() throws Exception
    {
        restClient.authenticateUser(userModel).withCoreAPI().usingUser(dataUser.getAdminUser()).getFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify request with empty site id")
    public void getFavoriteSiteWithEmptySiteId() throws Exception
    {
        restClient.authenticateUser(userModel).withCoreAPI();
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "people/{personId}/favorite-sites/{siteId}?{parameters}",
                userModel.getUsername(), "", restClient.getParameters());
        RestSiteModelsCollection sites = restClient.processModels(RestSiteModelsCollection.class, request);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sites.assertThat().entriesListIsNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify invalid request returns status 401 when user is empty")
    public void getFavoriteSiteWithEmptyPersonId() throws Exception
    {
        UserModel emptyUser = new UserModel("", "password");
        restClient.authenticateUser(emptyUser).withCoreAPI().usingAuthUser().getFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify invalid request returns status 404 when another user get favorite site of an user")
    public void getFavoriteSiteWithAnotherUser() throws Exception
    {
        UserModel user = dataUser.createRandomTestUser();
        restClient.authenticateUser(user).withCoreAPI().usingAuthUser().getFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, user.getUsername(), siteModel1.getId()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE)
                .containsErrorKey(RestErrorModel.RELATIONSHIP_NOT_FOUND_ERRORKEY);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify request returns status 200 when using -me-")
    public void getFavoriteSiteUsingMe() throws Exception
    {
        restSiteModel = restClient.authenticateUser(userModel).withCoreAPI().usingMe().getFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteModel.assertThat().field("id").is(siteModel1.getId())
                .and().field("title").is(siteModel1.getTitle())
                .and().field("role").is(UserRole.SiteManager.toString())
                .and().field("visibility").is(SiteService.Visibility.PUBLIC.toString())
                .and().field("guid").isNotEmpty()
                .and().field("description").is(siteModel1.getDescription())
                .and().field("preset").is("site-dashboard");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify invalid request returns status 404 when providing folder name instead of site id")
    public void getFavoriteSiteUsingFolder() throws Exception
    {
        FolderModel folder = dataContent.usingUser(userModel).usingSite(siteModel1).createFolder();
        SiteModel siteFolder = new SiteModel(folder.getName());
        restSiteModel = restClient.authenticateUser(userModel).withCoreAPI().usingMe().getFavoriteSite(siteFolder);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, userModel.getUsername(), folder.getName()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify invalid request returns status 404 when providing file name instead of site id")
    public void getFavoriteSiteUsingFile() throws Exception
    {
        FileModel file = dataContent.usingUser(userModel).usingSite(siteModel1).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        SiteModel siteFolder = new SiteModel(file.getName());
        restSiteModel = restClient.authenticateUser(userModel).withCoreAPI().usingMe().getFavoriteSite(siteFolder);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, userModel.getUsername(), file.getName()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify request returns status 200 when using valid parameters")
    public void getFavoriteSiteUsingParameters() throws Exception
    {
        restSiteModel = restClient.withParams("maxItems=100").authenticateUser(userModel).withCoreAPI().usingMe().getFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteModel.assertThat().field("id").is(siteModel1.getId())
                .and().field("title").is(siteModel1.getTitle());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify request returns status 400 when using maxItems=0")
    public void getFavoriteSiteUsingInvalidMaxItems() throws Exception
    {
        restSiteModel = restClient.withParams("maxItems=0").authenticateUser(userModel).withCoreAPI().usingMe().getFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify request returns status 401 when using invalid network")
    public void getFavoriteSiteUsingInvalidNetwork() throws Exception
    {
        UserModel invalidUserNetwork = dataUser.createRandomTestUser();
        SiteModel site = dataSite.usingUser(invalidUserNetwork).createPublicRandomSite();
        dataSite.usingUser(invalidUserNetwork).usingSite(site).addSiteToFavorites();
        invalidUserNetwork.setDomain("invalidNetwork");
        restSiteModel = restClient.authenticateUser(invalidUserNetwork).withCoreAPI().usingMe().getFavoriteSite(site);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }
}
