package org.alfresco.rest.sites.groups;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.exception.DataPreparationException;
import org.alfresco.utility.model.GroupModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class RemoveSiteGroupTests  extends RestTest
{
    private SiteModel publicSiteModel, privateSiteModel;
    private UserModel adminUserModel;
    private DataUser.ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws DataPreparationException
    {
        adminUserModel = dataUser.getAdminUser();
        UserModel siteCreator = dataUser.createRandomTestUser();

        publicSiteModel = dataSite.usingUser(siteCreator).createPublicRandomSite();
        privateSiteModel = dataSite.usingUser(siteCreator).createPrivateRandomSite();

        usersWithRoles = dataUser.addUsersWithRolesToSite(publicSiteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer, UserRole.SiteContributor);
        addGroupToSite(publicSiteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer, UserRole.SiteContributor);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.SITES }, executionType = ExecutionType.SANITY, description = "Verify manager can delete site group and gets status code 204, 'No Content'")
    public void siteManagerIsAbleToDeleteSiteGroup() throws Exception
    {
        GroupModel group = addGroupToSite(publicSiteModel, UserRole.SiteConsumer).get(0);
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingSite(publicSiteModel).deleteSiteGroup(getId(group));
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        Utility.sleep(300, 30000, () -> restClient.withCoreAPI().usingSite(publicSiteModel)
                .getSiteGroups().assertThat().entriesListDoesNotContain("id", getId(group)));
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.SITES }, executionType = ExecutionType.REGRESSION, description = "Verify collaborator cannot delete site group and gets status code 403, 'Forbidden'")
    public void siteCollaboratorIsNotAbleToDeleteSiteGroup() throws Exception
    {
        GroupModel group = addGroupToSite(publicSiteModel, UserRole.SiteConsumer).get(0);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));
        restClient.withCoreAPI().usingSite(publicSiteModel).deleteSiteGroup(getId(group));
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY);

        Utility.sleep(300, 30000, () -> restClient.withCoreAPI()
                .usingSite(publicSiteModel).getSiteGroups()
                .assertThat().entriesListContains("id", getId(group)));
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.SITES }, executionType = ExecutionType.REGRESSION, description = "Verify contributor cannot delete site group and gets status code 403, 'Forbidden'")
    public void siteContributorIsNotAbleToDeleteSiteGroup() throws Exception
    {
        GroupModel group = addGroupToSite(publicSiteModel, UserRole.SiteConsumer).get(0);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        restClient.withCoreAPI().usingSite(publicSiteModel).deleteSiteGroup(getId(group));
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY);

        Utility.sleep(300, 30000, () -> restClient.withCoreAPI().usingSite(publicSiteModel)
                .getSiteGroups().assertThat().entriesListContains("id", getId(group)));
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.SITES }, executionType = ExecutionType.REGRESSION, description = "Verify consumer cannot delete site member and gets status code 403, 'Forbidden'")
    public void siteConsumerIsNotAbleToDeleteSiteGroup() throws Exception
    {
        GroupModel group = addGroupToSite(publicSiteModel, UserRole.SiteConsumer).get(0);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).withCoreAPI().usingSite(publicSiteModel).deleteSiteGroup(getId(group));
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY);

        Utility.sleep(300, 30000, () -> restClient.withCoreAPI().usingSite(publicSiteModel)
                .getSiteGroups().assertThat().entriesListContains("id", getId(group)));
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.SITES }, executionType = ExecutionType.SANITY, description = "Verify that unauthenticated user is not able to delete site group")
    public void unauthenticatedUserIsNotAuthorizedToDeleteSiteGroup() throws Exception
    {
        GroupModel group = addGroupToSite(publicSiteModel, UserRole.SiteConsumer).get(0);

        UserModel inexistentUser = new UserModel("inexistent user", "inexistent password");
        restClient.authenticateUser(inexistentUser).withCoreAPI().usingSite(publicSiteModel).deleteSiteGroup(getId(group));

        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify manager can NOT delete site group for an inexistent user and gets status code 404, 'Not Found'")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void managerIsNotAbleToDeleteInexistentSiteGroup() throws Exception
    {
        GroupModel inexistentUser = new GroupModel("inexistentUser");

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingSite(publicSiteModel).deleteSiteGroup(getId(inexistentUser));
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format("An authority was not found for %s", getId(inexistentUser)));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify manager can NOT delete site group for a non site group and gets status code 400, 'Bad Request'")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void managerIsNotAbleToDeleteNotSiteGroup() throws Exception
    {
        GroupModel nonMember = dataGroup.createRandomGroup();

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingSite(publicSiteModel).deleteSiteGroup(getId(nonMember));
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format("Given authority is not a member of the site"));
    }


    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that manager can NOT delete site group for an invalid site and gets status code 404, 'Not Found'")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void managerIsNotAbleToDeleteSiteMemberOfInvalidSite() throws Exception
    {
        SiteModel invalidSite = new SiteModel("invalidSite");
        GroupModel testGroupModel = dataGroup.createRandomGroup();

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingSite(invalidSite).deleteSiteGroup(getId(testGroupModel));
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_WAS_NOT_FOUND, "invalidSite"));
    }


    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that admin can delete a site group of private site and gets status code 204")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    public void adminIsAbleToDeletePrivateSiteGroup() throws Exception
    {
        GroupModel group = addGroupToSite(privateSiteModel, UserRole.SiteConsumer).get(0);

        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(privateSiteModel).deleteSiteGroup(getId(group));
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that admin can not delete a site group twice and gets status code 404 for the second attempt")
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @Bug(id="ACE-5447")
    public void adminIsNotAbleToRemoveSiteGroupTwice() throws Exception
    {
        GroupModel group = addGroupToSite(publicSiteModel, UserRole.SiteContributor).get(0);

        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(publicSiteModel).deleteSiteGroup(getId(group));
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(publicSiteModel).deleteSiteGroup(getId(group));
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary("Given authority is not a member of the site");
    }

    List<GroupModel> addGroupToSite(SiteModel siteModel, UserRole ...roles) {
        List<GroupModel> groups = new ArrayList<GroupModel>();
        for (UserRole role: roles) {
            GroupModel group =  dataGroup.createRandomGroup();
            restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingSite(siteModel).addSiteGroup(getId(group), role);
            groups.add(group);
        }
        return groups;
    }

    String getId(GroupModel group) {
        return "GROUP_" + group.getGroupIdentifier();
    }
}

