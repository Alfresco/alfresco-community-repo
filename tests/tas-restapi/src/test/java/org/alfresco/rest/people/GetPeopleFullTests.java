package org.alfresco.rest.people;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.model.RestPersonModel;
import org.alfresco.rest.model.RestPersonModelsCollection;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetPeopleFullTests extends RestTest
{
    UserModel userModel;
    SiteModel siteModel;
    UserModel searchedUser, managerUser;
    UserModel adminUser;
    private RestPersonModel personModel;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userModel = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        searchedUser = dataUser.createRandomTestUser();
        managerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(managerUser, siteModel, UserRole.SiteManager);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify entry details for get person response with Rest API")
    public void checkResponseSchemaForGetPerson() throws Exception
    {
        RestPersonModel newUser = RestPersonModel.getRandomPersonModel("aspectNames", "avatarId", "statusUpdatedAt","displayName");
        newUser = restClient.authenticateUser(adminUser).withCoreAPI().usingAuthUser().createPerson(newUser);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        personModel = restClient.authenticateUser(userModel).withCoreAPI().usingUser(new UserModel(newUser.getId(), newUser.getPassword())).getPerson();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        
        personModel.assertThat().field("id").is(newUser.getId())
            .and().field("firstName").is(newUser.getFirstName())
            .and().field("lastName").is(newUser.getLastName())
            .and().field("description").is(newUser.getDescription())
            .and().field("email").is(newUser.getEmail())
            .and().field("skypeId").is(newUser.getSkypeId())
            .and().field("googleId").is(newUser.getGoogleId())
            .and().field("instantMessageId").is(newUser.getInstantMessageId())
            .and().field("jobTitle").is(newUser.getJobTitle())
            .and().field("location").is(newUser.getLocation())
            .and().field("mobile").is(newUser.getMobile())
            .and().field("telephone").is(newUser.getTelephone())
            .and().field("userStatus").is(newUser.getUserStatus())
            .and().field("enabled").is(newUser.getEnabled())
            .and().field("emailNotificationsEnabled").is(newUser.getEmailNotificationsEnabled());
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify user gets a person with empty personId with Rest API and response is successful")
    public void userGetPersonWithEmptyPersonId() throws Exception
    {
        restClient.authenticateUser(managerUser).withCoreAPI();
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "people/{personId}?{parameters}", "", restClient.getParameters());
        RestPersonModelsCollection persons = restClient.processModels(RestPersonModelsCollection.class, request);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        persons.assertThat().entriesListIsNotEmpty();
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify user gets admin user with Rest API and response is successful")
    public void managerUserGetAdminPerson() throws Exception
    {
        personModel = restClient.authenticateUser(managerUser).withCoreAPI().usingUser(adminUser).getPerson();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        personModel.assertThat().field("id").is(adminUser.getUsername())
                    .and().field("firstName").is("Administrator")
                    .and().field("email").is("admin@alfresco.com")
                    .and().field("emailNotificationsEnabled").is("true")
                    .and().field("enabled").is("true");
    }
}
