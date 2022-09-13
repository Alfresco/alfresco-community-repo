package org.alfresco.rest.sites;

import org.alfresco.dataprep.SiteService;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.*;
import org.alfresco.utility.constants.ContainerName;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author iulia.cojocea
 */
public class GetSitesTests extends RestTest
{
    private UserModel adminUser;
    private UserModel lastPrivateSiteManager;
    private ListUserWithRoles usersWithRoles;
    private SiteModel siteModel;
    private UserModel regularUser, privateSiteManager, privateSiteConsumer;
    private SiteModel firstPublicSite, firstPrivateSite, firstModeratedSite, deletedSite;
    private SiteModel lastPublicSite, lastPrivateSite, lastModeratedSite;
    private RestSiteModelsCollection sites;
    private String name;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        regularUser = dataUser.createRandomTestUser();
        lastPrivateSiteManager = dataUser.createRandomTestUser();
        privateSiteManager = dataUser.createRandomTestUser();
        privateSiteConsumer = dataUser.createRandomTestUser();

        siteModel = new SiteModel(RandomData.getRandomName("0-PublicSite"));
        firstPublicSite = dataSite.usingAdmin().createSite(siteModel);
        siteModel = new SiteModel(RandomData.getRandomName("0-PrivateSite"), SiteService.Visibility.PRIVATE);
        firstPrivateSite = dataSite.usingAdmin().createSite(siteModel);
        siteModel = new SiteModel(RandomData.getRandomName("0-ModeratedSite"), SiteService.Visibility.MODERATED);
        firstModeratedSite = dataSite.usingAdmin().createSite(siteModel);
        dataUser.addUserToSite(privateSiteManager, firstPrivateSite, UserRole.SiteManager);
        dataUser.addUserToSite(privateSiteConsumer, firstPrivateSite, UserRole.SiteConsumer);
        deletedSite = dataSite.usingAdmin().createPublicRandomSite();

        name = RandomData.getRandomName("ZZZZZZZZZ-PublicSite");
        lastPublicSite = dataSite.usingAdmin().createSite(new SiteModel(SiteService.Visibility.PUBLIC, "guid", name, name, name));
        lastPrivateSite = dataSite.usingAdmin().createSite(new SiteModel(RandomData.getRandomName("ZZZZZZZZZ-PrivateSite"), SiteService.Visibility.PRIVATE));
        lastModeratedSite = dataSite.usingAdmin().createSite(new SiteModel(RandomData.getRandomName("ZZZZZZZZZ-ModeratedSite"), SiteService.Visibility.MODERATED));

        adminUser = dataUser.getAdminUser();
        siteModel = dataSite.usingAdmin().createPublicRandomSite();
        usersWithRoles = dataUser.addUsersWithRolesToSite(siteModel,UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer, UserRole.SiteContributor);
        dataUser.addUserToSite(lastPrivateSiteManager, lastPrivateSite, UserRole.SiteManager);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY, 
            description = "Verify user with Manager role gets sites information and gets status code OK (200)")
    public void managerIsAbleToRetrieveSites() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).withParams("maxItems=10000")
                  .withCoreAPI().getSites()             
                	.assertThat().entriesListIsNotEmpty()
                	.assertThat().entriesListContains("id", siteModel.getId())
                	.and().paginationExist();
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user with Collaborator role gets sites information and gets status code OK (200)")
    public void collaboratorIsAbleToRetrieveSites() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withParams("maxItems=10000")
                  .withCoreAPI().getSites().assertThat().entriesListIsNotEmpty()
                  .assertThat().entriesListContains("id", siteModel.getId())
                  .and().paginationExist();
                  
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user with Contributor role gets sites information and gets status code OK (200)")
    public void contributorIsAbleToRetrieveSites() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).withParams("maxItems=10000")
                  .withCoreAPI().getSites()	
                	.assertThat().entriesListIsNotEmpty()
                	.assertThat().entriesListContains("id", siteModel.getId())
                	.and().paginationExist();
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user with Consumer role gets sites information and gets status code OK (200)")
    public void consumerIsAbleToRetrieveSites() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).withParams("maxItems=10000")
                  .withCoreAPI().getSites()
                  .assertThat().entriesListIsNotEmpty()
              	  .assertThat().entriesListContains("id", siteModel.getId())
              	  .and().paginationExist();
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user with Admin user gets sites information and gets status code OK (200)")
    public void adminUserIsAbleToRetrieveSites() throws Exception
    {
        restClient.authenticateUser(adminUser).withParams("maxItems=10000")
                  .withCoreAPI().getSites()
                	.assertThat().entriesListIsNotEmpty()
                	.assertThat().entriesListContains("id", siteModel.getId())
                	.and().paginationExist();
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY, 
            description = "Failed authentication get sites call returns status code 401")
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void unauthenticatedUserIsNotAuthorizedToRetrieveSites() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        UserModel userModel = dataUser.createRandomTestUser();
        userModel.setPassword("user wrong password");
        dataUser.addUserToSite(userModel, siteModel, UserRole.SiteManager);
        restClient.authenticateUser(userModel).withParams("maxItems=1")
                  .withCoreAPI().getSites();
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get sites request returns status code 400 when invalid maxItems parameter is used")
    public void getSitesWithInvalidMaxItems() throws Exception
    {
        restClient.authenticateUser(regularUser).withParams("maxItems=0=09")
                .withCoreAPI().getSites();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.INVALID_MAXITEMS, "0=09"));

        restClient.withParams("maxItems=A")
                .withCoreAPI().getSites();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.INVALID_MAXITEMS, "A"));

        restClient.withParams("maxItems=0")
                .withCoreAPI().getSites();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get sites request returns status code 400 when invalid skipCount parameter is used")
    public void getSitesWithInvalidSkipCount() throws Exception
    {
        restClient.authenticateUser(regularUser).withParams("skipCount=A")
                .withCoreAPI().getSites();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.INVALID_SKIPCOUNT, "A"));

        restClient.authenticateUser(regularUser).withParams("skipCount=-1")
                .withCoreAPI().getSites();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(RestErrorModel.NEGATIVE_VALUES_SKIPCOUNT);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if User gets sites ordered by title ascending and status code is 200")
    public void getSitesOrderedByTitleASC() throws Exception
    {
        sites = restClient.authenticateUser(privateSiteManager).withParams("orderBy=title ASC")
                .withCoreAPI().getSites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sites.assertThat().entriesListIsNotEmpty();
        List<RestSiteModel> sitesList = sites.getEntries();
        sitesList.get(0).onModel().assertThat().field("title").is(firstModeratedSite.getTitle());
        sitesList.get(1).onModel().assertThat().field("title").is(firstPrivateSite.getTitle());
        sitesList.get(2).onModel().assertThat().field("title").is(firstPublicSite.getTitle());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if a regular user gets all public and moderated sites and status code is 200")
    public void regularUserGetsOnlyPublicAndModeratedSites() throws Exception
    {
        sites = restClient.authenticateUser(regularUser).withParams("maxItems=5000")
                .withCoreAPI().getSites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sites.assertThat().entriesListContains("title", firstPublicSite.getTitle())
                .and().entriesListContains("title", firstModeratedSite.getTitle())
                .and().entriesListDoesNotContain("title", firstPrivateSite.getTitle());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if a member of a private site gets the private site, all public sites and all moderated sites. Verify if status code is 200")
    public void privateSiteMemberGetsSitesVisibleForHim() throws Exception
    {
        sites = restClient.authenticateUser(privateSiteConsumer).withParams("maxItems=5000")
                .withCoreAPI().getSites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sites.assertThat().entriesListContains("title", firstPublicSite.getTitle())
                .and().entriesListContains("title", firstModeratedSite.getTitle())
                .and().entriesListContains("title", firstPrivateSite.getTitle());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if a site is not retrieved anymore after deletion and status code is 200")
    public void checkDeletedSiteIsNotRetrieved() throws Exception
    {
        sites = restClient.authenticateUser(regularUser).withParams("maxItems=5000").withCoreAPI().getSites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sites.assertThat().entriesListContains("title", deletedSite.getTitle());

        dataSite.usingAdmin().deleteSite(deletedSite);

        sites = restClient.withParams("maxItems=5000").withCoreAPI().getSites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sites.assertThat().entriesListDoesNotContain("title", deletedSite.getTitle());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION})
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if User gets sites ordered by title descending and status code is 200")
    public void getSitesOrderedByTitleDESC() throws Exception
    {
        int totalItems = restClient.authenticateUser(lastPrivateSiteManager).withCoreAPI().getSites().getPagination().getTotalItems();
        sites = restClient.authenticateUser(lastPrivateSiteManager).withParams(String.format("maxItems=%s&orderBy=title DESC", totalItems))
                .withCoreAPI().getSites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sites.assertThat().entriesListIsNotEmpty();
        List<RestSiteModel> sitesList = sites.getEntries();
        sitesList.get(0).onModel().assertThat().field("title").is(lastPublicSite.getTitle());
        sitesList.get(1).onModel().assertThat().field("title").is(lastPrivateSite.getTitle());
        sitesList.get(2).onModel().assertThat().field("title").is(lastModeratedSite.getTitle());
        sitesList.get(sitesList.size()-1).onModel().assertThat().field("title").is(firstModeratedSite.getTitle());
    }


    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION})
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if User gets sites ordered by id ascending and status code is 200")
    public void getSitesOrderedByIdASC() throws Exception
    {
        int totalItems = restClient.authenticateUser(lastPrivateSiteManager).withCoreAPI().getSites().getPagination().getTotalItems();
        sites = restClient.authenticateUser(lastPrivateSiteManager).withParams(String.format("maxItems=%s&orderBy=id ASC", totalItems))
                .withCoreAPI().getSites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sites.assertThat().entriesListIsNotEmpty();
        List<RestSiteModel> sitesList = sites.getEntries();
        sitesList.get(sitesList.size()-1).onModel().assertThat().field("id").is(lastPublicSite.getId());
        sitesList.get(sitesList.size()-2).onModel().assertThat().field("id").is(lastPrivateSite.getId());
        sitesList.get(sitesList.size()-3).onModel().assertThat().field("id").is(lastModeratedSite.getId());
        sitesList.get(0).onModel().assertThat().field("id").is(firstModeratedSite.getId());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION})
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify pagination")
    public void checkPagination() throws Exception
    {
        sites = restClient.authenticateUser(regularUser).withCoreAPI().getSites();
        sites.getPagination().assertThat()
                .field("totalItems").isNotEmpty().and()
                .field("maxItems").is("100").and()
                .field("hasMoreItems").is((sites.getPagination().getTotalItems() > sites.getPagination().getMaxItems()) ? "true" : "false").and()
                .field("skipCount").is("0").and()
                .field("count").is((sites.getPagination().isHasMoreItems()) ? sites.getPagination().getMaxItems() : sites.getPagination().getTotalItems());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION})
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify user can get only first two sites and status code is 200")
    public void getFirstTwoSites() throws Exception
    {
        sites = restClient.authenticateUser(regularUser).withParams(String.format("maxItems=%s&orderBy=title ASC", 2))
                .withCoreAPI().getSites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sites.getPagination().assertThat().field("maxItems").is("2").and().field("count").is("2");
        sites.assertThat().entriesListCountIs(2).and().entriesListDoesNotContain(firstPublicSite.getId());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION})
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify user can get sites using high skipCount parameter and status code is 200")
    public void getSitesUsingHighSkipCount() throws Exception
    {
        RestSiteModelsCollection allSites = restClient.authenticateUser(regularUser).withCoreAPI().getSites();
        sites = restClient.withParams("skipCount=100").withCoreAPI().getSites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sites.assertThat().paginationField("skipCount").is("100");
        if(allSites.getPagination().getTotalItems() > 100)
            sites.assertThat().entriesListIsNotEmpty();
        else
            sites.assertThat().entriesListIsEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION})
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify user can not get sites using zero maxItems parameter and status code is 400")
    public void userCanNotGetSitesUsingZeroMaxItems() throws Exception
    {
        sites = restClient.authenticateUser(regularUser).withParams("maxItems=0").withCoreAPI().getSites();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError()
                .containsErrorKey(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                .containsSummary(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION})
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify that getSites request applies valid properties param and status code is 200")
    public void getSitesRequestWithValidPropertiesParam() throws Exception
    {
        sites = restClient.authenticateUser(regularUser).withParams("properties=id").withCoreAPI().getSites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sites.assertThat().entriesListDoesNotContain("description")
                .assertThat().entriesListDoesNotContain("title")
                .assertThat().entriesListDoesNotContain("visibility")
                .assertThat().entriesListDoesNotContain("guid")
                .assertThat().entriesListContains("id");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION})
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify pagination when skipCount and MaxItems are used")
    public void checkPaginationWithSkipCountAndMaxItems() throws Exception
    {
        sites = restClient.authenticateUser(regularUser).withParams("skipCount=10&maxItems=110").withCoreAPI().getSites();

        int expectedCount;
        if (sites.getPagination().isHasMoreItems())
        {
            expectedCount = sites.getPagination().getMaxItems();
        }
        else
        {
            if (sites.getPagination().getTotalItems() < sites.getPagination().getSkipCount())
            {
                expectedCount = 0;
            }
            else
            {
                expectedCount = sites.getPagination().getTotalItems() - sites.getPagination().getSkipCount();
            }
        }

        sites.getPagination().assertThat()
                .field("totalItems").isNotEmpty().and()
                .field("maxItems").is("110").and()
                .field("hasMoreItems").is((sites.getPagination().getTotalItems() - sites.getPagination().getSkipCount() > sites.getPagination().getMaxItems())?"true":"false").and()
                .field("skipCount").is("10").and()
                .field("count").is(expectedCount);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION})
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if User gets sites ordered by id ascending and status code is 200")
    public void getSitesOrderedByTitleASCAndVisibilityASC() throws Exception
    {
        SiteModel secondPublicSite = dataSite.usingAdmin().createSite(
                new SiteModel(SiteService.Visibility.PUBLIC, "guid", name+"A", name+"A", name));
        SiteModel thirdPublicSite = dataSite.usingAdmin().createSite(
                new SiteModel(SiteService.Visibility.PUBLIC, "guid", name+"B", name+"B", name));

        int totalItems = restClient.authenticateUser(regularUser).withCoreAPI().getSites().getPagination().getTotalItems();
        sites = restClient.authenticateUser(regularUser).withParams(String.format("maxItems=%s&orderBy=title ASC&orderBy=description ASC", totalItems))
                .withCoreAPI().getSites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sites.assertThat().entriesListIsNotEmpty();
        List<RestSiteModel> sitesList = sites.getEntries();
        sitesList.get(sitesList.size()-1).onModel().assertThat().field("title").is(thirdPublicSite.getTitle());
        sitesList.get(sitesList.size()-2).onModel().assertThat().field("title").is(secondPublicSite.getTitle());

        dataSite.usingAdmin().deleteSite(secondPublicSite);
        dataSite.usingAdmin().deleteSite(thirdPublicSite);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION} )
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Check that relations parameter is applied for containers")
    public void checkThatRelationsParameterIsAppliedForContainers() throws Exception
    {
        List<List<Object>> jsonObjects = restClient.authenticateUser(adminUser)
                .withParams("relations=containers").withCoreAPI().usingSite(lastPublicSite).getSitesWithRelations();

        List<Object> siteObjects = jsonObjects.get(0);
        for (int i = 0; i < siteObjects.size(); i++)
        {
            RestSiteModel siteModel = (RestSiteModel) siteObjects.get(i);
            siteModel.assertThat().field("visibility").isNotEmpty()
                    .and().field("id").isNotEmpty()
                    .and().field("title").isNotEmpty()
                    .and().field("preset").is("site-dashboard")
                    .and().field("guid").isNotEmpty();
        }

        List<Object> containerObjects = jsonObjects.get(1);
        for (int i = 0; i < containerObjects.size(); i++)
        {
            RestSiteContainerModelsCollection containers = (RestSiteContainerModelsCollection) containerObjects.get(i);
            containers.assertThat().entriesListIsNotEmpty().and().entriesListContains("folderId", ContainerName.documentLibrary.toString());
        }
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION} )
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Check that relations parameter is applied for members")
    public void checkThatRelationsParameterIsAppliedForMembers() throws Exception
    {
        List<List<Object>> jsonObjects = restClient.authenticateUser(adminUser)
                .withParams("relations=members").withCoreAPI().usingSite(lastPublicSite).getSitesWithRelations();

        List<Object> siteObjects = jsonObjects.get(0);
        for (int i = 0; i < siteObjects.size(); i++)
        {
            RestSiteModel siteModel = (RestSiteModel) siteObjects.get(i);
            siteModel.assertThat().field("visibility").isNotEmpty()
                    .and().field("id").isNotEmpty()
                    .and().field("title").isNotEmpty()
                    .and().field("preset").is("site-dashboard")
                    .and().field("guid").isNotEmpty();
        }

        List<Object> memberObjects = jsonObjects.get(1);
        for (int i = 0; i < memberObjects.size(); i++)
        {
            RestSiteMemberModelsCollection siteMembers = (RestSiteMemberModelsCollection) memberObjects.get(i);
            siteMembers.assertThat().entriesListIsNotEmpty().assertThat().entriesListContains("id").assertThat().entriesListContains("role", UserRole.SiteManager.toString());
            siteMembers.getOneRandomEntry().onModel().assertThat().field("person.firstName").isNotEmpty().and().field("person.id").isNotEmpty();
        }
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION} )
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Check that relations parameter is applied for members and containers")
    public void checkThatRelationsParameterIsAppliedForMembersAndContainers() throws Exception
    {
        List<List<Object>> jsonObjects = restClient.authenticateUser(adminUser)
                .withParams("relations=containers,members").withCoreAPI().usingSite(lastPublicSite).getSitesWithRelations();

        List<Object> siteObjects = jsonObjects.get(0);
        for (int i = 0; i < siteObjects.size(); i++)
        {
            RestSiteModel siteModel = (RestSiteModel) siteObjects.get(i);
            siteModel.assertThat().field("visibility").isNotEmpty()
                    .and().field("id").isNotEmpty()
                    .and().field("title").isNotEmpty()
                    .and().field("preset").is("site-dashboard")
                    .and().field("guid").isNotEmpty();
        }

        List<Object> containerObjects = jsonObjects.get(1);
        for (int i = 0; i < containerObjects.size(); i++)
        {
            RestSiteContainerModelsCollection containers = (RestSiteContainerModelsCollection) containerObjects.get(i);
            containers.assertThat().entriesListIsNotEmpty().and().entriesListContains("folderId", ContainerName.documentLibrary.toString());
        }

        List<Object> memberObjects = jsonObjects.get(2);
        for (int i = 0; i < memberObjects.size(); i++)
        {
            RestSiteMemberModelsCollection siteMembers = (RestSiteMemberModelsCollection) memberObjects.get(i);
            siteMembers.assertThat().entriesListIsNotEmpty().assertThat().entriesListContains("id").assertThat().entriesListContains("role", UserRole.SiteManager.toString());
            siteMembers.getOneRandomEntry().onModel().assertThat().field("person.firstName").isNotEmpty().and().field("person.id").isNotEmpty();
        }
    }

    @AfterClass(alwaysRun=true)
    public void cleanup() throws Exception
    {
        dataSite.usingAdmin().deleteSite(firstModeratedSite);
        dataSite.usingAdmin().deleteSite(firstPrivateSite);
        dataSite.usingAdmin().deleteSite(firstPublicSite);

        dataSite.usingAdmin().deleteSite(lastPublicSite);
        dataSite.usingAdmin().deleteSite(lastPrivateSite);
        dataSite.usingAdmin().deleteSite(lastModeratedSite);
    }
}
