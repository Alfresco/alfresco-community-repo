package org.alfresco.rest.sites.members;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestSiteMemberModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class UpdateSiteMemberTests extends RestTest
{
    private UserModel adminUser, siteCreator, regularUser;
    private SiteModel publicSite, moderatedSite, privateSite;
    private ListUserWithRoles publicSiteUsers;
    private UserModel userToBeUpdated;
    private RestSiteMemberModel updatedMember;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        siteCreator = dataUser.createRandomTestUser();        
        publicSite = dataSite.usingUser(siteCreator).createPublicRandomSite();
        moderatedSite = dataSite.usingUser(siteCreator).createModeratedRandomSite();
        privateSite = dataSite.usingUser(siteCreator).createPrivateRandomSite();
        publicSiteUsers = dataUser.addUsersWithRolesToSite(publicSite, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);

        regularUser = dataUser.createRandomTestUser();
        regularUser.setUserRole(UserRole.SiteConsumer);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify that collaborator is not able to update site member and gets status code FORBIDDEN (403)")
    public void collaboratorIsNotAbleToUpdateSiteMember() throws Exception
    {
        userToBeUpdated = dataUser.createRandomTestUser();
        dataUser.addUserToSite(userToBeUpdated, publicSite, UserRole.SiteConsumer);
        userToBeUpdated.setUserRole(UserRole.SiteCollaborator);
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteCollaborator));
        restClient.withCoreAPI().usingSite(publicSite).updateSiteMember(userToBeUpdated);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY);
        restClient.assertLastError()
            .containsSummary(String.format("The current user does not have permissions to modify the membership details of the site %s.", publicSite.getTitle()));        
    }
    
    @Bug(id = "REPO-1832")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify that collaborator is not able to update site member and gets status code FORBIDDEN (403)")
    public void collaboratorIsNotAbleToUpdateSiteMemberWithTheSameRole() throws Exception
    {
        userToBeUpdated = dataUser.createRandomTestUser();
        dataUser.addUserToSite(userToBeUpdated, publicSite, UserRole.SiteConsumer);
        userToBeUpdated.setUserRole(UserRole.SiteConsumer);
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteCollaborator));
        restClient.withCoreAPI().usingSite(publicSite).updateSiteMember(userToBeUpdated);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY);
        restClient.assertLastError()
            .containsSummary(String.format("The current user does not have permissions to modify the membership details of the site %s.", publicSite.getTitle()));        
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify that contributor is not able to update site member and gets status code FORBIDDEN (403)")
    public void contributorIsNotAbleToUpdateSiteMember() throws Exception
    {
        userToBeUpdated = dataUser.createRandomTestUser();
        dataUser.addUserToSite(userToBeUpdated, publicSite, UserRole.SiteConsumer);
        userToBeUpdated.setUserRole(UserRole.SiteCollaborator);
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteContributor));
        restClient.withCoreAPI().usingSite(publicSite).updateSiteMember(userToBeUpdated);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY);
        restClient.assertLastError()
        .containsSummary(String.format("The current user does not have permissions to modify the membership details of the site %s.", publicSite.getTitle()));        
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify that consumer is not able to update site member and gets status code FORBIDDEN (403)")
    public void consumerIsNotAbleToUpdateSiteMember() throws Exception
    {
        userToBeUpdated = dataUser.createRandomTestUser();
        dataUser.addUserToSite(userToBeUpdated, publicSite, UserRole.SiteConsumer);
        userToBeUpdated.setUserRole(UserRole.SiteCollaborator);
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteConsumer));
        restClient.withCoreAPI().usingSite(publicSite).updateSiteMember(userToBeUpdated);
        restClient.assertLastError()
        .containsSummary(String.format("The current user does not have permissions to modify the membership details of the site %s.", publicSite.getTitle()));
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify that admin is able to update site member and gets status code OK (200)")
    public void adminIsAbleToUpdateSiteMember() throws Exception
    {
        userToBeUpdated = dataUser.createRandomTestUser();
        dataUser.addUserToSite(userToBeUpdated, publicSite, UserRole.SiteConsumer);
        userToBeUpdated.setUserRole(UserRole.SiteCollaborator);
        restClient.authenticateUser(adminUser);
        restClient.withCoreAPI().usingSite(publicSite).updateSiteMember(userToBeUpdated)
               .assertThat().field("id").is(userToBeUpdated.getUsername())
               .and().field("role").is(userToBeUpdated.getUserRole());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY, 
            description = "Verify that unauthenticated user is not able to update site member")
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void unauthenticatedUserIsNotAuthorizedToUpdateSiteMmeber() throws Exception
    {
        userToBeUpdated = dataUser.createRandomTestUser();
        dataUser.addUserToSite(userToBeUpdated, publicSite, UserRole.SiteConsumer);
        userToBeUpdated.setUserRole(UserRole.SiteCollaborator);
        UserModel inexistentUser = new UserModel("inexistent user", "inexistent password");
        restClient.authenticateUser(inexistentUser);
        restClient.withCoreAPI().usingSite(publicSite).updateSiteMember(userToBeUpdated);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if update site member request returns status code 404 when nonexistent siteId is used")
    public void updateSiteMemberOfNonexistentSite() throws Exception
    {
        SiteModel deletedSite = dataSite.usingUser(adminUser).createPublicRandomSite();
        dataUser.addUserToSite(regularUser, deletedSite, UserRole.SiteConsumer);
        dataSite.deleteSite(deletedSite);

        restClient.authenticateUser(adminUser).withCoreAPI()
                .usingSite(deletedSite).updateSiteMember(regularUser);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, deletedSite.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if update site member request returns status code 400 when personId is not member of the site")
    public void updateNotASiteMember() throws Exception
    {
        restClient.authenticateUser(adminUser).withCoreAPI()
                .usingSite(publicSite).updateSiteMember(regularUser);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary("User is not a member of the site");
    }

    //    @Bug(id="REPO-1642", description = "reproduced on 5.2.1 only, it works on 5.2.0")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if update site member request returns status code 404 when personId does not exist")
    public void updateNonexistentSiteMember() throws Exception
    {
        UserModel nonexistentUser = new UserModel("nonexistentUser", DataUser.PASSWORD);
        nonexistentUser.setUserRole(UserRole.SiteContributor);
        restClient.authenticateUser(adminUser).withCoreAPI()
                .usingSite(publicSite).updateSiteMember(nonexistentUser);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, nonexistentUser.getUsername()));
    }

    @Bug(id="REPO-1941")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if a manager is able to downgrade to contributor using -me- string in place of personId.")
    public void updateSiteManagerToSiteContributorUsingMe() throws Exception
    {
        UserModel meManager = dataUser.createRandomTestUser();
        dataUser.addUserToSite(meManager, publicSite, UserRole.SiteManager);
        UserModel meUser = new UserModel("-me-", "password");
        meUser.setUserRole(UserRole.SiteContributor);
        updatedMember = restClient.authenticateUser(meManager).withCoreAPI()
                .usingSite(publicSite).updateSiteMember(meUser);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(meManager.getUsername())
                .and().field("role").is(meUser.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if update site member request returns status code 404 when empty siteId is used")
    public void updateSiteMemberUsingEmptySiteId() throws Exception
    {
        restClient.authenticateUser(adminUser).withCoreAPI()
                .usingSite("").updateSiteMember(regularUser);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, ""));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if update site member request returns status code 405 when empty personId is used")
    public void updateSiteMemberUsingEmptyPersonId() throws Exception
    {
        UserModel emptyUser = new UserModel("", DataUser.PASSWORD);
        emptyUser.setUserRole(UserRole.SiteCollaborator);
        restClient.authenticateUser(adminUser).withCoreAPI()
                .usingSite(publicSite).updateSiteMember(emptyUser);
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED)
                .assertLastError().containsSummary(RestErrorModel.PUT_EMPTY_ARGUMENT);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if update site member request returns status code 400 when invalid role is used")
    public void updateSiteMemberUsingInvalidRole() throws Exception
    {
        restClient.authenticateUser(siteCreator).withCoreAPI();
        UserModel siteConsumer = publicSiteUsers.getOneUserWithRole(UserRole.SiteConsumer);
        String json = JsonBodyGenerator.keyValueJson("role","invalidRole");
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, json, "sites/{siteId}/members/{personId}", publicSite.getId(), siteConsumer.getUsername());
        restClient.processModel(RestSiteMemberModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.UNKNOWN_ROLE, "invalidRole"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if manager is able to update another site manager to site collaborator")
    public void managerUpdateSiteManagerToSiteCollaborator() throws Exception
    {
        UserModel siteManager = dataUser.createRandomTestUser();
        dataUser.addUserToSite(siteManager, publicSite, UserRole.SiteManager);
        siteManager.setUserRole(UserRole.SiteCollaborator);
        updatedMember = restClient.authenticateUser(siteCreator).withCoreAPI()
                .usingSite(publicSite).updateSiteMember(siteManager);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(siteManager.getUsername())
                .and().field("role").is(siteManager.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if manager is able to update site contributor to site manager")
    public void managerUpdateSiteContributorToSiteManager() throws Exception
    {
        UserModel siteContributor = publicSiteUsers.getOneUserWithRole(UserRole.SiteContributor);
        siteContributor.setUserRole(UserRole.SiteManager);
        updatedMember = restClient.authenticateUser(siteCreator).withCoreAPI()
                .usingSite(publicSite).updateSiteMember(siteContributor);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(siteContributor.getUsername())
                .and().field("role").is(siteContributor.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if manager is able to update site collaborator to site manager")
    public void managerUpdateSiteCollaboratorToSiteManager() throws Exception
    {
        UserModel siteCollaborator = publicSiteUsers.getOneUserWithRole(UserRole.SiteCollaborator);
        siteCollaborator.setUserRole(UserRole.SiteManager);
        updatedMember = restClient.authenticateUser(siteCreator).withCoreAPI()
                .usingSite(publicSite).updateSiteMember(siteCollaborator);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(siteCollaborator.getUsername())
                .and().field("role").is(siteCollaborator.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.SANITY,
            description= "Verify if manager is able to update site consumer to site manager")
    public void managerUpdateSiteConsumerToSiteManager() throws Exception
    {
        UserModel siteConsumer = publicSiteUsers.getOneUserWithRole(UserRole.SiteConsumer);
        siteConsumer.setUserRole(UserRole.SiteManager);
        updatedMember = restClient.authenticateUser(siteCreator).withCoreAPI()
                .usingSite(publicSite).updateSiteMember(siteConsumer);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(siteConsumer.getUsername())
                .and().field("role").is(siteConsumer.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if manager is able to downgrade himself to site consumer")
    public void publicSiteManagerDowngradesRole() throws Exception
    {
        UserModel siteManager = publicSiteUsers.getOneUserWithRole(UserRole.SiteManager);
        siteManager.setUserRole(UserRole.SiteConsumer);
        updatedMember = restClient.authenticateUser(siteManager).withCoreAPI()
                .usingSite(publicSite).updateSiteMember(siteManager);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(siteManager.getUsername())
                .and().field("role").is(siteManager.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if manager is able to update another site manager to site contributor")
    public void managerUpdateSiteManagerToSiteContributor() throws Exception
    {
        UserModel siteManager = dataUser.createRandomTestUser();
        dataUser.addUserToSite(siteManager, publicSite, UserRole.SiteManager);

        siteManager.setUserRole(UserRole.SiteContributor);
        updatedMember = restClient.authenticateUser(siteCreator).withCoreAPI()
                .usingSite(publicSite).updateSiteMember(siteManager);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(siteManager.getUsername()).and().field("role").is(siteManager.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if manager is able to update another site manager to site consumer")
    public void managerUpdateSiteManagerToSiteConsumer() throws Exception
    {
        UserModel siteManager = dataUser.createRandomTestUser();
        dataUser.addUserToSite(siteManager, publicSite, UserRole.SiteManager);

        siteManager.setUserRole(UserRole.SiteConsumer);
        updatedMember = restClient.authenticateUser(siteCreator).withCoreAPI()
                .usingSite(publicSite).updateSiteMember(siteManager);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(siteManager.getUsername()).and().field("role").is(siteManager.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if manager is able to update site contributor to site collaborator")
    public void managerUpdateSiteContributorToSiteCollaborator() throws Exception
    {
        UserModel siteContributor = dataUser.createRandomTestUser();
        dataUser.addUserToSite(siteContributor, publicSite, UserRole.SiteContributor);

        siteContributor.setUserRole(UserRole.SiteCollaborator);
        updatedMember = restClient.authenticateUser(siteCreator).withCoreAPI()
                .usingSite(publicSite).updateSiteMember(siteContributor);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(siteContributor.getUsername()).and().field("role").is(siteContributor.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if manager is able to update site contributor to site consumer")
    public void managerUpdateSiteContributorToSiteConsumer() throws Exception
    {
        UserModel siteContributor = dataUser.createRandomTestUser();
        dataUser.addUserToSite(siteContributor, publicSite, UserRole.SiteContributor);

        siteContributor.setUserRole(UserRole.SiteConsumer);
        updatedMember = restClient.authenticateUser(siteCreator).withCoreAPI()
                .usingSite(publicSite).updateSiteMember(siteContributor);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(siteContributor.getUsername()).and().field("role").is(siteContributor.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if manager is able to update site collaborator to site contributor")
    public void managerUpdateSiteCollaboratorToSiteContributor() throws Exception
    {
        UserModel siteCollaborator = dataUser.createRandomTestUser();
        dataUser.addUserToSite(siteCollaborator, publicSite, UserRole.SiteCollaborator);

        siteCollaborator.setUserRole(UserRole.SiteContributor);
        updatedMember = restClient.authenticateUser(siteCreator).withCoreAPI()
                .usingSite(publicSite).updateSiteMember(siteCollaborator);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(siteCollaborator.getUsername()).and().field("role").is(siteCollaborator.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if manager is able to update site collaborator to site consumer")
    public void managerUpdateSiteCollaboratorToSiteConsumer() throws Exception
    {
        UserModel siteCollaborator = dataUser.createRandomTestUser();
        dataUser.addUserToSite(siteCollaborator, publicSite, UserRole.SiteCollaborator);

        siteCollaborator.setUserRole(UserRole.SiteConsumer);
        updatedMember = restClient.authenticateUser(siteCreator).withCoreAPI()
                .usingSite(publicSite).updateSiteMember(siteCollaborator);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(siteCollaborator.getUsername()).and().field("role").is(siteCollaborator.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if manager is able to update site consumer to site collaborator")
    public void managerUpdateSiteConsumerToSiteCollaborator() throws Exception
    {
        UserModel siteConsumer = dataUser.createRandomTestUser();
        dataUser.addUserToSite(siteConsumer, publicSite, UserRole.SiteConsumer);

        siteConsumer.setUserRole(UserRole.SiteCollaborator);
        updatedMember = restClient.authenticateUser(siteCreator).withCoreAPI()
                .usingSite(publicSite).updateSiteMember(siteConsumer);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(siteConsumer.getUsername()).and().field("role").is(siteConsumer.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if manager is able to update site consumer to site contributor")
    public void managerUpdateSiteConsumerToSiteContributor() throws Exception
    {
        UserModel siteConsumer = dataUser.createRandomTestUser();
        dataUser.addUserToSite(siteConsumer, publicSite, UserRole.SiteConsumer);

        siteConsumer.setUserRole(UserRole.SiteContributor);
        updatedMember = restClient.authenticateUser(siteCreator).withCoreAPI()
                .usingSite(publicSite).updateSiteMember(siteConsumer);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(siteConsumer.getUsername()).and().field("role").is(siteConsumer.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if manager is able to update another site manager to site manager")
    public void managerUpdateSiteManagerToSiteManager() throws Exception
    {
        UserModel siteManager = dataUser.createRandomTestUser();
        dataUser.addUserToSite(siteManager, publicSite, UserRole.SiteManager);

        siteManager.setUserRole(UserRole.SiteManager);
        updatedMember = restClient.authenticateUser(siteCreator).withCoreAPI()
                .usingSite(publicSite).updateSiteMember(siteManager);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(siteManager.getUsername()).and().field("role").is(siteManager.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if manager is able to update site collaborator to site collaborator")
    public void managerUpdateSiteCollaboratorToSiteCollaborator() throws Exception
    {
        UserModel siteCollaborator = dataUser.createRandomTestUser();
        dataUser.addUserToSite(siteCollaborator, publicSite, UserRole.SiteCollaborator);

        siteCollaborator.setUserRole(UserRole.SiteCollaborator);
        updatedMember = restClient.authenticateUser(siteCreator).withCoreAPI()
                .usingSite(publicSite).updateSiteMember(siteCollaborator);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(siteCollaborator.getUsername()).and().field("role").is(siteCollaborator.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if manager is able to update site contributor to site contributor")
    public void managerUpdateSiteContributorToSiteContributor() throws Exception
    {
        UserModel siteContributor = dataUser.createRandomTestUser();
        dataUser.addUserToSite(siteContributor, publicSite, UserRole.SiteContributor);

        siteContributor.setUserRole(UserRole.SiteContributor);
        updatedMember = restClient.authenticateUser(siteCreator).withCoreAPI()
                .usingSite(publicSite).updateSiteMember(siteContributor);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(siteContributor.getUsername()).and().field("role").is(siteContributor.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if manager is able to update site consumer to site consumer")
    public void managerUpdateSiteConsumerToSiteConsumer() throws Exception
    {
        UserModel siteConsumer = dataUser.createRandomTestUser();
        dataUser.addUserToSite(siteConsumer, publicSite, UserRole.SiteConsumer);

        siteConsumer.setUserRole(UserRole.SiteConsumer);
        updatedMember = restClient.authenticateUser(siteCreator).withCoreAPI()
                .usingSite(publicSite).updateSiteMember(siteConsumer);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(siteConsumer.getUsername()).and().field("role").is(siteConsumer.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if manager of a private site is able to downgrade his role")
    public void privateSiteManagerDowngradesRole() throws Exception
    {
        UserModel siteManager = dataUser.createRandomTestUser();
        dataUser.addUserToSite(siteManager, privateSite, UserRole.SiteManager);

        siteManager.setUserRole(UserRole.SiteConsumer);
        updatedMember = restClient.authenticateUser(siteManager).withCoreAPI()
                .usingSite(privateSite).updateSiteMember(siteManager);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(siteManager.getUsername()).and().field("role").is(siteManager.getUserRole());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if manager of a moderated site is able to downgrade his role")
    public void moderatedSiteManagerDowngradesRole() throws Exception
    {
        UserModel siteManager = dataUser.createRandomTestUser();
        dataUser.addUserToSite(siteManager, moderatedSite, UserRole.SiteManager);

        siteManager.setUserRole(UserRole.SiteConsumer);
        updatedMember = restClient.authenticateUser(siteManager).withCoreAPI()
                .usingSite(moderatedSite).updateSiteMember(siteManager);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedMember.assertThat().field("id").is(siteManager.getUsername()).and().field("role").is(siteManager.getUserRole());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify the response of updating a site member with empty body at request")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void managerCanNotUpdateSiteMemberWithEmptyBody() throws Exception
    {
        restClient.authenticateUser(siteCreator).withCoreAPI();
        UserModel siteCollaborator = dataUser.createRandomTestUser();
        dataUser.addUserToSite(siteCollaborator, publicSite, UserRole.SiteCollaborator);
        siteCollaborator.setUserRole(UserRole.SiteConsumer);

        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, "", "sites/{siteId}/members/{personId}", publicSite.getId(), siteCollaborator.getUsername());
        restClient.processModel(RestSiteMemberModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.NO_CONTENT, "No content to map due to end-of-input"))
                .containsErrorKey(String.format(RestErrorModel.NO_CONTENT, "No content to map due to end-of-input"))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if manager is able to update a user that has created a site membership request, but it's not a member of the site yet")
    public void managerCanNotUpdateUserWithSiteMembershipRequest() throws Exception
    {
        UserModel siteConsumer = dataUser.createRandomTestUser();
        siteConsumer.setUserRole(UserRole.SiteConsumer);
        restClient.authenticateUser(siteConsumer).withCoreAPI().usingUser(siteConsumer).addSiteMembershipRequest(moderatedSite);

        updatedMember = restClient.authenticateUser(siteCreator).withCoreAPI()
                .usingSite(moderatedSite).updateSiteMember(siteConsumer);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(RestErrorModel.NOT_A_MEMBER);
    }
}
