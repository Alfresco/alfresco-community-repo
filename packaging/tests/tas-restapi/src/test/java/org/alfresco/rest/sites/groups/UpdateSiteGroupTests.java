package org.alfresco.rest.sites.groups;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestSiteMemberModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.GroupModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class UpdateSiteGroupTests extends RestTest
{
    private UserModel adminUser, siteCreator;
    private GroupModel regularGroup;
    private SiteModel publicSite;
    private DataUser.ListUserWithRoles publicSiteUsers;
    private GroupModel groupToBeUpdated;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        siteCreator = dataUser.createRandomTestUser();
        publicSite = dataSite.usingUser(siteCreator).createPublicRandomSite();
        publicSiteUsers = dataUser.addUsersWithRolesToSite(publicSite, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);
        regularGroup = dataGroup.createRandomGroup();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that collaborator is not able to update site group membership and gets status code FORBIDDEN (403)")
    public void collaboratorIsNotAbleToUpdateSiteGroup() throws Exception
    {
        groupToBeUpdated = dataGroup.createRandomGroup();
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingSite(publicSite).addSiteGroup(getId(groupToBeUpdated), UserRole.SiteConsumer);
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteCollaborator));
        restClient.withCoreAPI().usingSite(publicSite).updateSiteGroup(getId(groupToBeUpdated), UserRole.SiteCollaborator);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY);
        restClient.assertLastError()
                .containsSummary(String.format("The current user does not have permissions to modify the membership details of the site %s.", publicSite.getTitle()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that contributor is not able to update site membership and gets status code FORBIDDEN (403)")
    public void contributorIsNotAbleToUpdateSiteGroup() throws Exception
    {
        groupToBeUpdated = dataGroup.createRandomGroup();
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingSite(publicSite).addSiteGroup(getId(groupToBeUpdated), UserRole.SiteConsumer);
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteContributor));
        restClient.withCoreAPI().usingSite(publicSite).updateSiteGroup(getId(groupToBeUpdated), UserRole.SiteCollaborator);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY);
        restClient.assertLastError()
                .containsSummary(String.format("The current user does not have permissions to modify the membership details of the site %s.", publicSite.getTitle()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that consumer is not able to update site group and gets status code FORBIDDEN (403)")
    public void consumerIsNotAbleToUpdateSiteGroup() throws Exception
    {
        groupToBeUpdated = dataGroup.createRandomGroup();
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingSite(publicSite).addSiteGroup(getId(groupToBeUpdated), UserRole.SiteConsumer);
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteConsumer));
        restClient.withCoreAPI().usingSite(publicSite).updateSiteGroup(getId(groupToBeUpdated), UserRole.SiteCollaborator);
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY);
        restClient.assertLastError()
                .containsSummary(String.format("The current user does not have permissions to modify the membership details of the site %s.", publicSite.getTitle()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that admin is able to update site member and gets status code OK (200)")
    public void adminIsAbleToUpdateSiteGroup() throws Exception
    {
        groupToBeUpdated = dataGroup.createRandomGroup();
        restClient.authenticateUser(adminUser);
        restClient.withCoreAPI().usingSite(publicSite).addSiteGroup(getId(groupToBeUpdated), UserRole.SiteConsumer);
        restClient.withCoreAPI().usingSite(publicSite).updateSiteGroup(getId(groupToBeUpdated), UserRole.SiteCollaborator)
                .assertThat().field("id").is(getId(groupToBeUpdated))
                .and().field("role").is(UserRole.SiteCollaborator);
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY,
            description = "Verify that unauthenticated user is not able to update site member")

    public void unauthenticatedUserIsNotAuthorizedToUpdateSiteGroup() throws Exception
    {
        groupToBeUpdated = dataGroup.createRandomGroup();
        restClient.authenticateUser(adminUser);
        restClient.withCoreAPI().usingSite(publicSite).addSiteGroup(getId(groupToBeUpdated), UserRole.SiteConsumer);
        UserModel inexistentUser = new UserModel("inexistent user", "inexistent password");
        restClient.authenticateUser(inexistentUser);
        restClient.withCoreAPI().usingSite(publicSite).updateSiteGroup(getId(groupToBeUpdated), UserRole.SiteCollaborator);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if update site group request returns status code 404 when nonexistent siteId is used")
    public void updateSiteGroupOfNonexistentSite() throws Exception
    {
        groupToBeUpdated = dataGroup.createRandomGroup();
        SiteModel deletedSite = dataSite.usingUser(adminUser).createPublicRandomSite();
        dataSite.deleteSite(deletedSite);

        restClient.authenticateUser(adminUser).withCoreAPI().usingSite(deletedSite).updateSiteGroup(getId(groupToBeUpdated), UserRole.SiteConsumer);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, deletedSite.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if update site member request returns status code 400 when personId is not member of the site")
    public void updateNotASiteMember() throws Exception
    {
        groupToBeUpdated = dataGroup.createRandomGroup();
        restClient.authenticateUser(adminUser).withCoreAPI().usingSite(publicSite).updateSiteGroup(getId(groupToBeUpdated), UserRole.SiteConsumer);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary("authority is not a member of the site");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if update site member request returns status code 404 when personId does not exist")
    public void updateNonexistentSiteMember() throws Exception
    {
        GroupModel nonexistentUser = new GroupModel("nonexistentUser");
        restClient.authenticateUser(adminUser).withCoreAPI().usingSite(publicSite).updateSiteGroup(getId(nonexistentUser), UserRole.SiteConsumer);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format("An authority was not found for %s", getId(nonexistentUser)));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if update site member request returns status code 405 when empty personId is used")
    public void updateSiteMemberUsingEmptyPersonId() throws Exception
    {
        GroupModel emptyGroup = new GroupModel("");
        restClient.authenticateUser(adminUser).withCoreAPI().usingSite(publicSite).updateSiteGroup(getId(emptyGroup), UserRole.SiteConsumer);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format("An authority was not found for %s", getId(emptyGroup)));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if update site member request returns status code 400 when invalid role is used")
    public void updateSiteMemberUsingInvalidRole() throws Exception
    {
        groupToBeUpdated = dataGroup.createRandomGroup();
        restClient.authenticateUser(siteCreator).withCoreAPI();
        restClient.authenticateUser(adminUser).withCoreAPI().usingSite(publicSite).updateSiteGroup(getId(groupToBeUpdated), UserRole.SiteConsumer);
        String json = JsonBodyGenerator.keyValueJson("role","invalidRole");
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, json, "sites/{siteId}/group-members/{groupId}", publicSite.getId(), getId(groupToBeUpdated));
        restClient.processModel(RestSiteMemberModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary("authority is not a member of the site");
    }

    String getId(GroupModel group) {
        return "GROUP_" + group.getGroupIdentifier();
    }
}
