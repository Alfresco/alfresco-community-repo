package org.alfresco.rest.favorites;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestSiteModel;
import org.alfresco.rest.model.RestSiteModelsCollection;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetFavoriteSitesTests extends RestTest
{
    private UserModel userModel, testUser1;
    private SiteModel siteModel, testSite1, testSite2, testSite3;
    private RestSiteModelsCollection restSiteModelsCollection;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        userModel = dataUser.createRandomTestUser();
        testUser1 = dataUser.createRandomTestUser();

        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        testSite1 = new SiteModel("A-" + RandomData.getRandomAlphanumeric());
        testSite2 = new SiteModel("B-" + RandomData.getRandomAlphanumeric());
        testSite3 = new SiteModel("C-" + RandomData.getRandomAlphanumeric());

        dataSite.usingUser(userModel).usingSite(siteModel).addSiteToFavorites();

        dataSite.usingUser(testUser1).createSite(testSite1);
        dataSite.usingUser(testUser1).createSite(testSite2);
        dataSite.usingUser(testUser1).createSite(testSite3);

        dataSite.usingUser(testUser1).usingSite(testSite3).addSiteToFavorites();
        dataSite.usingUser(testUser1).usingSite(testSite2).addSiteToFavorites();
        dataSite.usingUser(testUser1).usingSite(testSite1).addSiteToFavorites();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify manager user fails to get an user favorite sites with Rest API (403)")
    public void managerUserFailsToGetAnUserFavoriteSites() throws Exception
    {
        UserModel managerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(managerUser, siteModel, UserRole.SiteManager);
        UserModel anotherUser = dataUser.usingAdmin().createRandomTestUser();
        dataSite.usingUser(anotherUser).usingSite(siteModel).addSiteToFavorites();

        restClient.authenticateUser(managerUser).withCoreAPI().usingUser(anotherUser).getFavoriteSites();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify collaborator user fails to get an user favorite sites with Rest API (403)")
    public void collaboratorUserFailsToGetAnUserFavoriteSites() throws Exception
    {
        UserModel collaboratorUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(collaboratorUser, siteModel, UserRole.SiteCollaborator);
        UserModel contributorUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(contributorUser, siteModel, UserRole.SiteContributor);
        dataSite.usingUser(contributorUser).usingSite(siteModel).addSiteToFavorites();

        restClient.authenticateUser(collaboratorUser).withCoreAPI().usingUser(contributorUser).getFavoriteSites();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify contributor user fails to get an user favorite sites with Rest API (403)")
    public void contributorUserFailsToGetAnUserFavoriteSites() throws Exception
    {
        UserModel contributorUser = dataUser.usingAdmin().createRandomTestUser();
        UserModel contributorUser2 = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(contributorUser, siteModel, UserRole.SiteContributor);
        dataUser.usingUser(userModel).addUserToSite(contributorUser2, siteModel, UserRole.SiteContributor);
        dataSite.usingUser(contributorUser2).usingSite(siteModel).addSiteToFavorites();

        restClient.authenticateUser(contributorUser).withCoreAPI().usingUser(contributorUser2).getFavoriteSites();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify consumer user fails to get an user favorite sites with Rest API (403)")
    public void consumerUserFailsToGetAnUserFavoriteSites() throws Exception
    {
        UserModel consumerUser = dataUser.usingAdmin().createRandomTestUser();
        UserModel collaboratorUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(consumerUser, siteModel, UserRole.SiteConsumer);
        dataUser.usingUser(collaboratorUser).addUserToSite(collaboratorUser, siteModel, UserRole.SiteCollaborator);
        dataSite.usingUser(collaboratorUser).usingSite(siteModel).addSiteToFavorites();

        restClient.authenticateUser(consumerUser).withCoreAPI().usingUser(collaboratorUser).getFavoriteSites();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify any user gets its own user favorite sites with Rest API and response is successful (200)")
    public void anyUserGetsHisFavoriteSites() throws Exception
    {
        UserModel anyUser = dataUser.usingAdmin().createRandomTestUser();
        dataSite.usingUser(anyUser).usingSite(siteModel).addSiteToFavorites();

        restSiteModelsCollection = restClient.authenticateUser(anyUser).withCoreAPI().usingAuthUser().getFavoriteSites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteModelsCollection.assertThat().entriesListIsNotEmpty().and().entriesListContains("id", siteModel.getId()).and().paginationExist();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })    
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify any user is NOT Authorized to get its favorite sites with Rest API when authentication fails (401)")
//    @Bug(id = "MNT-16904", description = "It fails only on environment with tenants")
    public void unauthenticatedUserIsNotAuthorizedToGetFavoriteSites() throws Exception
    {
        UserModel anyUser = dataUser.usingAdmin().createRandomTestUser();
        dataSite.usingUser(anyUser).usingSite(siteModel).addSiteToFavorites();
        anyUser.setPassword("newpassword");

        restClient.authenticateUser(anyUser).withCoreAPI().usingAuthUser().getFavoriteSites();
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify that getFavoriteSites request status code is 404 for a personId that does not exist")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void getFavoriteSitesReturns404ForAPersonIdThatDoesNotExist() throws Exception
    {
        UserModel invalidUser = new UserModel(RandomData.getRandomName("User"), DataUser.PASSWORD);

        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingUser(invalidUser).getFavoriteSites();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, invalidUser.getUsername()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify that getFavoriteSites request status code is 403 if the user doesn't have access to the person favorite sites")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void userCannotGetFavoriteSitesForAnotherUser() throws Exception
    {
        restClient.authenticateUser(userModel).withCoreAPI().usingUser(testUser1).getFavoriteSites();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError()
                .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify that admin has access to regular user site favorites")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void adminCanGetFavoriteSitesForARegularUser() throws Exception
    {
        restSiteModelsCollection = restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingUser(userModel).getFavoriteSites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteModelsCollection.assertThat().entriesListIsNotEmpty()
                .assertThat().entriesListContains("id", siteModel.getId());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify that regular user can not see admin user site favorites")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void regularUserCannotGetFavoriteSitesForAdminUser() throws Exception
    {
        restClient.authenticateUser(testUser1).withCoreAPI().usingUser(dataUser.getAdminUser()).getFavoriteSites();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError()
                .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify that favorite site can be removed")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void verifyThatFavoriteSiteCanBeRemoved() throws Exception
    {
        UserModel randomTestUser = dataUser.createRandomTestUser();
        dataSite.usingUser(randomTestUser).usingSite(siteModel).addSiteToFavorites();

        restSiteModelsCollection = restClient.authenticateUser(randomTestUser).withCoreAPI().usingAuthUser().getFavoriteSites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteModelsCollection.assertThat().entriesListIsNotEmpty()
                .assertThat().entriesListContains("id", siteModel.getId());

        dataSite.usingUser(randomTestUser).usingSite(siteModel).removeSiteFromFavorites();
        restSiteModelsCollection = restClient.withCoreAPI().usingAuthUser().getFavoriteSites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteModelsCollection.assertThat().entriesListIsEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify that if maxItems param is invalid status code returned is BAD_REQUEST (400)")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void getFavoriteSitesMaxItemsInvalidValueTest() throws Exception
    {
        restClient.authenticateUser(userModel).withParams("maxItems=-1").withCoreAPI().usingAuthUser().getFavoriteSites();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS);

        restClient.authenticateUser(userModel).withParams("maxItems=abc").withCoreAPI().usingAuthUser().getFavoriteSites();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.INVALID_MAXITEMS, "abc"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify that if skipCount param is invalid status code returned is BAD_REQUEST (400)")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void getFavoriteSitesSkipCountInvalidValueTest() throws Exception
    {
        restClient.authenticateUser(userModel).withParams("skipCount=-1").withCoreAPI().usingAuthUser().getFavoriteSites();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(RestErrorModel.NEGATIVE_VALUES_SKIPCOUNT);

        restClient.authenticateUser(userModel).withParams("skipCount=abc").withCoreAPI().usingAuthUser().getFavoriteSites();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.INVALID_SKIPCOUNT, "abc"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify that getFavoriteSites request response status using -me- string in place of personId is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void userGetsFavoriteSiteWithSuccessUsingMEForRequest() throws Exception
    {
        restSiteModelsCollection = restClient.authenticateUser(testUser1).withCoreAPI().usingMe().getFavoriteSites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteModelsCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListContains("id", testSite1.getId())
                .and().paginationExist();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify that getFavoriteSites request response status for a user that has no favorite sites is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void getFavoriteSiteRequestForUserWithNoFavoriteSitesIsSuccessful() throws Exception
    {
        UserModel randomTestUser = dataUser.createRandomTestUser();

        restSiteModelsCollection = restClient.authenticateUser(randomTestUser).withCoreAPI().usingMe().getFavoriteSites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteModelsCollection.assertThat().entriesListIsEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify that getFavoriteSites request response status for a user with several favorite sites is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void getFavoriteSiteRequestForUserWithSeveralFavoriteSitesIsSuccessful() throws Exception
    {
        restSiteModelsCollection = restClient.authenticateUser(testUser1).withCoreAPI().usingAuthUser().getFavoriteSites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteModelsCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListContains("id", testSite1.getId())
                .and().entriesListContains("id", testSite2.getId())
                .and().entriesListContains("id", testSite3.getId())
                .and().paginationExist();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify that getFavoriteSites request can only be called with a positive maxItems param")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void userCanNotGetFavoriteSitesWith0MaxItems() throws Exception
    {
        restClient.authenticateUser(testUser1).withParams("maxItems=0").withCoreAPI().usingAuthUser().getFavoriteSites();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError()
                .containsErrorKey(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                .containsSummary(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify that getFavoriteSites request response status code for a high skipCount param is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void userCanGetFavoriteSitesWithHighSkipCount() throws Exception
    {
        restSiteModelsCollection = restClient.authenticateUser(testUser1).withParams("skipCount=999999999").withCoreAPI().usingAuthUser().getFavoriteSites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteModelsCollection.assertThat().entriesListIsEmpty()
                .assertThat().paginationField("skipCount").is("999999999");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify that user can retrieve the last 2 favorite sites")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void userCanRetrieveLast2FavoriteSites() throws Exception
    {
        restSiteModelsCollection = restClient.authenticateUser(testUser1).withParams("skipCount=1&maxCount=2").
                withCoreAPI().usingAuthUser().getFavoriteSites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteModelsCollection.assertThat().entriesListIsNotEmpty()
                .assertThat().entriesListCountIs(2)
                .assertThat().entriesListContains("id", testSite2.getId())
                .assertThat().entriesListContains("id", testSite3.getId());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify that getFavoriteSites request applies valid properties param")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void getFavoriteSitesRequestWithValidPropertiesParam() throws Exception
    {
        RestSiteModel restSiteModel = restClient.authenticateUser(testUser1).withParams("properties=title")
                .withCoreAPI().usingAuthUser().getFavoriteSites().getOneRandomEntry().onModel();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteModel.assertThat().fieldsCount().is(1)
                .assertThat().field("title").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify that getFavoriteSites request applies invalid properties param")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void getFavoriteSitesRequestWithInvalidPropertiesParam() throws Exception
    {
        restClient.authenticateUser(testUser1).withParams("properties=tas").withCoreAPI().usingAuthUser()
                .getFavoriteSites().getOneRandomEntry().onModel().assertThat().fieldsCount().is(0);
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }
}
