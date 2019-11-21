package org.alfresco.rest.sites.members;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.exception.DataPreparationException;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author iulia.cojocea
 */
public class GetSiteMemberTests extends RestTest
{
    private UserModel adminUser;
    private SiteModel publicSiteModel, moderatedSiteModel, privateSiteModel;
    private ListUserWithRoles usersWithRoles;
    private UserModel manager, consumer, collaborator, contributor;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws DataPreparationException
    {
        adminUser = dataUser.getAdminUser();        
        publicSiteModel = dataSite.usingUser(adminUser).createPublicRandomSite();
        moderatedSiteModel = dataSite.usingUser(adminUser).createModeratedRandomSite();
        privateSiteModel = dataSite.usingUser(adminUser).createPrivateRandomSite();
        usersWithRoles = dataUser.addUsersWithRolesToSite(publicSiteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);
        
        consumer = dataUser.createRandomTestUser();
        manager = usersWithRoles.getOneUserWithRole(UserRole.SiteManager);
        collaborator = usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator);
        contributor = usersWithRoles.getOneUserWithRole(UserRole.SiteContributor);

        dataUser.addUserToSite(consumer, publicSiteModel, UserRole.SiteConsumer);
        dataUser.addUserToSite(consumer, moderatedSiteModel, UserRole.SiteConsumer);
        dataUser.addUserToSite(manager, moderatedSiteModel, UserRole.SiteManager);
        dataUser.addUserToSite(consumer, privateSiteModel, UserRole.SiteConsumer);
        dataUser.addUserToSite(manager, privateSiteModel, UserRole.SiteManager);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY, 
            description = "Verify user with Manager role gets site member and status code is OK (200)")
    public void getSiteMemberWithManagerRole() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteMember(consumer)
                    .assertThat().field("id").is(consumer.getUsername())
                    .and().field("role").is(consumer.getUserRole());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user with Collaborator role gets site member and gets status code OK (200)")
    public void getSiteMemberWithCollaboratorRole() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteMember(consumer)
                    .and().field("id").is(consumer.getUsername())
                    .and().field("role").is(consumer.getUserRole());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user with Contributor role gets site member and gets status code OK (200)")
    public void getSiteMemberWithContributorRole() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteMember(consumer)
                  .and().field("id").is(consumer.getUsername())
                  .and().field("role").is(consumer.getUserRole());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user with Consumer role gets site member and gets status code OK (200)")
    public void getSiteMemberWithConsumerRole() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer));
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteMember(consumer)
                    .and().field("id").is(consumer.getUsername())
                    .and().field("role").is(consumer.getUserRole());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user with admin user gets site member and gets status code OK (200)")
    public void getSiteMemberWithAdminUser() throws Exception
    {
        restClient.authenticateUser(adminUser);
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteMember(consumer)
                    .and().field("id").is(consumer.getUsername())
                    .and().field("role").is(consumer.getUserRole());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY, 
            description = "Failed authentication get site member call returns status code 401")
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void unauthenticatedUserIsNotAuthorizedToRetrieveSiteMember() throws JsonToModelConversionException, Exception
    {
        UserModel inexistentUser = new UserModel("inexistent user", "inexistent password");
        restClient.authenticateUser(inexistentUser);
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteMember(consumer);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with Manager role doesn't get a site member of inexistent site and status code is Not Found (404)")
    public void getSiteMemberOfInexistentSite() throws Exception
    {
        SiteModel invalidSite = new SiteModel("invalidSite");

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingSite(invalidSite).getSiteMember(consumer);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, consumer.getUsername(), invalidSite.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with Manager role doesn't get non site member of inexistent site and status code is Not Found (404)")
    public void getSiteMemberForNonSiteMember() throws Exception
    {
        UserModel nonMember = dataUser.createRandomTestUser();

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteMember(nonMember);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, nonMember.getUsername(), publicSiteModel.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with Manager role doesn't get not existing site member and status code is Not Found (404)")
    public void getSiteMemberForInexistentSiteMember() throws Exception
    {
        UserModel inexistentUser = new UserModel("inexistentUser", "password");

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteMember(inexistentUser);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, inexistentUser.getUsername()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with Manager role can get site member using \"-me-\" in place of personId")
    public void getSiteMemberUsingMeForPersonId() throws Exception
    {
        UserModel meUser = new UserModel("-me-", "password");

        restClient.authenticateUser(manager);
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteMember(meUser)
                .assertThat().field("id").is(manager.getUsername())
                .and().field("role").is(manager.getUserRole());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with Manager role can get site member for empty siteId")
    public void getSiteMemberForEmptySiteId() throws Exception
    {
        SiteModel emptySite = new SiteModel("");

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingSite(emptySite).getSiteMember(consumer);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, consumer.getUsername(), emptySite.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with Manager role gets site member with Manager role and status code is OK (200)")
    public void getSiteManagerMemberWithManagerRole() throws Exception
    {
        UserModel anotherManager = dataUser.createRandomTestUser();
        dataUser.addUserToSite(anotherManager, publicSiteModel, UserRole.SiteManager);

        restClient.authenticateUser(manager);
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteMember(anotherManager)
                .assertThat().field("id").is(anotherManager.getUsername())
                .and().field("role").is(anotherManager.getUserRole());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with Collaborator role gets site member with Manager role and status code is OK (200)")
    public void getSiteManagerMemberWithCollaboratorRole() throws Exception
    {
        restClient.authenticateUser(collaborator);
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteMember(manager)
                .assertThat().field("id").is(manager.getUsername())
                .and().field("role").is(manager.getUserRole());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with Consumer role gets site member with Manager role and status code is OK (200)")
    public void getSiteManagerMemberWithConsumerRole() throws Exception
    {
        restClient.authenticateUser(consumer);
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteMember(manager)
                .assertThat().field("id").is(manager.getUsername())
                .and().field("role").is(manager.getUserRole());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with Contributor role gets site member with Manager role and status code is OK (200)")
    public void getSiteManagerMemberWithContributorRole() throws Exception
    {
        restClient.authenticateUser(contributor);
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteMember(manager)
                .assertThat().field("id").is(manager.getUsername())
                .and().field("role").is(manager.getUserRole());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with Contributor role gets admin site member and status code is OK (200)")
    public void getSiteAdminManagerMember() throws Exception
    {
        restClient.authenticateUser(contributor);
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteMember(adminUser)
                .assertThat().field("id").is(adminUser.getUsername())
                .and().field("role").is(UserRole.SiteManager);
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with Collaborator role gets site member with Contributor role and status code is OK (200)")
    public void getSiteContributorMemberWithCollaboratorRole() throws Exception
    {
        restClient.authenticateUser(collaborator).withCoreAPI().usingSite(publicSiteModel).getSiteMember(contributor)
                .assertThat().field("id").is(contributor.getUsername())
                .and().field("role").is(contributor.getUserRole());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with Contributor role gets site member with Collaborator role and status code is OK (200)")
    public void getSiteCollaboratorMemberWithContributorRole() throws Exception
    {
        restClient.authenticateUser(contributor).withCoreAPI().usingSite(publicSiteModel).getSiteMember(collaborator)
                .assertThat().field("id").is(collaborator.getUsername())
                .and().field("role").is(collaborator.getUserRole());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with Collaborator role gets admin role and status code is OK (200)")
    public void getAdminWithCollaboratorRole() throws Exception
    {
        restClient.authenticateUser(collaborator).withCoreAPI().usingSite(publicSiteModel).getSiteMember(adminUser)
                .assertThat().field("id").is(adminUser.getUsername())
                .and().field("role").is(UserRole.SiteManager);
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with Collaborator role gets site member with Consumer role and status code is OK (200)")
    public void getSiteConsumerMemberWithCollaboratorRole() throws Exception
    {
        restClient.authenticateUser(collaborator).withCoreAPI().usingSite(publicSiteModel).getSiteMember(consumer)
                .assertThat().field("id").is(consumer.getUsername())
                .and().field("role").is(consumer.getUserRole());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user gets site member of private site and status code is OK (200)")
    public void getSiteMemberOfPrivateSite() throws Exception
    {
        restClient.authenticateUser(manager).withCoreAPI().usingSite(privateSiteModel).getSiteMember(consumer)
                .assertThat().field("id").is(consumer.getUsername())
                .and().field("role").is(consumer.getUserRole());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify not joined user is not is not able to get site member of private site and status code is 404")
    public void regularUserIsNotAbleToGetSiteMemberOfPrivateSite() throws Exception {
        UserModel regularUser = dataUser.createRandomTestUser();

        restClient.authenticateUser(regularUser).withCoreAPI().usingSite(privateSiteModel).getSiteMember(consumer);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, consumer.getUsername(), privateSiteModel.getTitle()));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin is not able to get from site a user that created a member request that was not accepted yet")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void adminIsNotAbleToGetFromSiteANonExistingMember() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSiteModel);

        restClient.authenticateUser(adminUser).withCoreAPI().usingSite(moderatedSiteModel).getSiteMember(newMember);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, newMember.getUsername(), moderatedSiteModel.getTitle()))
                .containsErrorKey(RestErrorModel.RELATIONSHIP_NOT_FOUND_ERRORKEY)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user gets site creator and status code is OK (200)")
    public void getSiteCreator() throws Exception
    {
        SiteModel newSiteModel = dataSite.usingUser(collaborator).createModeratedRandomSite();
        dataUser.addUserToSite(consumer, publicSiteModel, UserRole.SiteConsumer);

        restClient.authenticateUser(consumer).withCoreAPI().usingSite(newSiteModel).getSiteMember(collaborator)
                .assertThat().field("id").is(collaborator.getUsername())
                .and().field("role").is(UserRole.SiteManager);
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with Consumer role can get site member using \"-me-\" in place of personId")
    public void getSiteMemberOfPrivateSiteUsingMeForPersonId() throws Exception
    {
        UserModel meUser = new UserModel("-me-", "password");

        restClient.authenticateUser(consumer).withCoreAPI().usingSite(privateSiteModel).getSiteMember(meUser)
                .assertThat().field("id").is(consumer.getUsername())
                .and().field("role").is(consumer.getUserRole());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user gets site member of moderated site and status code is OK (200)")
    public void getSiteMemberOfModeratedSite() throws Exception
    {
        restClient.authenticateUser(manager).withCoreAPI().usingSite(moderatedSiteModel).getSiteMember(consumer)
                .assertThat().field("id").is(consumer.getUsername())
                .and().field("role").is(consumer.getUserRole());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify not joined user gets site member of moderated site and status code is OK (200)")
    public void regularUserIsAbleToGetSiteMemberOfModeratedSite() throws Exception
    {
        UserModel regularUser = dataUser.createRandomTestUser();

        restClient.authenticateUser(regularUser).withCoreAPI().usingSite(moderatedSiteModel).getSiteMember(consumer);
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get site member request with properties parameter returns status code 200 and parameter is applied")
    public void getSiteMemberUsingPropertiesParameter() throws Exception
    {
        restClient.authenticateUser(manager)
                .withCoreAPI().usingSite(publicSiteModel).usingParams("properties=id").getSiteMember(consumer)
                .assertThat().fieldsCount().is(1)
                .and().field("id").isNotEmpty()
                .and().field("role").isNull()
                .and().field("person").isNull();
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }
}
