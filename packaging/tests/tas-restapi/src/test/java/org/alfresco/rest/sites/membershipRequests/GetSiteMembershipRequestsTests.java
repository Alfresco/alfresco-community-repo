package org.alfresco.rest.sites.membershipRequests;

import static org.alfresco.utility.report.log.Step.STEP;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.*;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for
 * GET /people/{personId}/site-membership-requests
 * GET /site-membership-requests
 * POST /sites/{siteId}/site-membership-requests/{inviteeId}/approve
 * POST /sites/{siteId}/site-membership-requests/{inviteeId}/reject
 * 
 * @author Cristina Axinte
 */

public class GetSiteMembershipRequestsTests extends RestTest
{
    UserModel siteManager, newMember, adminUser, regularUser, testUser, testUser1;
    DataUser.ListUserWithRoles usersWithRoles;
    SiteModel moderatedSite1, moderatedSite2;
    RestSiteMembershipRequestModelsCollection siteMembershipRequests;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        siteManager = dataUser.createRandomTestUser();
        newMember = dataUser.createRandomTestUser();
        regularUser = dataUser.createRandomTestUser();

        testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteConsumer);
        testUser1 = dataUser.createRandomTestUser("testUser1");
        testUser1.setUserRole(UserRole.SiteConsumer);
        
        moderatedSite1 = dataSite.usingUser(siteManager).createModeratedRandomSite();
        moderatedSite2 = dataSite.usingUser(siteManager).createModeratedRandomSite();
        
        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite1);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.authenticateUser(adminUser).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite1);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        usersWithRoles = dataUser.addUsersWithRolesToSite(moderatedSite1, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @Bug(id = "MNT-16557")
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify manager user gets all site membership requests of a specific person with Rest API and response is successful (200)")
    public void managerUserGetsSiteMembershipRequestsWithSuccess() throws Exception
    {
        siteMembershipRequests = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).withCoreAPI()
                .usingUser(newMember).getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembershipRequests.assertThat().entriesListIsNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @Bug(id = "MNT-16557")
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify collaborator user fails to get all site membership requests of another user with Rest API (403)")
    public void collaboratorUserFailsToGetSiteMembershipRequestsOfAnotherUser() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI()
                .usingUser(newMember).getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @Bug(id = "MNT-16557")
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify contributor user fails to get all site membership requests of another user with Rest API (403)")
    public void contributorUserFailsToGetSiteMembershipRequestsOfAnotherUser() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).withCoreAPI()
                .usingUser(newMember).getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @Bug(id = "MNT-16557")
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify consumer user fails to get all site membership requests of another user with Rest API (403)")
    public void consumerUserFailsToGetSiteMembershipRequestsOfAnotherUser() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).withCoreAPI().usingUser(newMember).getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @Bug(id = "MNT-16557")
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify admin user gets all site membership requests of a specific person with Rest API and response is successful (200)")
    public void adminGetsSiteMembershipRequestsOfanotherUserWithSuccess() throws Exception
    {
        siteMembershipRequests = restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI()
                .usingUser(newMember).getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembershipRequests.assertThat().entriesListIsNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
//    @Bug(id = "MNT-16904", description = "It fails only on environment with tenants")
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify manager user fails to get all site membership requests of a specific person with Rest API when the authentication fails (401)")
    public void unauthorizedUserFailsToGetSiteMembershipRequests() throws Exception
    {
        restClient.authenticateUser(new UserModel("random user", "random password")).withCoreAPI()
                .usingAuthUser().getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify a user gets all its own site membership requests with Rest API and response is successful (200)")
    public void oneUserGetsItsOwnSiteMembershipRequestsWithSuccess() throws Exception
    {
        siteMembershipRequests = restClient.authenticateUser(newMember).withCoreAPI().usingUser(newMember).getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembershipRequests.assertThat().entriesListContains("id", moderatedSite1.getId())
                .assertThat().entriesListContains("message", "Please accept me")
                .assertThat().entriesListCountIs(2);
        RestSiteModel firstSite = siteMembershipRequests.getEntries().get(0).onModel().getSite();
        if (firstSite.getId().equals(moderatedSite1.getId()))
        {
            firstSite.assertThat().field("visibility").is(moderatedSite1.getVisibility())
                    .assertThat().field("guid").is(moderatedSite1.getGuid())
                    .assertThat().field("description").is(moderatedSite1.getDescription())
                    .assertThat().field("id").is(moderatedSite1.getId())
                    .assertThat().field("title").is(moderatedSite1.getTitle());
        }
        else
        {
            firstSite.assertThat().field("visibility").is(moderatedSite2.getVisibility())
                    .assertThat().field("guid").is(moderatedSite2.getGuid())
                    .assertThat().field("description").is(moderatedSite2.getDescription())
                    .assertThat().field("id").is(moderatedSite2.getId())
                    .assertThat().field("title").is(moderatedSite2.getTitle());
        }
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE },
            executionType = ExecutionType.REGRESSION,
            description = "Verify that for invalid maxItems parameter status code returned is 400.")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void checkInvalidMaxItemsStatusCode() throws Exception
    {
        restClient.authenticateUser(adminUser).withParams("maxItems=AB")
                .withCoreAPI().usingUser(newMember).getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.INVALID_MAXITEMS, "AB"));

        restClient.authenticateUser(adminUser).withParams("maxItems=0")
                .withCoreAPI().usingUser(newMember).getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE },
            executionType = ExecutionType.REGRESSION,
            description = "Verify that for invalid skipCount parameter status code returned is 400.")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void checkInvalidSkipCountStatusCode() throws Exception
    {
        restClient.authenticateUser(adminUser).withParams("skipCount=AB")
                .withCoreAPI().usingUser(newMember).getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.INVALID_SKIPCOUNT, "AB"));

        restClient.authenticateUser(adminUser).withParams("skipCount=-1")
                .withCoreAPI().usingUser(newMember).getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(RestErrorModel.NEGATIVE_VALUES_SKIPCOUNT);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE },
            executionType = ExecutionType.REGRESSION,
            description = "Verify that if personId does not exist status code returned is 404.")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void ifPersonIdDoesNotExist() throws Exception
    {
        UserModel nonexistentUser = dataUser.createRandomTestUser();
        nonexistentUser.setUsername("nonexistent");

        restClient.authenticateUser(adminUser).withCoreAPI().usingUser(nonexistentUser).getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "nonexistent"))
                .statusCodeIs(HttpStatus.NOT_FOUND)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE)
                .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE },
            executionType = ExecutionType.REGRESSION,
            description = "Specify -me- string in place of <personid> for request.")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void replacePersonIdWithMeRequest() throws Exception
    {
        UserModel meUser = dataUser.createRandomTestUser();
        restClient.authenticateUser(meUser).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite1);
        siteMembershipRequests = restClient.authenticateUser(meUser).withCoreAPI().usingMe().getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembershipRequests.assertThat().entriesListContains("id", moderatedSite1.getId());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE },
            executionType = ExecutionType.REGRESSION,
            description = "Verify that if empty personId is used status code returned is 404.")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void useEmptyPersonId() throws Exception
    {
        UserModel emptyNameMember = dataUser.createRandomTestUser();
        emptyNameMember.setUsername(" ");

        restClient.authenticateUser(adminUser).withCoreAPI().usingUser(emptyNameMember).getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, emptyNameMember.getUsername()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE },
            executionType = ExecutionType.REGRESSION,
            description = "Get site membership requests to a public site.")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void getRequestsToPublicSite() throws Exception
    {
        SiteModel publicSite = dataSite.usingAdmin().createPublicRandomSite();
        restClient.authenticateUser(newMember).withCoreAPI().usingAuthUser().addSiteMembershipRequest(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        siteMembershipRequests = restClient.withCoreAPI().usingAuthUser().getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembershipRequests.assertThat().entriesListDoesNotContain("id", publicSite.getId());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE },
            executionType = ExecutionType.REGRESSION,
            description = "Approve request then get requests.")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    public void approveRequestThenGetRequests() throws Exception
    {
        UserModel siteManager = dataUser.createRandomTestUser();

        SiteModel moderatedSite = dataSite.usingUser(siteManager).createModeratedRandomSite();
        restClient.authenticateUser(newMember).withCoreAPI().usingMe().addSiteMembershipRequest(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        RestTaskModel taskModel = restClient.authenticateUser(newMember).withWorkflowAPI().getTasks().getTaskModelByDescription(moderatedSite);
        workflow.approveSiteMembershipRequest(siteManager.getUsername(), siteManager.getPassword(), taskModel.getId(), true, "Approve");

        siteMembershipRequests = restClient.authenticateUser(newMember).withCoreAPI().usingMe().getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembershipRequests.assertThat().entriesListDoesNotContain("id", moderatedSite.getId());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE },
            executionType = ExecutionType.REGRESSION,
            description = "Verify admin user gets all its own site membership requests with Rest API and response is successful (200)")
    public void adminGetsItsOwnSiteMembershipRequestsWithSuccess() throws Exception
    {
        siteMembershipRequests = restClient.authenticateUser(adminUser).withCoreAPI().usingAuthUser().getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembershipRequests.assertThat().entriesListContains("id", moderatedSite1.getId())
                .assertThat().entriesListContains("id", moderatedSite2.getId());
    }

    @Bug(id = "MNT-16557")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE },
            executionType = ExecutionType.REGRESSION,
            description = "Verify regular user fails to get all site membership requests of another user with Rest API (403)")
    public void regularUserFailsToGetSiteMembershipRequestsOfAnotherUser() throws Exception
    {
        restClient.authenticateUser(regularUser).withCoreAPI()
                .usingUser(newMember).getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
                .assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Bug(id = "MNT-16557")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE },
            executionType = ExecutionType.REGRESSION,
            description = "Verify regular user fails to get all site membership requests of admin with Rest API (403)")
    public void regularUserFailsToGetSiteMembershipRequestsOfAdmin() throws Exception
    {
        restClient.authenticateUser(regularUser).withCoreAPI()
                .usingUser(adminUser).getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
                .assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE },
            executionType = ExecutionType.REGRESSION,
            description = "Verify regular user is able to get his site membership requests with, but skip the first one")
    public void getSiteMembershipRequestsButSkipTheFirstOne() throws Exception
    {
        siteMembershipRequests = restClient.authenticateUser(newMember).withParams("orderBy=id ASC").withCoreAPI()
                .usingMe().getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembershipRequests.assertThat().entriesListContains("id", moderatedSite1.getId())
                .assertThat().entriesListContains("id", moderatedSite2.getId());

        RestSiteMembershipRequestModel firstSiteMembershipRequest = siteMembershipRequests.getEntries().get(0).onModel();
        RestSiteMembershipRequestModel secondSiteMembershipRequest = siteMembershipRequests.getEntries().get(1).onModel();

        siteMembershipRequests = restClient.authenticateUser(newMember).withParams("orderBy=id ASC&skipCount=1").withCoreAPI()
                .usingMe().getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembershipRequests.assertThat().entriesListDoesNotContain("id", firstSiteMembershipRequest.getId())
                .assertThat().entriesListContains("id", secondSiteMembershipRequest.getId())
                .assertThat().entriesListContains("id", secondSiteMembershipRequest.getSite().getId());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE },
            executionType = ExecutionType.REGRESSION,
            description = "Verify regular user is able to get his site membership requests with higher skipCount than number of requests")
    public void getSiteMembershipRequestsWithHighSkipCount() throws Exception
    {
        siteMembershipRequests = restClient.authenticateUser(newMember).withParams("skipCount=3").withCoreAPI()
                .usingMe().getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembershipRequests.assertThat().entriesListIsEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE },
            executionType = ExecutionType.REGRESSION,
            description = "Verify get site membership requests returns an empty list where there are no requests")
    public void getSiteMembershipRequestsWhereThereAreNoRequests() throws Exception
    {
        siteMembershipRequests = restClient.authenticateUser(regularUser).withCoreAPI().usingMe().getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembershipRequests.assertThat().entriesListIsEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE },
            executionType = ExecutionType.REGRESSION,
            description = "Reject request then get site membership requests.")
    public void rejectRequestThenGetSiteMembershipRequests() throws Exception
    {
        UserModel userWithRejectedRequests = dataUser.createRandomTestUser();

        restClient.authenticateUser(userWithRejectedRequests).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite1);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        RestTaskModel taskModel = restClient.authenticateUser(userWithRejectedRequests).withWorkflowAPI().getTasks().getTaskModelByDescription(moderatedSite1);
        workflow.approveSiteMembershipRequest(siteManager.getUsername(), siteManager.getPassword(), taskModel.getId(), false, "Rejected");

        siteMembershipRequests = restClient.authenticateUser(userWithRejectedRequests).withCoreAPI().usingMe().getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembershipRequests.assertThat().entriesListDoesNotContain("id", moderatedSite1.getId())
                .assertThat().entriesListContains("id", moderatedSite2.getId());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE },
            executionType = ExecutionType.REGRESSION,
            description = "Check get site membership requests when properties parameter is used")
    public void getSiteMembershipRequestsWithProperties() throws Exception
    {
        siteMembershipRequests = restClient.authenticateUser(newMember).withParams("properties=id")
                .withCoreAPI().usingAuthUser().getSiteMembershipRequests();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembershipRequests.assertThat().entriesListContains("id", moderatedSite1.getId())
                .assertThat().entriesListContains("id", moderatedSite2.getId());
        RestSiteMembershipRequestModel siteMembershipRequest = siteMembershipRequests.getOneRandomEntry().onModel();
        siteMembershipRequest.assertThat().fieldsCount().is(1);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.SITES }, executionType = ExecutionType.SANITY, description = "Sanity test for endpoints: GET /site-membership-requests, POST /sites/{siteId}/site-membership-requests/{inviteeId}/approve, POST /sites/{siteId}/site-membership-requests/{inviteeId}/reject")
    public void approveRejectSiteMembership() throws Exception
    {
        STEP("1. Make membership requests for 2 users on moderatedSite.");
        restClient.authenticateUser(testUser).withCoreAPI().usingMe().addSiteMembershipRequest(moderatedSite1);
        restClient.authenticateUser(testUser1).withCoreAPI().usingMe().addSiteMembershipRequest(moderatedSite1);

        STEP("2. Get site memberships and filter using username and siteId ");
        RestSitePersonMembershipRequestModelsCollection membership = restClient.authenticateUser(siteManager)
                .withParams("where=(personId='" + testUser.getUsername() + "' AND siteId='" + moderatedSite1.getId() + "')").withCoreAPI()
                .usingSite(moderatedSite1).getSiteMemberships();

        restClient.assertStatusCodeIs(HttpStatus.OK);
        membership.getEntryByIndex(0).getSite().assertThat().field("id").is(moderatedSite1.getId());
        membership.getEntryByIndex(0).getPersonModel().assertThat().field("id").is(testUser.getUsername());

        STEP("3. Approve site membership for one of the users. Check that the user is now a member of the site");
        restClient.authenticateUser(siteManager).withCoreAPI().usingSite(moderatedSite1).approveSiteMembership(testUser);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restClient.withCoreAPI().usingSite(moderatedSite1).getSiteMember(testUser).assertThat().field("id").is(testUser.getUsername()).and().field("role")
                .is(testUser.getUserRole());

        STEP("4. Reject site membership for the second user. Check that the user is not a member of the site");
        restClient.authenticateUser(siteManager).withCoreAPI().usingSite(moderatedSite1).rejectSiteMembership(testUser1);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        RestSiteMemberModelsCollection memberList = restClient.withCoreAPI().usingSite(moderatedSite1).getSiteMembers();
        memberList.assertThat().entriesListDoesNotContain("id", testUser1.getUsername());
    }
}
