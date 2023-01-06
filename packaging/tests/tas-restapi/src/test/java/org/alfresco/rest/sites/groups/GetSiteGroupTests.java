package org.alfresco.rest.sites.groups;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.exception.DataPreparationException;
import org.alfresco.utility.model.GroupModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetSiteGroupTests extends RestTest
{
    private UserModel adminUser;
    private SiteModel publicSiteModel;
    private DataUser.ListUserWithRoles usersWithRoles;
    private GroupModel manager, consumer;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws DataPreparationException
    {
        adminUser = dataUser.getAdminUser();
        publicSiteModel = dataSite.usingUser(adminUser).createPublicRandomSite();
        usersWithRoles = dataUser.addUsersWithRolesToSite(publicSiteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);

        consumer = dataGroup.createRandomGroup();
        dataGroup.addListOfUsersToGroup(consumer, dataUser.createRandomTestUser());

        manager = dataGroup.createRandomGroup();
        dataGroup.addListOfUsersToGroup(consumer, dataUser.createRandomTestUser());

        restClient.authenticateUser(adminUser).withCoreAPI().usingSite(publicSiteModel).addSiteGroup(getId(manager), UserRole.SiteManager);
        restClient.authenticateUser(adminUser).withCoreAPI().usingSite(publicSiteModel).addSiteGroup(getId(consumer), UserRole.SiteConsumer);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin user can get site group and gets status code OK (200)")
    public void getSiteGroupWithAdminUser() throws Exception
    {
        restClient.authenticateUser(adminUser);
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteGroup(getId(manager))
                .and().field("id").is(getId(manager))
                .and().field("role").is(UserRole.SiteManager);
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY,
            description = "Failed authentication get site group call returns status code 401")
    public void unauthenticatedUserIsNotAuthorizedToRetrieveSiteGroup() throws JsonToModelConversionException, Exception
    {
        UserModel inexistentUser = new UserModel("inexistent user", "inexistent password");
        restClient.authenticateUser(inexistentUser).withCoreAPI().usingSite(publicSiteModel).getSiteGroup(getId(consumer));
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Manager role doesn't get a site group of inexistent site and status code is Not Found (404)")
    public void getSiteGroupOfInexistentSite() throws Exception
    {
        SiteModel invalidSite = new SiteModel("invalidSite");

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingSite(invalidSite).getSiteGroup(getId(consumer));
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_WAS_NOT_FOUND, invalidSite.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Manager role doesn't get non site group of inexistent site and status code is Not Found (404)")
    public void getSiteGroupForNonSiteGroup() throws Exception
    {
        GroupModel nonGroup = dataGroup.createRandomGroup();

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteGroup(getId(nonGroup));
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format("Given authority is not a member of the site"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Manager role doesn't get not existing site group and status code is Not Found (404)")
    public void getSiteGroupForInexistentSiteGroup() throws Exception
    {
        GroupModel inexistentGroup = new GroupModel("inexistentGroup");

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteGroup(getId(inexistentGroup));
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format("An authority was not found for %s", getId(inexistentGroup)));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Manager role gets site groups with Manager role and status code is OK (200)")
    public void getSiteGroupWithManagerRole() throws Exception
    {
        GroupModel anotherManager = dataGroup.createRandomGroup();
        restClient.withCoreAPI().usingSite(publicSiteModel).addSiteGroup(getId(anotherManager), UserRole.SiteManager);

        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteGroup(getId(anotherManager))
                .assertThat().field("id").is(getId(anotherManager))
                .and().field("role").is(UserRole.SiteManager);
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    String getId(GroupModel group) {
        return "GROUP_" + group.getGroupIdentifier();
    }
}
