package org.alfresco.rest.people.activities;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestActivityModelsCollection;
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
 *         Tests for getActivities (/people/{personId}/activities) RestAPI call
 */

public class GetPeopleActivitiesSanityTests extends RestTest
{
    UserModel userModel, unauthenticatedUser;
    SiteModel siteModel;
    private DataUser.ListUserWithRoles usersWithRoles;
    private RestActivityModelsCollection restActivityModelsCollection;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        userModel = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        dataContent.usingUser(userModel).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        usersWithRoles = dataUser.addUsersWithRolesToSite(siteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);
        unauthenticatedUser = dataUser.usingAdmin().createRandomTestUser();
        unauthenticatedUser.setPassword("newpassword");
        
        // only once the activity list is checked with retry in order not to wait the entire list in each test
        restActivityModelsCollection = restClient.authenticateUser(userModel).withCoreAPI().usingAuthUser().getPersonActivitiesUntilEntriesCountIs(6);
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE, TestGroup.ACTIVITIES }, executionType = ExecutionType.SANITY, description = "Verify manager user gets its activities with Rest API and response is successful")
    public void managerUserShouldGetPeopleActivitiesList() throws Exception
    {
        UserModel managerUser = usersWithRoles.getOneUserWithRole(UserRole.SiteManager);
        dataContent.usingUser(managerUser).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);

        restActivityModelsCollection = restClient.authenticateUser(managerUser).withCoreAPI()
                .usingAuthUser().getPersonActivitiesUntilEntriesCountIs(6);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restActivityModelsCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListContains("siteId", siteModel.getId()).and().paginationExist();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE, TestGroup.ACTIVITIES }, executionType = ExecutionType.SANITY, description = "Verify collaborator user gets its activities with Rest API and response is successful")
    public void collaboratorUserShouldGetPeopleActivitiesList() throws Exception
    {
        UserModel collaboratorUser = usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator);
        dataContent.usingUser(collaboratorUser).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);

        restActivityModelsCollection = restClient.authenticateUser(collaboratorUser).withCoreAPI()
                .usingAuthUser().getPersonActivitiesUntilEntriesCountIs(6);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restActivityModelsCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListContains("siteId", siteModel.getId()).and().paginationExist();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.ACTIVITIES }, executionType = ExecutionType.SANITY, description = "Verify contributor user gets its activities with Rest API and response is successful")
    public void contributorUserShouldGetPeopleActivitiesList() throws Exception
    {
        UserModel contributorUser = usersWithRoles.getOneUserWithRole(UserRole.SiteContributor);
        dataContent.usingUser(contributorUser).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);

        restActivityModelsCollection = restClient.authenticateUser(contributorUser).withCoreAPI()
                .usingAuthUser().getPersonActivitiesUntilEntriesCountIs(6);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restActivityModelsCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListContains("siteId", siteModel.getId()).and().paginationExist();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.ACTIVITIES }, executionType = ExecutionType.SANITY, description = "Verify consumer user gets its activities with Rest API and response is successful")
    public void consumerUserShouldGetPeopleActivitiesList() throws Exception
    {
        UserModel consumerUser = usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer);

        restActivityModelsCollection = restClient.authenticateUser(consumerUser).withCoreAPI()
                .usingAuthUser().getPersonActivitiesUntilEntriesCountIs(6);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restActivityModelsCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListContains("siteId", siteModel.getId()).and().paginationExist();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.ACTIVITIES }, executionType = ExecutionType.SANITY, description = "Verify admin user gets another user activities with Rest API and response is successful")
    public void adminUserShouldGetPeopleActivitiesList() throws Exception
    {
        restActivityModelsCollection = restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI()
                .usingUser(userModel).getPersonActivitiesUntilEntriesCountIs(6);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restActivityModelsCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListContains("siteId", siteModel.getId()).and().paginationExist();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API,  TestGroup.PEOPLE,TestGroup.ACTIVITIES }, executionType = ExecutionType.SANITY, description = "Verify unauthenticated user is NOT Authorized to gets another user activities with Rest API")
    public void unauthenticatedUserShouldNotGetPeopleActivitiesList() throws Exception
    {
        restClient.authenticateUser(unauthenticatedUser).withCoreAPI().usingUser(userModel).getPersonActivities();
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }
}
