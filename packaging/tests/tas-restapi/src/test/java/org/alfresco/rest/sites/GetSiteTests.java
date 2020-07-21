package org.alfresco.rest.sites;

import org.alfresco.dataprep.SiteService;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.model.*;
import org.alfresco.utility.constants.ContainerName;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.exception.DataPreparationException;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * @author iulia.cojocea
 */

public class GetSiteTests extends RestTest
{
    private UserModel adminUserModel;
    private ListUserWithRoles usersWithRoles;
    private UserModel userModel, privateSiteConsumer;
    private SiteModel publicSite, privateSite, moderatedSite;
    private RestSiteModel restSiteModel;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws DataPreparationException
    {
        adminUserModel = dataUser.getAdminUser();
        restClient.authenticateUser(adminUserModel);        
        publicSite = dataSite.usingUser(adminUserModel).createPublicRandomSite();
        usersWithRoles = dataUser.addUsersWithRolesToSite(publicSite, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer, UserRole.SiteContributor);

        userModel = dataUser.createRandomTestUser();
        privateSiteConsumer = dataUser.createRandomTestUser();

        privateSite = dataSite.usingAdmin().createPrivateRandomSite();
        moderatedSite = dataSite.usingAdmin().createModeratedRandomSite();
        dataUser.addUserToSite(privateSiteConsumer, privateSite, UserRole.SiteConsumer);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY, 
            description = "Verify user with Manager role gets site information and gets status code OK (200)")
    public void getSiteWithManagerRole() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                  .withCoreAPI().usingSite(publicSite)
                  .getSite()
                  .and().field("id").is(publicSite.getId())
                  .and().field("title").is(publicSite.getTitle());
        
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user with Collaborator role gets site information and gets status code OK (200)")
    public void getSiteWithCollaboratorRole() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                  .withCoreAPI().usingSite(publicSite)
                  .getSite()
                  .and().field("id").is(publicSite.getId())
                  .and().field("title").is(publicSite.getTitle());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user with Contributor role gets site information and gets status code OK (200)")
    public void getSiteWithContributorRole() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                  .withCoreAPI().usingSite(publicSite)
                  .getSite()
                  .and().field("id").is(publicSite.getId())
                  .and().field("title").is(publicSite.getTitle());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user with Consumer role gets site information and gets status code OK (200)")
    public void getSiteWithConsumerRole() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                  .withCoreAPI().usingSite(publicSite)
                  .getSite()
                  .and().field("id").is(publicSite.getId())
                  .and().field("title").is(publicSite.getTitle());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user with admin role gets site information and gets status code OK (200)")
    public void getSiteWithAdminRole() throws Exception
    {
        restClient.authenticateUser(adminUserModel)
                  .withCoreAPI().usingSite(publicSite)
                  .getSite()
                  .and().field("id").is(publicSite.getId())
                  .and().field("title").is(publicSite.getTitle());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY, 
            description = "Failed authentication get site call returns status code 401")
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void unauthenticatedUserIsNotAuthorizedToRetrieveSite() throws Exception
    {
        UserModel unauthenticatedManager = dataUser.createRandomTestUser();
        unauthenticatedManager.setPassword("user wrong password");
        dataUser.addUserToSite(unauthenticatedManager, publicSite, UserRole.SiteManager);
        restClient.authenticateUser(unauthenticatedManager).withParams("maxItems=10000")
                  .withCoreAPI()
                  .getSites();
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify invalid request returns status code 404 if siteId does not exist")
    public void checkStatusCodeForNonExistentSiteId() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite("NonExistentSiteId").getSite();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "NonExistentSiteId"))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify user gets all public and moderated sites if an empty siteId is provided")
    public void checkStatusCodeForEmptySiteId() throws Exception
    {
        restClient.authenticateUser(userModel).withCoreAPI();
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "sites/{siteId}", "");
        RestSiteModelsCollection sites = restClient.processModels(RestSiteModelsCollection.class, request);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        sites.assertThat().entriesListIsNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if any user gets public site details and status code is 200")
    public void getPublicSiteByNotASiteMember() throws Exception
    {
        restSiteModel = restClient.authenticateUser(userModel).withCoreAPI()
                .usingSite(publicSite).getSite();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteModel.assertThat().field("visibility").is(publicSite.getVisibility())
                .and().field("id").is(publicSite.getId())
                .and().field("description").is(publicSite.getDescription())
                .and().field("title").is(publicSite.getTitle())
                .and().field("guid").isNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if any user gets moderated site details and status code is 200")
    public void getModeratedSiteByNotASiteMember() throws Exception
    {
        restSiteModel = restClient.authenticateUser(userModel).withCoreAPI()
                .usingSite(moderatedSite).getSite();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteModel.assertThat().field("visibility").is(moderatedSite.getVisibility())
                .and().field("id").is(moderatedSite.getId())
                .and().field("description").is(moderatedSite.getDescription())
                .and().field("title").is(moderatedSite.getTitle())
                .and().field("guid").isNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if member of a private site gets that site details and status code is 200")
    public void getPrivateSiteBySiteMember() throws Exception
    {
        restSiteModel = restClient.authenticateUser(privateSiteConsumer).withCoreAPI()
                .usingSite(privateSite).getSite();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteModel.assertThat().field("visibility").is(privateSite.getVisibility())
                .and().field("id").is(privateSite.getId())
                .and().field("description").is(privateSite.getDescription())
                .and().field("title").is(privateSite.getTitle())
                .and().field("guid").isNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if user that is not member of a private site does not get that site details and status code is 200")
    public void getPrivateSiteByNotASiteMember() throws Exception
    {
        restSiteModel = restClient.authenticateUser(userModel).withCoreAPI()
                .usingSite(privateSite).getSite();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, privateSite.getTitle()))
                .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION }, expectedExceptions = java.lang.AssertionError.class)
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Check that properties parameter is applied (guid field is mandatory, thus assertion error is expected)")
    public void checkThatPropertiesParameterIsApplied() throws Exception
    {
        restSiteModel = restClient.authenticateUser(adminUserModel).withParams("properties=id, visibility").withCoreAPI().usingSite(publicSite).getSite();
        restSiteModel.assertThat().field("id").is(publicSite.getId()).and().field("visibility").is(SiteService.Visibility.PUBLIC.toString());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION } )
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Check that properties parameter is applied")
    public void checkThatPropertiesParameterIsAppliedPositiveTest() throws Exception
    {
        restSiteModel = restClient.authenticateUser(adminUserModel).withParams("properties=id,guid,title,visibility").withCoreAPI().usingSite(publicSite).getSite();
        restSiteModel.assertThat().field("id").is(publicSite.getId()).and().field("visibility").is(SiteService.Visibility.PUBLIC.toString())
                .and().field("description").isNull()
                .and().field("role").isNull();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION } )
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Delete site then get site details")
    public void deleteSiteThenGetSiteDetails() throws Exception
    {
        SiteModel newSite = dataSite.usingAdmin().createPublicRandomSite();
        dataSite.deleteSite(newSite);
        restSiteModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(newSite).getSite();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, newSite.getId()))
                .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION } )
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Delete site then get site details")
    public void updateSiteVisibilityToPrivateThenGetSite() throws Exception
    {
        SiteModel newSite = dataSite.usingAdmin().createPublicRandomSite();
        restSiteModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(newSite).getSite();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        dataSite.updateSiteVisibility(newSite, SiteService.Visibility.PRIVATE);
        restSiteModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingSite(newSite).getSite();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restSiteModel.assertThat().field("id").is(newSite.getId())
                .and().field("visibility").is(SiteService.Visibility.PRIVATE.toString());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION } )
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Check that relations parameter is applied for containers")
    public void checkThatRelationsParameterIsAppliedForContainers() throws Exception
    {
        List<Object> jsonObjects = restClient.authenticateUser(adminUserModel)
                .withParams("relations=containers").withCoreAPI().usingSite(publicSite).getSiteWithRelations();

        RestSiteModel siteModel = (RestSiteModel) jsonObjects.get(0);
        RestSiteContainerModelsCollection containers = (RestSiteContainerModelsCollection) jsonObjects.get(1);

        siteModel.assertThat().field("visibility").is(publicSite.getVisibility())
                .and().field("id").is(publicSite.getId())
                .and().field("description").is(publicSite.getDescription())
                .and().field("title").is(publicSite.getTitle())
                .and().field("preset").is("site-dashboard")
                .and().field("guid").isNotEmpty();

        containers.assertThat().entriesListCountIs(1)
                .and().entriesListContains("folderId", ContainerName.documentLibrary.toString());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION } )
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Check that relations parameter is applied for members")
    public void checkThatRelationsParameterIsAppliedForMembers() throws Exception
    {
        SiteModel publicRandomSite = dataSite.usingUser(adminUserModel).createPublicRandomSite();
        List<Object> jsonObjects = restClient.authenticateUser(adminUserModel)
                .withParams("relations=members").withCoreAPI().usingSite(publicRandomSite).getSiteWithRelations();

        RestSiteModel siteModel = (RestSiteModel) jsonObjects.get(0);
        RestSiteMemberModelsCollection siteMembers = (RestSiteMemberModelsCollection) jsonObjects.get(1);

        siteModel.assertThat().field("visibility").is(publicRandomSite.getVisibility())
                .and().field("id").is(publicRandomSite.getId())
                .and().field("description").is(publicRandomSite.getDescription())
                .and().field("title").is(publicRandomSite.getTitle())
                .and().field("preset").is("site-dashboard")
                .and().field("guid").isNotEmpty();

        siteMembers.assertThat().entriesListCountIs(1)
                .assertThat().entriesListContains("id", adminUserModel.getUsername())
                .assertThat().entriesListContains("role", UserRole.SiteManager.toString());
        siteMembers.getOneRandomEntry().onModel().assertThat().field("person.firstName").is("Administrator")
                .and().field("person.id").is("admin");
    }

    @Test(groups="demo")
    public void checkThatRelationsParameterIsAppliedForMembersCustom()
    {
        /*1 - select API endpoint*/
        restClient.withCoreAPI();
        
        /*2 - define request */
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "sites/{siteId}?{parameters}", publicSite.getId(), "relations=members");
        
        /*3 - send request */
        RestResponse response  = restClient.authenticateUser(adminUserModel).process(request);
        
        /*assertions */
        response.assertThat().body("entry.id", equalTo(publicSite.getId()));
        response.assertThat().body("relations.members.list.entries.entry[0].role", equalTo("SiteManager"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION } )
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Check that relations parameter is applied for containers and members")
    public void checkThatRelationsParameterIsAppliedForContainersAndMembers() throws Exception
    {
        SiteModel publicRandomSite = dataSite.usingUser(adminUserModel).createPublicRandomSite();
        List<Object> jsonObjects = restClient.authenticateUser(adminUserModel)
                .withParams("relations=containers,members").withCoreAPI().usingSite(publicRandomSite).getSiteWithRelations();

        RestSiteModel siteModel = (RestSiteModel) jsonObjects.get(0);
        RestSiteContainerModelsCollection containers = (RestSiteContainerModelsCollection) jsonObjects.get(1);
        RestSiteMemberModelsCollection siteMembers = (RestSiteMemberModelsCollection) jsonObjects.get(2);

        siteModel.assertThat().field("visibility").is(publicRandomSite.getVisibility())
                .and().field("id").is(publicRandomSite.getId())
                .and().field("description").is(publicRandomSite.getDescription())
                .and().field("title").is(publicRandomSite.getTitle())
                .and().field("preset").is("site-dashboard")
                .and().field("guid").isNotEmpty();

        containers.assertThat().entriesListCountIs(1)
                .and().entriesListContains("folderId", ContainerName.documentLibrary.toString());

        siteMembers.assertThat().entriesListCountIs(1)
                .assertThat().entriesListContains("id", adminUserModel.getUsername())
                .assertThat().entriesListContains("role", UserRole.SiteManager.toString());
        siteMembers.getOneRandomEntry().onModel().assertThat().field("person.firstName").is("Administrator")
                .and().field("person.id").is("admin");
    }
}
