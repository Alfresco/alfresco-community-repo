package org.alfresco.rest.people.preferences;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestPreferenceModel;
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
 * Tests for Get a Peference (/people/{personId}/preferences/{preferenceName}) RestAPI call
 * 
 * @author Cristina Axinte
 */

public class GetPeoplePreferenceSanityTests extends RestTest
{
    UserModel userModel;
    SiteModel siteModel;
    private RestPreferenceModel restPreferenceModel;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        userModel = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.PREFERENCES }, executionType = ExecutionType.SANITY, description = "Verify manager user gets a specific preference with Rest API and response is successful (200)")
    public void managerUserGetsAPreferenceWithSuccess() throws Exception
    {
        UserModel managerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(managerUser, siteModel, UserRole.SiteManager);
        dataSite.usingUser(managerUser).usingSite(siteModel).addSiteToFavorites();

        restPreferenceModel = restClient.authenticateUser(managerUser).withCoreAPI().usingAuthUser()
                .getPersonPreferenceInformation(String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), siteModel.getId()));
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restPreferenceModel.assertThat().field("id").is(String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), siteModel.getId())).and().field("value").is("true");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.PREFERENCES }, executionType = ExecutionType.SANITY, description = "Verify collaborator user gets a specific preference with Rest API and response is successful (200)")
    public void collaboratorUserGetsAPreferenceWithSuccess() throws Exception
    {
        UserModel collaboratorUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(collaboratorUser, siteModel, UserRole.SiteCollaborator);
        dataSite.usingUser(collaboratorUser).usingSite(siteModel).addSiteToFavorites();

        restPreferenceModel = restClient.authenticateUser(collaboratorUser).withCoreAPI().usingAuthUser()
                .getPersonPreferenceInformation(String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), siteModel.getId()));
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restPreferenceModel.assertThat().field("id").is(String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), siteModel.getId())).and().field("value").is("true");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.PREFERENCES }, executionType = ExecutionType.SANITY, description = "Verify contributor user gets a specific preference with Rest API and response is successful (200)")
    public void contributorUserGetsAPreferenceWithSuccess() throws Exception
    {
        UserModel contributorUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(contributorUser, siteModel, UserRole.SiteContributor);
        dataSite.usingUser(contributorUser).usingSite(siteModel).addSiteToFavorites();

        restPreferenceModel = restClient.authenticateUser(contributorUser).withCoreAPI().usingAuthUser()
                .getPersonPreferenceInformation(String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), siteModel.getId()));
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restPreferenceModel.assertThat().field("id").is(String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), siteModel.getId())).and().field("value").is("true");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE, TestGroup.PREFERENCES }, executionType = ExecutionType.SANITY, description = "Verify consumer user gets a specific preference with Rest API and response is successful (200)")
    public void consumerUserGetsAPreferenceWithSuccess() throws Exception
    {
        UserModel consumerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(consumerUser, siteModel, UserRole.SiteConsumer);
        dataSite.usingUser(consumerUser).usingSite(siteModel).addSiteToFavorites();

        restPreferenceModel = restClient.authenticateUser(consumerUser).withCoreAPI().usingAuthUser()
                .getPersonPreferenceInformation(String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(),siteModel.getId()));
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restPreferenceModel.assertThat().field("id").is(String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(),siteModel.getId())).and().field("value").is("true");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.PREFERENCES }, executionType = ExecutionType.SANITY, description = "Verify admin user gets a specific preference with Rest API and response is successful (200)")
    public void adminUserGetsAPreferenceWithSuccess() throws Exception
    {
        UserModel adminUser = dataUser.getAdminUser();
        dataSite.usingUser(adminUser).usingSite(siteModel).addSiteToFavorites();

        restPreferenceModel = restClient.authenticateUser(adminUser).withCoreAPI().usingAuthUser()
                .getPersonPreferenceInformation(String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), siteModel.getId()));
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restPreferenceModel.assertThat().field("id").is(String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), siteModel.getId())).and().field("value").is("true");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.PREFERENCES }, executionType = ExecutionType.SANITY, description = "Verify manager user is NOT Authorized to get a specific preference with Rest API when authentication fails (401)")
//    @Bug(id = "MNT-16904", description = "fails only on environment with tenants")
    public void managerUserNotAuthorizedFailsToGetsAPreference() throws Exception
    {
        UserModel managerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(managerUser, siteModel, UserRole.SiteManager);
        dataSite.usingUser(managerUser).usingSite(siteModel).addSiteToFavorites();
        managerUser.setPassword("newpassword");

        restClient.authenticateUser(managerUser).withCoreAPI().usingAuthUser()
                .getPersonPreferenceInformation(String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), siteModel.getId()));
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }
}
