package org.alfresco.rest.sites.members;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestSiteMemberModel;
import org.alfresco.rest.model.RestSiteMemberModelsCollection;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
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
public class GetSiteMembersTests extends RestTest
{
    private SiteModel publicSite, privateSite, moderatedSite, moderatedSite2, moderatedSite3;
    private RestSiteMemberModelsCollection siteMembers;
    private ListUserWithRoles usersWithRoles, moderatedSiteUsers;
    private UserModel regularUser, privateSiteConsumer, siteCreator;
    private RestSiteMemberModel firstSiteMember, secondSiteMember, thirdSiteMember, fourthSiteMember;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        siteCreator = dataUser.createRandomTestUser();
        regularUser = dataUser.createRandomTestUser();
        privateSiteConsumer = dataUser.createRandomTestUser();
        
        publicSite = dataSite.usingUser(siteCreator).createPublicRandomSite();
        privateSite = dataSite.usingUser(siteCreator).createPrivateRandomSite();
        moderatedSite = dataSite.usingUser(siteCreator).createModeratedRandomSite();
        moderatedSite2 = dataSite.usingUser(siteCreator).createModeratedRandomSite();
        moderatedSite3 = dataSite.usingUser(siteCreator).createModeratedRandomSite();

        dataUser.addUserToSite(privateSiteConsumer, privateSite, UserRole.SiteConsumer);
        dataUser.addUserToSite(privateSiteConsumer, moderatedSite3, UserRole.SiteConsumer);
        usersWithRoles = dataUser.addUsersWithRolesToSite(publicSite,UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer, UserRole.SiteContributor);
        moderatedSiteUsers = dataUser.addUsersWithRolesToSite(moderatedSite, UserRole.SiteCollaborator, UserRole.SiteConsumer, UserRole.SiteContributor);

        siteMembers = restClient.authenticateUser(siteCreator).withCoreAPI()
                .usingSite(moderatedSite).usingParams("properties=role,id").getSiteMembers();
        firstSiteMember = siteMembers.getEntries().get(0).onModel();
        secondSiteMember = siteMembers.getEntries().get(1).onModel();
        thirdSiteMember = siteMembers.getEntries().get(2).onModel();
        fourthSiteMember = siteMembers.getEntries().get(3).onModel();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY, 
            description = "Verify user with Manager role gets site members and gets status code OK (200)")
    public void getSiteMembersWithManagerRole() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                  .withCoreAPI().usingSite(publicSite).getSiteMembers().assertThat().entriesListIsNotEmpty()
                  .and().entriesListContains("id", usersWithRoles.getOneUserWithRole(UserRole.SiteManager).getUsername())
                  .and().entriesListContains("role", usersWithRoles.getOneUserWithRole(UserRole.SiteManager).getUserRole().toString());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with Collaborator role gets site members and gets status code OK (200)")
    public void getSiteMembersWithCollaboratorRole() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                  .withCoreAPI().usingSite(publicSite).getSiteMembers().assertThat().entriesListIsNotEmpty().assertThat()
                  .entriesListContains("id", usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator).getUsername()).and()
                  .entriesListContains("role", usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator).getUserRole().toString());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with Contributor role gets site members and gets status code OK (200)")
    public void getSiteMembersWithContributorRole() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
            .withCoreAPI().usingSite(publicSite).getSiteMembers().assertThat().entriesListIsNotEmpty()
            .and().entriesListContains("id", usersWithRoles.getOneUserWithRole(UserRole.SiteContributor).getUsername())
            .and().entriesListContains("role", usersWithRoles.getOneUserWithRole(UserRole.SiteContributor).getUserRole().toString());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with Consumer role gets site members and gets status code OK (200)")
    public void getSiteMembersWithConsumerRole() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
            .withCoreAPI().usingSite(publicSite).getSiteMembers().assertThat().entriesListIsNotEmpty()
            .and().entriesListContains("id", usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer).getUsername())
            .and().entriesListContains("role", usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer).getUserRole().toString());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user with admin usere gets site members and gets status code OK (200)")
    public void getSiteMembersWithAdminUser() throws Exception
    {
        restClient.authenticateUser(dataUser.getAdminUser())
                  .withCoreAPI().usingSite(publicSite).getSiteMembers().assertThat().entriesListIsNotEmpty()
                  .and().entriesListContains("id", siteCreator.getUsername())
                  .when().assertThat().entriesListContains("role", "SiteManager");
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY, 
            description = "Failed authentication get site members call returns status code 401")
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void unauthenticatedUserIsNotAuthorizedToRetrieveSiteMembers() throws Exception
    {
        UserModel userModel = dataUser.createRandomTestUser();
        userModel.setPassword("user wrong password");
        dataUser.addUserToSite(userModel, publicSite, UserRole.SiteManager);
        restClient.authenticateUser(userModel)
                  .withCoreAPI().usingSite(publicSite).getSiteMembers();
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify get site members call returns status code 404 if siteId does not exist")
    public void checkStatusCodeForNonExistentSiteId() throws Exception
    {
        restClient.authenticateUser(regularUser).withCoreAPI()
                .usingSite("NonExistentSiteId").getSiteMembers();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "NonExistentSiteId"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify get site members call returns status code 400 for invalid maxItems")
    public void checkStatusCodeForInvalidMaxItems() throws Exception
    {
        restClient.authenticateUser(regularUser).withParams("maxItems=0")
                .withCoreAPI().usingSite(publicSite).getSiteMembers();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                .containsErrorKey(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);

        restClient.withParams("maxItems=A")
                .withCoreAPI().usingSite(publicSite).getSiteMembers();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.INVALID_MAXITEMS, "A"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify get site members call returns status code 400 for invalid skipCount ")
    public void checkStatusCodeForInvalidSkipCount() throws Exception
    {
        restClient.authenticateUser(regularUser).withParams("skipCount=A")
                .withCoreAPI().usingSite(publicSite).getSiteMembers();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.INVALID_SKIPCOUNT, "A"));

        restClient.withParams("skipCount=-1")
                .withCoreAPI().usingSite(publicSite).getSiteMembers();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(RestErrorModel.NEGATIVE_VALUES_SKIPCOUNT);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if any user gets public site members and status code is 200")
    public void getPublicSiteMembers() throws Exception
    {
        siteMembers = restClient.authenticateUser(regularUser).withCoreAPI()
                .usingSite(publicSite).getSiteMembers();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembers.assertThat().entriesListContains("id", siteCreator.getUsername())
                .and().entriesListContains("id", usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer).getUsername())
                .and().entriesListContains("id", usersWithRoles.getOneUserWithRole(UserRole.SiteContributor).getUsername())
                .and().entriesListContains("id", usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator).getUsername())
                .and().entriesListContains("id", usersWithRoles.getOneUserWithRole(UserRole.SiteManager).getUsername())
                .and().paginationField("count").is("5");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if any user gets moderated site members and status code is 200")
    public void getModeratedSiteMembers() throws Exception
    {
        siteMembers = restClient.authenticateUser(regularUser).withCoreAPI()
                .usingSite(moderatedSite).getSiteMembers();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembers.assertThat().entriesListContains("id", siteCreator.getUsername())
                .and().entriesListContains("id", moderatedSiteUsers.getOneUserWithRole(UserRole.SiteCollaborator).getUsername())
                .and().entriesListContains("id", moderatedSiteUsers.getOneUserWithRole(UserRole.SiteContributor).getUsername())
                .and().entriesListContains("id", moderatedSiteUsers.getOneUserWithRole(UserRole.SiteConsumer).getUsername())
                .and().paginationField("count").is("4");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if user gets private site members if he is a member of that site and status code is 200")
    public void getPrivateSiteMembersByASiteMember() throws Exception
    {
        siteMembers = restClient.authenticateUser(privateSiteConsumer).withCoreAPI()
                .usingSite(privateSite).getSiteMembers();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembers.assertThat().entriesListContains("id", siteCreator.getUsername())
                .and().entriesListContains("id", privateSiteConsumer.getUsername())
                .and().paginationField("count").is("2");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if user doesn't get private site members if he is not a member of that site and status code is 404")
    public void getPrivateSiteMembersByNotASiteMember() throws Exception
    {
        restClient.authenticateUser(regularUser).withCoreAPI()
                .usingSite(privateSite).getSiteMembers();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, privateSite.getTitle()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if user gets moderated site members after the adding of a new member and status code is 200")
    public void getSiteMembersAfterAddingNewMember() throws Exception
    {
        siteMembers = restClient.authenticateUser(regularUser).withCoreAPI()
                .usingSite(moderatedSite2).getSiteMembers();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembers.assertThat().entriesListContains("id", siteCreator.getUsername())
                .and().paginationField("count").is("1");

        restClient.authenticateUser(siteCreator).withCoreAPI().usingSite(moderatedSite2).addPerson(privateSiteConsumer);

        siteMembers = restClient.authenticateUser(regularUser).withCoreAPI()
                .usingSite(moderatedSite2).getSiteMembers();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembers.assertThat().entriesListContains("id", siteCreator.getUsername())
                .and().entriesListContains("id", privateSiteConsumer.getUsername())
                .and().paginationField("count").is("2");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if any user gets moderated site members with properties parameter applied and status code is 200")
    public void getModeratedSiteMembersUsingPropertiesParameter() throws Exception
    {
        siteMembers = restClient.authenticateUser(siteCreator).withCoreAPI()
                .usingSite(moderatedSite).usingParams("properties=role,id").getSiteMembers();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembers.assertThat().entriesListCountIs(4)
                .and().entriesListDoesNotContain("person")
                .and().entriesListContains("role", UserRole.SiteManager.toString())
                .and().entriesListContains("id", siteCreator.getUsername())
                .and().entriesListContains("role", UserRole.SiteContributor.toString())
                .and().entriesListContains("id", moderatedSiteUsers.getOneUserWithRole(UserRole.SiteContributor).getUsername())
                .and().entriesListContains("role", UserRole.SiteCollaborator.toString())
                .and().entriesListContains("id", moderatedSiteUsers.getOneUserWithRole(UserRole.SiteCollaborator).getUsername())
                .and().entriesListContains("role", UserRole.SiteConsumer.toString())
                .and().entriesListContains("id", moderatedSiteUsers.getOneUserWithRole(UserRole.SiteConsumer).getUsername());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if any user gets moderated site members with skipCount parameter applied")
    public void getModeratedSiteMembersUsingSkipCountParameter() throws Exception
    {
        siteMembers = restClient.authenticateUser(siteCreator).withCoreAPI()
                .usingSite(moderatedSite).usingParams("skipCount=2").getSiteMembers();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembers.assertThat().paginationField("count").is("2");
        siteMembers.assertThat().paginationField("skipCount").is("2");
        siteMembers.assertThat().entriesListDoesNotContain("id", firstSiteMember.getId())
                .and().entriesListDoesNotContain("id", secondSiteMember.getId())
                .and().entriesListContains("role", thirdSiteMember.getRole().toString())
                .and().entriesListContains("id", thirdSiteMember.getId())
                .and().entriesListContains("role", fourthSiteMember.getRole().toString())
                .and().entriesListContains("id", fourthSiteMember.getId());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if any user gets moderated site members with high skipCount parameter applied")
    public void getModeratedSiteMembersUsingHighSkipCountParameter() throws Exception
    {
        siteMembers = restClient.authenticateUser(siteCreator).withCoreAPI()
                .usingSite(moderatedSite).usingParams("skipCount=100").getSiteMembers();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembers.assertThat().paginationField("count").is("0");
        siteMembers.assertThat().paginationField("skipCount").is("100");
        siteMembers.assertThat().entriesListIsEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if any user gets moderated site members with maxItems parameter applied and check all pagination fields")
    public void getModeratedSiteMembersUsingMaxItemsParameter() throws Exception
    {
        siteMembers = restClient.authenticateUser(siteCreator).withCoreAPI()
                .usingSite(moderatedSite).usingParams("maxItems=1").getSiteMembers();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembers.assertThat().paginationField("count").is("1");
        siteMembers.assertThat().paginationField("hasMoreItems").is("true");
        siteMembers.assertThat().paginationField("maxItems").is("1");
        siteMembers.assertThat().paginationField("totalItems").isNotPresent();
        siteMembers.assertThat().entriesListContains("id", firstSiteMember.getId())
                .and().entriesListContains("role", firstSiteMember.getRole().toString())
                .and().entriesListDoesNotContain("id", secondSiteMember.getId())
                .and().entriesListDoesNotContain("id", thirdSiteMember.getId());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if user gets moderated site members with member not joined yet and status code is 200")
    public void getSiteMembersFromNotJoinedModeratedSite() throws Exception
    {
        UserModel userNotJoined = dataUser.createRandomTestUser();
        restClient.authenticateUser(userNotJoined).withCoreAPI().usingAuthUser().addSiteMembershipRequest(moderatedSite2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        siteMembers = restClient.authenticateUser(siteCreator).withCoreAPI()
                .usingSite(moderatedSite2).getSiteMembers();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembers.assertThat().entriesListContains("id", siteCreator.getUsername())
                .and().entriesListDoesNotContain("id", userNotJoined.getUsername());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if any user gets site members after the member was removed from site and status code is 200")
    public void getSiteMembersAfterRemovingASiteMember() throws Exception
    {
        siteMembers = restClient.authenticateUser(siteCreator).withCoreAPI()
                .usingSite(moderatedSite3).getSiteMembers();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembers.assertThat().entriesListCountIs(2);

        restClient.withCoreAPI().usingSite(moderatedSite3).deleteSiteMember(privateSiteConsumer);

        siteMembers = restClient.withCoreAPI().usingSite(moderatedSite3).getSiteMembers();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        siteMembers.assertThat().entriesListCountIs(1)
                .and().entriesListContains("role", UserRole.SiteManager.toString())
                .and().entriesListContains("id", siteCreator.getUsername());
    }
}
