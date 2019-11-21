package org.alfresco.rest.people.preferences;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestPreferenceModelsCollection;
import org.alfresco.utility.constants.PreferenceName;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
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
public class GetPeoplePreferencesCoreTests extends RestTest
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
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE, TestGroup.PREFERENCES }, executionType = ExecutionType.REGRESSION, description = "Verify user gets its preferences with invalid maxItems parameter with Rest API and response is 400")
    public void userGetsPeoplePreferencesUsingInvalidMaxItemsParameter() throws Exception
    {
        restPreferenceModelsCollection = restClient.authenticateUser(user1).withCoreAPI().usingMe().usingParams("maxItems=0").getPersonPreferences();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
            .assertLastError().containsErrorKey(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                    .containsSummary(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                    .stackTraceIs(RestErrorModel.STACKTRACE)
                    .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER);
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES }, executionType = ExecutionType.REGRESSION, description = "Verify user gets its preferences with skipCount parameter applied with Rest API and response is successful")
    public void userGetsPeoplePreferencesUsingInvalidSkipCountParameter() throws Exception
    {
        restPreferenceModelsCollection = restClient.authenticateUser(user1).withCoreAPI().usingMe().usingParams("skipCount=-1").getPersonPreferences();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
            .assertLastError().containsErrorKey(RestErrorModel.NEGATIVE_VALUES_SKIPCOUNT)
                        .containsSummary(RestErrorModel.NEGATIVE_VALUES_SKIPCOUNT)
                        .stackTraceIs(RestErrorModel.STACKTRACE)
                        .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER);
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE, TestGroup.PREFERENCES }, executionType = ExecutionType.REGRESSION, description = "Verify user which doesn't have access to another user preferences fails to get its preferences with Rest API and response is permission denied")
    public void userWithNoAccessToOtherUserPreferencesIsForbiddenToGetItsPreferences() throws Exception
    {
        UserModel noAccessUser = dataUser.createRandomTestUser();
        dataSite.usingUser(noAccessUser).usingSite(siteModel).addSiteToFavorites();
        
        restPreferenceModelsCollection = restClient.authenticateUser(user1).withCoreAPI().usingUser(noAccessUser).getPersonPreferences();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN);
        restClient.assertLastError().containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
            .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
            .stackTraceIs(RestErrorModel.STACKTRACE)
            .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER);
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE, TestGroup.PREFERENCES }, executionType = ExecutionType.REGRESSION, description = "Verify user fails to get preferences for inexistent personId with Rest API and response is 404")
    public void userCannotGetPeoplePreferencesForInexistentPersonId() throws Exception
    {
        UserModel inexistentUserName = new UserModel("inexistent", "password");
        
        restPreferenceModelsCollection = restClient.authenticateUser(user1).withCoreAPI().usingUser(inexistentUserName).getPersonPreferences();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
        restClient.assertLastError().containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                            .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, inexistentUserName.getUsername()))
                            .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                            .stackTraceIs(RestErrorModel.STACKTRACE);
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.PREFERENCES }, executionType = ExecutionType.REGRESSION, description = "Verify user get preferences for a user with no preferences with Rest API and response is empty")
    public void userGetsPeoplePreferencesForUserWithNoPreferences() throws Exception
    {
        UserModel userNoActivities = dataUser.createRandomTestUser();
        
        restPreferenceModelsCollection = restClient.authenticateUser(adminUser).withCoreAPI().usingUser(userNoActivities).getPersonPreferences();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restPreferenceModelsCollection.assertThat().paginationField("count").is("0");
        restPreferenceModelsCollection.assertThat().entriesListIsEmpty();
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE, TestGroup.PREFERENCES }, executionType = ExecutionType.REGRESSION, description = "Verify user fails to get preferences for a removed preference with Rest API and response is 404")
    public void userFailsToGetPeoplePreferencesIfPreferenceWasRemoved() throws Exception
    {
        SiteModel preferenceSite = dataSite.usingUser(user1).createPublicRandomSite();
        dataSite.usingUser(user1).usingSite(preferenceSite).addSiteToFavorites();
        restPreferenceModelsCollection = restClient.authenticateUser(user1).withCoreAPI().usingAuthUser().getPersonPreferences();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restPreferenceModelsCollection.assertThat().paginationField("count").is("6");
        
        dataSite.usingUser(user1).usingSite(preferenceSite).removeSiteFromFavorites();
        restPreferenceModelsCollection = restClient.authenticateUser(user1).withCoreAPI().usingAuthUser().getPersonPreferences();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restPreferenceModelsCollection.assertThat().paginationField("count").is("5");
        restPreferenceModelsCollection.assertThat().entriesListDoesNotContain("id", String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), preferenceSite.getId()));
    }
}
