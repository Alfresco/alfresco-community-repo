package org.alfresco.rest.sites.groups;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.*;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.GroupModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class GetSiteGroupsTests extends RestTest
{
    private SiteModel publicSite, moderatedSite;
    private List<GroupModel> publicSiteGroups, moderatedSiteGroups;
    private DataUser.ListUserWithRoles publicSiteUsers;
    private UserModel siteCreator;
    private UserModel regularUser;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        siteCreator = dataUser.createRandomTestUser();
        regularUser = dataUser.createRandomTestUser();

        publicSite = dataSite.usingUser(siteCreator).createPublicRandomSite();
        moderatedSite = dataSite.usingUser(siteCreator).createModeratedRandomSite();

        publicSiteUsers = dataUser.addUsersWithRolesToSite(publicSite,UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer, UserRole.SiteContributor);
        publicSiteGroups = addGroupToSite(publicSite,UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer, UserRole.SiteContributor);
        moderatedSiteGroups = addGroupToSite(moderatedSite, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer, UserRole.SiteContributor);

        RestSiteGroupModelsCollection siteGroups = restClient.authenticateUser(siteCreator).withCoreAPI().usingSite(moderatedSite).usingParams("properties=role,id").getSiteGroups();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY,
            description = "Verify Manager role gets site groups and gets status code OK (200)")
    public void getSiteGroupsWithManagerRole() throws Exception
    {
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingSite(publicSite).getSiteGroups()
                .assertThat()
                .entriesListCountIs(4)
                .and().entriesListContains("id", getId(publicSiteGroups.get(0)))
                .and().entriesListContains("role", UserRole.SiteManager.name())
                .and().entriesListContains("id", getId(publicSiteGroups.get(1)))
                .and().entriesListContains("role", UserRole.SiteCollaborator.name())
                .and().entriesListContains("id", getId(publicSiteGroups.get(2)))
                .and().entriesListContains("role", UserRole.SiteConsumer.name())
                .and().entriesListContains("id", getId(publicSiteGroups.get(3)))
                .and().entriesListContains("role", UserRole.SiteContributor.name());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Collaborator role gets site groups and gets status code OK (200)")
    public void getSiteGroupsWithCollaboratorRole() throws Exception
    {
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteCollaborator))
                .withCoreAPI().usingSite(publicSite).getSiteGroups()
                .assertThat()
                .entriesListCountIs(4)
                .and().entriesListContains("id", getId(publicSiteGroups.get(1)))
                .and().entriesListContains("role", UserRole.SiteCollaborator.name());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Contributor role gets site groups and gets status code OK (200)")
    public void getSiteGroupsWithContributorRole() throws Exception
    {
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteContributor))
                .withCoreAPI().usingSite(publicSite).getSiteGroups()
                .assertThat()
                .entriesListCountIs(4)
                .and().entriesListContains("id", getId(publicSiteGroups.get(3)))
                .and().entriesListContains("role", UserRole.SiteContributor.name());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Consumer role gets site groups and gets status code OK (200)")
    public void getSiteGroupsWithConsumerRole() throws Exception
    {
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteConsumer))
                .withCoreAPI().usingSite(publicSite).getSiteGroups()
                .assertThat()
                .entriesListCountIs(4)
                .and().entriesListContains("id", getId(publicSiteGroups.get(2)))
                .and().entriesListContains("role", UserRole.SiteConsumer.name());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin user gets site groups and gets status code OK (200)")
    public void getSiteGroupsWithAdminUser() throws Exception
    {
        restClient.authenticateUser(dataUser.getAdminUser())
                .withCoreAPI().usingSite(publicSite).getSiteGroups()
                .assertThat()
                .entriesListCountIs(4)
                .and().entriesListContains("id", getId(publicSiteGroups.get(0)))
                .and().entriesListContains("role", UserRole.SiteManager.name());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY,
            description = "Failed authentication get site groups call returns status code 401")
    public void unauthenticatedUserIsNotAuthorizedToRetrieveSiteGroups() throws Exception
    {
        UserModel userModel = dataUser.createRandomTestUser();
        userModel.setPassword("user wrong password");
        dataUser.addUserToSite(userModel, publicSite, UserRole.SiteManager);
        restClient.authenticateUser(userModel).withCoreAPI().usingSite(publicSite).getSiteGroups();
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify get site members call returns status code 404 if siteId does not exist")
    public void checkStatusCodeForNonExistentSiteId() throws Exception
    {
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteContributor)).withCoreAPI()
                .usingSite("NonExistentSiteId").getSiteGroups();
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY)
                .assertLastError().containsSummary(String.format("Site %s does not exist", "NonExistentSiteId"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify get site groups call returns status code 400 for invalid maxItems")
    public void checkStatusCodeForInvalidMaxItems() throws Exception
    {
        restClient.authenticateUser(regularUser).withParams("maxItems=0").withCoreAPI().usingSite(publicSite).getSiteGroups();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                .containsErrorKey(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);

        restClient.withParams("maxItems=A").withCoreAPI().usingSite(publicSite).getSiteGroups();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.INVALID_MAXITEMS, "A"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify get site groups call returns status code 400 for invalid skipCount ")
    public void checkStatusCodeForInvalidSkipCount() throws Exception
    {
        restClient.authenticateUser(regularUser).withParams("skipCount=A")
                .withCoreAPI().usingSite(publicSite).getSiteGroups();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.INVALID_SKIPCOUNT, "A"));

        restClient.withParams("skipCount=-1")
                .withCoreAPI().usingSite(publicSite).getSiteGroups();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(RestErrorModel.NEGATIVE_VALUES_SKIPCOUNT);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if any user gets public site groups and status code is 200")
    public void getPublicSiteGroups() throws Exception
    {
        RestSiteGroupModelsCollection siteGroups = restClient.authenticateUser(regularUser).withCoreAPI().usingSite(publicSite).getSiteGroups();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteGroups.assertThat().entriesListCountIs(4).assertThat()
                .entriesListContains("id", getId(publicSiteGroups.get(0)))
                .and().entriesListContains("id", getId(publicSiteGroups.get(1)))
                .and().entriesListContains("id", getId(publicSiteGroups.get(2)))
                .and().entriesListContains("id", getId(publicSiteGroups.get(3)))
                .and().paginationField("count").is("4");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if any user gets moderated site groups and status code is 200")
    public void getModeratedSiteGroups() throws Exception
    {
        RestSiteGroupModelsCollection siteGroups = restClient.authenticateUser(regularUser).withCoreAPI().usingSite(moderatedSite).getSiteGroups();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteGroups.assertThat().entriesListCountIs(4).assertThat()
                .entriesListContains("id", getId(moderatedSiteGroups.get(0))).and()
                .entriesListContains("id", getId(moderatedSiteGroups.get(1))).and()
                .entriesListContains("id", getId(moderatedSiteGroups.get(2))).and()
                .entriesListContains("id", getId(moderatedSiteGroups.get(3))).and()
                .paginationField("count").is("4");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if any user gets moderated site groups with properties parameter applied and status code is 200")
    public void getModeratedSiteGroupsUsingPropertiesParameter() throws Exception
    {
        RestSiteGroupModelsCollection siteGroups = restClient.authenticateUser(siteCreator).withCoreAPI().usingSite(moderatedSite).usingParams("properties=role,id").getSiteGroups();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteGroups.assertThat().entriesListCountIs(4)
                .and().entriesListDoesNotContain("person")
                .and().entriesListContains("role", UserRole.SiteManager.toString())
                .and().entriesListContains("id", getId(moderatedSiteGroups.get(0)))
                .and().entriesListContains("role", UserRole.SiteContributor.toString())
                .and().entriesListContains("id", getId(moderatedSiteGroups.get(1)))
                .and().entriesListContains("role", UserRole.SiteCollaborator.toString())
                .and().entriesListContains("id", getId(moderatedSiteGroups.get(2)))
                .and().entriesListContains("role", UserRole.SiteConsumer.toString())
                .and().entriesListContains("id", getId(moderatedSiteGroups.get(3)));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if any user gets moderated site groups with skipCount parameter applied")
    public void getModeratedSiteGroupsUsingSkipCountParameter() throws Exception
    {
        RestSiteGroupModelsCollection siteGroups = restClient.authenticateUser(siteCreator).withCoreAPI().usingSite(moderatedSite).usingParams("skipCount=2").getSiteGroups();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteGroups.assertThat().paginationField("count").is("2");
        siteGroups.assertThat().paginationField("skipCount").is("2");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if any user gets moderated site groups with high skipCount parameter applied")
    public void getModeratedSiteGroupsUsingHighSkipCountParameter() throws Exception
    {
        RestSiteGroupModelsCollection siteGroups = restClient.authenticateUser(siteCreator).withCoreAPI().usingSite(moderatedSite).usingParams("skipCount=100").getSiteGroups();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteGroups.assertThat().paginationField("count").is("0");
        siteGroups.assertThat().paginationField("skipCount").is("100");
        siteGroups.assertThat().entriesListIsEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if any user gets moderated site groups with maxItems parameter applied and check all pagination fields")
    public void getModeratedSiteGroupsUsingMaxItemsParameter() throws Exception
    {
        RestSiteGroupModelsCollection siteGroups = restClient.authenticateUser(siteCreator).withCoreAPI().usingSite(moderatedSite).usingParams("maxItems=1").getSiteGroups();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteGroups.assertThat().paginationField("count").is("1");
        siteGroups.assertThat().paginationField("hasMoreItems").is("true");
        siteGroups.assertThat().paginationField("maxItems").is("1");
        siteGroups.assertThat().paginationField("totalItems").is("4");
        siteGroups.assertThat().entriesListCountIs(1);
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
