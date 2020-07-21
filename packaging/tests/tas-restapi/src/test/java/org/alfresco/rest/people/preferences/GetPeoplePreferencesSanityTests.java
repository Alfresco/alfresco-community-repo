package org.alfresco.rest.people.preferences;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestPreferenceModelsCollection;
import org.alfresco.utility.constants.PreferenceName;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for Get Peferences (/people/{personId}/preferences) RestAPI call
 * 
 * @author Cristina Axinte
 */

public class GetPeoplePreferencesSanityTests extends RestTest
{
    UserModel userModel;
    SiteModel siteModel;
    private RestPreferenceModelsCollection restPreferenceModelsCollection;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        userModel = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.PREFERENCES }, executionType = ExecutionType.SANITY, description = "Verify manager user gets its preferences with Rest API and response is successful (200)")
    public void managerUserGetsPeoplePreferencesWithSuccess() throws Exception
    {
        UserModel managerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(managerUser, siteModel, UserRole.SiteManager);
        dataSite.usingUser(managerUser).usingSite(siteModel).addSiteToFavorites();

        restPreferenceModelsCollection = restClient.authenticateUser(managerUser).withCoreAPI().usingAuthUser().getPersonPreferences();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restPreferenceModelsCollection.assertThat().entriesListIsNotEmpty().assertThat().paginationExist().and()
                .entriesListContains("id", String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), siteModel.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE, TestGroup.PREFERENCES }, executionType = ExecutionType.SANITY, description = "Verify collaborator user gets its preferences with Rest API and response is successful (200)")
    public void collaboratorUserGetsPeoplePreferencesWithSuccess() throws Exception
    {
        UserModel collaboratorUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(collaboratorUser, siteModel, UserRole.SiteCollaborator);
        dataSite.usingUser(collaboratorUser).usingSite(siteModel).addSiteToFavorites();

        restPreferenceModelsCollection = restClient.authenticateUser(collaboratorUser).withCoreAPI().usingAuthUser().getPersonPreferences();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restPreferenceModelsCollection.assertThat().entriesListIsNotEmpty().assertThat().paginationExist().and()
                .entriesListContains("id", String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), siteModel.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.PREFERENCES }, executionType = ExecutionType.SANITY, description = "Verify contributor user gets its preferences with Rest API and response is successful (200)")
    public void contributorUserGetsPeoplePreferencesWithSuccess() throws Exception
    {
        UserModel contributorUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(contributorUser, siteModel, UserRole.SiteContributor);
        dataSite.usingUser(contributorUser).usingSite(siteModel).addSiteToFavorites();

        restPreferenceModelsCollection = restClient.authenticateUser(contributorUser).withCoreAPI().usingAuthUser().getPersonPreferences();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restPreferenceModelsCollection.assertThat().entriesListIsNotEmpty().assertThat().paginationExist().and()
                .entriesListContains("id", String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), siteModel.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE, TestGroup.PREFERENCES }, executionType = ExecutionType.SANITY, description = "Verify consumer user gets its preferences with Rest API and response is successful (200)")
    public void consumerUserGetsPeoplePreferencesWithSuccess() throws Exception
    {
        UserModel consumerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(consumerUser, siteModel, UserRole.SiteConsumer);
        dataSite.usingUser(consumerUser).usingSite(siteModel).addSiteToFavorites();

        restPreferenceModelsCollection = restClient.authenticateUser(consumerUser).withCoreAPI().usingAuthUser().getPersonPreferences();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restPreferenceModelsCollection.assertThat().entriesListIsNotEmpty().assertThat().paginationExist().and()
                .entriesListContains("id", String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), siteModel.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.PREFERENCES }, executionType = ExecutionType.SANITY, description = "Verify admin user gets another user preferences with Rest API and response is successful (200)")
    public void adminUserGetsPeoplePreferencesWithSuccess() throws Exception
    {
        UserModel managerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(managerUser, siteModel, UserRole.SiteManager);
        dataSite.usingUser(managerUser).usingSite(siteModel).addSiteToFavorites();
        UserModel adminUser = dataUser.getAdminUser();

        restPreferenceModelsCollection = restClient.authenticateUser(adminUser).withCoreAPI().usingUser(managerUser).getPersonPreferences();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restPreferenceModelsCollection.assertThat().entriesListIsNotEmpty().assertThat().paginationExist().and()
                .entriesListContains("id", String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), siteModel.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.PREFERENCES }, executionType = ExecutionType.SANITY, description = "Verify manager user is NOT Authorized to gets its preferences with Rest API when authentication fails(401)")
//    @Bug(id = "MNT-16904", description = "It fails only on environment with tenants")
    public void managerUserGetsPeoplePreferencesIsNotAuthorized() throws Exception
    {
        UserModel managerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(managerUser, siteModel, UserRole.SiteManager);
        dataSite.usingUser(managerUser).usingSite(siteModel).addSiteToFavorites();
        managerUser.setPassword("newpassword");

        restClient.authenticateUser(managerUser).withCoreAPI().usingAuthUser().getPersonPreferences();
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }
}
