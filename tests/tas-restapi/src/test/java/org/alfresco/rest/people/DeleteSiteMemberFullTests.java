package org.alfresco.rest.people;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DeleteSiteMemberFullTests extends RestTest
{
    private SiteModel publicSiteModel;
    private SiteModel moderatedSiteModel;
    private SiteModel privateSiteModel;
    private UserModel adminUserModel;
    private ListUserWithRoles usersWithRolesPublicSite;
    private ListUserWithRoles usersWithRolesModeratedSite;
    private ListUserWithRoles usersWithRolesPrivateSite;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUserModel = dataUser.getAdminUser();
        adminUserModel.setUserRole(UserRole.SiteManager);
        publicSiteModel = dataSite.usingUser(adminUserModel).createPublicRandomSite();
        moderatedSiteModel = dataSite.usingUser(adminUserModel).createModeratedRandomSite();
        privateSiteModel = dataSite.usingUser(adminUserModel).createPrivateRandomSite();
        
        usersWithRolesPublicSite = dataUser.addUsersWithRolesToSite(publicSiteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);
        usersWithRolesModeratedSite = dataUser.addUsersWithRolesToSite(moderatedSiteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);
        usersWithRolesPrivateSite = dataUser.addUsersWithRolesToSite(privateSiteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify admin is able to remove collaborator member of a public site")
    public void adminIsAbleToRemoveCollaboratorSiteMembershipFromPublicSite() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(usersWithRolesPublicSite.getOneUserWithRole(UserRole.SiteCollaborator)).deleteSiteMember(publicSiteModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(publicSiteModel).addPerson(usersWithRolesPublicSite.getOneUserWithRole(UserRole.SiteCollaborator));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify admin is able to remove contributor member of a public site")
    public void adminIsAbleToRemoveContributorSiteMembershipFromPublicSite() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(usersWithRolesPublicSite.getOneUserWithRole(UserRole.SiteContributor)).deleteSiteMember(publicSiteModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(publicSiteModel).addPerson(usersWithRolesPublicSite.getOneUserWithRole(UserRole.SiteContributor));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify user is not able to remove contributor member of a public site and response is 422")
    public void userIsNotAbleToRemoveContributorSiteMembershipFromPublicSite() throws Exception
    {
        restClient.authenticateUser(usersWithRolesPublicSite.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI().usingUser(usersWithRolesPublicSite.getOneUserWithRole(UserRole.SiteContributor)).deleteSiteMember(publicSiteModel);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY).assertLastError()
        .containsSummary(String.format(RestErrorModel.NOT_SUFFICIENT_PERMISSIONS, publicSiteModel.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify user is not able to remove consumer member of a public site and response is 422")
    public void userIsNotAbleToRemoveConsumerSiteMembershipFromPublicSite() throws Exception
    {
        restClient.authenticateUser(usersWithRolesPublicSite.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI().usingUser(usersWithRolesPublicSite.getOneUserWithRole(UserRole.SiteConsumer)).deleteSiteMember(publicSiteModel);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY).assertLastError()
        .containsSummary(String.format(RestErrorModel.NOT_SUFFICIENT_PERMISSIONS, publicSiteModel.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify admin is able to remove collaborator member of a moderated site")
    public void adminIsAbleToRemoveCollaboratorSiteMembershipFromModeratedSite() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(usersWithRolesModeratedSite.getOneUserWithRole(UserRole.SiteCollaborator)).deleteSiteMember(moderatedSiteModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(moderatedSiteModel).addPerson(usersWithRolesModeratedSite.getOneUserWithRole(UserRole.SiteCollaborator));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify admin is able to remove contributor member of a moderated site")
    public void adminIsAbleToRemoveContributorSiteMembershipFromModeratedSite() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(usersWithRolesModeratedSite.getOneUserWithRole(UserRole.SiteContributor)).deleteSiteMember(moderatedSiteModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(moderatedSiteModel).addPerson(usersWithRolesModeratedSite.getOneUserWithRole(UserRole.SiteContributor));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify user is not able to remove contributor member of a moderated site and response is 422")
    public void userIsNotAbleToRemoveContributorSiteMembershipFromModeratedSite() throws Exception
    {
        restClient.authenticateUser(usersWithRolesModeratedSite.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI().usingUser(usersWithRolesModeratedSite.getOneUserWithRole(UserRole.SiteContributor)).deleteSiteMember(moderatedSiteModel);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY).assertLastError()
            .containsSummary(String.format(RestErrorModel.NOT_SUFFICIENT_PERMISSIONS, moderatedSiteModel.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify user is not able to remove consumer member of a moderated site and response is 422")
    public void userIsNotAbleToRemoveConsumerSiteMembershipFromModeratedSite() throws Exception
    {
        restClient.authenticateUser(usersWithRolesModeratedSite.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI().usingUser(usersWithRolesModeratedSite.getOneUserWithRole(UserRole.SiteConsumer)).deleteSiteMember(moderatedSiteModel);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY).assertLastError()
            .containsSummary(String.format(RestErrorModel.NOT_SUFFICIENT_PERMISSIONS, moderatedSiteModel.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify admin is able to remove collaborator member of a private site")
    public void adminIsAbleToRemoveCollaboratorSiteMembershipFromPrivateSite() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(usersWithRolesPrivateSite.getOneUserWithRole(UserRole.SiteCollaborator)).deleteSiteMember(privateSiteModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(privateSiteModel).addPerson(usersWithRolesPrivateSite.getOneUserWithRole(UserRole.SiteCollaborator));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify admin is able to remove contributor member of a private site")
    public void adminIsAbleToRemoveContributorSiteMembershipFromPrivateSite() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(usersWithRolesPrivateSite.getOneUserWithRole(UserRole.SiteContributor)).deleteSiteMember(privateSiteModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(privateSiteModel).addPerson(usersWithRolesPrivateSite.getOneUserWithRole(UserRole.SiteContributor));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify user is not able to remove contributor member of a private site and response is 422")
    public void userIsNotAbleToRemoveContributorSiteMembershipFromPrivateSite() throws Exception
    {
        restClient.authenticateUser(usersWithRolesPrivateSite.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI().usingUser(usersWithRolesPrivateSite.getOneUserWithRole(UserRole.SiteContributor)).deleteSiteMember(privateSiteModel);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY).assertLastError()
            .containsSummary(String.format(RestErrorModel.NOT_SUFFICIENT_PERMISSIONS, privateSiteModel.getId()));
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify user is not able to remove consumer member of a private site and response is 422")
    public void userIsNotAbleToRemoveConsumerSiteMembershipFromPrivateSite() throws Exception
    {
        restClient.authenticateUser(usersWithRolesPrivateSite.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI().usingUser(usersWithRolesPrivateSite.getOneUserWithRole(UserRole.SiteConsumer)).deleteSiteMember(privateSiteModel);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY).assertLastError()
            .containsSummary(String.format(RestErrorModel.NOT_SUFFICIENT_PERMISSIONS, privateSiteModel.getId()));
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify admin is not able to remove same user twice and response is 400")
    @Bug(id="ACE-5447")
    public void adminIsNotAbleToRemoveSameUserTwice() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(usersWithRolesPublicSite.getOneUserWithRole(UserRole.SiteCollaborator)).deleteSiteMember(publicSiteModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(usersWithRolesPublicSite.getOneUserWithRole(UserRole.SiteCollaborator)).deleteSiteMember(publicSiteModel);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(RestErrorModel.ENTITY_NOT_FOUND);
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(publicSiteModel).addPerson(usersWithRolesPublicSite.getOneUserWithRole(UserRole.SiteCollaborator));
        Utility.sleep(300, 30000, () -> restClient.withCoreAPI().usingSite(publicSiteModel)
                .getSiteMembers().assertThat()
                .entriesListContains("id",
                        usersWithRolesPublicSite.getOneUserWithRole(UserRole.SiteCollaborator).getUsername())
                .when().getSiteMember(usersWithRolesPublicSite.getOneUserWithRole(UserRole.SiteCollaborator).getUsername())
                .assertSiteMemberHasRole(UserRole.SiteCollaborator));
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify single manager is not able to delete himself and response is 400")
    public void lastManagerIsNotAbleToDeleteHimself() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(usersWithRolesPublicSite.getOneUserWithRole(UserRole.SiteManager)).deleteSiteMember(publicSiteModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withCoreAPI().usingUser(adminUserModel).deleteSiteMember(publicSiteModel);
        
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
            .assertLastError().containsSummary(String.format(RestErrorModel.DELETE_LAST_MANAGER, publicSiteModel.getTitle()))   
            .containsErrorKey(String.format(RestErrorModel.DELETE_LAST_MANAGER, publicSiteModel.getTitle()))
            .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER).stackTraceIs(RestErrorModel.STACKTRACE); 
        
        restClient.withCoreAPI().usingSite(publicSiteModel).addPerson(usersWithRolesPublicSite.getOneUserWithRole(UserRole.SiteManager));
        Utility.sleep(300, 30000, () -> restClient.withCoreAPI().usingSite(publicSiteModel)
                .getSiteMembers().assertThat()
                .entriesListContains("id", usersWithRolesPublicSite.getOneUserWithRole(UserRole.SiteManager).getUsername()));
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify site creator can be deleted")
    public void siteCreatorCanBeDeleted() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(adminUserModel).deleteSiteMember(publicSiteModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.authenticateUser(usersWithRolesPublicSite.getOneUserWithRole(UserRole.SiteManager)).withCoreAPI().usingSite(publicSiteModel).addPerson(adminUserModel);
        Utility.sleep(300, 30000, () -> restClient.withCoreAPI().usingSite(publicSiteModel)
                .getSiteMembers().assertThat().entriesListContains("id", adminUserModel.getUsername()));
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify admin is not able to remove from site a user that created a member request that was not accepted yet")
    @Bug(id="ACE-5447")
    public void adminIsNotAbleToRemoveFromSiteANonExistingMember() throws Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSiteModel);
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(newMember).deleteSiteMember(moderatedSiteModel);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
            .containsSummary(RestErrorModel.ENTITY_NOT_FOUND);
    }
    
}