package org.alfresco.rest.people;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestPersonModel;
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

public class GetPeopleSanityTests extends RestTest
{
    UserModel userModel;
    SiteModel siteModel;
    UserModel searchedUser;
    UserModel adminUser;
    private RestPersonModel personModel;
    private String domain = "@tas-automation.org";

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userModel = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        searchedUser = dataUser.createRandomTestUser();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify manager user gets a person with Rest API and response is successful")
    public void managerUserChecksIfPersonIsPresent() throws Exception
    {
        UserModel managerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(managerUser, siteModel, UserRole.SiteManager);

        personModel = restClient.authenticateUser(managerUser).withCoreAPI().usingUser(searchedUser).getPerson();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        personModel.assertThat().field("id").is(searchedUser.getUsername()).assertThat().field("firstName").is(searchedUser.getUsername() + " FirstName").and()
                .field("email").is(searchedUser.getUsername() + domain).and().field("emailNotificationsEnabled").is("true");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify collaborator user gets a person with Rest API and response is successful")
    public void collaboratorUserChecksIfPersonIsPresent() throws Exception
    {
        UserModel collaboratorUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(collaboratorUser, siteModel, UserRole.SiteCollaborator);

        personModel = restClient.authenticateUser(collaboratorUser).withCoreAPI().usingUser(searchedUser).getPerson();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        personModel.assertThat().field("id").is(searchedUser.getUsername()).assertThat().field("firstName").is(searchedUser.getUsername() + " FirstName").and()
                .field("email").is(searchedUser.getUsername() + domain).and().field("emailNotificationsEnabled").is("true");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.COMMENTS, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify contributor user gets a person with Rest API and response is successful")
    public void contributorUserChecksIfPersonIsPresent() throws Exception
    {
        UserModel contributorUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(contributorUser, siteModel, UserRole.SiteContributor);

        personModel = restClient.authenticateUser(contributorUser).withCoreAPI().usingUser(searchedUser).getPerson();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        personModel.assertThat().field("id").is(searchedUser.getUsername()).assertThat().field("firstName").is(searchedUser.getUsername() + " FirstName").and()
                .field("email").is(searchedUser.getUsername() + domain).and().field("emailNotificationsEnabled").is("true");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify consumer user gets a person with Rest API and response is successful")
    public void consumerUserChecksIfPersonIsPresent() throws Exception
    {
        UserModel consumerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(consumerUser, siteModel, UserRole.SiteConsumer);

        personModel = restClient.authenticateUser(consumerUser).withCoreAPI().usingUser(searchedUser).getPerson();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        personModel.assertThat().field("id").is(searchedUser.getUsername()).assertThat().field("firstName").is(searchedUser.getUsername() + " FirstName").and()
                .field("email").is(searchedUser.getUsername() + domain).and().field("emailNotificationsEnabled").is("true");
        ;
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify admin user gets a person with Rest API and response is successful")
    public void adminUserChecksIfPersonIsPresent() throws Exception
    {
        personModel = restClient.authenticateUser(adminUser).withCoreAPI().usingUser(searchedUser).getPerson();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        personModel.assertThat().field("id").is(searchedUser.getUsername()).assertThat().field("firstName").is(searchedUser.getUsername() + " FirstName").and()
                .field("email").is(searchedUser.getUsername() + domain).and().field("emailNotificationsEnabled").is("true");
        ;
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify manager user gets a non existing person with Rest API and person is not found")
    public void managerUserChecksIfNonExistingPersonIsPresent() throws Exception
    {
        UserModel managerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(managerUser, siteModel, UserRole.SiteManager);
        UserModel searchedNonUser = new UserModel("nonexistinguser", DataUser.PASSWORD);

        restClient.authenticateUser(managerUser).withCoreAPI().usingUser(searchedNonUser).getPerson();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, ""));
    }
}