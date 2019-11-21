package org.alfresco.rest.sites.members;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestSiteMemberModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AddSiteMemberTests extends RestTest
{
    private UserModel adminUserModel;
    private SiteModel publicSiteModel, moderatedSiteModel, privateSiteModel;
    private ListUserWithRoles publicSiteUsersWithRoles, moderatedSiteUsersWithRoles, privateSiteUsersWithRoles;
    private String addMemberJson, addMembersJson;
    private RestSiteMemberModel memberModel;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        adminUserModel = dataUser.getAdminUser();        
        publicSiteModel = dataSite.usingUser(adminUserModel).createPublicRandomSite();
        moderatedSiteModel = dataSite.usingUser(adminUserModel).createModeratedRandomSite();
        privateSiteModel = dataSite.usingUser(adminUserModel).createPrivateRandomSite();
        publicSiteUsersWithRoles = dataUser.addUsersWithRolesToSite(publicSiteModel,
                UserRole.SiteManager,
                UserRole.SiteCollaborator,
                UserRole.SiteConsumer,
                UserRole.SiteContributor);
        moderatedSiteUsersWithRoles = dataUser.addUsersWithRolesToSite(moderatedSiteModel, 
                UserRole.SiteManager, 
                UserRole.SiteCollaborator,
                UserRole.SiteConsumer, 
                UserRole.SiteContributor);
        privateSiteUsersWithRoles = dataUser.addUsersWithRolesToSite(privateSiteModel, 
                UserRole.SiteManager, 
                UserRole.SiteCollaborator,
                UserRole.SiteConsumer, 
                UserRole.SiteContributor);
        addMemberJson = "{\"role\":\"%s\",\"id\":\"%s\"}";
        addMembersJson = "{\"role\":\"%s\",\"id\":\"%s\"}, {\"role\":\"%s\",\"id\":\"%s\"}";
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY, 
            description = "Verify that manager is able to add site member and gets status code CREATED (201)")
    public void managerIsAbleToAddSiteMember() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteCollaborator);
        restClient.authenticateUser(publicSiteUsersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingSite(publicSiteModel).addPerson(testUser)
               .assertThat().field("id").is(testUser.getUsername())
               .and().field("role").is(testUser.getUserRole());
        restClient.assertStatusCodeIs(HttpStatus.CREATED);       
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify that site collaborator is not able to add site member and gets status code FORBIDDEN (403)")
    public void collaboratorIsNotAbleToAddSiteMember() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteConsumer);
        restClient.authenticateUser(publicSiteUsersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                  .withCoreAPI().usingSite(publicSiteModel).addPerson(testUser);
        
        restClient.assertLastError().containsSummary("Permission was denied");
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN);       
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION})
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify that site contributor is not able to add site member and gets status code FORBIDDEN (403)")
    public void contributorIsNotAbleToAddSiteMember() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteConsumer);
        restClient.authenticateUser(publicSiteUsersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                  .withCoreAPI().usingSite(publicSiteModel).addPerson(testUser);        
        restClient.assertLastError().containsSummary("Permission was denied");
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN);       
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify that site consumer is not able to add site member and gets status code FORBIDDEN (403)")
    public void consumerIsNotAbleToAddSiteMember() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteConsumer);
        restClient.authenticateUser(publicSiteUsersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                  .withCoreAPI().usingSite(publicSiteModel).addPerson(testUser);
        restClient.assertLastError().containsSummary("Permission was denied");
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN);       
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY, 
            description = "Verify that admin user is able to add site member and gets status code CREATED (201)")
    public void adminIsAbleToAddSiteMember() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteConsumer);
        restClient.authenticateUser(adminUserModel)
                  .withCoreAPI().usingSite(publicSiteModel).addPerson(testUser)
                  .and().field("id").is(testUser.getUsername())
                  .and().field("role").is(testUser.getUserRole());
        restClient.assertStatusCodeIs(HttpStatus.CREATED);       
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY, 
            description = "Verify that unauthenticated user is not able to add site member")
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void unauthenticatedUserIsNotAuthorizedToAddSiteMmeber() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteConsumer);
        
        UserModel userModel = dataUser.createRandomTestUser();
        userModel.setPassword("user wrong password");
        dataUser.addUserToSite(userModel, publicSiteModel, UserRole.SiteManager);
        
        restClient.authenticateUser(userModel)
                  .withCoreAPI().usingSite(publicSiteModel).addPerson(testUser);        
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that manager can add another user as manager to a public site and gets status code CREATED (201)")
    public void addManagerToPublicSite() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteManager);
        memberModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(publicSiteModel).addPerson(testUser);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        memberModel.assertThat().field("id").is(testUser.getUsername())
                .and().field("role").is(testUser.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that manager can add another user as manager to a moderated site and gets status code CREATED (201)")
    public void addManagerToModeratedSite() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteManager);
        memberModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(moderatedSiteModel).addPerson(testUser);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        memberModel.assertThat().field("id").is(testUser.getUsername())
                .and().field("role").is(testUser.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that manager can add another user as manager to a private site and gets status code CREATED (201)")
    public void addManagerToPrivateSite() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteManager);
        memberModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(privateSiteModel).addPerson(testUser);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        memberModel.assertThat().field("id").is(testUser.getUsername())
                .and().field("role").is(testUser.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that consumer role is not able to add another user to a moderated site and gets status code 403")
    public void addUserByConsumerToModeratedSite() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteConsumer);
        restClient.authenticateUser(moderatedSiteUsersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).withCoreAPI().usingSite(moderatedSiteModel).addPerson(testUser);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that consumer role is not able to add another user to a private site and gets status code 403")
    public void addUserByConsumerToPrivateSite() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteConsumer);
        restClient.authenticateUser(privateSiteUsersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).withCoreAPI().usingSite(privateSiteModel).addPerson(testUser);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that collaborator role is not able to add another user to a moderated site and gets status code 403")
    public void addUserByCollaboratorToModeratedSite() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteConsumer);
        restClient.authenticateUser(moderatedSiteUsersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI().usingSite(moderatedSiteModel).addPerson(testUser);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that collaborator role is not able to add another user to a private site and gets status code 403")
    public void addUserByCollaboratorToPrivateSite() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteConsumer);
        restClient.authenticateUser(privateSiteUsersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI().usingSite(privateSiteModel).addPerson(testUser);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that contributor role is not able to add another user to a moderated site and gets status code 403")
    public void addUserByContributorToModeratedSite() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteConsumer);
        restClient.authenticateUser(moderatedSiteUsersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).withCoreAPI().usingSite(moderatedSiteModel).addPerson(testUser);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that contributor role is not able to add another user to a private site and gets status code 403")
    public void addUserByContributorToPrivateSite() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteConsumer);
        restClient.authenticateUser(privateSiteUsersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).withCoreAPI().usingSite(privateSiteModel).addPerson(testUser);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that a user without specified role can not be added to a site and gets status code 400")
    public void canNotAddUserWithoutSpecifyingRoleToSite() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        restClient.authenticateUser(adminUserModel).withCoreAPI();
        String json = JsonBodyGenerator.keyValueJson("id", testUser.getUsername());
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, json, "sites/{siteId}/members?{parameters}", publicSiteModel.getId(), restClient.getParameters());
        restClient.processModel(RestSiteMemberModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary(RestErrorModel.MUST_PROVIDE_ROLE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that a user with inexistent role can not be added to a site and gets status code 400")
    public void canNotAddUserWithInexistentRoleToSite() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        restClient.authenticateUser(adminUserModel).withCoreAPI();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, String.format("{\"role\":\"inexistentRole\",\"id\":\"%s\"}", testUser.getUsername()), "sites/{siteId}/members?{parameters}", publicSiteModel.getId(), restClient.getParameters());
        restClient.processModel(RestSiteMemberModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary(String.format(RestErrorModel.UNKNOWN_ROLE, "inexistentRole"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that user can not add himself as a manager to a public site and gets status code 403")
    public void userAddHimselfAsManagerToPublicSite() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteManager);
        restClient.authenticateUser(testUser).withCoreAPI().usingSite(publicSiteModel).addPerson(testUser);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that user can not add himself as a manager to a moderated site and gets status code 403")
    public void userAddHimselfAsManagerToModeratedSite() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteManager);
        restClient.authenticateUser(testUser).withCoreAPI().usingSite(moderatedSiteModel).addPerson(testUser);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that user can not add himself as a manager to a private site and gets status code 404")
    public void userAddHimselfAsManagerToPrivateSite() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteManager);
        restClient.authenticateUser(testUser).withCoreAPI().usingSite(privateSiteModel).addPerson(testUser);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, privateSiteModel.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that user can not be added to an inexistent site and gets status code 404")
    public void userIsNotAbleToAddUserToAnInexistentSite() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteManager);
        SiteModel inexistentSite = new SiteModel("inexistentSite");
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(inexistentSite).addPerson(testUser);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, inexistentSite.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that user can not be added to a site if an empty site id is provided and gets status code 404")
    public void userIsNotAbleToAddAnotherUserUsingEmptySiteId() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteManager);
        SiteModel inexistentSite = new SiteModel("");
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(inexistentSite).addPerson(testUser);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, inexistentSite.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that inexistent user can not be added to site and gets status code 404")
    public void userIsNotAbleToAddInexistentUserToSite() throws Exception
    {
        UserModel testUser = new UserModel("inexistentUser", "password");
        testUser.setUserRole(UserRole.SiteManager);
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(publicSiteModel).addPerson(testUser);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, testUser.getUsername()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that empty username can not be added to site and gets status code 400")
    public void userIsNotAbleToAddEmptyUserIdToSite() throws Exception
    {
        UserModel testUser = new UserModel("", "password");
        testUser.setUserRole(UserRole.SiteManager);
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(publicSiteModel).addPerson(testUser);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary(String.format(RestErrorModel.NO_CONTENT, testUser.getUsername()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that user can not add another user that is already a memeber and gets status code 409")
    public void userIsNotAbleToAddUserThatIsAlreadyAMember() throws Exception
    {
        UserModel collaborator = moderatedSiteUsersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator);
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(moderatedSiteModel).addPerson(collaborator);
        restClient.assertStatusCodeIs(HttpStatus.CONFLICT).assertLastError().containsSummary(String.format(RestErrorModel.ALREADY_Site_MEMBER, collaborator.getUsername(), moderatedSiteModel.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that several users with different roles can be added once in a row to a site and gets status code 201")
    public void addSeveralUsersWithDifferentRolesToASite() throws Exception
    {
        UserModel firstUser = dataUser.createRandomTestUser("testUser");
        firstUser.setUserRole(UserRole.SiteContributor);
        UserModel secondUser = dataUser.createRandomTestUser("testUser");
        secondUser.setUserRole(UserRole.SiteCollaborator);
        restClient.authenticateUser(adminUserModel).withCoreAPI();
        String json = String.format(addMembersJson, firstUser.getUserRole(), firstUser.getUsername(), secondUser.getUserRole(), secondUser.getUsername());
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, json, "sites/{siteId}/members?{parameters}", publicSiteModel.getId(), restClient.getParameters());
        restClient.processModel(RestSiteMemberModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that several users with same roles can be added once in a row to a site and gets status code 201")
    public void addSeveralUsersWithSameRolesToASite() throws Exception
    {
        UserModel firstUser = dataUser.createRandomTestUser("testUser");
        firstUser.setUserRole(UserRole.SiteCollaborator);
        UserModel secondUser = dataUser.createRandomTestUser("testUser");
        secondUser.setUserRole(UserRole.SiteCollaborator);
        restClient.authenticateUser(adminUserModel).withCoreAPI();
        String json = String.format(addMembersJson, firstUser.getUserRole(), firstUser.getUsername(), secondUser.getUserRole(), secondUser.getUsername());
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, json, "sites/{siteId}/members?{parameters}", publicSiteModel.getId(), restClient.getParameters());
        restClient.processModel(RestSiteMemberModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that several users that are already added to the site can not be added once in a row and gets status code 400")
    public void addSeveralUsersThatAreAlreadyAddedToASite() throws Exception
    {
        UserModel collaborator = moderatedSiteUsersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator);
        UserModel consumer = moderatedSiteUsersWithRoles.getOneUserWithRole(UserRole.SiteConsumer);
        restClient.authenticateUser(adminUserModel).withCoreAPI();
        String json = String.format(addMembersJson, collaborator.getUserRole(), collaborator.getUsername(), consumer.getUserRole(), consumer.getUsername());
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, json, "sites/{siteId}/members?{parameters}", moderatedSiteModel.getId(), restClient.getParameters());
        restClient.processModel(RestSiteMemberModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.CONFLICT).assertLastError().containsSummary(String.format(RestErrorModel.ALREADY_Site_MEMBER, collaborator.getUsername(), moderatedSiteModel.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Add new site member request by providing an empty body")
    public void addSiteMemberUsingEmptyBody() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, "", "sites/{siteId}/members?{parameters}",
                publicSiteModel.getId(), restClient.getParameters());
        restClient.processModel(RestSiteMemberModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.NO_CONTENT, "No content to map due to end-of-input"))
                .containsErrorKey(String.format(RestErrorModel.NO_CONTENT, "No content to map due to end-of-input"))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Check lower and upper case letters for role field")
    public void checkLowerUpperCaseLettersForRole() throws Exception
    {
        UserModel user = dataUser.createRandomTestUser();

        String json = String.format(addMemberJson, "SITEMANAGER", user.getUsername());
        restClient.authenticateUser(adminUserModel).withCoreAPI();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, json, "sites/{siteId}/members?{parameters}", publicSiteModel.getId(), restClient.getParameters());
        restClient.processModel(RestSiteMemberModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.UNKNOWN_ROLE, "SITEMANAGER"))
                .containsErrorKey(String.format(RestErrorModel.UNKNOWN_ROLE, "SITEMANAGER"))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);

        json = String.format(addMemberJson, "sitemanager", user.getUsername());
        restClient.authenticateUser(adminUserModel).withCoreAPI();
        request = RestRequest.requestWithBody(HttpMethod.POST, json, "sites/{siteId}/members?{parameters}", publicSiteModel.getId(), restClient.getParameters());
        restClient.processModel(RestSiteMemberModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.UNKNOWN_ROLE, "sitemanager"))
                .containsErrorKey(String.format(RestErrorModel.UNKNOWN_ROLE, "sitemanager"))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Check empty value for user role")
    public void checkEmptyValueForRole() throws Exception
    {
        UserModel user = dataUser.createRandomTestUser();

        String json = String.format(addMemberJson, "", user.getUsername());
        restClient.authenticateUser(adminUserModel).withCoreAPI();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, json, "sites/{siteId}/members?{parameters}", publicSiteModel.getId(), restClient.getParameters());
        restClient.processModel(RestSiteMemberModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsErrorKey(String.format(RestErrorModel.NO_CONTENT, "N/A"))
                .containsSummary(String.format(RestErrorModel.NO_CONTENT, "N/A"))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that contributor role can be added to public site")
    public void addContributorToPublicSite() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteContributor);
        memberModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(publicSiteModel).addPerson(testUser);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        memberModel.assertThat().field("id").is(testUser.getUsername())
                .and().field("role").is(testUser.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that collaborator role can be added to moderated site")
    public void addCollaboratorToModeratedSite() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteCollaborator);
        memberModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(moderatedSiteModel).addPerson(testUser);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        memberModel.assertThat().field("id").is(testUser.getUsername())
                .and().field("role").is(testUser.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that contributor role can be added to moderated site")
    public void addContributorToModeratedSite() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteContributor);
        memberModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(moderatedSiteModel).addPerson(testUser);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        memberModel.assertThat().field("id").is(testUser.getUsername())
                .and().field("role").is(testUser.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that consumer role can be added to moderated site")
    public void addConsumerToModeratedSite() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteConsumer);
        memberModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(moderatedSiteModel).addPerson(testUser);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        memberModel.assertThat().field("id").is(testUser.getUsername())
                .and().field("role").is(testUser.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that collaborator role can be added to private site")
    public void addCollaboratorToPrivateSite() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteCollaborator);
        memberModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(privateSiteModel).addPerson(testUser);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        memberModel.assertThat().field("id").is(testUser.getUsername())
                .and().field("role").is(testUser.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that contributor role can be added to private site")
    public void addContributorToPrivateSite() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteContributor);
        memberModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(privateSiteModel).addPerson(testUser);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        memberModel.assertThat().field("id").is(testUser.getUsername())
                .and().field("role").is(testUser.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that consumer role can be added to private site")
    public void addConsumerToPrivateSite() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteConsumer);
        memberModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(privateSiteModel).addPerson(testUser);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        memberModel.assertThat().field("id").is(testUser.getUsername())
                .and().field("role").is(testUser.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that admin can be added to private site by site manager")
    public void adminCanBeAddedToPrivateSiteBySiteManager() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser();
        SiteModel privateSite = dataSite.usingUser(testUser).createPrivateRandomSite();
        adminUserModel.setUserRole(UserRole.SiteContributor);
        memberModel = restClient.authenticateUser(testUser).withCoreAPI().usingSite(privateSite).addPerson(adminUserModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        memberModel.assertThat().field("id").is(adminUserModel.getUsername()).and().field("role").is(adminUserModel.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that admin cannot be added to private site by site collaborator")
    public void adminCannotBeAddedToPrivateSiteBySiteCollaborator() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser();
        SiteModel privateSite = dataSite.usingUser(testUser).createPrivateRandomSite();
        UserModel siteCollaborator = dataUser.createRandomTestUser();
        dataUser.addUserToSite(siteCollaborator, privateSite, UserRole.SiteCollaborator);
        adminUserModel.setUserRole(UserRole.SiteContributor);
        memberModel = restClient.authenticateUser(siteCollaborator).withCoreAPI().usingSite(privateSite).addPerson(adminUserModel);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }
}
