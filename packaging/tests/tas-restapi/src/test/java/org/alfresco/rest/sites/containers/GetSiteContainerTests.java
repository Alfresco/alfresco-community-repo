package org.alfresco.rest.sites.containers;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestSiteContainerModel;
import org.alfresco.utility.constants.ContainerName;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author iulia.cojocea
 */
public class GetSiteContainerTests extends RestTest
{    
    private UserModel adminUserModel;
    private SiteModel publicSiteModel, moderatedSiteModel, privateSiteModel, adminPrivateSiteModel;
    private ListUserWithRoles usersWithRoles;
    private RestSiteContainerModel siteContainerModel;
    private UserModel testUser;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        adminUserModel = dataUser.getAdminUser();
        testUser = dataUser.createRandomTestUser();
        
        publicSiteModel = dataSite.usingAdmin().createPublicRandomSite();
        moderatedSiteModel = dataSite.usingUser(testUser).createModeratedRandomSite();
        privateSiteModel = dataSite.usingUser(testUser).createPrivateRandomSite();
        adminPrivateSiteModel = dataSite.usingAdmin().createPrivateRandomSite();
        
        usersWithRoles = dataUser.addUsersWithRolesToSite(publicSiteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);

        dataLink.usingAdmin().usingSite(publicSiteModel).createRandomLink();

        dataLink.usingAdmin().usingSite(adminPrivateSiteModel).createRandomLink();

        dataLink.usingUser(testUser).usingSite(privateSiteModel).createRandomLink();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY, 
            description = "Verify user with Manager role gets site container and gets status code OK (200)")
    public void getSiteContainerWithManagerRole() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        siteContainerModel = restClient.withCoreAPI().usingSite(publicSiteModel).getSiteContainers().getOneRandomEntry();
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteContainer(siteContainerModel.onModel())
                .assertThat().field("id").is(siteContainerModel.onModel().getId())
                .and().field("folderId").is(siteContainerModel.onModel().getFolderId());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user with Collaborator role gets site container and gets status code OK (200)")
    public void getSiteContainerWithCollaboratorRole() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));
        siteContainerModel = restClient.withCoreAPI().usingSite(publicSiteModel).getSiteContainers().getOneRandomEntry();
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteContainer(siteContainerModel.onModel())
               .and().field("id").is(siteContainerModel.onModel().getId())
               .and().field("folderId").is(siteContainerModel.onModel().getFolderId());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user with Contributor role gets site container and gets status code OK (200)")
    public void getSiteContainerWithContributorRole() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        siteContainerModel = restClient.withCoreAPI().usingSite(publicSiteModel).getSiteContainers().getOneRandomEntry();
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteContainer(siteContainerModel.onModel())
               .assertThat().field("id").is(siteContainerModel.onModel().getId())
               .and().field("folderId").is(siteContainerModel.onModel().getFolderId());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user with Consumer role gets site container and gets status code OK (200)")
    public void getSiteContainerWithConsumerRole() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer));
        siteContainerModel = restClient.withCoreAPI().usingSite(publicSiteModel).getSiteContainers().getOneRandomEntry();
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteContainer(siteContainerModel.onModel())
               .assertThat().field("id").is(siteContainerModel.onModel().getId())
               .and().field("folderId").is(siteContainerModel.onModel().getFolderId());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user with admin user gets site container and gets status code OK (200)")
    public void getSiteContainerWithAdminUser() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        siteContainerModel = restClient.withCoreAPI().usingSite(publicSiteModel).getSiteContainers().getOneRandomEntry();
        restClient.withCoreAPI().usingSite(publicSiteModel).getSiteContainer(siteContainerModel.onModel())
               .assertThat().field("id").is(siteContainerModel.onModel().getId())
               .and().field("folderId").is(siteContainerModel.onModel().getFolderId());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY, 
            description = "Failed authentication get site container call returns status code 401")
    public void unauthenticatedUserIsNotAuthorizedToRetrieveSiteContainer() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        siteContainerModel = restClient.withCoreAPI().usingSite(publicSiteModel).getSiteContainers().getOneRandomEntry();
        UserModel userModel = dataUser.createRandomTestUser();
        userModel.setPassword("user wrong password");
        dataUser.addUserToSite(userModel, publicSiteModel, UserRole.SiteManager);
        restClient.authenticateUser(userModel)
                  .withCoreAPI().usingSite(publicSiteModel).getSiteContainer(siteContainerModel.onModel());
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get container request returns status code 400 when site doesn't exist")
    public void getContainerWithNonExistentSite() throws Exception
    {
        restClient.authenticateUser(testUser)
                .withCoreAPI().usingSite("NonExistentSiteId").getSiteContainer(ContainerName.links.toString());
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, "NonExistentSiteId", ContainerName.links.toString()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get container request returns status code 400 when container item doesn't exist")
    public void getContainerWithNonExistentItem() throws Exception
    {
        restClient.authenticateUser(testUser)
                .withCoreAPI().usingSite(publicSiteModel).getSiteContainer("NonExistentFolder");
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, publicSiteModel.getId(), "NonExistentFolder"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify regular user can get container for public site and request returns status 200")
    public void getContainerForPublicSite() throws Exception
    {
        restClient.authenticateUser(testUser)
                .withCoreAPI().usingSite(publicSiteModel).getSiteContainer(ContainerName.links.toString())
                .assertThat().field("folderId").is(ContainerName.links.toString());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify manager is able to get container for his private site and request returns status 200")
    public void getContainerForPrivateSite() throws Exception
    {
        restClient.authenticateUser(testUser)
                .withCoreAPI().usingSite(privateSiteModel).getSiteContainer(ContainerName.links.toString())
                .assertThat().field("folderId").is(ContainerName.links.toString());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify get container request returns status 200 for moderated site")
    public void getContainerForModeratedSite() throws Exception
    {
        restClient.authenticateUser(testUser)
                .withCoreAPI().usingSite(moderatedSiteModel).getSiteContainer(ContainerName.documentLibrary.toString())
                .assertThat().field("folderId").is(ContainerName.documentLibrary.toString());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get container request with properties parameter returns status code 200 and parameter is applied")
    public void getContainerUsingPropertiesParameter() throws Exception
    {
        restClient.authenticateUser(testUser)
                .withCoreAPI().usingSite(publicSiteModel).usingParams("properties=id").getSiteContainer(ContainerName.links.toString())
                .assertThat().fieldsCount().is(1)
                .and().field("id").isNotEmpty()
                .and().field("folderId").isNull();
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get container request for a container that does not belong to site returns status code 404")
    public void getContainerThatDoesNotBelongToSite() throws Exception
    {
        restClient.authenticateUser(testUser)
                .withCoreAPI().usingSite(moderatedSiteModel).getSiteContainer(ContainerName.links.toString());
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsErrorKey(RestErrorModel.RELATIONSHIP_NOT_FOUND_ERRORKEY)
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, moderatedSiteModel.getId(), ContainerName.links.toString()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get container request for empty siteId returns status code 404")
    public void getContainerForEmptySiteId() throws Exception
    {
        restClient.authenticateUser(testUser)
                .withCoreAPI().usingSite("").getSiteContainer(ContainerName.links.toString());
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsErrorKey(RestErrorModel.RELATIONSHIP_NOT_FOUND_ERRORKEY)
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, "", ContainerName.links.toString()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get container request for empty container returns status code 200 and the list of containers")
    public void getContainerForEmptyContainer() throws Exception
    {
        restClient.authenticateUser(testUser)
                .withCoreAPI().usingSite(moderatedSiteModel).getSiteContainer("");
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get container with name containing special chars returns status code 404")
    public void getContainerWithNameContainingSpecialChars() throws Exception
    {
        String containerSpecialName = RandomStringUtils.randomAlphabetic(2) + "~!%40%23%24%25%5E%26*()_%2B%5B%5D%7B%7D%7C%5C%3B%27%3A%22%2C.%2F%3C%3E";

        restClient.authenticateUser(testUser)
                .withCoreAPI().usingSite(publicSiteModel).getSiteContainer(containerSpecialName);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsErrorKey(RestErrorModel.RELATIONSHIP_NOT_FOUND_ERRORKEY)
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, publicSiteModel.getId(), containerSpecialName))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if admin can get container request for a private site created by another user and status code is 200")
    public void adminCanGetContainerForPrivateSiteCreatedByUser() throws Exception
    {
        restClient.authenticateUser(adminUserModel)
                .withCoreAPI().usingSite(privateSiteModel).getSiteContainer(ContainerName.links.toString())
                .assertThat().field("folderId").is(ContainerName.links.toString())
                .and().field("id").isNotEmpty();
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if user cannot get container request for a private site created by admin and status code is 404")
    public void userCannotGetContainerForPrivateSiteCreatedByAdmin() throws Exception
    {
        restClient.authenticateUser(testUser)
                .withCoreAPI().usingSite(adminPrivateSiteModel).getSiteContainer(ContainerName.links.toString());
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsErrorKey(RestErrorModel.RELATIONSHIP_NOT_FOUND_ERRORKEY)
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, adminPrivateSiteModel.getId(), ContainerName.links.toString()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if get container request for a deleted container returns status code is 404")
    public void getContainerThatWasDeleted() throws Exception
    {
        restClient.authenticateUser(testUser)
                .withCoreAPI().usingSite(privateSiteModel).getSiteContainer(ContainerName.links.toString())
                .assertThat().field("folderId").is(ContainerName.links.toString());
        restClient.assertStatusCodeIs(HttpStatus.OK);

        //use dataprep for delete 'links' container
        FolderModel folder= new FolderModel("links");
        folder.setCmisLocation(String.format("/Sites/%s/%s", privateSiteModel.getId(), ContainerName.links.toString()));
        dataContent.deleteTree(folder);

        restClient.authenticateUser(testUser)
                .withCoreAPI().usingSite(privateSiteModel).getSiteContainer(ContainerName.links.toString());
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsErrorKey(RestErrorModel.RELATIONSHIP_NOT_FOUND_ERRORKEY)
                .containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND, privateSiteModel.getId(), ContainerName.links.toString()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }
}
