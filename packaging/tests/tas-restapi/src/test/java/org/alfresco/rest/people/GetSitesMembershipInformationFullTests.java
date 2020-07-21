package org.alfresco.rest.people;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestSiteMembershipModelsCollection;
import org.alfresco.rest.model.RestTaskModel;
import org.alfresco.utility.constants.UserRole;
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
 * 
 * @author Cristina Axinte
 *
 */
public class GetSitesMembershipInformationFullTests extends RestTest
{
    private SiteModel publicSiteModel;
    private SiteModel moderatedSiteModel;
    private SiteModel privateSiteModel;
    private SiteModel anotherUserSite;
    private UserModel userModel, adminUser, anotherUserModel;
    private RestSiteMembershipModelsCollection sitesMembershipInformation;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws DataPreparationException
    {
        adminUser = dataUser.getAdminUser();
        anotherUserSite = dataSite.usingUser(adminUser).createPublicRandomSite();
        userModel = dataUser.createRandomTestUser();
        anotherUserModel = dataUser.createRandomTestUser();
        publicSiteModel = dataSite.usingUser(adminUser).createPublicRandomSite();
        dataUser.addUserToSite(userModel, publicSiteModel, UserRole.SiteCollaborator);
        moderatedSiteModel = dataSite.usingUser(adminUser).createModeratedRandomSite();
        dataUser.addUserToSite(userModel, moderatedSiteModel, UserRole.SiteConsumer);
        privateSiteModel = dataSite.usingUser(userModel).createPrivateRandomSite();
        dataUser.addUserToSite(anotherUserModel, anotherUserSite, UserRole.SiteCollaborator);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, 
        description = "Verify if get site membership information request returns status code 200 for valid properties parameter")
    public void getSiteMembershipInformationUsingPropertiesParameter() throws Exception
    {
        sitesMembershipInformation = restClient.authenticateUser(userModel).withParams("properties=id").withCoreAPI().usingAuthUser().getSitesMembershipInformation();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sitesMembershipInformation.assertThat().entriesListIsNotEmpty().getPagination().assertThat().field("count").is("3");
        sitesMembershipInformation.assertThat().entriesListContains("id", publicSiteModel.getId())
            .and().entriesListContains("id", moderatedSiteModel.getId())
            .and().entriesListContains("id", privateSiteModel.getId())
            .and().entriesListDoesNotContain("site")
            .and().entriesListDoesNotContain("role")
            .and().entriesListDoesNotContain("guid");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, 
        description = "Verify if get site membership information request returns status code 200 for order ASC by parameter")
    public void getSiteMembershipInformationUsingOrderAscByParameter() throws Exception
    {
        sitesMembershipInformation = restClient.authenticateUser(userModel).withParams("orderBy=id ASC").withCoreAPI().usingAuthUser().getSitesMembershipInformation();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sitesMembershipInformation.assertThat().entriesListIsNotEmpty().getPagination().assertThat().field("count").is("3");
        sitesMembershipInformation.assertThat().entriesListIsSortedAscBy("id");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, 
        description = "Verify if get site membership information request returns status code 200 for order DESC by parameter")
    public void getSiteMembershipInformationUsingOrderDescByParameter() throws Exception
    {
        sitesMembershipInformation = restClient.authenticateUser(userModel).withParams("orderBy=id Desc").withCoreAPI().usingAuthUser().getSitesMembershipInformation();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sitesMembershipInformation.assertThat().entriesListIsNotEmpty().getPagination().assertThat().field("count").is("3");
        sitesMembershipInformation.assertThat().entriesListIsSortedDescBy("id");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, 
        description = "Verify if get site membership information request returns status code 200 for order by role parameter")
    public void getSiteMembershipInformationUsingOrderByRoleParameter() throws Exception
    {
        sitesMembershipInformation = restClient.authenticateUser(userModel).withParams("orderBy=role").withCoreAPI().usingAuthUser().getSitesMembershipInformation();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sitesMembershipInformation.assertThat().entriesListIsNotEmpty().getPagination().assertThat().field("count").is("3");
        sitesMembershipInformation.assertThat().entriesListIsSortedAscBy("role");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, 
        description = "Verify if user gets site membership information of another user successfully")
    public void userCanGetSiteMembershipInformationOfAnotherUser() throws Exception
    {
        sitesMembershipInformation = restClient.authenticateUser(userModel).withCoreAPI().usingUser(anotherUserModel).getSitesMembershipInformation();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sitesMembershipInformation.assertThat().entriesListIsNotEmpty().getPagination().assertThat().field("count").is("1");
        sitesMembershipInformation.getEntries().get(0).onModel()
            .assertThat().field("site.visibility").is(anotherUserSite.getVisibility().toString())
            .and().field("site.guid").is(anotherUserSite.getGuid())
            .and().field("site.description").is(anotherUserSite.getDescription())
            .and().field("site.id").is(anotherUserSite.getId())
            .and().field("site.preset").is("site-dashboard")
            .and().field("site.title").is(anotherUserSite.getTitle())
            .and().field("id").is(anotherUserSite.getId())
            .and().field("guid").is(anotherUserSite.getGuid())
            .and().field("role").is(UserRole.SiteCollaborator.toString());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, 
        description = "Verify if get site membership information request returns status code 200 for relations parameter")
    public void getSiteMembershipInformationUsingRelationsParameter() throws Exception
    {
        sitesMembershipInformation = restClient.authenticateUser(userModel).withParams("relations=site-membership-requests").withCoreAPI().usingAuthUser().getSitesMembershipInformation();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sitesMembershipInformation.assertThat().entriesListIsNotEmpty().getPagination().assertThat().field("count").is("3");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, 
            description = "Verify get site membership information request when a site membership request is rejected")
    public void getSiteMembershipInformationWhenASiteRequestIsRejected() throws Exception
    {
        UserModel userWithRequests = dataUser.createRandomTestUser();

        restClient.authenticateUser(userWithRequests).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSiteModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        sitesMembershipInformation = restClient.authenticateUser(adminUser).withCoreAPI().usingUser(userWithRequests).getSitesMembershipInformation();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sitesMembershipInformation.assertThat().entriesListIsEmpty()
            .and().paginationField("count").is("0");

        RestTaskModel taskModel = restClient.authenticateUser(userWithRequests).withWorkflowAPI().getTasks().getTaskModelByDescription(moderatedSiteModel);
        workflow.approveSiteMembershipRequest(adminUser.getUsername(), adminUser.getPassword(), taskModel.getId(), false, "Rejected");

        sitesMembershipInformation = restClient.authenticateUser(adminUser).withCoreAPI().usingUser(userWithRequests).getSitesMembershipInformation();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sitesMembershipInformation.assertThat().entriesListIsEmpty()
            .and().paginationField("count").is("0");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, 
            description = "Verify get site membership information request when a site membership request is approved")
    public void getSiteMembershipInformationWhenASiteRequestIsAppropved() throws Exception
    {
        UserModel userWithRequests = dataUser.createRandomTestUser();

        restClient.authenticateUser(userWithRequests).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSiteModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        sitesMembershipInformation = restClient.authenticateUser(adminUser).withCoreAPI().usingUser(userWithRequests).getSitesMembershipInformation();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sitesMembershipInformation.assertThat().entriesListIsEmpty()
            .and().paginationField("count").is("0");

        RestTaskModel taskModel = restClient.authenticateUser(userWithRequests).withWorkflowAPI().getTasks().getTaskModelByDescription(moderatedSiteModel);
        workflow.approveSiteMembershipRequest(adminUser.getUsername(), adminUser.getPassword(), taskModel.getId(), true, "Approve");

        sitesMembershipInformation = restClient.authenticateUser(adminUser).withCoreAPI().usingUser(userWithRequests).getSitesMembershipInformation();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sitesMembershipInformation.assertThat().entriesListIsNotEmpty()
            .and().entriesListContains("role", UserRole.SiteConsumer.toString())
            .and().entriesListContains("guid", moderatedSiteModel.getGuid())
            .and().entriesListContains("id", moderatedSiteModel.getId())
            .and().paginationField("count").is("1");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, 
        description = "Verify if get site membership information request returns status code 200 for valid maxItems parameter")
    public void getSiteMembershipInformationUsingMaxItemsParameter() throws Exception
    {
        sitesMembershipInformation = restClient.authenticateUser(userModel).withParams("maxItems=2").withCoreAPI().usingAuthUser().getSitesMembershipInformation();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sitesMembershipInformation.assertThat().entriesListCountIs(2)
            .getPagination().assertThat().field("count").is("2")
            .and().field("hasMoreItems").is("true")
            .and().field("totalItems").is("3")
            .and().field("skipCount").is("0")
            .and().field("maxItems").is("2");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, 
        description = "Verify if get site membership information request returns status code 200 for valid skipCount parameter")
    public void getSiteMembershipInformationUsingSkipCountParameter() throws Exception
    {
        sitesMembershipInformation = restClient.authenticateUser(userModel).withParams("skipCount=2").withCoreAPI().usingAuthUser().getSitesMembershipInformation();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sitesMembershipInformation.assertThat().entriesListCountIs(1)
            .getPagination().assertThat().field("count").is("1")
            .and().field("hasMoreItems").is("false")
            .and().field("totalItems").is("3")
            .and().field("skipCount").is("2")
            .and().field("maxItems").is("100");
    }
}
