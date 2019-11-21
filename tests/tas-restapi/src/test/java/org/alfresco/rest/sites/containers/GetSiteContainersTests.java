package org.alfresco.rest.sites.containers;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestSiteContainerModelsCollection;
import org.alfresco.utility.constants.ContainerName;
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
public class GetSiteContainersTests extends RestTest
{
    private UserModel adminUserModel;
    private SiteModel publicSiteModel, publicSiteWithContainers;
    private SiteModel moderatedSiteModel, privateSiteModel;
    private ListUserWithRoles publicSiteUsers;
    private ListUserWithRoles publicSiteWithContainersUsers;
    private RestSiteContainerModelsCollection restSiteContainers;
    private int totalItems;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        adminUserModel = dataUser.getAdminUser();        
        publicSiteModel = dataSite.usingAdmin().createPublicRandomSite();
        publicSiteWithContainers = dataSite.usingAdmin().createPublicRandomSite();
        moderatedSiteModel = dataSite.usingAdmin().createModeratedRandomSite();
        privateSiteModel = dataSite.usingAdmin().createPrivateRandomSite();

        publicSiteUsers = dataUser.addUsersWithRolesToSite(publicSiteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer, UserRole.SiteContributor);

        publicSiteWithContainersUsers = dataUser
                .addUsersWithRolesToSite(publicSiteWithContainers, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                        UserRole.SiteContributor);

        dataLink.usingAdmin().usingSite(publicSiteWithContainers).createRandomLink();

        dataLink.usingAdmin().usingSite(moderatedSiteModel).createRandomLink();

        dataLink.usingAdmin().usingSite(privateSiteModel).createRandomLink();

        restSiteContainers = restClient.authenticateUser(publicSiteWithContainersUsers.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingSite(publicSiteWithContainers).getSiteContainers();
        totalItems = restSiteContainers.getPagination().getTotalItems();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY, 
            description = "Verify user with Manager role gets site containers and gets status code OK (200)")
    public void getSiteContainersWithManagerRole() throws Exception
    {
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteContainers()
        	.assertThat().entriesListIsNotEmpty()
        	.assertThat().paginationExist()
        	.and().paginationField("count").isNot("0");
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with Collaborator role gets site containers and gets status code OK (200)")
    public void getSiteContainersWithCollaboratorRole() throws Exception
    {
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteCollaborator));
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteContainers()
                	.assertThat().entriesListIsNotEmpty()
                	.assertThat().paginationExist()
                	.and().paginationField("count").isNot("0");
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with Contributor role gets site containers and gets status code OK (200)")
    public void getSiteContainersWithContributorRole() throws Exception
    {
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteContributor));
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteContainers()
                	.assertThat().entriesListIsNotEmpty()
                	.assertThat().paginationExist()
                	.and().paginationField("count").isNot("0");
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with Consumer role gets site containers and gets status code OK (200)")
    public void getSiteContainersWithConsumerRole() throws Exception
    {
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteConsumer));
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteContainers()
                	.assertThat().entriesListIsNotEmpty()
                	.assertThat().paginationExist()
                	.and().paginationField("count").isNot("0");
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with Admin user gets site containers information and gets status code OK (200)")
    public void getSiteContainersWithAdminUser() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteContainers()
                	.assertThat().entriesListIsNotEmpty()
                	.assertThat().paginationExist()
                	.and().paginationField("count").isNot("0");
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY, 
            description = "Failed authentication get site containers call returns status code 401 with Manager role")
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void unauthenticatedUserIsNotAuthorizedToRetrieveSiteContainers() throws Exception
    {
        UserModel userModel = dataUser.createRandomTestUser();
        userModel.setPassword("user wrong password");
        dataUser.addUserToSite(userModel, publicSiteModel, UserRole.SiteManager);
        restClient.authenticateUser(userModel)
                  .withCoreAPI().usingSite(publicSiteModel).getSiteContainers();
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get site container request returns status code 200 with valid maxItems parameter")
    public void getContainersWithValidMaxItems() throws Exception
    {
        restClient.authenticateUser(publicSiteWithContainersUsers.getOneUserWithRole(UserRole.SiteManager))
                .withParams("maxItems=5")
                .withCoreAPI().usingSite(publicSiteWithContainers).getSiteContainers()
                .assertThat().entriesListCountIs(2)
                .and().entriesListContains("folderId" , ContainerName.documentLibrary.toString())
                .and().entriesListContains("folderId", ContainerName.links.toString());
        restClient.assertStatusCodeIs(HttpStatus.OK);

        restClient.withParams("maxItems=1")
                .withCoreAPI().usingSite(publicSiteWithContainers).getSiteContainers()
                .assertThat().entriesListCountIs(1);

        restClient.withParams("maxItems=3")
                .withCoreAPI().usingSite(publicSiteModel).getSiteContainers()
                .assertThat().entriesListCountIs(1)
                .and().entriesListContains("folderId", ContainerName.documentLibrary.toString());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get site container request returns status code 400 when invalid maxItems parameter is used")
    public void getContainersWithMaxItemsZero () throws Exception
    {
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteManager))
                .withParams("maxItems=0")
                .withCoreAPI().usingSite(publicSiteModel).getSiteContainers();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary("Only positive values supported for maxItems");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get site container request returns status code 400 when invalid maxItems parameter is used")
    public void getContainersWithMaxItemsCharacter () throws Exception
    {
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteCollaborator))
                .withParams("maxItems=test")
                .withCoreAPI().usingSite(publicSiteModel).getSiteContainers();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.INVALID_MAXITEMS, "test"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get site container request returns status code 200 when maxItems parameter starts with multiple zero")
    public void getContainersWithMaxItemsMultipleZero () throws Exception
    {
        restClient.authenticateUser(publicSiteWithContainersUsers.getOneUserWithRole(UserRole.SiteCollaborator))
                .withParams("maxItems=000007")
                .withCoreAPI().usingSite(publicSiteWithContainers).getSiteContainers()
                .assertThat().entriesListCountIs(2);
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get site containers request returns status code 200 when valid skipCount parameter is used")
    public void getSiteContainersWithValidSkipCount() throws Exception
    {
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteManager)).withParams("skipCount=1")
                .withCoreAPI().usingSite(publicSiteModel).getSiteContainers()
                .assertThat().entriesListCountIs(0);
        restClient.assertStatusCodeIs(HttpStatus.OK);

        restClient.authenticateUser(publicSiteWithContainersUsers.getOneUserWithRole(UserRole.SiteCollaborator)).withParams("skipCount=1")
                .withCoreAPI().usingSite(publicSiteWithContainers).getSiteContainers()
                .assertThat().entriesListCountIs(1);
        restClient.assertStatusCodeIs(HttpStatus.OK);

        restClient.authenticateUser(publicSiteWithContainersUsers.getOneUserWithRole(UserRole.SiteConsumer)).withParams("skipCount=2")
                .withCoreAPI().usingSite(publicSiteWithContainers).getSiteContainers()
                .assertThat().entriesListCountIs(0);
        restClient.assertStatusCodeIs(HttpStatus.OK);

        restClient.authenticateUser(publicSiteWithContainersUsers.getOneUserWithRole(UserRole.SiteCollaborator)).withParams("skipCount=0")
                .withCoreAPI().usingSite(publicSiteWithContainers).getSiteContainers()
                .assertThat().entriesListCountIs(2);
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get site containers request returns status code 400 when invalid skipCount parameter is used")
    public void getSiteContainersWithSkipCountCharacter() throws Exception
    {
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteCollaborator)).withParams("skipCount=abc")
                .withCoreAPI().usingSite(publicSiteModel).getSiteContainers();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.INVALID_SKIPCOUNT, "abc"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get site containers request returns status code 200 when skipCount parameter starts with multiple zero")
    public void getSiteContainersWithSkipCountMultipleZero() throws Exception
    {
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteCollaborator)).withParams("skipCount=00002")
                .withCoreAPI().usingSite(publicSiteWithContainers).getSiteContainers()
                .assertThat().entriesListCountIs(0);
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get site containers request returns status code 400 when site doesn't exist")
    public void getSiteContainersWithNonExistentSite() throws Exception
    {
        restClient.authenticateUser(publicSiteUsers.getOneUserWithRole(UserRole.SiteCollaborator))
                .withCoreAPI().usingSite("NonExistentSiteId").getSiteContainers();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "NonExistentSiteId"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify get site containers request returns status 200 for private site")
    public void getSiteContainersForPrivateSite() throws Exception
    {
        restClient.authenticateUser(adminUserModel)
                .withCoreAPI().usingSite(privateSiteModel).getSiteContainers()
                .assertThat().entriesListCountIs(2);
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify get site containers request returns status 200 for moderated site")
    public void getSiteContainersForModeratedSite() throws Exception
    {
        restClient.authenticateUser(adminUserModel)
                .withCoreAPI().usingSite(moderatedSiteModel).getSiteContainers()
                .assertThat().entriesListCountIs(2);
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify get site containers request returns status 200 for several containers")
    public void getSiteContainersForSeveralItems() throws Exception
    {
        restClient.authenticateUser(adminUserModel)
                .withCoreAPI().usingSite(publicSiteWithContainers).getSiteContainers()
                .assertThat().entriesListCountIs(2)
                .and().entriesListContains("folderId" ,ContainerName.documentLibrary.toString())
                .and().entriesListContains("folderId", ContainerName.links.toString());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify get site containers request returns status 200 for one container")
    public void getSiteContainersWithOneItem() throws Exception
    {
        restClient.authenticateUser(adminUserModel)
                .withCoreAPI().usingSite(publicSiteModel).getSiteContainers()
                .assertThat().entriesListCountIs(1)
                .and().entriesListContains("folderId" ,ContainerName.documentLibrary.toString());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get site container request with properties parameter applied returns status code 200")
    public void getSiteContainersUsingPropertiesParameter() throws Exception
    {
        restClient.authenticateUser(publicSiteWithContainersUsers.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingSite(publicSiteWithContainers).usingParams("properties=folderId").getSiteContainers()
                .assertThat().entriesListCountIs(2)
                .and().entriesListContains("folderId", ContainerName.documentLibrary.toString())
                .and().entriesListContains("folderId", ContainerName.links.toString())
                .and().entriesListDoesNotContain("id");
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get site container request for a container that is not empty returns status code 200")
    public void getSiteContainersThatIsNotEmpty() throws Exception
    {
        restClient.authenticateUser(publicSiteWithContainersUsers.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingSite(publicSiteWithContainers).getSiteContainers()
                .assertThat().entriesListCountIs(2)
                .and().entriesListContains("folderId", ContainerName.documentLibrary.toString());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get site containers request returns status code 200 when first container is skipped")
    public void getSiteContainersAndSkipFirst() throws Exception
    {
        restClient.authenticateUser(publicSiteWithContainersUsers.getOneUserWithRole(UserRole.SiteCollaborator))
                .withParams("skipCount=1").withCoreAPI().usingSite(publicSiteWithContainers).getSiteContainers()
                .assertThat().entriesListCountIs(1)
                .and().entriesListDoesNotContain("folderId", restSiteContainers.getEntries().get(0).onModel().getFolderId())
                .and().entriesListContains("folderId", restSiteContainers.getEntries().get(1).onModel().getFolderId());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get site containers request and get last container returns status code 200")
    public void getLastSiteContainer() throws Exception
    {
        restClient.authenticateUser(publicSiteWithContainersUsers.getOneUserWithRole(UserRole.SiteCollaborator))
                .withParams("skipCount=" + String.valueOf(totalItems-1)).withCoreAPI().usingSite(publicSiteWithContainers).getSiteContainers()
                .assertThat().entriesListCountIs(1)
                .and().entriesListDoesNotContain("folderId", restSiteContainers.getEntries().get(0).onModel().getFolderId())
                .and().entriesListContains("folderId", restSiteContainers.getEntries().get(1).onModel().getFolderId());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get site containers request returns status code 200 when high skipCount parameter is used")
    public void getSiteContainersWithHighSkipCount() throws Exception
    {
        restClient.authenticateUser(publicSiteWithContainersUsers.getOneUserWithRole(UserRole.SiteCollaborator))
                .withParams("skipCount=999999").withCoreAPI().usingSite(publicSiteWithContainers).getSiteContainers()
                .assertThat().entriesListIsEmpty()
                .and().entriesListCountIs(0)
                .and().paginationField("skipCount").is("999999");
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get site container request for an empty siteId returns status code 404")
    public void getSiteContainersForEmptySiteId() throws Exception
    {
        restClient.authenticateUser(publicSiteWithContainersUsers.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingSite("").getSiteContainers();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, ""))
                .stackTraceIs(RestErrorModel.STACKTRACE)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER);
    }
}
