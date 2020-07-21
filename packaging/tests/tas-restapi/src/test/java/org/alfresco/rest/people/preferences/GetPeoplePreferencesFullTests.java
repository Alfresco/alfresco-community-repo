package org.alfresco.rest.people.preferences;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestPreferenceModelsCollection;
import org.alfresco.utility.constants.PreferenceName;
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

/**
 * 
 * @author Cristina Axinte
 *
 */
public class GetPeoplePreferencesFullTests extends RestTest
{
    UserModel userModel, user1, user2, adminUser;
    SiteModel siteModel;
    FolderModel folderModel;
    private RestPreferenceModelsCollection restPreferenceModelsCollection;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userModel = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        
        user1 = dataUser.createRandomTestUser();
        dataSite.usingUser(user1).usingSite(siteModel).addSiteToFavorites();
        dataContent.usingUser(user1).usingSite(siteModel).addFolderToFavorites(folderModel);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.PREFERENCES }, executionType = ExecutionType.REGRESSION, description = "Verify user gets its preferences with skipCount parameter applied with Rest API and response is successful")
    public void userGetsItsPeoplePreferencesUsingSkipCountParameter() throws Exception
    {
        restPreferenceModelsCollection = restClient.authenticateUser(user1).withCoreAPI().usingMe().usingParams("skipCount=2").getPersonPreferences();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restPreferenceModelsCollection.assertThat().paginationField("count").is("2");
        restPreferenceModelsCollection.assertThat().paginationField("skipCount").is("2");
        restPreferenceModelsCollection.assertThat().entriesListIsNotEmpty()
            .and().entriesListDoesNotContain("id", String.format(PreferenceName.EXT_FOLDERS_FAVORITES_PREFIX.toString(), "workspace://SpacesStore/" + folderModel.getNodeRef()))
            .and().entriesListDoesNotContain("id", String.format(PreferenceName.EXT_SITES_FAVORITES_PREFIX.toString(), siteModel.getId()))
            .and().entriesListContains("id", PreferenceName.FOLDERS_FAVORITES_PREFIX.toString())
            .and().entriesListContains("value", "workspace://SpacesStore/" + folderModel.getNodeRef())
            .and().entriesListContains("id", String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), siteModel.getId()))
            .and().entriesListContains("value", "true");
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE, TestGroup.PREFERENCES }, executionType = ExecutionType.REGRESSION, description = "Verify user gets its preferences with maxItems parameter applied with Rest API and response is successful")
    public void userGetsItsPeoplePreferencesUsingMaxItemsParameter() throws Exception
    {
        restPreferenceModelsCollection = restClient.authenticateUser(user1).withCoreAPI().usingMe().usingParams("maxItems=1").getPersonPreferences();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restPreferenceModelsCollection.assertThat().paginationField("count").is("1");
        restPreferenceModelsCollection.assertThat().paginationField("maxItems").is("1");
        restPreferenceModelsCollection.assertThat().entriesListIsNotEmpty()
            .and().entriesListContains("id", String.format(PreferenceName.EXT_FOLDERS_FAVORITES_PREFIX.toString(), "workspace://SpacesStore/" + folderModel.getNodeRef()))
            .and().entriesListDoesNotContain("id", String.format(PreferenceName.EXT_SITES_FAVORITES_PREFIX.toString(), siteModel.getId()))
            .and().entriesListDoesNotContain("id", PreferenceName.FOLDERS_FAVORITES_PREFIX.toString())
            .and().entriesListDoesNotContain("value", "workspace://SpacesStore/" + folderModel.getNodeRef())
            .and().entriesListDoesNotContain("id", String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), siteModel.getId()))
            .and().entriesListDoesNotContain("value", "true");
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE,TestGroup.PREFERENCES }, executionType = ExecutionType.REGRESSION, description = "Verify user gets admin preferences with Rest API and response is permission denied")
    public void userIsForbiddenToGetAdminPreferences() throws Exception
    {
        restPreferenceModelsCollection = restClient.authenticateUser(user1).withCoreAPI().usingUser(adminUser).getPersonPreferences();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN);
        restClient.assertLastError().containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
            .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
            .stackTraceIs(RestErrorModel.STACKTRACE)
            .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER);
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES }, executionType = ExecutionType.REGRESSION, description = "Verify user gets its preferences with skipCount parameter higher then no of entries with Rest API and response is empty")
    public void userGetsItsPeoplePreferencesUsingHighSkipCount() throws Exception
    {
        restPreferenceModelsCollection = restClient.authenticateUser(user1).withCoreAPI().usingMe().usingParams("skipCount=100").getPersonPreferences();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restPreferenceModelsCollection.assertThat().paginationField("count").is("0");
        restPreferenceModelsCollection.assertThat().paginationField("skipCount").is("100");
        restPreferenceModelsCollection.assertThat().entriesListIsEmpty();
    }
    
    @Bug(id = "REPO-1911")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.PREFERENCES }, executionType = ExecutionType.REGRESSION, description = "Verify user cannot get preferences for empty user with Rest API and response is 400")
    public void userGetsItsPeoplePreferencesForEmptyPersonId() throws Exception
    {
        UserModel emptyUserName = new UserModel("", "password");
        
        restPreferenceModelsCollection = restClient.authenticateUser(user1).withCoreAPI().usingUser(emptyUserName).getPersonPreferences();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
        .assertLastError().containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                            .containsSummary(RestErrorModel.LOCAL_NAME_CONSISTANCE)
                            .stackTraceIs(RestErrorModel.STACKTRACE)
                            .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER);
    }
}
