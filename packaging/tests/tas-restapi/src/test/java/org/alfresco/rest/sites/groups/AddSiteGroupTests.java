package org.alfresco.rest.sites.groups;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestSiteGroupModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
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

public class AddSiteGroupTests extends RestTest
{
    private UserModel adminUserModel;
    private SiteModel publicSiteModel, moderatedSiteModel;
    private ListUserWithRoles publicSiteUsersWithRoles;
    private String addMemberJson, addMembersJson;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        adminUserModel = dataUser.getAdminUser();
        publicSiteModel = dataSite.usingUser(adminUserModel).createPublicRandomSite();
        moderatedSiteModel = dataSite.usingUser(adminUserModel).createModeratedRandomSite();
        publicSiteUsersWithRoles = dataUser.addUsersWithRolesToSite(publicSiteModel,
                UserRole.SiteManager,
                UserRole.SiteCollaborator,
                UserRole.SiteConsumer,
                UserRole.SiteContributor);
        addMemberJson = "{\"role\":\"%s\",\"id\":\"%s\"}";
        addMembersJson = "{\"role\":\"%s\",\"id\":\"%s\"}, {\"role\":\"%s\",\"id\":\"%s\"}";
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that user can not be added to an inexistent site and gets status code 404")
    public void notAbleToAddGroupToAnInExistentSite() throws Exception
    {
        GroupModel group = dataGroup.createRandomGroup();
        SiteModel inexistentSite = new SiteModel("inexistentSite");
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(inexistentSite).addSiteGroup(getId(group), UserRole.SiteManager);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, inexistentSite.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that inexistent group can not be added to site and gets status code 404")
    public void notAbleToAddInExistentGroupToSite() throws Exception
    {
        GroupModel group = new GroupModel("inExistentGroup");
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(publicSiteModel).addSiteGroup(group.getGroupIdentifier(), UserRole.SiteManager);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsErrorKey(RestErrorModel.API_DEFAULT_ERRORKEY)
                .containsSummary(String.format("An authority was not found for %s", group.getGroupIdentifier()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that user can not add group that is already present and gets status code 409")
    public void notAbleToAddGroupThatIsAlreadyAPresent() throws Exception
    {
        GroupModel group = dataGroup.createRandomGroup();
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(moderatedSiteModel).addSiteGroup(getId(group), UserRole.SiteManager);
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(moderatedSiteModel).addSiteGroup(getId(group), UserRole.SiteManager);
        restClient.assertStatusCodeIs(HttpStatus.CONFLICT).assertLastError().containsSummary(String.format(RestErrorModel.ALREADY_Site_MEMBER, getId(group), moderatedSiteModel.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that several groups with different roles can be added once in a row to a site and gets status code 201")
    public void addSeveralGroupsWithDifferentRolesToASite() throws Exception
    {
        GroupModel firstGroup = dataGroup.createRandomGroup();
        GroupModel secondGroup = dataGroup.createRandomGroup();
        restClient.authenticateUser(adminUserModel).withCoreAPI();
        String json = String.format(addMembersJson, UserRole.SiteContributor, getId(firstGroup), UserRole.SiteCollaborator, getId(secondGroup));
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, json, "sites/{siteId}/group-members?{parameters}", publicSiteModel.getId(), restClient.getParameters());
        restClient.processModel(RestSiteGroupModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that several groups with same roles can be added once in a row to a site and gets status code 201")
    public void addSeveralGroupsWithSameRolesToASite() throws Exception
    {
        GroupModel firstGroup = dataGroup.createRandomGroup();
        GroupModel secondGroup = dataGroup.createRandomGroup();
        restClient.authenticateUser(adminUserModel).withCoreAPI();
        String json = String.format(addMembersJson, UserRole.SiteCollaborator, getId(firstGroup), UserRole.SiteCollaborator, getId(secondGroup));
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, json, "sites/{siteId}/group-members?{parameters}", publicSiteModel.getId(), restClient.getParameters());
        restClient.processModel(RestSiteGroupModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that several users that are already added to the site can not be added once in a row and gets status code 400")
    public void addSeveralGroupsThatAreAlreadyAddedToASite() throws Exception
    {
        GroupModel firstGroup = dataGroup.createRandomGroup();
        GroupModel secondGroup = dataGroup.createRandomGroup();
        restClient.authenticateUser(adminUserModel).withCoreAPI();
        String json = String.format(addMembersJson, UserRole.SiteCollaborator, getId(firstGroup), UserRole.SiteCollaborator, getId(secondGroup));
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, json, "sites/{siteId}/group-members?{parameters}", publicSiteModel.getId(), restClient.getParameters());
        restClient.processModel(RestSiteGroupModel.class, request);
        restClient.processModel(RestSiteGroupModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.CONFLICT).assertLastError().containsSummary(String.format(RestErrorModel.ALREADY_Site_MEMBER, getId(firstGroup), publicSiteModel.getId())) ;
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Add new site group membership request by providing an empty body")
    public void addSiteGroupsUsingEmptyBody() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, "", "sites/{siteId}/group-members?{parameters}", publicSiteModel.getId(), restClient.getParameters());
        restClient.processModel(RestSiteGroupModel.class, request);
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
        GroupModel group = dataGroup.createRandomGroup();
        String json = String.format(addMemberJson, "SITEMANAGER", getId(group));
        restClient.authenticateUser(adminUserModel).withCoreAPI();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, json, "sites/{siteId}/group-members?{parameters}", publicSiteModel.getId(), restClient.getParameters());
        restClient.processModel(RestSiteGroupModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format("An authority was not found for GROUP_site_%s_%s", publicSiteModel.getId(), "SITEMANAGER"))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);

        json = String.format(addMemberJson, "sitemanager", getId(group));
        restClient.authenticateUser(adminUserModel).withCoreAPI();
        request = RestRequest.requestWithBody(HttpMethod.POST, json, "sites/{siteId}/group-members?{parameters}", publicSiteModel.getId(), restClient.getParameters());
        restClient.processModel(RestSiteGroupModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format("An authority was not found for GROUP_site_%s_%s", publicSiteModel.getId(), "sitemanager"))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Check empty value for user role")
    public void checkEmptyValueForRole() throws Exception
    {
        GroupModel group = dataGroup.createRandomGroup();
        String json = String.format(addMemberJson, "", getId(group));
        restClient.authenticateUser(adminUserModel).withCoreAPI();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, json, "sites/{siteId}/group-members?{parameters}", publicSiteModel.getId(), restClient.getParameters());
        restClient.processModel(RestSiteGroupModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsErrorKey(String.format(RestErrorModel.NO_CONTENT, "N/A"))
                .containsSummary(String.format(RestErrorModel.NO_CONTENT, "N/A"))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY,
            description = "Verify that manager is able to add site group membership and gets status code CREATED (201)")
    public void managerIsAbleToAddSiteGroup() throws Exception
    {
        GroupModel group = dataGroup.createRandomGroup();
        restClient.authenticateUser(publicSiteUsersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingSite(publicSiteModel).addSiteGroup(getId(group), UserRole.SiteCollaborator)
                .assertThat()
                .field("id").is(getId(group))
                .and()
                .field("role").is(UserRole.SiteCollaborator);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that site collaborator is not able to add site group membership and gets status code FORBIDDEN (403)")
    public void collaboratorIsNotAbleToAddSiteGroup() throws Exception
    {
        GroupModel group = dataGroup.createRandomGroup();
        restClient.authenticateUser(publicSiteUsersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .withCoreAPI().usingSite(publicSiteModel).addSiteGroup(getId(group), UserRole.SiteCollaborator);

        restClient.assertLastError().containsSummary("The current user does not have permissions to modify the membership details of the site");
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION})
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that site contributor is not able to add site group membership and gets status code FORBIDDEN (403)")
    public void contributorIsNotAbleToAddSiteGroup() throws Exception
    {
        GroupModel group = dataGroup.createRandomGroup();
        restClient.authenticateUser(publicSiteUsersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                .withCoreAPI().usingSite(publicSiteModel).addSiteGroup(getId(group), UserRole.SiteContributor);

        restClient.assertLastError().containsSummary("The current user does not have permissions to modify the membership details of the site");
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that site consumer is not able to add site group membership and gets status code FORBIDDEN (403)")
    public void consumerIsNotAbleToAddSiteGroup() throws Exception
    {
        GroupModel group = dataGroup.createRandomGroup();
        restClient.authenticateUser(publicSiteUsersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                .withCoreAPI().usingSite(publicSiteModel).addSiteGroup(getId(group), UserRole.SiteConsumer);;

        restClient.assertLastError().containsSummary("The current user does not have permissions to modify the membership details of the site");
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY,
            description = "Verify that admin user is able to add site group membership and gets status code CREATED (201)")
    public void adminIsAbleToAddSiteGroup() throws Exception
    {
        GroupModel group = dataGroup.createRandomGroup();
        restClient.authenticateUser(adminUserModel)
                .withCoreAPI().usingSite(publicSiteModel).addSiteGroup(getId(group), UserRole.SiteConsumer)
                .assertThat()
                .field("id").is(getId(group))
                .and()
                .field("role").is(UserRole.SiteConsumer);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY,
            description = "Verify that unauthenticated user is not able to add site group membership")
    public void unauthenticatedUserIsNotAuthorizedToAddSiteGroup() throws Exception
    {
        GroupModel group = dataGroup.createRandomGroup();

        UserModel userModel = dataUser.createRandomTestUser();
        userModel.setPassword("user wrong password");
        dataUser.addUserToSite(userModel, publicSiteModel, UserRole.SiteManager);

        restClient.authenticateUser(userModel)
                .withCoreAPI().usingSite(publicSiteModel).addSiteGroup(getId(group), UserRole.SiteCollaborator);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify that manager can add another user as manager to a public site and gets status code CREATED (201)")
    public void addManagerToPublicSite() throws Exception
    {
        GroupModel group = dataGroup.createRandomGroup();
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(publicSiteModel).addSiteGroup(getId(group), UserRole.SiteManager)
                .assertThat()
                .field("id").is(getId(group))
                .and()
                .field("role").is(UserRole.SiteManager);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
    }

    String getId(GroupModel group) {
        return "GROUP_" + group.getGroupIdentifier();
    }
}
